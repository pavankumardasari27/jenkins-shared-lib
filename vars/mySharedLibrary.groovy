def checkout(String credentialsId, String gitRepo) {
    git credentialsId: credentialsId, url: gitRepo
}

def sshToServer(String pemKey, String serverIp) {
    sshagent([pemKey]) {
        sh """
        ssh -o StrictHostKeyChecking=no -l ubuntu ${serverIp} "ping -c 4 ${serverIp}"
        """
    }
}

def sshAndPrintSuccessMessage(String credentialsId, String serverIp) {
    withCredentials([sshUserPrivateKey(credentialsId: credentialsId, keyFileVariable: 'SSH_KEY')]) {
        def sshCommand = "ssh -o StrictHostKeyChecking=no -i ${SSH_KEY} ${SSH_USERNAME}@${serverIp}"
        sh "${sshCommand} echo 'Connection successful to ${serverIp}'"
    }
}

def givePermissions(String user) {
    sh """
    sudo chown ${user}:${user} -R *
    sudo find . -type d -exec chmod 755 {} \\;
    sudo find . -type f -exec chmod 644 {} \\;
    """
}

def composerInstallAndSetup(String user) {
    def directoryPermission = '755'
    def filePermission = '664'

    sh """
    sudo chown -R ${user}:${user} .
    sudo chmod -R ${directoryPermission} .
    sudo -u ${user} composer update
    sudo apt-get install php-xml
    sudo apt-get install php-pdo php-mysql
    sudo chown ${user}:${user} composer.lock
    sudo chmod ${filePermission} composer.lock
    sudo chmod -R 777 storage
    sudo -u ${user} php artisan key:generate --ansi
    sudo -u ${user} php artisan config:cache
    sudo -u ${user} php artisan migrate
    """
}

def codeQualityTesting() {
    sh """
    sudo chmod -R 664 composer.lock
    sudo chmod -R 664 composer.json
    composer require laravel/pint --dev
    ./vendor/bin/pint --test
    ./vendor/bin/pint -v
    composer require nunomaduro/phpinsights --dev
    ./vendor/bin/phpinsights
    php artisan insights --fix
    """
}

def dbSetUp() {
    sh "php artisan migrate"
}

def runLaravelApp() {
    def artisanPath = sh(
        script: "find . -name artisan | head -1",
        returnStdout: true
    ).trim()

    def instanceIp = sh(
        script: "curl -s http://169.254.169.254/latest/meta-data/local-ipv4",
        returnStdout: true
    ).trim()

    dir("${WORKSPACE}") {
        timeout(time: 5, unit: 'MINUTES') {
            sh "php artisan key:generate --ansi &"  // Run in the background

            // Wait for the application to be ready
            waitUntil {
                def response = sh(
                    script: "curl -s -o /dev/null -w \"%{http_code}\" http://${instanceIp}:8000",
                    returnStatus: true
                )
                return response == 200
            }

            sh "echo 'Application is ready.'"

            // Now you can perform additional steps or checks as needed
            // ...

            // Stop the background process (optional)
            sh "kill \$(ps aux | grep 'php artisan serve' | awk '{print \$2}')"
        }
    }
}


def setupNginx(String serverIp) {
    sh """
    ssh -o StrictHostKeyChecking=no -l ubuntu ${serverIp} << EOF
    sudo cp /path/to/your/nginx.conf /etc/nginx/sites-available/
    sudo ln -s /etc/nginx/sites-available/nginx.conf /etc/nginx/sites-enabled/
    sudo service nginx restart
    EOF
    """
}

def cleanup() {
    sh '''
    echo "Cleaning up workspace"
    rm -rf *
    '''
}

def sendTeamsNotification(String message) {
    def teamsWebhookUrl = 'https://truequations0.webhook.office.com/webhookb2/a541c094-f87b-474d-a877-e29ff1e03e63@f9fd21cd-c649-4d2b-940e-c7946fb06a1d/JenkinsCI/d1f5492fb64946fca66301ba50cc12d1/0421cf00-4bef-46a0-9b03-ed0cacf88bd0'

    def teamsMessage = JsonOutput.toJson([
        "@type": "MessageCard",
        "@context": "http://schema.org/extensions",
        "summary": "Jenkins Pipeline Notification",
        "themeColor": "0076D7",
        "sections": [
            [
                "activityTitle": "Jenkins Pipeline Notification",
                "activitySubtitle": message,
                "markdown": true
            ]
        ]
    ])

    httpRequest(
        acceptType: 'APPLICATION_JSON',
        contentType: 'APPLICATION_JSON',
        httpMode: 'POST',
        requestBody: teamsMessage,
        responseHandle: 'NONE',
        url: teamsWebhookUrl
    )
}

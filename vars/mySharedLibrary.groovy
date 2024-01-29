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

def givePermissions() {
    sh """
    sudo chown www-data:www-data -R *
    sudo find . -type d -exec chmod 777 {} \\;
    sudo find . -type f -exec chmod 644 {} \\;
    """
}

def composerInstallAndSetup() {
    sh """
    sudo chown -R www-data:www-data .
    sudo chmod -R 755 .
    composer update
    sudo apt-get install php-xml
    sudo apt-get install php-pdo php-mysql
    sudo chown www-data:www-data composer.lock
    sudo chmod 664 composer.lock
    sudo chmod -R 777 storage
    php artisan key:generate --ansi
    php artisan config:cache
    php artisan migrate
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

def runLaravelApp() {
    // Find artisan path
    def artisanPath = sh(
        script: "find . -name artisan | head -1",
        returnStdout: true
    ).trim()

    dir("${WORKSPACE}") {
        // Run artisan serve in the foreground with a timeout of 2 minutes
        timeout(time: 2, unit: 'MINUTES') {
            sh "php artisan key:generate --ansi"
            sh "${artisanPath} serve --host=0.0.0.0 --port=8000"
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
    # Add your cleanup commands here
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

def connectToServer(def serverIp, def pemKey) {
    echo "Checking server connectivity..."
    retry(3) {
        sh "ssh -o StrictHostKeyChecking=no -i ${pemKey} ec2-user@${serverIp} 'echo Connected'"
    }
}

def givePermissions() {
    echo "Giving permissions to files and folders..."
    sh "sudo chown -R www-data:www-data ."
    sh "sudo chmod -R 755 *"
    sh "sudo chmod -R 644 *"
}

def copyEnvFile(def envFileId) {
    echo "Copying .env file..."
    withCredentials([file(credentialsId: envFileId, variable: 'SECRET_ENV_FILE')]) {
        sh "cp ${SECRET_ENV_FILE} .env"
    }
}

def runComposerInstall() {
    echo "Running Composer Install..."
    sh 'composer install --no-interaction --no-progress --prefer-dist'
    sh 'php artisan config:cache'
    sh 'php artisan migrate'
    sh 'php artisan db:seed'
}

def runCodeQualityTests() {
    echo "Running code quality tests..."
    try {
        sh 'composer require laravel/pint --dev'
        sh './vendor/bin/pint --test'
        sh './vendor/bin/pint -v'
        sh 'composer require nunomaduro/phpinsights --dev'
        sh './vendor/bin/phpinsights'
        sh 'php artisan insights --fix'
    } catch (Exception e) {
        currentBuild.result = 'FAILURE'
        error("Code quality tests failed. Check the logs for details.")
    }
}

def startApplication() {
    echo "Running the application..."
    sh 'php artisan serve --host=0.0.0.0 --port=8000 &'
    sleep 10
}

def checkApplicationStatus() {
    echo "Checking application status..."
    try {
        sh 'curl -s -I http://localhost:8000 | grep "HTTP/1.1 200 OK"'
    } catch (Exception e) {
        currentBuild.result = 'FAILURE'
        error("Application did not start successfully. Check the logs for details.")
    }
}

def copyNginxConfig() {
    echo "Copying Nginx config..."
    sh 'cd /etc/nginx/sites-available'
    sh 'cp your-nginx-config.conf /etc/nginx/sites-available'
    sh 'ln -s /etc/nginx/sites-available/your-nginx-config.conf /etc/nginx/sites-enabled/'
    sh 'nginx -s reload'
}

def shareNginxOutput() {
    echo "Sharing Nginx output..."
    // Add logic to share relevant information like domain, etc.
}

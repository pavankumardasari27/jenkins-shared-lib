// vars/laravelPipeline.groovy

class LaravelPipeline {
    def checkoutRepo(String url, String credentials) {
        git url: url, credentialsId: credentials
    }

    def sshToServer(String ip, String sshKey) {
        sh "ssh -i ${sshKey} ubuntu@${ip}"
    } 

    def composerUpdate() {
        sh 'composer update'
    }

    def composerInstall() {
        sh 'composer install'
    }

    def copyEnvFile(String secretName) {
        withCredentials([string(credentialsId: secretName, variable: 'ENV')]) {
            sh 'cp env.example .env'
            sh "echo \$ENV > .env"
        }
    }

    def setPermissions() {
        sh 'chmod -R 755 storage bootstrap/cache'
        sh 'chmod -R 644 .env' 
    }

    def installSecurityTools() {
        sh './vendor/bin/composer require laravel/pint --dev'
        sh './vendor/bin/pint --test'
        sh './vendor/bin/pint -v'
    }

    def copyNginxConfig(String secretName) {
        withCredentials([string(credentialsId: secretName, variable: 'NGINX')]) {
            sh 'mkdir -p /etc/nginx/sites-available'
            sh "echo \$NGINX > /etc/nginx/sites-available/default"
        }
    }

    def linkNginxConfig() {
        sh 'ln -sf /etc/nginx/sites-available/default /etc/nginx/sites-enabled/default'
        sh 'nginx -t'
    }

    def generateCerts() {
        // generate and install certs
        sh '...' 
    }

    def notifyTeams(String webhookUrl) {
        sh "curl -X POST -H 'Content-type: application/json' --data '\"Build succeeded\"' ${webhookUrl}" 
    }
}

return new LaravelPipeline()
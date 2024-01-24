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

def givePermissions() {
    sh """
    sudo chown www-data:www-data -R *
    sudo find . -type d -exec chmod 777 {} \\;
    sudo find . -type f -exec chmod 644 {} \\;
    """
}

def composerInstallAndSetup() {
    sh """
    sudo chown -R jenkins:jenkins .
    sudo chmod -R 755 .
    composer update
    sudo chown jenkins:jenkins composer.lock
    sudo chmod 664 composer.lock
    sudo chmod -R 777 storage
    php artisan key:generate --ansi
    php artisan config:cache
    php artisan migrate
    php artisan db:seed
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
    sh 'composer install'
    // Run artisan serve using full path
    sh "${artisanPath} serve --host=0.0.0.0 --port=8000 > /dev/null 2>&1 &"
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

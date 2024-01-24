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
    sudo chown -R www-data:www-data .
    sudo chmod -R 755 .
    sudo chmod -R 777 bootstrap
    sudo chmod -R 777 vendor
    sudo chmod -R 777 storage
    sudo chmod -R 777 .env
    composer install
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

def runApplication() {
    sh """
    php artisan serve --host=0.0.0.0 --port=8000 &
    sleep 5
    curl -I http://localhost:8000
    """
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

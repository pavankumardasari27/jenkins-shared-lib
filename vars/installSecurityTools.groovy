def installSecurityTools() {
    sh './vendor/bin/composer require laravel/pint --dev'
    sh './vendor/bin/pint --test'
    sh './vendor/bin/pint -v'
}
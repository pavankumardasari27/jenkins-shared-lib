def checkoutGitRepo(String url, String branch = 'main') {
  git url: url, branch: branch, credentialsId: 'git-credentials'
}

def setPermissions(String path) {
  sh "sudo chown -R www-data:www-data ${path}"
  sh "sudo chmod -R 755 ${path}" 
  sh "sudo chmod -R 644 ${path}/*"
}

def installDependencies(String path) {
  sh "cd ${path} && composer install"
  sh "cd ${path} && php artisan config:cache"
  sh "cd ${path} && php artisan migrate"  
}

def runCodeQuality(String path) {
  sh "cd ${path} && composer require laravel/pint --dev"
  sh "cd ${path} && ./vendor/bin/pint --test"
}

def startApp(String path, String host, String port) {
  sh "cd ${path} && php artisan serve --host=${host} --port=${port} &"
  sh "curl --retry 10 --retry-delay 5 http://localhost:${port}"
  sh "kill \$(lsof -t -i:${port})"
}

return this

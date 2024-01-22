def setPermissions() {
    sh 'chmod -R 755 storage bootstrap/cache'
    sh 'chmod -R 644 .env' 
}
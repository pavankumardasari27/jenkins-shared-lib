def linkNginxConfig() {
    sh 'ln -sf /etc/nginx/sites-available/default /etc/nginx/sites-enabled/default'
    sh 'nginx -t'
}
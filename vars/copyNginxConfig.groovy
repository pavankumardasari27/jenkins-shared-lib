def copyNginxConfig(String secretName) {
    withCredentials([string(credentialsId: secretName, variable: 'NGINX')]) {
        sh 'mkdir -p /etc/nginx/sites-available'
        sh "echo \$NGINX > /etc/nginx/sites-available/default"
    }
}
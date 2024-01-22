def copyEnvFile(String secretName) {
    withCredentials([string(credentialsId: secretName, variable: 'ENV')]) {
        sh 'cp env.example .env'
        sh "echo \$ENV > .env"
    }
}
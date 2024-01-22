def notifyTeams(String webhookUrl) {
    sh "curl -X POST -H 'Content-type: application/json' --data '\"Build succeeded\"' ${webhookUrl}" 
}
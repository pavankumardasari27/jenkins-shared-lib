def sshToServer(String ip, String credentialsId) {
  withCredentials([sshUserPrivateKey(credentialsId: credentialsId, 
   username: 'ubuntu', keyFileVariable: 'KEY')]) {
     sh "ssh -i ${KEY} ubuntu@${ip}" 
  }
}
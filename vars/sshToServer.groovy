def sshToServer(String ip, String sshKey) {
    sh "ssh -i ${sshKey} ubuntu@${ip}"
} 
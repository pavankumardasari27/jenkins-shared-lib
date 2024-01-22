def checkoutRepo(String url, String credentials) {
        git url: url, credentialsId: credentials
}
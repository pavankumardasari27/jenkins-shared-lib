def call(repositoryUrl, credentialsId, branch = 'main') {
    git branch: branch,
        credentialsId: credentialsId,
        url: repositoryUrl
}

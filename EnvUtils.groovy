// vars/env/EnvUtils.groovy

package com.te.utils

class EnvUtils {

  def loadEnvFile(file) {
    sh "cp ${file} .env"
  }

  def getEnvVariable(key) {
    sh(script: "echo \$${key}", returnStdout: true).trim()
  }

  def setEnvVariable(key, value) {
    sh "export ${key}=${value}"
  }
}

// vars/ssh/SshUtils.groovy

package com.te.utils

class SshUtils {

  def key

  SshUtils(key) {
    this.key = key
  }

  def connect(ip) {
    sh "ssh -i ${this.key} ubuntu@${ip}"
  }

  def sendCommands(ip, commands) {
    sshCommand remote: ip, command: commands
  }

  def copyFile(ip, source, destination) {
    sh "scp -i ${this.key} ${source} ubuntu@${ip}:${destination}"
  }

  def closeConnection(ip) {
    sh "ssh -i ${this.key} ubuntu@${ip} exit"
  }
}

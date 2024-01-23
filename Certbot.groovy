// vars/certbot/Certbot.groovy

package com.te.utils

class Certbot {

  def generateCert(email, domain) {
    sh "certbot certonly --standalone --email ${email} --agree-tos --no-eff-email -d ${domain} --expand"
  }

  def renewCerts() {
    sh "certbot renew"
  }

  def revokeCert(domain) {
    sh "certbot revoke --cert-name ${domain} --delete-after-revoke"
  }
}

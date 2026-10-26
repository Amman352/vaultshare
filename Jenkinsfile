pipeline {
  agent any
  options { timestamps() }
  triggers { pollSCM('H/5 * * * *') }
  parameters { string(name: 'IMAGE_REPO', defaultValue: 'tanay04032006/vaultshare', description: 'Docker registry repository') }
  stages {
    stage('Build and test') { steps { dir('app') { sh 'mvn -B verify' } } }
    stage('Build image') { steps { sh 'docker build -t ${IMAGE_REPO}:${BUILD_NUMBER} app' } }
    stage('Publish image') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'docker-registry', usernameVariable: 'REGISTRY_USER', passwordVariable: 'REGISTRY_PASSWORD')]) {
          sh 'printf %s "$REGISTRY_PASSWORD" | docker login -u "$REGISTRY_USER" --password-stdin'
          sh 'docker push ${IMAGE_REPO}:${BUILD_NUMBER}'
        }
      }
    }
    stage('Deploy to K3s') {
      steps { sh 'sudo /usr/local/sbin/vaultshare-deploy ${IMAGE_REPO}:${BUILD_NUMBER}' }
    }
  }
  post { always { sh 'docker logout || true' } }
}

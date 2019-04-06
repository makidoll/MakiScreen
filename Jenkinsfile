pipeline {
  agent {
    docker {
      image 'maven:3.6.0-jdk-8-alpine'
    }
  }
  stages {
    stage('Build') {
      steps {
        sh 'mvn -B package'
      }
    }
    stage('Deliver') {
      steps {
        archiveArtifacts 'target/*.jar'
      }
    }
  }
}
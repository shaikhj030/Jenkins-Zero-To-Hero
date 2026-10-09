def call(String imgname) {
    stage('Docker Build & Push') {
        withCredentials([usernameColonPassword(credentialsId: 'dockerhub', variable: 'Dockerhublogin'), usernamePassword(credentialsId: 'dockerhub', passwordVariable: 'dockerhubpass', usernameVariable: 'dockerhubuser')]) {
               sh "echo $dockerhubpass | docker login -u $dockerhubuser --password-stdin"
               sh "docker build -t ${imgname} ."
               sh "docker tag ${imgname} $dockerhubuser/${imgname}"
               sh "docker push $dockerhubuser/${imgname}"
        }
    }
}

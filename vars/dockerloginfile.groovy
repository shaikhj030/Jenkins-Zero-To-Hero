def call(String imgname) {
    stage('Docker Build & Push') {
        withCredentials([usernameColonPassword(credentialsId: 'dockerhub', variable: 'Dockerhublogin'), usernamePassword(credentialsId: 'dockerhub', passwordVariable: 'dockerhubpass', usernameVariable: 'dockerhubuser')]) {
               sh '''
               echo $dockerhubpass | docker login -u $dockerhubuser --password-stdin
               docker build -t ${imgname} .
               docker tag ${imgname} $dockerhubuser/${imgname}
               docker push $dockerhubuser/${imgname}
               '''
        }
    }
}

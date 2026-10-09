def call(String imgname) {
    stage('Docker Build & Push') {
        withCredentials([usernameColonPassword(credentialsId: 'dockerhub', variable: 'Dockerhublogin'), usernamePassword(credentialsId: 'dockerhub', passwordVariable: 'dockerhubpass', usernameVariable: 'dockerhubuser')]) {
               sh 'echo "$dockerhubpass" | docker login -u "$dockerhubuser" --password-stdin'
               sh "docker build -t ${imgname}:${env.BUILD_ID} ."
               sh "docker tag ${imgname}:${env.BUILD_ID} ${dockerhubuser}/${imgname}:${env.BUILD_ID}"
               sh "docker push $dockerhubuser/${imgname}:${env.BUILD_ID}"
        }
    }
}

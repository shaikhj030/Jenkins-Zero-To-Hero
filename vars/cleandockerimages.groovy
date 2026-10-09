// var/cleandockerimages.groovy
def call() {
            sh '''
            echo "Cleaning images to free disk space"'
            docker images
            docker images | grep -v "IMAGE" | awk '{print $1}' | xags docker rmi
            '''
}

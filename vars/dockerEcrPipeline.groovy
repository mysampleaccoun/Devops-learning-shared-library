
def call(Map config = [:]) {

    stage('Checkout') {
        git(
            branch: config.gitBranch,
            url: config.gitRepo
        )
    }

    stage('Build Docker Image') {
        dir('jenkins') {
            sh """
                docker build \
                -t ${config.imageName}:${config.imageTag} \
                .
            """
        }
    }

    stage('Login to ECR') {
        withCredentials([
            [$class: 'AmazonWebServicesCredentialsBinding',
             credentialsId: 'aws-ecr']
        ]) {
            sh """
                aws ecr get-login-password \
                --region ${config.awsRegion} | \
                docker login \
                --username AWS \
                --password-stdin \
                683354427061.dkr.ecr.${config.awsRegion}.amazonaws.com
            """
        }
    }

    stage('Tag Image') {
        sh """
            docker tag \
            ${config.imageName}:${config.imageTag} \
            683354427061.dkr.ecr.${config.awsRegion}.amazonaws.com/${config.ecrRepository}:${config.imageTag}
        """
    }

    stage('Push to ECR') {
        sh """
            docker push \
            683354427061.dkr.ecr.${config.awsRegion}.amazonaws.com/${config.ecrRepository}:${config.imageTag}
        """
    }
}

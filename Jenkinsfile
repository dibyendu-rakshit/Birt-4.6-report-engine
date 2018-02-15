podTemplate(label: 'report-engine-2-pipeline-01', containers: [
        containerTemplate(name: 'jnlp', image: 'greytip/jenkins-slave:0.9.0', args: '${computer.jnlpmac} ${computer.name}'),
],
volumes:[
    hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock'),
    persistentVolumeClaim(claimName: 'maven-local-repo', mountPath: '/root/.m2'),
    persistentVolumeClaim(claimName: 'jenkins-tools', mountPath: '/home/jenkins/tools'),
]) {
        properties([
            buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '10')),
            parameters([
                string(defaultValue: '', description: 'Custom version number for deployment, if left empty default version will be computed by merging branch name and build number', name: 'version', trim: true),
                credentials(credentialType: 'com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl', defaultValue: 'greytip-docker-credentials', description: 'Docker Hub access credentials for Accessing/Uploading Private Repositories', name: 'greytipDockerCredentials', required: true)
            ])
        ])

    node('report-engine-2-pipeline-01') {
        stage('Checkout project') {
            checkout scm
        }

        def MAVEN_HOME = tool name: 'maven-3.5', type: 'maven'
        def CMD_MVN = MAVEN_HOME + '/bin/mvn '
        println 'MVN CMD: ' + CMD_MVN

        def version = params.version
        if( !version ) {
                version = env.BRANCH_NAME + '-' + env.BUILD_ID
        }

        stage('Build') {
            println "Building version: " + version
            sh CMD_MVN + 'versions:set -DnewVersion=' + version
            sh CMD_MVN + 'package -DskipTests=true'
        }

        stage('Docker Build') {
            sh 'docker build -t greytip/report-engine-2:' + version + ' --build-arg APP_FILE=birt-report-' + version + ' .'
            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: params.greytipDockerCredentials, usernameVariable: 'DOCKER_HUB_USER', passwordVariable: 'DOCKER_HUB_PWD']]){
                    sh 'docker login -u ' + env.DOCKER_HUB_USER + ' -p ' + env.DOCKER_HUB_PWD
            }
            sh 'docker push greytip/report-engine-2:' + version
        }

		stage('Tag') {
			sshagent(credentials: [scm.userRemoteConfigs[0].credentialsId]){
				sh 'git tag t-' + version
				sh 'git push --tags'
			}
        }
    }
}

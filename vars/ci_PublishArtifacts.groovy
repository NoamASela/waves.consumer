public def void call (pBuildDepot, pBuildBranch) {
    if (fileExists("${WORKSPACE}/${THIS_WORKSPACE}/${pBuildDepot}/${pBuildBranch}/Products")) {
        sh "mkdir -p /home/jenkins/artifacts/Consumer/${JOB_NAME}/${BUILD_NUMBER}/${pBuildDepot}/${pBuildBranch}/"
        sh "cp -r ${WORKSPACE}/${THIS_WORKSPACE}/${pBuildDepot}/${pBuildBranch}/Products /home/jenkins/artifacts/Consumer/${JOB_NAME}/${BUILD_NUMBER}/${pBuildDepot}/${pBuildBranch}/"
    }
}
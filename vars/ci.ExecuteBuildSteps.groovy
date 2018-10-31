import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException;

public def void call (HashMap pUserParameters, boolean pContinueOnBuildStepFailure) {
    def ArrayList buildSteps = pUserParameters.Projects.Steps
    def String buildDepot = pUserParameters.Projects.Depot
    def String buildBranch = pUserParameters.Projects.Branch
    
    buildSteps.each { buildStage ->
        try {
            def String stageName = "${buildStage.Target} ${buildStage.Configuration}"
            stage("${stageName}") {
                if (buildStage.Enabled) //If the build step is enabled by the user
                {
                    println "Starting ${stageName}..."
                    DoBuild(buildStage, buildDepot, buildBranch)
                } else {
                    println "Skipping ${stageName} due to enabled=false"
                }
            }
        }
        catch (FlowInterruptedException fie) {
            //When user abort a job, it might throw FIE or AE, depending on where it was stopped.
            println "Catched fie"
            println "Build Aborted"
            currentBuild.result = "ABORTED"
            throw fie
        }
        catch (hudson.AbortException ae) {
        //When user abort a job, it might throw FIE or AE, depending on where it was stopped.
        // however, ae is throws also when the build stage failed on certain situations.
        // thereof, we also check the exit code of 143, which means task abort.
            if (ae.getMessage().contains('script returned exit code 143')) {
                //Build aborted
                println "Catched ae, with exit 143"
                println "Build Aborted"
                currentBuild.result = "ABORTED"
                throw ae
            } else {
                //Build failed, so we continue based on ContinueOnBuildStepFailure
                println "Catched ae, with NON 143"
                currentBuild.result="FAILURE"
                print ae.toString()
                if (!pContinueOnBuildStepFailure) {
                    throw ae
                }
            }
        }
    }
}
private def void DoBuild (HashMap pBuildStage, String pBuildDepot, String pBuildBranch) {
    String stageName = "${pBuildStage.Target} ${pBuildStage.Configuration}"
    sh "mkdir -p ${WORKSPACE}/${THIS_WORKSPACE}/${pBuildDepot}/${pBuildBranch}/${pBuildStage.Project}/tmp/"
    dir ("${WORKSPACE}/${THIS_WORKSPACE}/${pBuildDepot}/${pBuildBranch}/${pBuildStage.Project}/tmp/") {
        sh 'pwd'
        sh "cmake .. -DCLEAN_BUILD=true -DCMAKE_BUILD_TYPE=${pBuildStage.Configuration} -DAPP_ABI=${pBuildStage.Architecture} ${pBuildStage.CMakeArguments}"
        sh "make -j 4 ${pBuildStage.Target} ${pBuildStage.MakeArguments}"
    } 
}
import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException;


public def void call (HashMap pUserParameters) {
    def ArrayList testSteps = pUserParameters.Projects.Tests
    def String buildDepot = pUserParameters.Projects.Depot
    def String buildBranch = pUserParameters.Projects.Branch
    
   testSteps.each { testStage ->
        try {
            def String stageName = "${testStage.Target} ${testStage.Configuration}"
            stage("${stageName}") {
                if (testStage.Enabled) 
                {
                    println "Starting ${stageName}..."
                    DoTest(testStage, buildDepot, buildBranch)
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
                //Test aborted
                println "Catched ae, with exit 143"
                println "Build Aborted"
                currentBuild.result = "ABORTED"
                throw ae            
            } else {
                //Test failed, so we continue to the next
                println "Catched ae, with NON 143"
                if (currentBuild.result!="FAILURE") currentBuild.result="UNSTABLE"
                println ae.toString()
            }
        }
    }
}
private def void DoTest (pTestStage, pBuildDepot, pBuildBranch) {
    String stageName = "${pTestStage.Target} ${pTestStage.Configuration}"
    timeout(time: pTestStage.Timeout, unit: 'MINUTES') {
        sh "mkdir -p ${WORKSPACE}/${THIS_WORKSPACE}/${pBuildDepot}/${pBuildBranch}/${pTestStage.Project}/tmp/"
        dir ("${WORKSPACE}/${THIS_WORKSPACE}/${pBuildDepot}/${pBuildBranch}/${pTestStage.Project}/tmp/") {
            sh 'pwd'
            sh "cmake .. -DCLEAN_BUILD=true -DCMAKE_BUILD_TYPE=${pTestStage.Configuration} -DAPP_ABI=${pTestStage.Architecture} ${pTestStage.CMakeArguments}"
            sh "make ${pTestStage.Target} ${pTestStage.MakeArguments}"
        }
    }
}
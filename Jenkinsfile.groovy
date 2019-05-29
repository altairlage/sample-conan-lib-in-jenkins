// Build-Library is a sample library to be imported
// In the real world, it would be a groovy library containing re-usable code for pipelines
@Library('Build-library')

// to be imported relative to Build-library
import org.altabuild.conan.ConanBuilder

conanBuilder = new ConanBuilder(this, 'windows')

def libName = "myMultiOSLib"

def githubUser = "jenkins-user"
def githubUrl = "https://<git code repo url>.git"

def branch = env.BRANCH_NAME
def environment = "dev" // Build or deploy environment


node("WinWithVS2017-node") {

    try {
        stage("Checkout") {
            dir("files"){
                // TO-DO Checkout code repo from git
            }
        }

        stage("Build"){
            dir("files") {
                // Gets git username and password credentials from Jenkins credentials list
                withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: githubUser, usernameVariable: 'gitUsername', passwordVariable: 'gitPassword']]) {
                    withEnv(["GIT_USERNAME=$gitUsername", "GIT_PASSWORD=$gitPassword"]) {
                        def buildInfo = [
                                qualifier: environment,
                                branch: branch,
                                libName  : libName,
                                startTime: currentBuild.startTimeInMillis
                        ]
                        def gitCredentials = [
                                userName: gitUsername,
                                password: gitPassword
                        ]
                        
                        // uses conanbuilder class from groovy library to build and create conan package 
                        conanBuilder.createPackage(buildInfo, gitCredentials)
                    }
                }
            }
        }

        stage("Upload to Artifactory"){
            dir("files"){
                // Gets artifactory username and password credentials from Jenkins credentials list
                withCredentials([[$class: "UsernamePasswordMultiBinding", credentialsId: "jenkins-artifactory", usernameVariable: "artifactoryUser", passwordVariable: "artifactoryPassword"]]) {
                    withEnv(["CONAN_LOGIN_USERNAME=$artifactoryUser", "CONAN_PASSWORD=$artifactoryPassword"]) {
                        // uses conanbuilder class from groovy library to upload conan package to artifactory
                        conanBuilder.uploadPackageToArtifactory(libName)
                    }
                }
            }
        }

    } catch (e) {
        throw e
    } finally {
        stage("Cleanup"){
            // uses conanbuilder class from groovy library to clean conan cache and internal build/source folders
            conanBuilder.cleanEnvironment(libName)
        }
    }
}



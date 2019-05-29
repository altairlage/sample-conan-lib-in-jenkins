package org.altabuild.conan

import groovy.json.JsonOutput
import java.util.Date
import org.altabuild.json.DependencyFile

class ConanBuilder implements Serializable {

    private static final String DEPENDENCY_FILE_PATH = 'dependency.json'
    private script
    private dependencyFile
    private os

    ConanBuilder(script, os) {
        this.script = script
        this.dependencyFile = new DependencyFile(script, DEPENDENCY_FILE_PATH)
        this.os = os
    }

    void createPackage(buildInfo, gitCredentials) {
        updateVersionFile(buildInfo.qualifier, buildInfo.startTime)
        def command = "conan create . ${getLibFullName(buildInfo.qualifier, buildInfo.libName)} " +
                "-o *:start_time=$buildInfo.startTime " +
                "-o *:branch=$buildInfo.branch " +
                "--build"
        if (this.os.toLowerCase() == "windows") {
            script.bat(command)
        } else {
            script.sh(command)
        }
    }

    private void updateVersionFile(qualifier, startTime) {
        String updatedDependencyFileContent = getUpdatedDependencyFileContent(qualifier, startTime)
        script.writeFile(file: DEPENDENCY_FILE_PATH, text: "$updatedDependencyFileContent")
    }

    private String getUpdatedDependencyFileContent(qualifier, startTime) {
        def formattedStartTime = parseStartTime(startTime)
        def dependency = dependencyFile.getContent()
        if(qualifier == 'prod') {
            dependency['version'] = "${dependency['version']}-$formattedStartTime"
        } else {
            dependency['version'] = "${dependency['version']}-$qualifier.$formattedStartTime"
        }
        return JsonOutput.toJson(dependency)
    }

    private String parseStartTime(startTime) {
        def date = new Date(startTime as long)
        return date.format("yyyyMMddHHmmss")
    }

    private String getLibFullName(qualifier, libName) {
        def buildType = qualifier == "prod" ? "stable" : "unstable"
        return "$libName/$buildType"
    }

    void uploadPackageToArtifactory(libName) {
        // You need to have a remote called 'artifactory' defined in conan to be able to upload
        // Command to define a remote:
        // conan remote add artifactory https://my-artifactory.myorg.com/artifactory/api/conan/c-libs
        def command = "conan upload $libName/* --remote=artifactory --confirm --all"
        if (this.os.toLowerCase() == "windows") {
            script.bat(command)
        } else {
            script.sh(command)
        }
    }

    void cleanEnvironment(libName) {
        def command = "conan remove $libName* -f"
        if (this.os.toLowerCase() == "windows") {
            script.bat(command)
        } else {
            script.sh(command)
        }
        
    }
}

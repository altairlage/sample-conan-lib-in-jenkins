package org.altabuild.json

/**
 * DependencyFile is the class that provides a common interface for parsing dependency.json files from text to Groovy
 * objects. The class is serializable and is meant to be used in a Jenkins pipeline.
 */
class DependencyFile implements Serializable {

    private script
    private JsonParser jsonParser
    private dependencyFilePath

    DependencyFile(script, String dependencyFilePath) {
        this.script = script
        this.dependencyFilePath = dependencyFilePath
        this.jsonParser = new JsonParser()
    }

    Object getContent() {
        String dependecyContent = this.script.readFile(file: dependencyFilePath)
        return this.jsonParser.fromText(dependecyContent)
    }
}
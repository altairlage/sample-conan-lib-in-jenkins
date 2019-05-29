package org.altabuild.json

import groovy.json.JsonOutput
import groovy.json.JsonSlurperClassic

/**
 * JsonParser is the class that provides a common interface parsing JSON files from text to Groovy objects
 * and brack from Groovy objects to JSON text. The class is serializable and is meant to be used in a Jenkins pipeline.
 */
class JsonParser implements Serializable {

    Object fromText(String jsonText) {
        return new JsonSlurperClassic().parseText(jsonText)
    }

    String toText(jsonObject) {
        return JsonOutput.toJson(jsonObject)
    }
}
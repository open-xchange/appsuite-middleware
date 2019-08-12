
import java.nio.file.Paths
import java.nio.file.Files

plugins {
    java
}

repositories {
    maven {
        url = uri("https://artifactory.open-xchange.com/artifactory/libs-snapshot")
    }
}

val openapiTools = configurations.create("openapiTools")
val openapiGenConfig = configurations.create("openapi")

dependencies {
    openapiTools("com.openexchange.appsuite.mw", "documentation-tools", "1.+")
    openapiGenConfig("org.openapitools", "openapi-generator-cli", "4.0.2")
}

tasks.register("buildJars"){
    dependsOn("http_api:buildTestClient", "drive_api:buildTestClient", "rest_api:buildTestClient")    
}


tasks.register("copyClients", Copy::class){
    from("client-gen/out/http_api_client/build/libs/")
    from("client-gen/out/rest_api_client/build/libs/")
    into("../openexchange-test/lib")
}

allprojects {
    group = "com.openexchange.appsuite.mw"
    version = "1.0-SNAPSHOT"
}

subprojects {
    apply(plugin="java")
    apply(plugin="maven-publish")
}

configure(subprojects.filter { it.name == "http_api" || it.name == "rest_api" || it.name == "drive_api"}) {

     tasks.register("resolve", JavaExec::class.java) {
             val arguments = mutableListOf("../" + this.project.name)

             classpath = rootProject.configurations.getByName("openapiTools")
             main = "src.com.openexchange.resolver.Main"
             args(arguments)
     }

     tasks.register("validate", JavaExec::class.java) {
        val arguments = mutableListOf(
                "validate",
                "--input-spec", "openAPI.json"
            )
            
            classpath = rootProject.configurations.getByName("openapi")
            main = "org.openapitools.codegen.OpenAPIGenerator"
            args(arguments)
    }

    tasks.register("buildTestClient", JavaExec::class.java) {
        if(this.project.name == "http_api" || this.project.name == "rest_api") {
            val arguments = mutableListOf(
                    "generate",
                    "-g", "java",
                    "--template-dir", "../client-gen/templates/${this.project.name}",
                    "--generate-alias-as-model",
                    "--minimal-update",
                    "--library", "jersey2",
                    "--config", "../client-gen/config/${this.project.name}.json",
                    "--input-spec", "openAPI.json",
                    "--output", "../client-gen/out/${this.project.name}_client"
            )

            classpath = rootProject.configurations.getByName("openapi")
            main = "org.openapitools.codegen.OpenAPIGenerator"
            args(arguments)
        }
    }

    tasks.register("buildHtml", JavaExec::class.java) {
        val arguments = mutableListOf(
            "../" + this.project.name,
            "../../documentation-generic/${this.project.name}",
            "openApi.json"
        )

        classpath = rootProject.configurations.getByName("openapiTools")
        main = "src.com.openexchange.processor.generator.Main"
        args(arguments)
        
    }


    tasks.register("insertMarkdown", Exec::class) {
        dependsOn("buildHtml")

        workingDir("../../documentation-generic/${this.project.name}")
        commandLine("./insertExtendedMarkdownDocu.sh")
    }

}

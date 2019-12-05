
tasks.named<JavaExec>("buildTestClient") {
        val arguments = mutableListOf(
                "generate",
                "-g", "java",
                "--template-dir", "../client-gen/templates/http_api",
                "--generate-alias-as-model",
                "--minimal-update",
                "--library", "jersey2",
                "--config", "../client-gen/config/http_api.json",
                "--input-spec", "openAPI.json",
                "--output", "../client-gen/out/http_api_client"
        )

        classpath = rootProject.configurations.getByName("openapi")
        main = "org.openapitools.codegen.OpenAPIGenerator"
        args(arguments)
}

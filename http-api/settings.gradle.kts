rootProject.name = "http_api"
include("http_api", "rest_api", "drive_api")

include(":http_api_client")
project(":http_api_client").projectDir = File("client-gen/out/http_api_client")

include(":rest_api_client")
project(":rest_api_client").projectDir = File("client-gen/out/rest_api_client")

plugins {
    java
}

repositories {
    maven {
        url = uri("https://artifactory.open-xchange.com/artifactory/libs-snapshot")
    }
}

val httpClient = configurations.create("httpClient")
val driveClient = configurations.create("driveClient")

dependencies {
    httpClient("com.openexchange.appsuite.mw", "http_api_client", "1.+"){
        isTransitive = false
    }
    driveClient("com.openexchange.appsuite.mw", "rest_api_client", "1.+"){
        isTransitive = false
    }
}

tasks.register("loadClients", Copy::class){
    from(httpClient.resolve())
    from(driveClient.resolve())
    into("lib/")
}
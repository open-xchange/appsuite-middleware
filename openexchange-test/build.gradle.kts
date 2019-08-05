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
    //httpClient("com.openexchange.appsuite.mw", "httpClient, "1.+")
    //driveClient("com.openexchange.appsuite.mw", "driveClient", "1.+")
}

tasks.register("loadClients", Copy::class){
    from(httpClient.resolve())
    from(driveClient.resolve())
    into("lib/")

    println("Currently not enabled!")
}
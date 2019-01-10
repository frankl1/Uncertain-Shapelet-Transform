plugins {
    java
    application
}

repositories {
    mavenCentral()    
    jcenter() 
}

dependencies {
    testImplementation("junit:junit:4.12")
    //compile "com.beust:jcommander:1.71"
}

application {
    mainClassName = "App"
}
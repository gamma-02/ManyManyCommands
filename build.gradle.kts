@file:Suppress("GradlePackageVersionRange")

plugins {
    id("maven-publish")
    id("fabric-loom") version "1.6-SNAPSHOT"
    id("org.jetbrains.kotlin.jvm") version "1.9.23"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
    idea
}

allprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "fabric-loom")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "idea")

    val transitiveInclude: Configuration by configurations.creating

    repositories {
        mavenCentral()
        maven("https://repo.repsy.io/mvn/amibeskyfy16/repo") // Use for my JsonConfig lib
        maven("https://www.cursemaven.com") {
            content { includeGroup("curse.maven") }
        }
        maven("https://maven.nucleoid.xyz")

    }

    dependencies {
        minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")
        mappings("net.fabricmc:yarn:${properties["yarn_mappings"]}:v2")

        modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"]}")
        modImplementation("net.fabricmc.fabric-api:fabric-api:${properties["fabric_version"]}")
        modImplementation("net.fabricmc:fabric-language-kotlin:${properties["fabric_kotlin_version"]}")
        modImplementation("net.silkmc:silk-game:${properties["silk_version"]}")
//        modImplementation("fr.catcore:server-translations-api:${properties["server_translations_version"]}")

        transitiveInclude(implementation("ch.skyfy.json5configlib:json5-config-lib:1.0.25")!!)

        handleIncludes(project, transitiveInclude)

        testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.23")
    }

    tasks {
        val javaVersion = JavaVersion.VERSION_21

        processResources {
            inputs.property("version", rootProject.version)
            filteringCharset = "UTF-8"
            filesMatching("fabric.mod.json") {
                expand(mutableMapOf("version" to rootProject.version))
            }
        }

        java {
            toolchain {
//            languageVersion.set(JavaLanguageVersion.of(javaVersion.toString()))
//            vendor.set(JvmVendorSpec.BELLSOFT)
            }
            withSourcesJar()
            withJavadocJar()
        }

        named<Javadoc>("javadoc") {
            options {
                (this as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
            }
        }

        named<Jar>("jar") {
            from("LICENSE") { rename { "${it}_${base.archivesName.get()}" } }
        }

        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
            kotlinOptions.jvmTarget = javaVersion.toString()
            kotlinOptions.freeCompilerArgs += "-Xskip-prerelease-check" // Required by others project like SilkMC. Also add this to intellij setting under Compiler -> Kotlin Compiler -> Additional ...
        }

        withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
            options.release.set(javaVersion.toString().toInt())
        }

        named<Test>("test") {
            useJUnitPlatform()

            testLogging {
                outputs.upToDateWhen { false } // When the build task is executed, stderr-stdout of test classes will be show
                showStandardStreams = true
            }
        }
    }

}

base {
    archivesName.set(properties["archives_name"].toString())
    group = property("maven_group")!!
    version = property("mod_version")!!
}

repositories {
    mavenCentral()
    maven("https://repo.repsy.io/mvn/amibeskyfy16/repo")
}

dependencies {
    implementation(project(path = ":api", configuration = "namedElements"))?.let { include(it) }
//    implementation(project(path = ":api", configuration = "namedElements"))
}

tasks {

    named<Wrapper>("wrapper") {
        gradleVersion = "8.7"
        distributionType = Wrapper.DistributionType.BIN
    }

    processResources { dependsOn(project(":api").tasks.processResources.get()) }

    loom {
        runs {
            this.getByName("client") {
                runDir = "testclient"

                val file = File("preconfiguration/doneclient.txt")
                if (!file.exists()) {
                    println("copying to client")
                    file.createNewFile()

                    // Copy some default files to the test client
                    copy {
                        from("preconfiguration/prepared_client/.")
                        into("testclient")
                        include("options.txt") // options.txt with my favorite settings
                    }

                    // Copying the world to use
                    copy {
                        from("preconfiguration/worlds/.")
                        include("testworld#1/**")
                        into("testclient/saves")
                    }

                    // Copying useful mods
                    copy {
                        from("preconfiguration/mods/client/.", "preconfiguration/mods/both/.")
                        include("*.jar")
                        into("testclient/mods")
                    }

                }
            }
            this.getByName("server") {
                runDir = "testserver"

                val file = File("preconfiguration/doneserver.txt")
                if (!file.exists()) {
                    file.createNewFile()
                    println("copying to server")

                    // Copy some default files to the test server
                    copy {
                        from("preconfiguration/prepared_server/.")
                        include("server.properties") // server.properties configured with usefully settings
                        include("eula.txt") // Accepted eula
                        into("testserver")
                    }

                    // Copying the world to use
                    copy {
                        from("preconfiguration/worlds/.")
                        include("testworld#1/**")
                        into("testserver")
                    }

                    // Copying useful mods
                    copy {
                        from("preconfiguration/mods/server/.", "preconfiguration/mods/both/.")
                        include("*.jar")
                        into("testserver/mods")
                    }
                }
            }
        }
    }

//    publish { finalizedBy(project(":api").tasks.publish.get()) }

    val copyJarToTestServer = register("copyJarToTestServer") {
        println("copying jar to server")
//        copyFile("build/libs/${project.properties["archives_name"]}-${project.properties["mod_version"]}.jar", project.property("testServerModsFolder") as String)
//        copyFile("build/libs/${project.properties["archives_name"]}-${project.properties["mod_version"]}.jar", project.property("testClientModsFolder") as String)
    }

    build { doLast { copyJarToTestServer.get() } }

}

fun copyFile(src: String, dest: String) = copy { from(src);into(dest) }

fun DependencyHandlerScope.includeTransitive(
    root: ResolvedDependency?,
    dependencies: Set<ResolvedDependency>,
    fabricLanguageKotlinDependency: ResolvedDependency,
    checkedDependencies: MutableSet<ResolvedDependency> = HashSet()
) {
    dependencies.forEach {
        if (checkedDependencies.contains(it) || (it.moduleGroup == "org.jetbrains.kotlin" && it.moduleName.startsWith("kotlin-stdlib")) || (it.moduleGroup == "org.slf4j" && it.moduleName == "slf4j-api"))
            return@forEach

        if (fabricLanguageKotlinDependency.children.any { kotlinDep -> kotlinDep.name == it.name }) {
            println("Skipping -> ${it.name} (already in fabric-language-kotlin)")
        } else {
            include(it.name)
            println("Including -> ${it.name} from ${root?.name}")
        }
        checkedDependencies += it

        includeTransitive(root ?: it, it.children, fabricLanguageKotlinDependency, checkedDependencies)
    }
}

// from : https://github.com/StckOverflw/TwitchControlsMinecraft/blob/4bf406893544c3edf52371fa6e7a6cc7ae80dc05/build.gradle.kts
fun DependencyHandlerScope.handleIncludes(project: Project, configuration: Configuration) {
    includeTransitive(
        null,
        configuration.resolvedConfiguration.firstLevelModuleDependencies,
        project.configurations.getByName("modImplementation").resolvedConfiguration.firstLevelModuleDependencies
            .first { it.moduleGroup == "net.fabricmc" && it.moduleName == "fabric-language-kotlin" }
    )
}
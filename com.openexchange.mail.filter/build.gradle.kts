import org.apache.tools.ant.filters.ReplaceTokens
import org.apache.tools.ant.taskdefs.Replace
import sun.nio.cs.US_ASCII

val mainSrcDir = "src"
val jSievePrefix = "org/apache"
val javaCCDir = "javacc"
val libDir = "lib"

pdeJava {
    installBundleAsDirectory.set(false)
}

tasks {

    val compileJJT by creating(JavaExec::class) {
        doFirst {
            mkdir("$mainSrcDir/$jSievePrefix/jsieve/parser/generated")
        }
        main = "jjtree"
        args("-output_directory=$mainSrcDir/$jSievePrefix/jsieve/parser/generated", "$javaCCDir/sieve.jjt")
        classpath = files("$libDir/javacc.jar")
    }

    val replaceToken by creating {
        val input = File(this.project.projectDir, "$mainSrcDir/$jSievePrefix/jsieve/parser/generated/SimpleNode.java")
        doLast {
            val content = input.readText(US_ASCII())
            if (content.contains("public class SimpleNode extends org.apache.jsieve.parser.SieveNode")) { return@doLast }
            val filtered = content.replace(oldValue = "public class SimpleNode", newValue = "public class SimpleNode extends org.apache.jsieve.parser.SieveNode")
            input.writeText(filtered, US_ASCII())
        }
        mustRunAfter(compileJJT)
    }

    val compileJJ by creating(JavaExec::class) {
        dependsOn(compileJJT, replaceToken)
        main = "javacc"
        args("-output_directory=$mainSrcDir/$jSievePrefix/jsieve/parser/generated",  "$mainSrcDir/$jSievePrefix/jsieve/parser/generated/sieve.jj")
        classpath = files("$libDir/javacc.jar")
        doLast {
            delete("$mainSrcDir/$jSievePrefix/jsieve/parser/generated/sieve.jj")
        }
    }

    val postClean by creating(Delete::class) {
        doFirst {
            println("Nom nom nom")
        }
        delete(File("$mainSrcDir/$jSievePrefix/jsieve/parser/generated"))
    }

    "compileJava" {
        dependsOn(compileJJ)
    }

    "clean" {
        finalizedBy(postClean)
    }
}

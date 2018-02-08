import java.io.FileWriter
import java.nio.file.Files

import scala.sys.process.Process

lazy val serverJar = settingKey[File]("Where the server JAR is located")
lazy val runServer = taskKey[Unit]("Runs the spigot server")

lazy val root = (project in file(".")).settings(
  name := "FireworkProjectiles",
  version := "0.1",
  scalaVersion := "2.12.4",
  resolvers += Resolver.mavenLocal,
  libraryDependencies += "org.spigotmc" % "spigot" % "1.12.2" % "provided", // Compile and mvn install this yourself
  serverJar := new File(Path.userHome.getAbsolutePath, "Documents/Spigot/spigot-1.12.2.jar"),
  runServer := {
    val t = target.value
    val sdir = new File(t, "server")
    sdir.mkdirs()
    val eula = new File(sdir, "eula.txt")
    eula.delete()
    val eulaWriter = new FileWriter(eula)
    eulaWriter.write("""#By changing the setting below to TRUE you are indicating your agreement to our EULA (https://account.mojang.com/documents/minecraft_eula).
                       |eula=true""".stripMargin.toCharArray)
    eulaWriter.close()
    val sjar = serverJar.value
    val njar = new File(sdir, sjar.getName)
    if(!njar.exists()) {
      Files.copy(sjar.toPath, njar.toPath)
    }
    val pdir = new File(sdir, "plugins")
    pdir.mkdirs()
    val pjar = assembly.value
    val npjar = new File(pdir, pjar.getName)
    if(npjar.exists()) npjar.delete()
    Files.copy(pjar.toPath, npjar.toPath)
    Process("java" :: "-Xmx1G" :: "-Xms1G" :: "-jar" :: njar.absolutePath :: Nil, sdir).!<
  }
)
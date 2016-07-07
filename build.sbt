// Defines the name of the project.
name := "ocpula"

// Defines the organization.
organization := "com.vsthost.rnd"

// Defines the version of the project.
version := "0.0.1-SNAPSHOT"

// Defines the scala version to be used.
scalaVersion := "2.11.8"

// Library dependencies:
libraryDependencies ++= Seq(
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "com.netaporter" %% "scala-uri" % "0.4.14"
)

// Define the remote repository for publising.
publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

// We want maven style:
publishMavenStyle := true

// Set credentials for publishing:
credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

// Define extra POM related information.
pomExtra :=
<licenses>
  <license>
    <name>Apache 2</name>
    <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
    <distribution>repo</distribution>
  </license>
</licenses>

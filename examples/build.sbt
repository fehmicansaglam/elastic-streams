name := "elastic-streams-examples"

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "net.fehmicansaglam" %% "tepkin" % "0.5-SNAPSHOT",
  "org.elasticsearch" % "elasticsearch" % "1.5.2",
  "org.slf4j" % "slf4j-simple" % "1.7.12"
)

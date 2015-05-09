name := "elastic-streams-examples"

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "net.fehmicansaglam" %% "tepkin" % "0.5-SNAPSHOT"
)

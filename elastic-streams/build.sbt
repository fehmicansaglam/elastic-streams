name := "elastic-streams"

libraryDependencies ++= Seq(
  "org.elasticsearch" % "elasticsearch" % "1.5.2" % Provided,
  "org.reactivestreams" % "reactive-streams" % "1.0.0",
  "org.scalatest" %% "scalatest" % "2.2.4" % Test
)

autoScalaLibrary := false

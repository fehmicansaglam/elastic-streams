# elastic-streams

Reactive Streams implementation for Elasticsearch

[![Join the chat at https://gitter.im/fehmicansaglam/elastic-streams](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/fehmicansaglam/elastic-streams?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

## Quick Start

### Setting up dependencies

Latest stable elastic-streams release is **0.1** and is available on Maven.

Scala developers, add the following dependency to your build.sbt:

```scala
libraryDependencies ++= Seq(
  "net.fehmicansaglam" % "elastic-streams" % "0.1"
)
```

Java developers, add the following dependency to your pom.xml:

```xml
<dependency>
  <groupId>net.fehmicansaglam</groupId>
  <artifactId>elastic-streams</artifactId>
  <version>0.1</version>
</dependency>
```

Or if you want to be on the bleeding edge using snapshots, latest snapshot release is **0.1-SNAPSHOT**.

Scala developers, add the following repository and dependency:
```scala
resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "net.fehmicansaglam" % "elastic-streams" % "0.1-SNAPSHOT"
)
```

Java developers add the following repository and dependency to your pom.xml:

```xml
<repository>
  <id>Sonatype Snapshots</id>
  <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
</repository>

<dependency>
	<groupId>net.fehmicansaglam</groupId>
	<artifactId>elastic-streams</artifactId>
	<version>0.1-SNAPSHOT</version>
</dependency>
```

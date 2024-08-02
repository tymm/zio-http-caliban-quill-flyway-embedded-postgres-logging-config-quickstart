enablePlugins(ZioSbtEcosystemPlugin)

inThisBuild(
  List(
    scalaVersion      := "3.4.1",
    organization      := "dev.zio",
    name              := "zio-http-caliban-quill-flyway-embedded-postgres-logging-config-quickstart",
    scalafixOnCompile := true,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)

val postgresql = {
  val osArch =
    System.getProperty("os.arch") match {
      case "aarch64" => "arm64v8"
      case _         => "amd64"
    }

  /** See https://github.com/zonkyio/embedded-postgres/issues/41 */
  val osVersion =
    System.getProperty("os.name").toLowerCase match {
      case osName if osName.contains("mac") =>
        "embedded-postgres-binaries-darwin"
      case osName if osName.contains("win") =>
        "embedded-postgres-binaries-windows"
      case osName if osName.contains("linux") =>
        "embedded-postgres-binaries-linux"
      case osName => throw new RuntimeException(s"Unknown operating system $osName")
    }

  Seq(
    "org.postgresql"         % "postgresql"          % "42.6.0",
    "io.zonky.test"          % "embedded-postgres"   % "2.0.7"  % Test,
    "io.zonky.test.postgres" % s"$osVersion-$osArch" % "16.2.0" % Test
  )
}

libraryDependencies ++= Seq(
  "com.github.ghostdogpr" %% "caliban"                    % "2.6.0",
  "com.github.ghostdogpr" %% "caliban-quick"              % "2.6.0",
  "dev.zio"               %% "zio"                        % "2.1.6",
  "dev.zio"               %% "zio-config"                 % "4.0.1",
  "dev.zio"               %% "zio-config-magnolia"        % "4.0.1",
  "dev.zio"               %% "zio-config-typesafe"        % "4.0.1",
  "dev.zio"               %% "zio-config-refined"         % "4.0.1",
  "dev.zio"               %% "zio-http"                   % "3.0.0-RC9",
  "dev.zio"               %% "zio-logging"                % "2.2.2",
  "dev.zio"               %% "zio-query"                  % "0.7.0",
  "io.getquill"           %% "quill-jdbc-zio"             % "4.8.3",
  "org.flywaydb"           % "flyway-core"                % "10.11.1",
  "org.flywaydb"           % "flyway-database-postgresql" % "10.11.1",
  "dev.zio"               %% "zio-test"                   % "2.1.6" % Test,
  "dev.zio"               %% "zio-test-sbt"               % "2.1.6" % Test,
  "dev.zio"               %% "zio-test-magnolia"          % "2.1.6" % Test
) ++ postgresql

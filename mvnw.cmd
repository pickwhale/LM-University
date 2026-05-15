@ECHO OFF
SETLOCAL

SET BASE_DIR=%~dp0
IF "%BASE_DIR:~-1%"=="\" SET BASE_DIR=%BASE_DIR:~0,-1%
SET WRAPPER_JAR=%BASE_DIR%\.mvn\wrapper\maven-wrapper.jar
SET MAVEN_USER_HOME=%BASE_DIR%\.m2

IF NOT "%JAVA_HOME%"=="" (
  SET JAVA_CMD=%JAVA_HOME%\bin\java.exe
) ELSE (
  SET JAVA_CMD=java.exe
)

"%JAVA_CMD%" -Dmaven.multiModuleProjectDirectory="%BASE_DIR%" -Dmaven.user.home="%MAVEN_USER_HOME%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
ENDLOCAL

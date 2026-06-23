@echo off
setlocal

set MAVEN_PROJECTBASEDIR=%CD%
set MAVEN_HOME=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper
set JAVA_HOME=C:\Program Files\Java
set MAVEN_OPTS=-Xmx256m

"C:\Program Files\Common Files\Oracle\Java\javapath\java.exe" -classpath ".mvn\wrapper\maven-wrapper.jar" "-Dmaven.home=%MAVEN_HOME%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*

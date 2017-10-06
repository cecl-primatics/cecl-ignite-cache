SET apacheIgniteHome=C:\apache-ignite-2.2.0-src\
SET projectDirectoryFolder=C:\Users\ttrott\eclipse-projects\ignite-cecl-project\ignite-service-1\
SET igniteDirectoryFolder=%apacheIgniteHome%bin\
SET igniteConfigDirectoryFolder=%apacheIgniteHome%config\
SET configFile=config\loss-amount-config.xml

start cmd.exe /k mvn clean install -f %projectDirectoryFolder%pom.xml
ping 127.0.0.1 -n 20 > nul

echo f | xcopy /f /v /y %projectDirectoryFolder%target\ignite-cecl-service-1.0-SNAPSHOT.jar.original %apacheIgniteHome%libs\ignite-cecl-service-1.0-SNAPSHOT.jar
ping 127.0.0.1 -n 5 > nul

start cmd.exe /k %igniteDirectoryFolder%ignite.bat %configFile%
ping 127.0.0.1 -n 15 > nul

start cmd.exe /k %igniteDirectoryFolder%ignite.bat %configFile%
ping 127.0.0.1 -n 15 > nul

::start cmd.exe /k %igniteDirectoryFolder%ignite.bat %configFile%
::ping 127.0.0.1 -n 15 > nul

::start cmd.exe /k %igniteDirectoryFolder%ignite.bat %configFile%
::ping 127.0.0.1 -n 15 > nul

::start cmd.exe /k %igniteDirectoryFolder%ignite.bat %configFile%
::ping 127.0.0.1 -n 15 > nul

::start cmd.exe /k %igniteDirectoryFolder%ignite.bat %configFile%
::ping 127.0.0.1 -n 15 > nul

::SET MAVEN_OPTS=-XX:+UseG1GC -Xms128m -Xmx1g -server -XX:+AggressiveOpts -XX:MaxMetaspaceSize=256m -XX:+UseG1GC -XX:+ScavengeBeforeFullGC -XX:+DisableExplicitGC -XX:+AlwaysPreTouch
::start cmd.exe /k mvn clean package exec:java -f %projectDirectoryFolder%pom.xml -Dexec.mainClass=com.primatics.ignite.CalculateLossAmountTest

::SET JVM_OPTS=-XX:+UseG1GC -Xms128m -Xmx2g -server -XX:+AggressiveOpts -XX:MaxMetaspaceSize=256m -XX:+UseG1GC -XX:+ScavengeBeforeFullGC -XX:+DisableExplicitGC -XX:+AlwaysPreTouch
::start cmd.exe /k "C:\Program Files\Java\jdk1.8.0_131\bin\java.exe" %JVM_OPTS% -jar %projectDirectoryFolder%target\ignite-service-1.0-SNAPSHOT-jar-with-dependencies.jar


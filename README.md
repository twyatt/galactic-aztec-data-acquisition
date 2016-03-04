# Galactic Aztec Data Acquisition Software

Server and client application suite for reading sensors from a Raspberry Pi and transmitting to a remote client.

## Server
### Installation
```
./gradlew :server:dist
scp server/build/libs/server.jar pi@raspberrypi:~
```

SSH into your Raspberry Pi:
```
ssh pi@raspberrypi
```

Then the server can be started with the following command:
```
sudo java -jar server.jar
```

## Client
### Run

The client application can either be started via IntelliJ or command line.

#### IntelliJ
Run `Launcher.main()`:
![Run Launcher](artwork/client_launch.png?raw=true)

#### Command Line
./gradlew :client:run
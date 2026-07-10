# Docker and Windows `.exe` Distribution Guide

## Short Answer

Yes, you can use Docker in the distribution workflow, but Docker should usually build the Java app artifacts, not the final Windows `.exe`.

For this JavaFX desktop app, the recommended flow is:

1. Use Docker to build the JAR and collect runtime dependencies.
2. Copy the build output to your Windows machine.
3. Use `jpackage` on Windows to generate the `.exe` installer.

Why? A Windows `.exe` installer is OS-specific. `jpackage --type exe` must run on Windows and needs WiX Toolset. A normal Linux Docker container cannot generate a proper Windows `.exe` installer.

## Best Workflow

```text
Source Code
   |
   v
Docker build container
   |
   v
target/lostandfound-1.0.jar + dependency jars
   |
   v
Windows host
   |
   v
jpackage + WiX Toolset
   |
   v
Campus Lost and Found-1.0.exe
```

## Requirements

Install these on your Windows machine:

- Docker Desktop
- JDK 26 or newer
- Maven, optional if Docker handles the Maven build
- WiX Toolset 3.x

Check tools:

```powershell
docker --version
java --version
jpackage --version
candle.exe -?
light.exe -?
```

If `candle.exe` or `light.exe` is not recognized, WiX is not installed or not added to `PATH`.

## Create a Dockerfile

Create a file named `Dockerfile.build` in the project root:

```dockerfile
FROM maven:3.9-eclipse-temurin-26

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -Dmaven.test.skip=true
RUN mkdir -p target/deploy
RUN cp target/lostandfound-1.0.jar target/deploy/
RUN mvn dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/deploy
```

This builds:

```text
target/lostandfound-1.0.jar
target/deploy/
```

## Build with Docker

Run this from the project root:

```powershell
docker build -f Dockerfile.build -t lf-v2-builder .
```

## Copy Build Output from Docker

Create a temporary container:

```powershell
docker create --name lf-v2-output lf-v2-builder
```

Copy the deploy folder:

```powershell
Remove-Item -Recurse -Force target\deploy -ErrorAction SilentlyContinue
docker cp lf-v2-output:/app/target/deploy target/deploy
docker rm lf-v2-output
```

After copying, confirm this exists:

```text
target/deploy/lostandfound-1.0.jar
```

The folder should also include JavaFX jars and the PostgreSQL JDBC driver.

## Generate the Windows `.exe`

Run this on Windows, not inside the Linux Docker container:

```powershell
Remove-Item -Recurse -Force target\installer -ErrorAction SilentlyContinue

jpackage `
  --type exe `
  --name "Campus Lost and Found" `
  --app-version 1.0 `
  --vendor "PUPSRC" `
  --input target\deploy `
  --main-jar lostandfound-1.0.jar `
  --main-class Main `
  --win-menu `
  --win-menu-group "PUPSRC" `
  --win-shortcut `
  --dest target\installer
```

Output:

```text
target/installer/Campus Lost and Found-1.0.exe
```

Send this `.exe` installer to users.

## Database Credentials on User PCs

Do not bake Supabase credentials into the installer.

Set these environment variables on every target machine:

```powershell
setx DB_URL "jdbc:postgresql://your-supabase-host:5432/postgres?sslmode=require"
setx DB_USER "postgres.your-project-ref"
setx DB_PASSWORD "your-password"
```

Close and reopen the app after setting credentials.

## Can Docker Generate the `.exe` Directly?

Usually, no.

A Linux Docker container can build the Java JAR, but it cannot properly run Windows `jpackage --type exe`.

Possible but not recommended:

- Windows containers can theoretically run Windows tools, but setup is heavier.
- You still need Windows base images, a compatible JDK, and WiX Toolset inside the container.
- JavaFX desktop packaging is easier and more reliable on a Windows host.

For school or team distribution, use Docker for repeatable builds and Windows `jpackage` for the installer.

## Quick Release Checklist

Before sharing the installer:

1. Build the deploy folder with Docker.
2. Run `jpackage` on Windows.
3. Install the generated `.exe` on a clean test machine.
4. Set `DB_URL`, `DB_USER`, and `DB_PASSWORD`.
5. Open the app.
6. Log in as admin.
7. Test dashboard, reporting, claim, resolve, account logs, and PDF generation.

## Troubleshooting

### Docker build fails downloading dependencies

Check internet connection and Maven Central access:

```powershell
docker build -f Dockerfile.build -t lf-v2-builder .
```

Run again after connection is fixed.

### `jpackage` is not recognized

Use a full JDK, not only a JRE:

```powershell
jpackage --version
```

### `.exe` generation fails because WiX is missing

Install WiX Toolset 3.x and reopen PowerShell:

```powershell
candle.exe -?
light.exe -?
```

### App installs but does not open

Check startup log:

```text
%LOCALAPPDATA%\CampusLostAndFound\logs\startup.log
```

Also confirm the package used:

```powershell
--main-class Main
```

### Database features fail after install

Check environment variables:

```powershell
echo $env:DB_URL
echo $env:DB_USER
echo $env:DB_PASSWORD
```

If empty, set them again with `setx`, then reopen the app.

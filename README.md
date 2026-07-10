# Campus Lost and Found

JavaFX desktop application for managing campus lost-and-found reports, claims, admin login, and activity logs. The app connects to a PostgreSQL/Supabase database through JDBC.

## Project Overview

- UI: JavaFX with FXML and CSS
- Build tool: Maven
- Database: PostgreSQL/Supabase
- Packaged main class: `Main`
- JavaFX application class: `App`
- JavaFX Maven entry point: `mvn javafx:run`
- Windows packaging tool: `jpackage`

## Requirements

Install these before building:

- JDK 24 or newer
- Maven
- Internet access for the first Maven dependency download
- PostgreSQL/Supabase database credentials

For a Windows installer `.exe`, also install:

- WiX Toolset 3.x

`jpackage` can create an app folder without WiX, but Windows `.exe` or `.msi` installers need WiX.

## Database Configuration

The app reads database credentials in this order:

1. System environment variables
2. `.env` file in the app working folder
3. `src/main/resources/config.properties`

The recommended setup for deployment is environment variables, because credentials should not be bundled into the installer.

### Option 1: Environment Variables

In PowerShell:

```powershell
setx DB_URL "jdbc:postgresql://your-supabase-host:5432/postgres?sslmode=require"
setx DB_USER "postgres.your-project-ref"
setx DB_PASSWORD "your-password"
```

Close and reopen PowerShell after running `setx`.

### Option 2: Local `.env` File

Copy `.env.example` to `.env` and fill in the real values:

```env
DB_URL=jdbc:postgresql://your-supabase-host:5432/postgres?sslmode=require
DB_USER=postgres.your-project-ref
DB_PASSWORD=your-password
```

Do not commit `.env`.

### Option 3: `config.properties`

Create or update `src/main/resources/config.properties`:

```properties
db.url=jdbc:postgresql://your-supabase-host:5432/postgres?sslmode=require
db.user=postgres.your-project-ref
db.password=your-password
```

Only use this for local testing. Maven is configured not to package `config.properties` into the installer, because database credentials should stay outside the app bundle.

## Run Locally

From the project folder:

```powershell
mvn clean compile
mvn javafx:run
```

The login window should open with the title `PUPSRC Lost and Found`.

## Build the JAR

Build the app:

```powershell
mvn clean package "-Dmaven.test.skip=true"
```

This creates:

```text
target/lostandfound-1.0.jar
```

The JAR by itself is not the easiest deployment format for JavaFX. Use `jpackage` to bundle it with a runtime and create a Windows app or installer.

## Prepare Files for `jpackage`

Create a deployment folder containing the app JAR and runtime dependencies:

```powershell
New-Item -ItemType Directory -Force target\deploy
Copy-Item target\lostandfound-1.0.jar target\deploy\
mvn dependency:copy-dependencies "-DincludeScope=runtime" "-DoutputDirectory=target/deploy"
```

After this, `target\deploy` should contain:

- `lostandfound-1.0.jar`
- JavaFX dependency JARs
- PostgreSQL JDBC driver JAR

## Create a Windows App Folder

This creates a portable app image. It does not require WiX.

```powershell
jpackage `
  --type app-image `
  --name "Campus Lost and Found" `
  --app-version 1.0 `
  --input target\deploy `
  --main-jar lostandfound-1.0.jar `
  --main-class Main `
  --dest target\installer
```

Output:

```text
target\installer\Campus Lost and Found\
```

Run the app:

```powershell
& "target\installer\Campus Lost and Found\Campus Lost and Found.exe"
```

## Create a Windows Installer `.exe`

Install WiX Toolset 3.x first, then make sure WiX is available from PowerShell:

```powershell
candle.exe -?
light.exe -?
```

Then run:

```powershell
jpackage `
  --type exe `
  --name "Campus Lost and Found" `
  --app-version 1.0 `
  --vendor "Group 2" `
  --input target\deploy `
  --main-jar lostandfound-1.0.jar `
  --main-class Main `
  --win-menu `
  --win-menu-group "Group 2" `
  --win-shortcut `
  --dest target\installer
```

Output:

```text
target\installer\Campus Lost and Found-1.0.exe
```

Send that installer to users.

After installing, open the app from:

```text
Start Menu > PUPSRC > Campus Lost and Found
```

The installer finishes after copying the app; it does not automatically open the login window.

## Recommended Release Checklist

Before sharing the installer:

1. Confirm the database is reachable.
2. Confirm `DB_URL`, `DB_USER`, and `DB_PASSWORD` are set on the target computer, or provide a safe `.env` setup process.
3. Run the app from the generated app folder.
4. Log in as admin.
5. Open the dashboard.
6. Add a lost/found item.
7. Verify claims and status changes work.

## Useful Commands

Run the JavaFX app:

```powershell
mvn javafx:run
```

Compile only:

```powershell
mvn clean compile
```

Build JAR:

```powershell
mvn clean package "-Dmaven.test.skip=true"
```

Rebuild deployment folder:

```powershell
Remove-Item -Recurse -Force target\deploy,target\installer -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force target\deploy
mvn clean package "-Dmaven.test.skip=true"
Copy-Item target\lostandfound-1.0.jar target\deploy\
mvn dependency:copy-dependencies "-DincludeScope=runtime" "-DoutputDirectory=target/deploy"
```

Create installer:

```powershell
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

## Troubleshooting

### `jpackage` is not recognized

Use a full JDK, not only a JRE. Check:

```powershell
java --version
jpackage --version
```

### `candle.exe` or `light.exe` is missing

WiX Toolset is missing or not on `PATH`. Install WiX Toolset 3.x, reopen PowerShell, and try again.

### App opens but database features fail

Check that these are set correctly:

```text
DB_URL
DB_USER
DB_PASSWORD
```

The app also supports `.env` and `config.properties`, but environment variables are safer for installed copies.

### Installed app shows no window and no error

Make sure the package uses `Main`, not `App`, as the `jpackage` main class:

```powershell
--main-class Main
```

`App` extends `javafx.application.Application` directly. If the packaged launcher starts `App`, Windows may hide the JavaFX startup error and the app can appear to do nothing.

The app also writes startup logs here:

```text
%LOCALAPPDATA%\CampusLostAndFound\logs\startup.log
```

If the installed app does not open, check that file for the exact error.

### `Database credentials are missing`

The app could not find credentials from environment variables, `.env`, or `config.properties`.

### PostgreSQL password authentication failed

The database password is incorrect or expired. Update `DB_PASSWORD`.

### Supabase connection refused or timeout

Check the database URL, Supabase project status, network connection, and whether the database allows the selected connection method.

### Java version errors

The project is configured for Java release `24` in `pom.xml` because it uses JavaFX `24`.

The Java release in `pom.xml` must be less than or equal to the JDK used by `jpackage`. For example, a Java 25 packaged runtime can run Java 24 class files, but it cannot run Java 26 class files.

Check both versions before packaging:

```powershell
java --version
javac --version
jpackage --version
```

## Project Structure

```text
src/main/java/Main.java                        Packaged launcher entry point
src/main/java/App.java                         JavaFX application class
src/main/java/controller/                      JavaFX controllers
src/main/java/model/                           UI-facing model/session classes
src/main/java/mapper/                          Mapping helpers
src/main/java/com/campuslf/database/           Database connection utilities
src/main/java/com/campuslf/dao/                DAO classes
src/main/java/com/campuslf/service/            Service classes
src/main/java/com/campuslf/models/             Database model classes
src/main/resources/fxml/                       JavaFX layouts
src/main/resources/css/                        App styles
src/main/resources/images/                     App images
```

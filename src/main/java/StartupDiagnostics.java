import javax.swing.JOptionPane;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public final class StartupDiagnostics {
    private static final Path LOG_FILE = resolveLogFile();
    private static boolean installed;

    private StartupDiagnostics() {
    }

    public static synchronized void install() {
        if (installed) {
            return;
        }
        installed = true;

        try {
            Files.createDirectories(LOG_FILE.getParent());
            PrintStream logStream = new PrintStream(
                    Files.newOutputStream(LOG_FILE,
                            java.nio.file.StandardOpenOption.CREATE,
                            java.nio.file.StandardOpenOption.APPEND),
                    true);
            System.setOut(new TeePrintStream(System.out, logStream));
            System.setErr(new TeePrintStream(System.err, logStream));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            log("Uncaught exception on thread " + thread.getName(), error);
            showFatalDialog(error);
        });

        log("Application starting. Log file: " + LOG_FILE);
    }

    public static void log(String message) {
        System.err.println("[" + LocalDateTime.now() + "] " + message);
    }

    public static void log(String message, Throwable error) {
        log(message);
        if (error != null) {
            error.printStackTrace(System.err);
        }
    }

    public static Path getLogFile() {
        return LOG_FILE;
    }

    public static void showFatalDialog(Throwable error) {
        String message = error == null ? "Unknown startup error" : error.toString();
        JOptionPane.showMessageDialog(
                null,
                "Campus Lost and Found could not start.\n\n"
                        + message
                        + "\n\nA log was saved here:\n"
                        + LOG_FILE,
                "Campus Lost and Found",
                JOptionPane.ERROR_MESSAGE);
    }

    private static Path resolveLogFile() {
        String localAppData = System.getenv("LOCALAPPDATA");
        Path base = localAppData == null || localAppData.isBlank()
                ? Path.of(System.getProperty("user.home"), "AppData", "Local")
                : Path.of(localAppData);
        return base.resolve("CampusLostAndFound").resolve("logs").resolve("startup.log");
    }

    private static final class TeePrintStream extends PrintStream {
        private final PrintStream second;

        private TeePrintStream(PrintStream first, PrintStream second) {
            super(new TeeOutputStream(first, second), true);
            this.second = second;
        }

        @Override
        public void close() {
            second.close();
        }
    }

    private static final class TeeOutputStream extends OutputStream {
        private final PrintStream first;
        private final PrintStream second;

        private TeeOutputStream(PrintStream first, PrintStream second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public void write(int b) {
            first.write(b);
            second.write(b);
        }

        @Override
        public void flush() {
            first.flush();
            second.flush();
        }
    }
}

public class Main {
    public static void main(String[] args) {
        StartupDiagnostics.install();
        try {
            App.main(args);
        } catch (Throwable error) {
            StartupDiagnostics.log("Fatal startup error", error);
            StartupDiagnostics.showFatalDialog(error);
            System.exit(1);
        }
    }
}

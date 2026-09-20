public final class BrowserBridge {
    private BrowserBridge() {
    }

    public static native void notifyReady(String stage);

    public static void signalReady(String stage) {
        if (!Boolean.getBoolean("portfolio.browser")) {
            return;
        }

        try {
            notifyReady(stage);
        } catch (UnsatisfiedLinkError ignored) {
            // Native bridge exists only inside the CheerpJ browser launcher.
        }
    }
}

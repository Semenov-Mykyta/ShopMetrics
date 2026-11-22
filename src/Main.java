import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Main <orders.csv>");
            System.exit(1);
            return;
        }

        try {
            ShopMetrics.printStatusReport(Path.of(args[0]));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Cannot read orders: " + e.getMessage());
            System.exit(1);
        }
    }
}

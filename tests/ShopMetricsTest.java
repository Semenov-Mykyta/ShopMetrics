import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ShopMetricsTest {
    public static void main(String[] args) throws Exception {
        String sampleReport = report(Path.of("examples/orders.csv"));
        check(sampleReport.contains("Orders: 7"), "all seven orders should be counted");
        check(sampleReport.contains("DELIVERED: 2"), "the last delivered order should be counted");
        check(sampleReport.contains("CANCELLED: 1"), "a quoted comma should not skip an order");
        check(sampleReport.contains("Skipped rows: 0"), "valid CSV rows should not be skipped");

        Path oneOrder = Files.createTempFile("shopmetrics-one-order-", ".csv");
        try {
            Files.writeString(oneOrder,
                    "order_id,date,product,supplier,sale_price,purchase_cost,marketplace_fee,status\n" +
                    "1,2026-11-03,Item,Supplier,5.00,3.00,0.20,PENDING\n");
            check(report(oneOrder).contains("Orders: 1"), "one-row CSV should count its last row");
        } finally {
            Files.deleteIfExists(oneOrder);
        }
        System.out.println("PASS: status summary regression tests");
    }

    private static String report(Path csv) throws Exception {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(captured, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            ShopMetrics.printStatusReport(csv);
        } finally {
            System.setOut(originalOut);
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}

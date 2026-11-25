import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class ShopMetrics {
    private static final String HEADER =
            "order_id,date,product,supplier,sale_price,purchase_cost,marketplace_fee,status";

    public static void printStatusReport(Path csvFile) throws IOException {
        List<String> lines = Files.readAllLines(csvFile, StandardCharsets.UTF_8);
        if (lines.isEmpty() || !HEADER.equals(lines.get(0))) {
            throw new IllegalArgumentException("Unexpected CSV header");
        }

        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String status : List.of(
                "PENDING", "IN_PROGRESS", "DELIVERED", "PROBLEM", "CANCELLED", "REFUNDED")) {
            counts.put(status, 0);
        }

        Set<String> orderIds = new HashSet<>();
        int skippedRows = 0;

        for (int i = 1; i < lines.size(); i++) {
            String[] fields = splitCsvLine(lines.get(i));
            String problem = validate(fields, counts, orderIds);
            if (problem != null) {
                skippedRows++;
                System.err.println("Line " + (i + 1) + " skipped: " + problem);
                continue;
            }

            String status = fields[7].trim();
            counts.put(status, counts.get(status) + 1);
            orderIds.add(fields[0].trim());
        }

        int total = counts.values().stream().mapToInt(Integer::intValue).sum();
        System.out.println("Orders: " + total);
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("Skipped rows: " + skippedRows);
    }

    private static String[] splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (character == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (character == ',' && !inQuotes) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(character);
            }
        }
        fields.add(field.toString());
        return fields.toArray(new String[0]);
    }

    private static String validate(String[] fields, Map<String, Integer> counts, Set<String> orderIds) {
        if (fields.length != 8) {
            return "expected 8 columns";
        }
        for (String field : fields) {
            if (field.isBlank()) {
                return "missing required field";
            }
        }
        try {
            LocalDate.parse(fields[1].trim());
        } catch (DateTimeParseException e) {
            return "invalid date";
        }
        for (int i = 4; i <= 6; i++) {
            try {
                if (new BigDecimal(fields[i].trim()).signum() < 0) {
                    return "negative monetary amount";
                }
            } catch (NumberFormatException e) {
                return "invalid monetary amount";
            }
        }
        if (!counts.containsKey(fields[7].trim())) {
            return "unknown status";
        }
        if (orderIds.contains(fields[0].trim())) {
            return "duplicate order ID";
        }
        return null;
    }
}

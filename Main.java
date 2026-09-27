import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase principal de procesamiento para la segunda entrega del proyecto
 * "Generación y clasificación de datos".
 * <p>
 * Lee los archivos de productos, vendedores y ventas generados por
 * GenerateInfoFiles y crea dos reportes CSV:
 * <p>
 * 1. salesmen_report.csv:
 *    vendedores ordenados de mayor a menor según dinero recaudado.
 * <p>
 * 2. products_report.csv:
 *    productos ordenados de mayor a menor según cantidad vendida.
 * <p>
 * Compatible con Java 8.
 */
public class Main {

    private static final String PRODUCTS_FILE = "products.csv";
    private static final String SALESMEN_FILE = "salesmen.csv";
    private static final String SALE_FILE_PREFIX = "sales_";

    private static final String SALESMEN_REPORT_FILE = "salesmen_report.csv";
    private static final String PRODUCTS_REPORT_FILE = "products_report.csv";

    /**
     * Punto de entrada del programa.
     * <p>
     * El programa no solicita información al usuario, de acuerdo con
     * los requisitos del proyecto.
     */
    public static void main(String[] args) {
        try {
            Map<Integer, Product> products = loadProducts();
            Map<Long, Salesman> salesmen = loadSalesmen();

            processSalesFiles(products, salesmen);

            createSalesmenReport(salesmen);
            createProductsReport(products);

            System.out.println("Proceso finalizado correctamente.");
            System.out.println("Archivos generados:");
            System.out.println("- " + SALESMEN_REPORT_FILE);
            System.out.println("- " + PRODUCTS_REPORT_FILE);

        } catch (Exception e) {
            System.out.println("Error durante el procesamiento: " + e.getMessage());
        }
    }

    /**
     * Lee el archivo de productos.
     */
    private static Map<Integer, Product> loadProducts() throws IOException {
        Map<Integer, Product> products = new HashMap<>();

        BufferedReader reader = new BufferedReader(new FileReader(Main.PRODUCTS_FILE));
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }

            String[] parts = line.split(";");

            if (parts.length < 3) {
                continue;
            }

            int id = Integer.parseInt(parts[0].trim());
            String name = parts[1].trim();
            double price = Double.parseDouble(parts[2].trim());

            products.put(id, new Product(name, price));
        }

        reader.close();
        return products;
    }

    /**
     * Lee el archivo general de vendedores.
     */
    private static Map<Long, Salesman> loadSalesmen() throws IOException {
        Map<Long, Salesman> salesmen = new HashMap<>();

        BufferedReader reader = new BufferedReader(new FileReader(Main.SALESMEN_FILE));
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }

            String[] parts = line.split(";");

            if (parts.length < 4) {
                continue;
            }

            String documentType = parts[0].trim();
            long documentNumber = Long.parseLong(parts[1].trim());
            String firstName = parts[2].trim();
            String lastName = parts[3].trim();

            Salesman salesman = new Salesman(
                    documentType,
                    documentNumber,
                    firstName,
                    lastName
            );

            salesmen.put(documentNumber, salesman);
        }

        reader.close();
        return salesmen;
    }

    /**
     * Busca en la carpeta actual todos los archivos que comiencen por
     * "sales_" y los procesa.
     */
    private static void processSalesFiles(
            Map<Integer, Product> products,
            Map<Long, Salesman> salesmen) throws IOException {

        File currentDirectory = new File(".");

        File[] salesFiles = currentDirectory.listFiles();

        if (salesFiles == null) {
            throw new IOException("No fue posible leer los archivos de la carpeta del proyecto.");
        }

        boolean salesFileFound = false;

        for (File file : salesFiles) {
            if (file.isFile()
                    && file.getName().startsWith(SALE_FILE_PREFIX)
                    && file.getName().endsWith(".csv")
                    && !file.getName().equals(SALESMEN_REPORT_FILE)) {

                salesFileFound = true;
                processSingleSalesFile(file, products, salesmen);
            }
        }

        if (!salesFileFound) {
            throw new IOException("No se encontraron archivos de ventas para procesar.");
        }
    }

    /**
     * Procesa un archivo de ventas de un vendedor.
     */
    private static void processSingleSalesFile(
            File salesFile,
            Map<Integer, Product> products,
            Map<Long, Salesman> salesmen) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(salesFile));

        String firstLine = reader.readLine();

        if (firstLine == null || firstLine.trim().isEmpty()) {
            reader.close();
            return;
        }

        String[] salesmanData = firstLine.split(";");

        if (salesmanData.length < 2) {
            reader.close();
            return;
        }

        long salesmanId = Long.parseLong(salesmanData[1].trim());
        Salesman salesman = salesmen.get(salesmanId);

        if (salesman == null) {
            reader.close();
            return;
        }

        String line;

        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }

            String[] parts = line.split(";");

            if (parts.length < 2) {
                continue;
            }

            int productId = Integer.parseInt(parts[0].trim());
            int quantity = Integer.parseInt(parts[1].trim());

            Product product = products.get(productId);

            if (product != null && quantity > 0) {
                double saleValue = product.getPrice() * quantity;

                salesman.addMoneyCollected(saleValue);
                product.addSoldQuantity(quantity);
            }
        }

        reader.close();
    }

    /**
     * Crea el reporte de vendedores ordenado por dinero recaudado.
     */
    private static void createSalesmenReport(
            Map<Long, Salesman> salesmen) throws IOException {

        List<Salesman> salesmanList = new ArrayList<>(salesmen.values());

        salesmanList.sort((s1, s2) -> Double.compare(
                s2.getMoneyCollected(),
                s1.getMoneyCollected()
        ));

        BufferedWriter writer =
                new BufferedWriter(new FileWriter(SALESMEN_REPORT_FILE));

        for (Salesman salesman : salesmanList) {
            writer.write(
                    salesman.getFullName()
                            + ";"
                            + formatNumber(salesman.getMoneyCollected())
            );
            writer.newLine();
        }

        writer.close();
    }

    /**
     * Crea el reporte de productos ordenado por cantidad vendida.
     */
    private static void createProductsReport(
            Map<Integer, Product> products) throws IOException {

        List<Product> productList = new ArrayList<>(products.values());

        productList.sort(new Comparator<>() {
            /// @param p1 the first object to be compared.
            /// @param p2 the second object to be compared.
            @Override
            public int compare(Product p1, Product p2) {
                return Integer.compare(p2.getSoldQuantity(), p1.getSoldQuantity());
            }
        });

        BufferedWriter writer =
                new BufferedWriter(new FileWriter(PRODUCTS_REPORT_FILE));

        for (Product product : productList) {
            writer.write(
                    product.getName()
                            + ";"
                            + formatNumber(product.getPrice())
                            + ";"
                            + product.getSoldQuantity()
            );
            writer.newLine();
        }

        writer.close();
    }

    /**
     * Convierte un número a texto sin decimales cuando su valor es entero.
     */
    private static String formatNumber(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }

        return String.valueOf(value);
    }

    /**
     * Representa un producto del archivo products.csv.
     */
    private static class Product {

        private final String name;
        private final double price;
        private int soldQuantity;

        Product(String name, double price) {
            this.name = name;
            this.price = price;
            this.soldQuantity = 0;
        }

         public String getName() {
            return name;
        }

        double getPrice() {
            return price;
        }

        int getSoldQuantity() {
            return soldQuantity;
        }

        void addSoldQuantity(int quantity) {
            soldQuantity += quantity;
        }
    }

    /**
     * Representa un vendedor del archivo salesmen.csv.
     */
    private static class Salesman {

        public final String documentType;
        public final long documentNumber;
        private final ThreadLocal<String> firstName = new ThreadLocal<>();
        private final String lastName;
        private double moneyCollected;

        Salesman(
                String documentType,
                long documentNumber,
                String firstName,
                String lastName) {

            this.documentType = documentType;
            this.documentNumber = documentNumber;
            this.firstName.set(firstName);
            this.lastName = lastName;
            this.moneyCollected = 0.0;
        }

        String getFullName() {
            return firstName + " " + lastName;
        }

        double getMoneyCollected() {
            return moneyCollected;
        }

        void addMoneyCollected(double value) {
            moneyCollected += value;
        }
    }
}

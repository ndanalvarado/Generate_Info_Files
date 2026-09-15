import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Clase encargada de generar archivos de prueba para el proyecto
 * Generación y clasificación de datos.
 * <p>
 * Los archivos generados contienen información de productos,
 * vendedores y ventas realizadas por cada vendedor.
 * <p>
 * Esta clase corresponde a la primera entrega del proyecto.
 */
public class Generate_Info_Files {

    private static final Random RANDOM = new Random();
    private static int productsCreated;
    private static String[] salesmanNames;
    private static long[] salesmanIds;

    private static final String[] PRODUCT_NAMES = {
            "Laptop", "Mouse", "Teclado", "Monitor", "Impresora",
            "Audifonos", "Camara", "Tablet", "Memoria USB", "Disco Duro"
    };

    private static final String[] FIRST_NAMES = {
            "Ana", "Carlos", "Laura", "Andres", "Maria",
            "Juan", "Sofia", "Daniel", "Camila", "Luis"
    };

    private static final String[] LAST_NAMES = {
            "Gomez", "Martinez", "Ramirez", "Lopez", "Rodriguez",
            "Hernandez", "Torres", "Diaz", "Castro", "Moreno"
    };

    public static void main(String[] args) {
        try {
            int productsCount = 8;
            int salesmanCount = 5;
            int salesPerSalesman = 5;

            createProductsFile(productsCount);
            createSalesManInfoFile(salesmanCount);

            for (int i = 0; i < salesmanCount; i++) {
                createSalesMenFile(
                        salesPerSalesman,
                        salesmanNames[i],
                        salesmanIds[i]
                );
            }

            System.out.println("Los archivos fueron generados correctamente.");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createProductsFile(int productsCount) throws IOException {
        productsCreated = productsCount;

        BufferedWriter writer =
                new BufferedWriter(new FileWriter("products.csv"));

        for (int i = 1; i <= productsCount; i++) {
            String productName;

            if (i <= PRODUCT_NAMES.length) {
                productName = PRODUCT_NAMES[i - 1];
            } else {
                productName = "Producto " + i;
            }

            int price = 10000 + RANDOM.nextInt(990001);

            writer.write(i + ";" + productName + ";" + price);
            writer.newLine();
        }

        writer.close();
    }

    public static void createSalesManInfoFile(int salesmanCount) throws IOException {
        salesmanNames = new String[salesmanCount];
        salesmanIds = new long[salesmanCount];

        BufferedWriter writer =
                new BufferedWriter(new FileWriter("salesmen.csv"));

        for (int i = 0; i < salesmanCount; i++) {
            String firstName =
                    FIRST_NAMES[RANDOM.nextInt(FIRST_NAMES.length)];

            String lastName =
                    LAST_NAMES[RANDOM.nextInt(LAST_NAMES.length)];

            long id =
                    1000000000L + RANDOM.nextInt(900000000);

            String completeName = firstName + " " + lastName;

            salesmanNames[i] = completeName;
            salesmanIds[i] = id;

            writer.write(
                    "CC;" + id + ";" + firstName + ";" + lastName
            );
            writer.newLine();
        }

        writer.close();
    }

    public static void createSalesMenFile(
            int randomSalesCount,
            String name,
            long id) throws IOException {

        String safeName = name.replace(" ", "_");
        String fileName =
                "sales_" + safeName + "_" + id + ".csv";

        BufferedWriter writer =
                new BufferedWriter(new FileWriter(fileName));

        writer.write("CC;" + id);
        writer.newLine();

        List<Integer> productIds = new ArrayList<>();

        for (int i = 1; i <= productsCreated; i++) {
            productIds.add(i);
        }

        Collections.shuffle(productIds);

        int salesToGenerate =
                Math.min(randomSalesCount, productsCreated);

        for (int i = 0; i < salesToGenerate; i++) {
            int productId = productIds.get(i);
            int quantity = 1 + RANDOM.nextInt(10);

            writer.write(productId + ";" + quantity + ";");
            writer.newLine();
        }

        writer.close();
    }
}

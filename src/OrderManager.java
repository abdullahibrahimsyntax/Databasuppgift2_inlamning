import java.sql.*;
import java.util.Scanner;


public class OrderManager {


    public static void addToCart(Connection conn, int customerID) {
        Scanner scanner = new Scanner(System.in);

        try {

            System.out.println("\n Tillgängliga produkter:");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT product_id, Name, stock FROM product WHERE stock > 0");

            while (rs.next()) {
                System.out.println("Produkt-ID: " + rs.getInt("product_id") +
                        " | Namn: " + rs.getString("Name") +
                        " | Lager: " + rs.getInt("stock"));
            }
            rs.close();
            stmt.close();


            System.out.print("\nAnge produkt-ID att lägga i kundvagnen: ");
            int productID = Integer.parseInt(scanner.nextLine());


            Integer orderID = getActiveOrderID(conn, customerID);


            String callSP = "{CALL AddToCart(?, ?, ?)}";
            CallableStatement stmtSP = conn.prepareCall(callSP);
            stmtSP.setInt(1, customerID);
            if (orderID == null) {
                stmtSP.setNull(2, Types.INTEGER);
            } else {
                stmtSP.setInt(2, orderID);
            }
            stmtSP.setInt(3, productID);


            boolean hasResultSet = stmtSP.execute();
            if (hasResultSet) {
                ResultSet rsSP = stmtSP.getResultSet();
                if (rsSP.next()) {
                    System.out.println("\n " + rsSP.getString(1));
                }
                rsSP.close();
            } else {
                System.out.println("\n Produkten lades till i beställningen!");
            }


            checkStock(conn, productID);

            stmtSP.close();

        } catch (SQLException e) {
            System.out.println("Fel vid orderläggning: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static Integer getActiveOrderID(Connection conn, int customerID) throws SQLException {
        String query = "SELECT ORDERID FROM ORDERS WHERE CustomerID = ? AND status = 'ACTIVE' LIMIT 1";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, customerID);
        ResultSet rs = stmt.executeQuery();

        Integer orderID = null;
        if (rs.next()) {
            orderID = rs.getInt("ORDERID");
        }
        rs.close();
        stmt.close();
        return orderID;
    }


    private static void checkStock(Connection conn, int productID) throws SQLException {
        String stockCheckQuery = "SELECT stock FROM product WHERE product_id = ?";
        PreparedStatement stockCheckStmt = conn.prepareStatement(stockCheckQuery);
        stockCheckStmt.setInt(1, productID);
        ResultSet stockRs = stockCheckStmt.executeQuery();

        if (stockRs.next()) {
            int remainingStock = stockRs.getInt("stock");

            if (remainingStock <= 3 && remainingStock > 0) {
                System.out.println(" Produkten har lågt lager: " + remainingStock + " kvar.");
            } else if (remainingStock == 0) {
                System.out.println(" VARNING: Produkten är nu slut i lager!");
            }
        }

        stockRs.close();
        stockCheckStmt.close();
    }


    public static void markOrderAsPaid(Connection conn, int customerID) {
        try {

            Integer orderID = getActiveOrderID(conn, customerID);

            if (orderID == null) {
                System.out.println("Du har ingen aktiv order att betala.");
                return;
            }


            String callSP = "{CALL MarkOrderAsPaid(?)}";
            CallableStatement stmtSP = conn.prepareCall(callSP);
            stmtSP.setInt(1, orderID);


            ResultSet rs = stmtSP.executeQuery();
            if (rs.next()) {
                System.out.println("\n " + rs.getString("Message"));
            }

            stmtSP.close();
            rs.close();
        } catch (SQLException e) {
            System.out.println(" Fel vid betalning av order: " + e.getMessage());
        }
    }
}

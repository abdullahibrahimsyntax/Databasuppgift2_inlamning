import java.sql.*;
import java.util.Scanner;


public class DBconnection {
    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/sos";
        String user = "root";
        String password = "Hejsan123";

        Connection conn = null;

        try {

            conn = DriverManager.getConnection(url, user, password);
            System.out.println(" Anslutning lyckades!");


            Scanner scanner = new Scanner(System.in);


            System.out.print("\nAnge ditt kund-ID: ");
            int customerID = scanner.nextInt();
            scanner.nextLine();

            System.out.print("Ange ditt lösenord: ");
            String userPassword = scanner.nextLine();


            if (authenticateUser(conn, customerID, userPassword)) {
                System.out.println(" Inloggning lyckades!\n");


                boolean running = true;
                while (running) {
                    System.out.println("\n Välj ett alternativ:");
                    System.out.println("1. Lägg till en produkt i din beställning");
                    System.out.println("2. Betala och slutför din beställning");
                    System.out.println("3. Avsluta");

                    System.out.print("\nDitt val: ");
                    int choice = scanner.nextInt();
                    scanner.nextLine();

                    switch (choice) {
                        case 1:

                            OrderManager.addToCart(conn, customerID);
                            break;
                        case 2:

                            OrderManager.markOrderAsPaid(conn, customerID);
                            break;
                        case 3:

                            running = false;
                            System.out.println("Tack för att du handlar hos oss!");
                            break;
                        default:
                            System.out.println("Ogiltigt val, försök igen.");
                    }
                }
            } else {
                System.out.println("Felaktigt kund-ID eller lösenord!");
            }


            conn.close();

        } catch (SQLException ex) {
            System.out.println(" Databasfel: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    public static boolean authenticateUser(Connection conn, int customerID, String password) throws SQLException {
        String query = "SELECT * FROM CUSTOMER WHERE CustomerID = ? AND password = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, customerID);
        stmt.setString(2, password);
        ResultSet rs = stmt.executeQuery();

        boolean isAuthenticated = rs.next();
        rs.close();
        stmt.close();
        return isAuthenticated;
    }
}


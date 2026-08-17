import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.List;
import java.util.ArrayList;

public class GameRental {
   private Connection _connection = null;
   static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

   public GameRental(String dbname, String dbport, String user, String passwd) throws SQLException {
      try {
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         this._connection = DriverManager.getConnection(url, user, passwd);
      } catch (Exception e) {
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
         System.exit(-1);
      }
   }

   public void executeUpdate(String sql) throws SQLException {
      Statement stmt = this._connection.createStatement();
      stmt.executeUpdate(sql);
      stmt.close();
   }

   public int executeQueryAndPrintResult(String query) throws SQLException {
      Statement stmt = this._connection.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      ResultSetMetaData rsmd = rs.getMetaData();
      int numCol = rsmd.getColumnCount();
      int rowCount = 0;
      boolean outputHeader = true;
      while (rs.next()) {
         if (outputHeader) {
            for (int i = 1; i <= numCol; i++) {
               System.out.print(rsmd.getColumnName(i) + "\t");
            }
            System.out.println();
            outputHeader = false;
         }
         for (int i = 1; i <= numCol; ++i)
            System.out.print(rs.getString(i) + "\t");
         System.out.println();
         ++rowCount;
      }
      stmt.close();
      return rowCount;
   }

   public List<List<String>> executeQueryAndReturnResult(String query) throws SQLException {
      Statement stmt = this._connection.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      ResultSetMetaData rsmd = rs.getMetaData();
      int numCol = rsmd.getColumnCount();
      List<List<String>> result = new ArrayList<>();
      while (rs.next()) {
         List<String> record = new ArrayList<>();
         for (int i = 1; i <= numCol; ++i)
            record.add(rs.getString(i));
         result.add(record);
      }
      stmt.close();
      return result;
   }

   public int executeQuery(String query) throws SQLException {
      Statement stmt = this._connection.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      int rowCount = 0;
      while (rs.next()) {
         rowCount++;
      }
      stmt.close();
      return rowCount;
   }

   public int getCurrSeqVal(String sequence) throws SQLException {
      Statement stmt = this._connection.createStatement();
      ResultSet rs = stmt.executeQuery(String.format("Select currval('%s')", sequence));
      if (rs.next())
         return rs.getInt(1);
      return -1;
   }

   public void cleanup() {
      try {
         if (this._connection != null) {
            this._connection.close();
         }
      } catch (SQLException e) {
      }
   }

   public static void main(String[] args) {
      if (args.length != 3) {
         System.err.println("Usage: java [-classpath <classpath>] " + GameRental.class.getName() + " <dbname> <port> <user>");
         return;
      }

      Greeting();
      GameRental esql = null;
      try {
         Class.forName("org.postgresql.Driver").newInstance();
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new GameRental(dbname, dbport, user, "");

         boolean keepon = true;
         while (keepon) {
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()) {
               case 1:
                  CreateUser(esql);
                  break;
               case 2:
                  authorisedUser = LogIn(esql);
                  break;
               case 9:
                  keepon = false;
                  break;
               default:
                  System.out.println("Unrecognized choice!");
                  break;
            }
            if (authorisedUser != null) {
               boolean usermenu = true;
               while (usermenu) {
                  System.out.println("MAIN MENU");
                  System.out.println("---------");
                  System.out.println("1. View Profile");
                  System.out.println("2. Update Profile");
                  System.out.println("3. View Catalog");
                  System.out.println("4. Place Rental Order");
                  System.out.println("5. View Full Rental Order History");
                  System.out.println("6. View Past 5 Rental Orders");
                  System.out.println("7. View Rental Order Information");
                  System.out.println("8. View Tracking Information");
                  System.out.println("9. Update Tracking Information");
                  System.out.println("10. Update Catalog");
                  System.out.println("11. Update User");
                  System.out.println("20. Log out");
                  switch (readChoice()) {
                     case 1:
                        viewProfile(esql, authorisedUser);
                        break;
                     case 2:
                        updateProfile(esql, authorisedUser);
                        break;
                     case 3:
                        viewCatalog(esql);
                        break;
                     case 4:
                        placeOrder(esql, authorisedUser);
                        break;
                     case 5:
                        viewAllOrders(esql, authorisedUser);
                        break;
                     case 6:
                        viewRecentOrders(esql, authorisedUser);
                        break;
                     case 7:
                        viewOrderInfo(esql, authorisedUser);
                        break;
                     case 8:
                        viewTrackingInfo(esql, authorisedUser);
                        break;
                     case 9:
                        updateTrackingInfo(esql, authorisedUser);
                        break;
                     case 10:
                        updateCatalog(esql);
                        break;
                     case 11:
                        updateUser(esql);
                        break;
                     case 20:
                        usermenu = false;
                        break;
                     default:
                        System.out.println("Unrecognized choice!");
                        break;
                  }
               }
            }
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      } finally {
         try {
            if (esql != null) {
               esql.cleanup();
            }
         } catch (Exception e) {
         }
      }
   }

   public static void Greeting() {
      System.out.println("\n\n*******************************************************\n" +
                         "              User Interface                           \n" +
                         "*******************************************************\n");
   }

   public static int readChoice() {
      int input;
      do {
         System.out.print("Please make your choice: ");
         try {
            input = Integer.parseInt(in.readLine());
            break;
         } catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }
      } while (true);
      return input;
   }

   public static void CreateUser(GameRental esql) {
      try {
         System.out.print("Enter Username: ");
         String username = in.readLine();
         System.out.print("Enter Password: ");
         String password = in.readLine();
         System.out.print("Enter Role (customer/employee/manager): ");
         String role = in.readLine();
         System.out.print("Enter Phone Number: ");
         String phoneNum = in.readLine();

         String favGames = "";
         int numOverDueGames = 0;

         String query = String.format("INSERT INTO Users (login, password, role, favGames, phoneNum, numOverDueGames) VALUES ('%s', '%s', '%s', '%s', '%s', %d);",
                                      username, password, role, favGames, phoneNum, numOverDueGames);
         esql.executeUpdate(query);
         System.out.println("User successfully created!");
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static String LogIn(GameRental esql) {
      try {
         System.out.print("Enter Username: ");
         String username = in.readLine();
         System.out.print("Enter Password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM Users WHERE login='%s' AND password='%s';", username, password);
         int userNum = esql.executeQuery(query);
         if (userNum > 0) {
            System.out.println("Login successful!");
            return username;
         } else {
            System.out.println("Invalid username or password.");
            return null;
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
         return null;
      }
   }

   public static void viewProfile(GameRental esql, String authorisedUser) {
      try {
         String query = String.format("SELECT * FROM Users WHERE login='%s';", authorisedUser);
         esql.executeQueryAndPrintResult(query);
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void updateProfile(GameRental esql, String authorisedUser) {
      try {
         System.out.print("Enter new Phone Number: ");
         String phoneNum = in.readLine();

         String query = String.format("UPDATE Users SET phoneNum='%s' WHERE login='%s';", phoneNum, authorisedUser);
         esql.executeUpdate(query);
         System.out.println("Profile successfully updated!");
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void viewCatalog(GameRental esql) {
      try {
	 System.out.print("Enter genre for games you would like to see: ");
         String genre = in.readLine();
         
	 System.out.print("Enter price for games you would like to see: ");
         Double price = Double.parseDouble(in.readLine());;

	 System.out.print("Order by 1.Ascending or 2.Descending order: ");
	 int order = Integer.parseInt(in.readLine());
         if(order == 1) {
         String query = String.format("SELECT * FROM Catalog WHERE genre = '%s'", 
	 genre, "AND price <= '%s'", price, "ORDER BY ASC");
         esql.executeQueryAndPrintResult(query);
	 } else if(order == 2){
	String query = String.format("SELECT * FROM Catalog WHERE genre = '%s'", 
         genre, "AND price <= '%s'", price, "ORDER BY ASC");
         esql.executeQueryAndPrintResult(query);
	 } else{ System.out.print("You didn't enter a correct choice"); }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void placeOrder(GameRental esql, String authorisedUser) {
     try {
          System.out.print("Enter Game ID: ");
          String gameID = in.readLine();
          System.out.print("Enter Rental Period (days): ");
          int period = Integer.parseInt(in.readLine());
          String gameQuery = String.format("SELECT price FROM Catalog WHERE gameID='%s';", gameID);
          List<List<String>> gameResult = esql.executeQueryAndReturnResult(gameQuery);
          if (gameResult.isEmpty()) {
              System.out.println("Game not found.");
              return;
          }
          double price = Double.parseDouble(gameResult.get(0).get(0));

          // Fetch the current maximum rentalOrderID and ensure it is treated as an integer for incrementation
          String maxOrderIDQuery = "SELECT COALESCE(MAX(CAST(rentalOrderID FROM 'gamerentalorder(\\d+)')AS INTEGER)),0) FROM RentalOrder;";
          List<List<String>> maxOrderIDResult = esql.executeQueryAndReturnResult(maxOrderIDQuery);
          int maxOrderID = Integer.parseInt(maxOrderIDResult.get(0).get(0));

          // Generate a new numeric rentalOrderID by incrementing the maxOrderID
         // int newOrderIDNumeric = maxOrderID + 1;
         //  String newOrderID = String.valueOf(newOrderIDNumeric);
	String newOrderID = "gamerentalorder" + (maxOrderID + 1);
          double totalPrice = price * period;
          String insertOrderQuery = String.format(
              "INSERT INTO RentalOrder (rentalOrderID, login, noOfGames, totalPrice, orderTimestamp, dueDate) " +
              "VALUES ('%s', '%s', %d, %.2f, NOW(), NOW() + INTERVAL '%d days');",
              newOrderID, authorisedUser, 1, totalPrice, period
          );
esql.executeUpdate(insertOrderQuery);

          String insertGameOrderQuery = String.format(
              "INSERT INTO GamesInOrder (rentalOrderID, gameID, unitsOrdered) VALUES ('%s', '%s', %d);",
              newOrderID, gameID, 1
          );
          esql.executeUpdate(insertGameOrderQuery);

          System.out.println("Order successfully placed!");
      }  catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void viewAllOrders(GameRental esql, String authorisedUser) {
      try {
         String query = String.format("SELECT * FROM RentalOrder WHERE login='%s';", authorisedUser);
         esql.executeQueryAndPrintResult(query);
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void viewRecentOrders(GameRental esql, String authorisedUser) {
      try {
         String query = String.format("SELECT * FROM RentalOrder WHERE login='%s' ORDER BY orderTimestamp DESC LIMIT 5;", authorisedUser);
         esql.executeQueryAndPrintResult(query);
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void viewOrderInfo(GameRental esql, String authorisedUser) {
      try {
         System.out.print("Enter Rental Order ID: ");
         String orderID = in.readLine();

         String query = String.format("SELECT * FROM RentalOrder WHERE rentalOrderID='%s' AND login='%s';", orderID, authorisedUser);
         esql.executeQueryAndPrintResult(query);
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void viewTrackingInfo(GameRental esql, String authorisedUser) {
      try {
          System.out.print("Enter Tracking ID: ");
         String trackingID = in.readLine();

         String query = String.format("SELECT T.trackingID, T.rentalOrderID, T.status, T.courierName, T.lastUpdateDate, T.additionalComments FROM TrackingInfo T, Users U, RentalOrder R WHERE R.rentalOrderID = T.rentalOrderID AND R.login = U.login AND U.login = '%s' AND T.trackingID='%s';", authorisedUser, trackingID);
         esql.executeQueryAndPrintResult(query);
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void updateTrackingInfo(GameRental esql, String authorisedUser) {
       try {
         String userQuery = String.format("SELECT role FROM users WHERE login ='%s'", authorisedUser);
         List<List<String>> result = esql.executeQueryAndReturnResult(userQuery);
         String type = result.get(0).get(0);
         String employee = "employee";
         String manager = "manager";
         if(type.equals(employee) || type.equals(manager)){
         System.out.print("Enter Tracking ID: ");
         String trackingID = in.readLine();
         System.out.print("Enter new current location: ");
         String currLoc = in.readLine();
         System.out.print("Enter new courier name: ");
         String courierN = in.readLine();
         System.out.print("Enter new additional comments: ");
         String addCom = in.readLine();
         String query = String.format("UPDATE TrackingInfo SET currentLocation ='%s', courierName = '%s', additionalComments = '%s' WHERE trackingID= '%s';", currLoc, courierN, addCom, trackingID);
         esql.executeUpdate(query);
         System.out.println("Tracking information successfully updated!");
         }else{
            System.out.print("Not an employee or manager.");

         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void updateCatalog(GameRental esql) {
      try {
         System.out.print("Enter Game ID: ");
         String gameID = in.readLine();
         System.out.print("Enter new Price: ");
         double newPrice = Double.parseDouble(in.readLine());

         String query = String.format("UPDATE Catalog SET price=%.2f WHERE gameID='%s';", newPrice, gameID);
         esql.executeUpdate(query);
         System.out.println("Catalog successfully updated!");
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void updateUser(GameRental esql) {
      try {
         System.out.print("Enter Username: ");
         String username = in.readLine();
         System.out.print("Enter new Role (customer/employee/manager): ");
         String newRole = in.readLine();

         String query = String.format("UPDATE Users SET role='%s' WHERE login='%s';", newRole, username);
         esql.executeUpdate(query);
         System.out.println("User role successfully updated!");
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }
}

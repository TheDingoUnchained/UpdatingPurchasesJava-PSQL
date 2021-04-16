import java.net.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.DateFormatter;

import java.time.LocalDate;

import pgpass.*;

public class AddPurchase {
	private Connection conDB;
	private String url;
	
	private Integer custID;
	private String custName;
	private String clubName;
	private String bookTitle;
	private Integer bookYear;
	private LocalDate whenP = LocalDate.now();
	private String purchaseDate;
	private Integer qnty = 1;
	private String user = "nrkjak";
	
	public static void main(String[] args) {
		AddPurchase ap = new AddPurchase(args);

	}
	
		public AddPurchase(String[] args) {
			if (args.length <= 7) {
				System.out.println("\nUsage: java AddPurchase -c <cid> -b <club> -t <title> -y <year> [-w <when> ] [-q <qnty> ] [-u <user> ]");
				System.exit(0);
			
			}	else if(args.length >= 8){
				int i;
				for(i = 0; i < args.length;i++) {
					if("-c".contentEquals(args[i])) {
						custID = Integer.parseInt(args[i+1]);
						i = i+1;
						
					}
					if ("-b".equals(args[i])) {
						clubName = args[i+1];
						i = i+1;
					}
					if ("-t".equals(args[i])) {
						bookTitle = args[i+1];
						i = i+1;
						
					} 
					if ("-y".equals(args[i])) {
						bookYear = Integer.parseInt(args[i+1]);
						i = i+1;
						
					}
					if ("-w".equals(args[i])) {
						purchaseDate = args[i+1];
						i = i+1;
						
					}
					if("-q".equals(args[i])) {
						qnty = Integer.parseInt(args[i+1]);
						i = i+1;
						
					}
					if("-u".equals(args[i])) {
						user = args[i+1];
						i = i+1;
					}
				}
			
		try {
			             // Register the driver with DriverManager.
			             
				Class.forName("org.postgresql.Driver").newInstance();
			         } catch (ClassNotFoundException e) {
			        	 e.printStackTrace();
			             System.exit(0);
			         } catch (InstantiationException e) {
			             e.printStackTrace();
			             System.exit(0);
			         } catch (IllegalAccessException e) {
			             e.printStackTrace();
			             System.exit(0);
			         }
					
					url = "jdbc:postgresql://db:5432/";
					
					 Properties props = new Properties();
				      try {
				            String passwd = PgPass.get("db", "5432", user, user);
				            props.setProperty("user",    user);
				            props.setProperty("password", passwd);
				           
				        } catch(PgPassException e) {
				            System.out.print("\nCould not obtain PASSWD from <.pgpass>.\n");
				            System.out.println(e.toString());
				            System.exit(0);
				        }
					
					
					try {
						//String passwd = PgPass.get("db", "*", user, user);
						conDB = DriverManager.getConnection(url, props);
					}catch(SQLException e) {
						System.out.print("\nSQL: database connection error.\n");
						System.out.println(e.toString());
						System.exit(0);	
					}
					
					try {
						conDB.setAutoCommit(false);
					} catch(SQLException e) {
						System.out.print("\nFailed trying to turn autocommit off.\n");
						e.printStackTrace();
						System.exit(0);
					} 
					
					
							
						//custID= new Integer(args[1]);
						//clubName = new String(args[1]);
						//bookTitle = new String(args[2]);
						//bookYear = new String(args[3]);
						
						//Pattern p = Pattern.compile("2021-[0-1][0-9]-[0-3][0-9]");
						//Matcher m = p.matcher(args[5]);
						//boolean b = m.matches();
						
						
						
					}	
					
					if(!custcheck()) {
						System.out.println("Customer with ID " + custID + " does not exist in the database.");
						System.exit(0);
					}
					if(!clubcheck()) {
						System.out.println("Club " + clubName + " does not exist in the database.");
						System.exit(0);
					}
					if(!bookcheck() || !yearcheck()) {
						System.out.println("Book does not exist in the database.");
						System.exit(0);
					}
					if(!custclubcheck()) {
						System.out.println("Customer with cid " + custID + " does not exist in club " + clubName + " .");
						System.exit(0);
					}
					if(!clubbookcheck()) {
						System.out.println("Club "+ clubName + " does not offer the book " + bookTitle + " .");
						System.exit(0);
					}
					String local = whenP.toString();
					if(!(purchaseDate.equals(local))) {
						System.out.println("The purchase was not made today.");
						System.out.println(local);
						System.exit(0);
					}
					if(qnty < 0) {
						System.out.println("Quantity value cannot be less than 0.");
						System.exit(0);
					}
					
					String date = whenP.toString();
					Date sqlDate = Date.valueOf(date);
					try {	
					String query = "INSERT into yrb_purchase (cid, club, title, year, whenp, qnty) values ( ?, ?, ?, ?, ?, ?)";
					PreparedStatement prep = conDB.prepareStatement(query);
					prep.setInt(1, custID);
					prep.setString(2, clubName);
					prep.setString(3, bookTitle);
					prep.setInt(4, bookYear);
					prep.setDate(5, sqlDate);
					prep.setInt(6, qnty);
					
					prep.execute();
					conDB.commit();
					conDB.close();
					}catch(SQLException e) {
						System.out.println("Failed to add purchase to database");
						System.out.println(e.toString());
						System.exit(0);
						
					}
					
					}
	
	
					
	
	
	
	public boolean custcheck() {
		String 				queryText = "";
		PreparedStatement 	querySt = null;
		ResultSet			answers = null;
		
		boolean inDB = false;
		
		queryText =
				"SELECT cid "
			+	"FROM yrb_customer "
			+	"WHERE cid = ? ";	 		
		try {
			querySt = conDB.prepareStatement(queryText);
		} catch(SQLException e) {
			System.out.println("SQL#1 failed in prepare");
			System.out.println(e.toString());
			System.exit(0);
			
		}
		
		try {
			querySt.setInt(1, custID);
			answers = querySt.executeQuery();
		}catch(SQLException e) {
			System.out.println("SQL#1 failed in execute");
			System.out.println(e.toString());
			System.exit(0);
		}
		
		try {
			if(answers.next()) {
				inDB = true;
				custID = answers.getInt("cid");
			} else {
				inDB = false;
				
			}
			
		} catch(SQLException e) {
			System.out.println("SQL$1 failed in cursor.");
			System.out.println(e.toString());
			System.exit(0);
		}
		try {
			answers.close();
		}catch (SQLException e) {
			System.out.print("SQL#1 failed closing curser.\n");
			System.out.println(e.toString());
			System.exit(0);
		}
		
		try {
			querySt.close();
		}catch(SQLException e) {
			System.out.print("SQL#1 failed closing the handle.\n");
			System.out.println(e.toString());
			System.exit(0);
		}
		return inDB;
		
	}
	public boolean clubcheck() {
		String 				queryText = "";
		PreparedStatement 	querySt = null;
		ResultSet			answers = null;
		
		boolean inDB = false;
		
		queryText =
				"SELECT club      "
			+	"FROM yrb_club "
			+	"WHERE club = ?    ";	 		
		try {
			querySt = conDB.prepareStatement(queryText);
		} catch(SQLException e) {
			System.out.println("SQL#1 failed in prepare");
			System.out.println(e.toString());
			System.exit(0);
			
		}
		
		try {
			querySt.setString(1, clubName.toString());
			answers = querySt.executeQuery();
		}catch(SQLException e) {
			System.out.println("SQL#1 failed in execute");
			System.out.println(e.toString());
			System.exit(0);
		}
		
		try {
			if(answers.next()) {
				inDB = true;
				clubName = answers.getString("club");
			} else {
				inDB = false;
				
			}
			
		} catch(SQLException e) {
			System.out.println("SQL$1 failed in cursor.");
			System.out.println(e.toString());
			System.exit(0);
		}
		try {
			answers.close();
		}catch (SQLException e) {
			System.out.print("SQL#1 failed closing curser.\n");
			System.out.println(e.toString());
			System.exit(0);
		}
		
		try {
			querySt.close();
		}catch(SQLException e) {
			System.out.print("SQL#1 failed closing the handle.\n");
			System.out.println(e.toString());
			System.exit(0);
		}
		return inDB;
		
	}
	
	public boolean bookcheck() {
		String 				queryText = "";
		PreparedStatement 	querySt = null;
		ResultSet			answers = null;
		
		boolean inDB = false;
		
		queryText =
				"SELECT title     "
			+	"FROM yrb_book "
			+	"WHERE title = ?    ";	 		
		try {
			querySt = conDB.prepareStatement(queryText);
		} catch(SQLException e) {
			System.out.println("SQL#1 failed in prepare");
			System.out.println(e.toString());
			System.exit(0);
			
		}
		
		try {
			querySt.setString(1, bookTitle.toString());
			answers = querySt.executeQuery();
		}catch(SQLException e) {
			System.out.println("SQL#1 failed in execute");
			System.out.println(e.toString());
			System.exit(0);
		}
		
		try {
			if(answers.next()) {
				inDB = true;
				bookTitle = answers.getString("title");
			} else {
				inDB = false;
			
			}
			
		} catch(SQLException e) {
			System.out.println("SQL$1 failed in cursor.");
			System.out.println(e.toString());
			System.exit(0);
		}
		try {
			answers.close();
		}catch (SQLException e) {
			System.out.print("SQL#1 failed closing curser.\n");
			System.out.println(e.toString());
			System.exit(0);
		}
		
		try {
			querySt.close();
		}catch(SQLException e) {
			System.out.print("SQL#1 failed closing the handle.\n");
			System.out.println(e.toString());
			System.exit(0);
		}
		return inDB;
		
	}
	
	public boolean yearcheck() {
		String 				queryText = "";
		PreparedStatement 	querySt = null;
		ResultSet			answers = null;
		
		boolean inDB = false;
		
		queryText =
				"SELECT title     "
			+	"FROM yrb_book "
			+	"WHERE year = ?  AND title = ?  ";	 		
		try {
			querySt = conDB.prepareStatement(queryText);
		} catch(SQLException e) {
			System.out.println("SQL#1 failed in prepare");
			System.out.println(e.toString());
			System.exit(0);
			
		}
		
		try {
			querySt.setInt(1, bookYear);
			querySt.setString(2, bookTitle);
			answers = querySt.executeQuery();
		}catch(SQLException e) {
			System.out.println("SQL#1 failed in execute");
			System.out.println(e.toString());
			System.exit(0);
		}
		
		try {
			if(answers.next()) {
				inDB = true;
				
			} else {
				inDB = false;
				
			}
			
		} catch(SQLException e) {
			System.out.println("SQL$1 failed in cursor.");
			System.out.println(e.toString());
			System.exit(0);
		}
		try {
			answers.close();
		}catch (SQLException e) {
			System.out.print("SQL#1 failed closing curser.\n");
			System.out.println(e.toString());
			System.exit(0);
		}
		
		try {
			querySt.close();
		}catch(SQLException e) {
			System.out.print("SQL#1 failed closing the handle.\n");
			System.out.println(e.toString());
			System.exit(0);
		}
		return inDB;
		
	}
	
	public boolean custclubcheck() {
		String 				queryText = "";
		PreparedStatement 	querySt = null;
		ResultSet			answers = null;
		
		boolean inDB = false;
		
		queryText =
				"SELECT club      "
			+	"FROM yrb_member "
			+	"WHERE cid = ?    ";	 		
		try {
			querySt = conDB.prepareStatement(queryText);
		} catch(SQLException e) {
			System.out.println("SQL#1 failed in prepare");
			System.out.println(e.toString());
			System.exit(0);
			
		}
		
		try {
			querySt.setInt(1, custID.intValue());
			answers = querySt.executeQuery();
		}catch(SQLException e) {
			System.out.println("SQL#1 failed in execute");
			System.out.println(e.toString());
			System.exit(0);
		}
		
		try {
			if(answers.next()) {
				inDB = true;
				clubName = answers.getString("club");
			} else {
				inDB = false;
				
			}
			
		} catch(SQLException e) {
			System.out.println("SQL$1 failed in cursor.");
			System.out.println(e.toString());
			System.exit(0);
		}
		try {
			answers.close();
		}catch (SQLException e) {
			System.out.print("SQL#1 failed closing curser.\n");
			System.out.println(e.toString());
			System.exit(0);
		}
		
		try {
			querySt.close();
		}catch(SQLException e) {
			System.out.print("SQL#1 failed closing the handle.\n");
			System.out.println(e.toString());
			System.exit(0);
		}
		return inDB;
		
	}
	
	public boolean clubbookcheck(){
		String 				queryText = "";
		PreparedStatement 	querySt = null;
		ResultSet			answers = null;
		
		boolean inDB = false;
		
		queryText =
				"SELECT club      "
			+	"FROM yrb_offer "
			+	"WHERE title = ? AND club = ?   ";	 		
		try {
			querySt = conDB.prepareStatement(queryText);
		} catch(SQLException e) {
			System.out.println("SQL#1 failed in prepare");
			System.out.println(e.toString());
			System.exit(0);
			
		}
		
		try {
			querySt.setString(1, bookTitle.toString());
			querySt.setString(2, clubName.toString());
			answers = querySt.executeQuery();
		}catch(SQLException e) {
			System.out.println("SQL#1 failed in execute");
			System.out.println(e.toString());
			System.exit(0);
		}
		
		try {
			if(answers.next()) {
				inDB = true;
				clubName = answers.getString("club");
			} else {
				inDB = false;
				
			}
			
		} catch(SQLException e) {
			System.out.println("SQL$1 failed in cursor.");
			System.out.println(e.toString());
			System.exit(0);
		}
		try {
			answers.close();
		}catch (SQLException e) {
			System.out.print("SQL#1 failed closing curser.\n");
			System.out.println(e.toString());
			System.exit(0);
		}
		
		try {
			querySt.close();
		}catch(SQLException e) {
			System.out.print("SQL#1 failed closing the handle.\n");
			System.out.println(e.toString());
			System.exit(0);
		}
		return inDB;
		
	}
	
	
	
}
	
	






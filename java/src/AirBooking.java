/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class AirBooking{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

	public AirBooking(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");

			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}

	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 *
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException {
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 *
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;

		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 *
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 * obtains the metadata object for the returned result set.  The metadata
		 * contains row and column info.
		*/
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;

		//iterates through the result set and saves the data returned by the query.
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>();
		while (rs.next()){
			List<String> record = new ArrayList<String>();
			for (int i=1; i<=numCol; ++i)
				record.add(rs.getString (i));
			result.add(record);
		}//end while
		stmt.close ();
		return result;
	}//end executeQueryAndReturnResult

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 *
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}

	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current
	 * value of sequence used for autogenerated keys
	 *
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */

	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();

		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 *
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + AirBooking.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if

		AirBooking esql = null;

		try{

			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}

			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];

			esql = new AirBooking (dbname, dbport, user, "");

			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Passenger");
				System.out.println("2. Book Flight");
				System.out.println("3. Review Flight");
				System.out.println("4. Insert or Update Flight");
				System.out.println("5. List Flights From Origin to Destination");
				System.out.println("6. List Most Popular Destinations");
				System.out.println("7. List Highest Rated Destinations");
				System.out.println("8. List Flights to Destination in order of Duration");
				System.out.println("9. Find Number of Available Seats on a given Flight");
				System.out.println("10. < EXIT");

				switch (readChoice()){
					case 1: AddPassenger(esql); break;
					case 2: BookFlight(esql); break;
					case 3: TakeCustomerReview(esql); break;
					case 4: InsertOrUpdateRouteForAirline(esql); break;
					case 5: ListAvailableFlightsBetweenOriginAndDestination(esql); break;
					case 6: ListMostPopularDestinations(esql); break;
					case 7: ListHighestRatedRoutes(esql); break;
					case 8: ListFlightFromOriginToDestinationInOrderOfDuration(esql); break;
					case 9: FindNumberOfAvailableSeatsForFlight(esql); break;
					case 10: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static void AddPassenger(AirBooking esql){//1
		//Add a new passenger to the database
		try{
			String query = "INSERT INTO Passenger VALUES(";
			System.out.print("Enter ID: ");
			String input = "";
			Integer repeatFlag = 1;

			// Insert pID
			do { //performs check to make sure user entered something
				input = in.readLine();
				repeatFlag = 1;
				String uniqueChecker = "SELECT * FROM Passenger WHERE pID = ";
				uniqueChecker += input + " LIMIT 1;";
				if (input == null || input.isEmpty()) {
					System.out.println("Please enter an ID\n");
					repeatFlag = 0;
				}
				// checks to make sure the number is unique
				else if (esql.executeQuery(uniqueChecker) == 1) {
					System.out.println("This ID already exists, try again\n");
					repeatFlag = 0;
				}
				//TODO: implement functionality to insert into free ID spot
			} while(repeatFlag == 0);
			query += input + ", ";

			// Insert Passport
			System.out.print("Enter Passport Number: "); // make sure it's unique
			do { //performs check to make sure user entered something
				input = in.readLine();
				repeatFlag = 1;
				if (input == null || input.isEmpty()) {
					System.out.println("Please enter an Passport Number\n");
					repeatFlag = 0;
				}
				else if (input.length() != 10) {
					System.out.println("Please enter a 10 digit Passport Number\n");
					repeatFlag = 0;
				}
				boolean allLetters = input.chars().allMatch(Character::isLetter);
				if (allLetters == false) {
					System.out.println("Please enter only characters\n");
					repeatFlag = 0;
				}
				String uniqueChecker = "SELECT * FROM Passenger WHERE passNum = '";
				uniqueChecker += input + "' LIMIT 1;";
				if (esql.executeQuery(uniqueChecker) == 1) {
					System.out.println("This Passport Number already exists\n");
					repeatFlag = 0;
				}
			} while(repeatFlag == 0);
			input = input.toUpperCase();
			query += input + ", ";

			//Input Name
			String name = "";
			System.out.print("Enter your first name: ");
			do { //performs check to make sure user entered something
				input = in.readLine();
				repeatFlag = 1;
				if (input == null || input.isEmpty()) {
					System.out.println("Please enter a first name\n");
					repeatFlag = 0;
				}
				boolean allLetters = input.chars().allMatch(Character::isLetter);
				if (allLetters == false) {
					System.out.println("Please enter only characters\n");
					repeatFlag = 0;
				}
			} while(repeatFlag == 0);
			input = input.substring(0, 1).toUpperCase() + input.substring(1); // capitalizes first letter if user didnt
			name = input;

			System.out.print("Enter your last name: ");
			do { //performs check to make sure user entered something
				input = in.readLine();
				repeatFlag = 1;
				if (input == null || input.isEmpty()) {
					System.out.println("Please enter a last name\n");
					repeatFlag = 0;
				}
				boolean allLetters = input.chars().allMatch(Character::isLetter);
				if (allLetters == false) {
					System.out.println("Please enter only characters\n");
					repeatFlag = 0;
				}
			} while(repeatFlag == 0);
			input = input.substring(0, 1).toUpperCase() + input.substring(1);
			name += " " + input;
			query += name + ", ";

			if (input.length() >= 24) {
				System.out.println("Please enter a name under 24 characters\n");
				repeatFlag = 0;
			}

			// Input Birthday
			Integer inputMonth = 0;
			Integer inputDay = 0;
			Integer inputYear = 0;
			do { //performs check to make sure user entered something
				repeatFlag = 1;
				System.out.print("Enter birth month in numbers (ex. 1 is January): ");
				inputMonth = Integer.parseInt(in.readLine());
				if (inputMonth < 1 || inputMonth > 12) {
					System.out.print("Please enter a valid month ");
					repeatFlag = 0;
				}
				System.out.print("Enter birth day in numbers (ex. 15): ");
				inputDay = Integer.parseInt(in.readLine());
				if (inputDay < 1 || inputDay > 31) {
					System.out.print("Please enter a valid day ");
					repeatFlag = 0;
				}
				System.out.print("Enter birth year greater than 1900: ");
				inputYear = Integer.parseInt(in.readLine());
				if (inputYear < 1900) {
					System.out.print("Please enter a valid year ");
					repeatFlag = 0;
				}
			} while(repeatFlag == 0);
			input = inputMonth + "/" + inputDay + "/" + inputYear;
			query += input + ", ";

			//Insert Country
			System.out.print("Enter Country: ");
			do { //performs check to make sure user entered something
				input = in.readLine();
				repeatFlag = 1;
				if (input == null || input.isEmpty()) {
					System.out.println("Please enter a Country\n");
					repeatFlag = 0;
				}
				if (input.length() >= 24) {
					System.out.println("Please enter a Country under 24 characters\n");
					repeatFlag = 0;
				}
				boolean allLetters = input.chars().allMatch(Character::isLetter);
				if (allLetters == false) {
					System.out.println("Please enter only characters\n");
					repeatFlag = 0;
				}
			} while(repeatFlag == 0);
			query += input;

			query += ")";
			System.out.println(query);
			// esql.executeQuery(query);
		}catch(Exception e){
			 System.err.println (e.getMessage());
		}
	}

	public static String getBookingID() { // Creates random booking ID
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
				String SALTNUMS = "1234567890";
        StringBuilder salt = new StringBuilder();
				StringBuilder salt2 = new StringBuilder();
				StringBuilder salt3 = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 5) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
				while (salt2.length() < 4) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTNUMS.length());
            salt2.append(SALTNUMS.charAt(index));
        }
				while (salt3.length() < 1) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt3.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
				String saltStr2 = salt2.toString();
				String saltStr3 = salt3.toString();
				String masterString = saltStr + saltStr2 + saltStr3;
        return masterString;
    }

	public static void BookFlight(AirBooking esql){//2
		//Book Flight for an existing customer
		try{
			//Select the Flight Number
			//TODO: add functionality to check if valid cities
			//TODO: do a select limit 1 and see if it exists
			Integer repeatFlag = 1;
			String originInput = "";
			String destInput = "";
			String input = "";
			List<List<String>> flightNum;
			List<List<String>> passNum;
			String query = "";
			do {
				query = "SELECT flightNum FROM Flight WHERE origin = '";
				repeatFlag = 1;
				System.out.println("Where are you traveling from?");
				originInput = in.readLine();
				query += originInput + "' AND destination = '";
				System.out.println("Where would you like to travel to?");
				destInput = in.readLine();
				query += destInput + "';";
				flightNum = esql.executeQueryAndReturnResult(query);
				if (flightNum.isEmpty()) {
					System.out.println("There is no flight from " +
					originInput + " to " + destInput);
					System.out.println("Please enter a valid flight path.\n");
					repeatFlag = 0;
				}
			} while (repeatFlag == 0);

			//Select the passenger ID
			do {
				repeatFlag = 1;
				query = "SELECT pID FROM Passenger WHERE passNum = '";
				System.out.println("Please enter your Passport Number");
				input = in.readLine();
				query += input + "';";
				passNum = esql.executeQueryAndReturnResult(query);
				if (passNum.isEmpty()) {
					System.out.println("There is no matching Passport Number.");
					repeatFlag = 0;
				}
			} while (repeatFlag == 0);

			//Choose the departure date
			System.out.println("Please enter the date you wish to leave");
			Integer inputMonth = 0;
			Integer inputDay = 0;
			Integer inputYear = 0;
			do { //performs check to make sure user entered something
				repeatFlag = 1;
				System.out.print("Enter departure month in numbers (ex. 1 is January): ");
				inputMonth = Integer.parseInt(in.readLine());
				if (inputMonth < 1 || inputMonth > 12) {
					System.out.print("Please enter a valid month ");
					repeatFlag = 0;
				}
				System.out.print("Enter departure day in numbers (ex. 15): ");
				inputDay = Integer.parseInt(in.readLine());
				if (inputDay < 1 || inputDay > 31) {
					System.out.print("Please enter a valid day ");
					repeatFlag = 0;
				}
				System.out.print("Enter departure year greater than 1900: ");
				inputYear = Integer.parseInt(in.readLine());
				if (inputYear < 1900) {
					System.out.print("Please enter a valid year ");
					repeatFlag = 0;
				}
			} while(repeatFlag == 0);
			String departureDate = inputMonth + "/" + inputDay + "/" + inputYear;

			//Insert into bookings table
			//Make sure pID, departure date and flight Num are all unique
			query = "INSERT INTO Booking VALUES(";
			String bookingID = getBookingID();
			query += bookingID + ", ";
			query += departureDate + ", ";
			query += flightNum.get(0).get(0).replaceAll("\\s+","") + ", "; // accessing index in list of list
			query += passNum.get(0).get(0) + ");";
			System.out.println(query);

			// esql.executeQuery(query);

		}catch(Exception e){
			 System.err.println (e.getMessage());
		}
	}

	public static void TakeCustomerReview(AirBooking esql){//3
		//Insert customer review into the ratings table

	}

	public static void InsertOrUpdateRouteForAirline(AirBooking esql){//4 EXTRA CREDIT
		//Insert a new route for the airline
	}

	public static void ListAvailableFlightsBetweenOriginAndDestination(AirBooking esql) throws Exception{//5
		//List all flights between origin and distination (i.e. flightNum,origin,destination,plane,duration)
		String input = "";
		String query = "SELECT flightNum, plane, duration FROM Flight WHERE origin = '";
		Integer repeatFlag = 1;

		System.out.print("Enter the origin for all flights you want to see: ");
		do { //performs check to make sure user entered something
			input = in.readLine();
			repeatFlag = 1;
			if (input == null || input.isEmpty()) {
				System.out.println("Please enter a origin location\n");
				repeatFlag = 0;
			}
			boolean allLetters = input.chars().allMatch(Character::isLetter);
			if (allLetters == false) {
				System.out.println("Please enter only characters\n");
				repeatFlag = 0;
			}
		} while(repeatFlag == 0);
		query += input + "', '";

		System.out.print("Enter the destination for all flights you want to see: ");
		do { //performs check to make sure user entered something
			input = in.readLine();
			repeatFlag = 1;
			if (input == null || input.isEmpty()) {
				System.out.println("Please enter a destination location\n");
				repeatFlag = 0;
			}
			boolean allLetters = input.chars().allMatch(Character::isLetter);
			if (allLetters == false) {
				System.out.println("Please enter only characters\n");
				repeatFlag = 0;
			}
		} while(repeatFlag == 0);
		query += input + "';";

		System.out.println(query);
	}


	public static void ListMostPopularDestinations(AirBooking esql){//6
		//Print the k most popular destinations based on the number of flights offered to them (i.e. destination, choices)
	}

	public static void ListHighestRatedRoutes(AirBooking esql){//7
		//List the k highest rated Routes (i.e. Airline Name, flightNum, Avg_Score)
	}

	public static void ListFlightFromOriginToDestinationInOrderOfDuration(AirBooking esql){//8
		//List flight to destination in order of duration (i.e. Airline name, flightNum, origin, destination, duration, plane)
	}

	public static void FindNumberOfAvailableSeatsForFlight(AirBooking esql){//9
		//

	}

}

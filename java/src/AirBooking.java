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
import java.util.Set;
import java.util.HashSet;
import java.text.DecimalFormat;

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
			String input = "";
			Integer repeatFlag = 1;
			List<List<String>> maxID;

			String testquery = "SELECT MAX(pID) from Passenger";
			maxID = esql.executeQueryAndReturnResult(testquery);
			query += (Integer.parseInt(maxID.get(0).get(0)) + 1) + ", ";

			// Insert Passport
			System.out.print("Enter your Passport Number: "); // make sure it's unique
			do { //performs check to make sure user entered something
				input = in.readLine();
				repeatFlag = 1;
				if (input == null || input.isEmpty()) {
					System.out.println("Please enter an Passport Number");
					repeatFlag = 0;
				}
				else if (input.length() != 10) {
					System.out.println("Please enter a 10 character Passport Number");
					repeatFlag = 0;
				}
				boolean allLetters = input.chars().allMatch(Character::isLetter);
				if (allLetters == false) {
					System.out.println("Please enter only characters");
					repeatFlag = 0;
				}
				String uniqueChecker = "SELECT * FROM Passenger WHERE passNum = '";
				uniqueChecker += input + "' LIMIT 1;";
				if (esql.executeQuery(uniqueChecker) == 1) {
					System.out.println("This Passport Number already exists");
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
				System.out.println("Enter birth month in numbers (ex. 1 is January): ");
				inputMonth = Integer.parseInt(in.readLine());
				if (inputMonth < 1 || inputMonth > 12) {
					System.out.println("Please enter a valid month ");
					repeatFlag = 0;
				}
				System.out.println("Enter birth day in numbers (ex. 15): ");
				inputDay = Integer.parseInt(in.readLine());
				if (inputDay < 1 | inputDay > 31) {
					System.out.println("Please enter a valid day ");
					repeatFlag = 0;
				}
				System.out.println("Enter birth year greater than 1900: ");
				inputYear = Integer.parseInt(in.readLine());
				if (inputYear < 1900) {
					System.out.println("Please enter a valid year ");
					repeatFlag = 0;
				}
			} while(repeatFlag == 0);
			input = inputMonth + "/" + inputDay + "/" + inputYear;
			query += input + ", ";

			//Insert Country
			System.out.println("Enter Country: ");
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
				boolean allLetters = input.replaceAll("\\s+","").chars().allMatch(Character::isLetter);
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
			//TODO: do a select limit 1 and see if it exists
			Integer repeatFlag = 1;
			String originInput = "";
			String destInput = "";
			String input = "";
			List<List<String>> flightNum;
			List<List<String>> passNum;
			String query = "";
			String multipleCityQuery = "";
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
					System.out.println("Please enter a valid flight path.");
					repeatFlag = 0;
				}
				if (flightNum.size() > 1) { // in case there are multiple flights
					System.out.println("There seems to be multiple flights from these two cities.");
					System.out.println("Please choose which one would you prefer to take by Flight Number.");
					multipleCityQuery = "SELECT * FROM Flight WHERE origin = '" + originInput
					+ "' AND destination = '" + destInput + "';";
					do {
						repeatFlag = 1;
						esql.executeQueryAndPrintResult(multipleCityQuery);
						input = in.readLine();
						String uniqueChecker = "SELECT * FROM Flight WHERE flightNum = '";
						uniqueChecker += input + "' AND Origin = '" + originInput + "' AND destination = '" + destInput + "' LIMIT 1;";
						if (esql.executeQuery(uniqueChecker) == 0) {
							System.out.println("There is no existing Flight Number from those two cities.");
							System.out.println("Try entering it again.");
							repeatFlag = 0;
						}
				 } while (repeatFlag == 0);
				 for (int i = 0; i < flightNum.size(); i++) {
				 	flightNum.get(i).clear();
				 }
				 flightNum.get(0).add(input);
				 break;
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
			// making sure this bookingID isn't used if the booking ID has been already
			// created it'll silently loop until it finds one that hasn't been used
			String bookingID = "";
			do {
				bookingID = getBookingID();
				String uniqueChecker = "SELECT * FROM Booking WHERE bookRef = '";
				uniqueChecker += input + "' LIMIT 1;";
				if (esql.executeQuery(uniqueChecker) == 1) {
					repeatFlag = 0;
				}
			} while(repeatFlag == 0);
			query += bookingID + ", ";
			query += departureDate + ", ";
			query += flightNum.get(0).get(0).replaceAll("\\s+","") + ", "; // accessing index in list of list
			query += passNum.get(0).get(0) + ");";
			System.out.println(query);

			System.out.println("You're all good to go! Your booking ID is: " + bookingID);
			System.out.println("Your flight from " + originInput + " to " + destInput + " on " + departureDate + " has been booked.");

			// all avaliable flights from origin to destination
			// esql.executeQuery(query);

		}catch(Exception e){
			 System.err.println (e.getMessage());
		}
	}

	public static void TakeCustomerReview(AirBooking esql){//3
		//Insert customer review into the ratings table
		try{
			String query = "INSERT INTO Ratings VALUES(";
			String input = "";
			String originInput = "";
			String destInput = "";
			List<List<String>> flightNum;
			List<List<String>> passNum;
			Integer repeatFlag = 1;
			Integer skipFlag = 0;
			String multipleCityQuery = "";
			List<List<String>> maxID;

			String testquery = "SELECT MAX(rID) from Ratings";
			maxID = esql.executeQueryAndReturnResult(testquery);
			query += (Integer.parseInt(maxID.get(0).get(0)) + 1) + ", ";

			String passportString = "";
			System.out.println("Please enter your Passport Number: ");
			// Insert pID
			do {
				repeatFlag = 1;
				passportString = "SELECT pID FROM Passenger WHERE passNum = '";
				input = in.readLine();
				passportString += input + "';";
				passNum = esql.executeQueryAndReturnResult(passportString);
				if (passNum.isEmpty()) {
					System.out.println("There is no matching Passport Number.");
					repeatFlag = 0;
				}
			} while (repeatFlag == 0);
			query += input + ", ";

			//Insert Flight Number
			//Either ask for flight number or ask for origin destination
			String flightFinder = "";
			System.out.println("Enter your flight number. If you only know the origin and destination, please type 'origin'.");
			do {
				input = in.readLine();
				if (input.equals("origin")) {
					do { // helps find the corresponding flight number
						flightFinder = "SELECT flightNum FROM Flight WHERE origin = '";
						repeatFlag = 1;
						System.out.println("Where are you traveling from?");
						originInput = in.readLine();
						flightFinder += originInput + "' AND destination = '";
						System.out.println("Where would you like to travel to?");
						destInput = in.readLine();
						flightFinder += destInput + "'";
						flightNum = esql.executeQueryAndReturnResult(flightFinder);
						if (flightNum.isEmpty()) {
							System.out.println("There is no flight from " +
							originInput + " to " + destInput);
							System.out.println("Please enter a valid flight path.");
							repeatFlag = 0;
						}
						if (flightNum.size() > 1) { // in case there are multiple flights
							System.out.println("There seems to be multiple flights from these two cities.");
							System.out.println("Please choose which one would you prefer to take by Flight Number.");
							multipleCityQuery = "SELECT * FROM Flight WHERE origin = '" + originInput
							+ "' AND destination = '" + destInput + "';";
							do {
								repeatFlag = 1;
								skipFlag = 1;
								esql.executeQueryAndPrintResult(multipleCityQuery);
								input = in.readLine();
								String uniqueChecker = "SELECT * FROM Flight WHERE flightNum = '";
								uniqueChecker += input + "' AND Origin = '" + originInput + "' AND destination = '" + destInput + "' LIMIT 1;";
								if (esql.executeQuery(uniqueChecker) == 0) {
									System.out.println("There is no existing Flight Number from those two cities.");
									System.out.println("Try entering it again.");
									repeatFlag = 0;
								}
						 } while (repeatFlag == 0);
					 }
					} while (repeatFlag == 0);
					if (skipFlag == 0) {
						System.out.println("The corresponding flightNum is: " + flightNum.get(0).get(0));
						input = flightNum.get(0).get(0).replaceAll("\\s+","");
					}
					break;
				}
				String uniqueChecker = "SELECT * FROM Flight WHERE flightNum = '";
				uniqueChecker += input + "' LIMIT 1;";
				if (esql.executeQuery(uniqueChecker) == 0) {
					System.out.println("This flightNum does not exist, try again");
					System.out.println("Type origin if you want to search for your flightNum");
					repeatFlag = 0;
				}
			} while (repeatFlag == 0);
			query += input + ", ";


			System.out.println("What would you rate this flight? (1 - 5 with a 1 being the lowest): ");
			do { //performs check to make sure user entered something
				input = in.readLine();
				repeatFlag = 1;
				if (input == null || input.isEmpty()) {
					System.out.println("Please enter a rating");
					repeatFlag = 0;
				}
				if (Integer.parseInt(input) < 1 || Integer.parseInt(input) > 5) {
					System.out.println("That is not a valid rating. Please try again.");
					repeatFlag = 0;
				}
			} while(repeatFlag == 0);
			query += input;

			System.out.println("Would you like to leave a comment? Y/N");
			do {
				input = in.readLine();
				repeatFlag = 1;
				if (input.equals("Y")) { // leaving a comment
					System.out.println("Please leave your comment. (Maximum 240 characters)");
					do {
						input = in.readLine();
						if (input.length() > 240) {
							System.out.println("Your comment is too long. Please write under 240 characters");
						}
					} while (repeatFlag == 0);
					query += ", " + input + ")";
				}
				else if (input.equals("N")) { // end query
					query += ")";
					break;
				}
				else {
					System.out.println("Please enter Y or N.");
					repeatFlag = 0;
				}
			} while (repeatFlag == 0);

			System.out.println(query);
			// esql.executeQuery(query);

		}catch(Exception e){
			 System.err.println (e.getMessage());
		}
	}

	public static void InsertOrUpdateRouteForAirline(AirBooking esql){//4 EXTRA CREDIT
		//Insert a new route for the airline
	}

	public static void ListAvailableFlightsBetweenOriginAndDestination(AirBooking esql) throws Exception{//5
		//List all flights between origin and distination (i.e. flightNum,origin,destination,plane,duration)
		String input = "";
		String query = "SELECT * FROM Flight WHERE origin = '";
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
		query += input + "' AND destination = '";

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

		esql.executeQueryAndPrintResult(query);
	}

	public static void ListMostPopularDestinations(AirBooking esql){//6
		//Print the k most popular destinations based on the number of flights offered to them (i.e. destination, choices)
		try {
			String input = "";
			String input2 = "";
			String query = "SELECT destination,COUNT(*) as count FROM Flight GROUP BY destination ORDER BY count DESC LIMIT ";
			Integer repeatFlag = 1;
			List<List<String>> popularDestinations;
			List<List<String>> totalNumberOfFlights;
			String totalFlights = "SELECT COUNT(*) FROM Flight GROUP BY destination";
			totalNumberOfFlights = esql.executeQueryAndReturnResult(totalFlights);

			System.out.println("How many popular destinations would you like to see?");
			do { //performs check to make sure user entered something
				input = in.readLine();
				repeatFlag = 1;
				if (Integer.parseInt(input) < 1) {
					System.out.println("Can not have less than one popular destination");
					repeatFlag = 0;
				}
				// Can add functionality to let user know there isn't that many flights
				if (Integer.parseInt(input) > totalNumberOfFlights.size()) {
					System.out.println("There are less destinations than the number you provided.");
					System.out.println("Would you like to see all avaliable destinations? Y/N");
					input2 = in.readLine();
					if (input2.equals("Y")) {
						System.out.println("Now outputting the total number of destinations: " + totalNumberOfFlights.size());
						break;
					}
					else if (input2.equals("N")) {
						System.out.println("Okay. Please try again.");
						System.out.println("How many popular destinations would you like to see?");
						repeatFlag = 0;
					}
					else {
						System.out.println("Please enter Y or N.");
						repeatFlag = 0;
					}
				}
			} while(repeatFlag == 0);
			query += input + ";";
			popularDestinations = esql.executeQueryAndReturnResult(query);
			for (int i = 0; i < popularDestinations.size(); i++) {
			System.out.println("Ranking: " + (i + 1));
			System.out.println("Destination: " + popularDestinations.get(i).get(0).replaceAll("\\s+",""));
			System.out.println("Number of flights: " + popularDestinations.get(i).get(1));
			System.out.println("---------");
			}
		}
		catch(Exception e){
			 System.err.println (e.getMessage());
		}
	}

	public static void ListHighestRatedRoutes(AirBooking esql){//7
		//List the k highest rated Routes (i.e. Airline Name, flightNum, Avg_Score)
		//airline name, flight number, origin, destination, plane, and avg_score.
		try{
			String input = "";
			String input2 = "";
			String query = "SELECT flightNum,AVG(score) as ratingScore FROM Ratings GROUP BY flightNum ORDER BY ratingScore DESC LIMIT ";
			Integer repeatFlag = 1;
			List<List<String>> flightNumAndScore;
			List<List<String>> airList;
			List<List<String>> airlineNameandID;
			List<List<String>> totalNumberofReviews;

			String totalReviews = "SELECT COUNT(*) FROM Ratings GROUP BY flightNum";
			totalNumberofReviews = esql.executeQueryAndReturnResult(totalReviews);

			System.out.println("How many of the highest rated flights would you like to see?");
			do { //performs check to make sure user entered something
				input = in.readLine();
				repeatFlag = 1;
				if (Integer.parseInt(input) < 1) {
					System.out.println("Can not have less than one number of highest rated flights");
					repeatFlag = 0;
				}
				// TODO: Can add functionality to let user know there isn't that many flights
				if (Integer.parseInt(input) > totalNumberofReviews.size()) {
					System.out.println("There are less number of reviews than the number you provided.");
					System.out.println("Would you like to see all avaliable reviews? Y/N");
					input2 = in.readLine();
					if (input2.equals("Y")) {
						System.out.println("Now outputting the total number of flight: " + totalNumberofReviews.size());
						break;
					}
					else if (input2.equals("N")) {
						System.out.println("Okay. Please try again.");
						System.out.println("How many highest rated reviews would you like to see?");
						repeatFlag = 0;
					}
					else {
						System.out.println("Please enter Y or N.");
						repeatFlag = 0;
					}
				}
			} while(repeatFlag == 0);
			query += input + ";";

			flightNumAndScore = esql.executeQueryAndReturnResult(query);
			// finding all airIDs, origin, destination,plane,flightNum
			query = "SELECT airID,origin,destination,plane,flightNum FROM Flight WHERE flightNum in (";
			for (int i = 0; i < flightNumAndScore.size(); i++) {
					if (i == (flightNumAndScore.size() - 1)) {
						query += "'" + flightNumAndScore.get(i).get(0).replaceAll("\\s+","") + "');";
					}
					else {
						query += "'" + flightNumAndScore.get(i).get(0).replaceAll("\\s+","") + "', ";
					}
			}
			airList = esql.executeQueryAndReturnResult(query);
			// finding the name of the airline
			query = "SELECT name,airID FROM Airline WHERE airID in (";
			for (int i = 0; i < airList.size(); i++) {
					if (i == (airList.size() - 1)) {
						query += "'" + airList.get(i).get(0) + "');";
					}
					else {
						query += "'" + airList.get(i).get(0) + "', ";
					}
			}
			airlineNameandID = esql.executeQueryAndReturnResult(query);
			List<String> printOut = new ArrayList<String>();

			//Need to combine all three lists now
			DecimalFormat df = new DecimalFormat("0.#####");

			for (int i = 0; i < flightNumAndScore.size(); i++) {
				System.out.println("Flight Number: " + flightNumAndScore.get(i).get(0));
				// TODO: Need to fix output of integer
				System.out.println("Score: " + flightNumAndScore.get(i).get(1));
				// Using flight number extract everything else
				for (int j = 0; j < airList.size(); j++) {
					if (flightNumAndScore.get(i).get(0).equals(airList.get(j).get(4))) {
						System.out.println("Origin: " + airList.get(j).get(1));
						System.out.println("Destination: " + airList.get(j).get(2));
						System.out.println("Plane type: " + airList.get(j).get(3));
					}
				}
				for (int j = 0; j < airlineNameandID.size(); j++) {
					if (airList.get(i).get(0).equals(airlineNameandID.get(j).get(1))) {
						System.out.println("Airline Name: " + airlineNameandID.get(j).get(0));
					}
				}
				System.out.println("---------");

			}
		}
		catch(Exception e){
			 System.err.println (e.getMessage());
		}
	}

	public static void ListFlightFromOriginToDestinationInOrderOfDuration(AirBooking esql){//8
		//List flight to destination in order of duration (i.e. Airline name, flightNum, origin, destination, duration, plane)
	}

	public static void FindNumberOfAvailableSeatsForFlight(AirBooking esql){//9
		//

	}

}

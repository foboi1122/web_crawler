import java.sql.*;

import org.apache.log4j.Logger;

import edu.uci.ics.crawler4j.url.WebURL;

public class SqlWrapper
{
	private Connection c = null;
	static Logger log = Logger.getLogger(
    		BasicCrawler.class.getName());
	
	public SqlWrapper()
	{
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:pages.db");

			stmt = c.createStatement();
			String sql = "CREATE TABLE IF NOT EXISTS PAGES " +
					"(ID INTEGER PRIMARY KEY   AUTOINCREMENT," +
					" URL            TEXT    NOT NULL, " + 
					" SUBDOMAIN      TEXT    NOT NULL, " + 
					" BODY           TEXT    NOT NULL, " + 
					" HTML           TEXT    NOT NULL)"; 
			stmt.executeUpdate(sql);
			stmt.close();
			
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			log.info(e.getClass().getName() + ": " + e.getMessage());
		}
		System.out.println("Opened database successfully");
	}
	
	public void Close(){
		try {
			c.close();
		} catch (SQLException e) {
			System.out.println("Attempted to close DB, DB already closed");
		}
	}
	
	public void InsertItem(String URL, String Text, String HTML) throws SQLException{
		WebURL curURL = new WebURL();
		curURL.setURL(URL);
		
		Statement stmt = null;
		try {
			stmt = c.createStatement();
		} catch (SQLException e) {
			System.out.println("Error, can't create statement in AddLink");
			e.printStackTrace();
		}

		PreparedStatement statement = c.prepareStatement("INSERT INTO PAGES (URL,SUBDOMAIN,BODY,HTML) values (?, ?, ?, ?)");
		statement.setString(1, URL);
		statement.setString(2, curURL.getSubDomain());
		statement.setString(3, Text);
		statement.setString(4, HTML);
		statement.executeUpdate();
	}
	
	public static void main( String args[] ) throws SQLException
	{
		SqlWrapper sql = new SqlWrapper();
		sql.InsertItem("www.test.com", "this is a wall of text", "<HTML>test test</HTML>");
		sql.Close();
	}
}

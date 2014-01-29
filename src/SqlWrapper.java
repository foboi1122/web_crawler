import java.sql.*;

public class SqlWrapper
{
	private Connection c = null;
	
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
					" BODY           TEXT    NOT NULL, " + 
					" HTML           TEXT    NOT NULL)"; 
			stmt.executeUpdate(sql);
			stmt.close();
			
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
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
		Statement stmt = null;
		try {
			stmt = c.createStatement();
		} catch (SQLException e) {
			System.out.println("Error, can't create statement in AddLink");
			e.printStackTrace();
		}
		
		String sql = "INSERT INTO PAGES (URL,BODY,HTML) " +
                "VALUES ( '" + URL + "', '" +
						Text + "', '" +
						HTML + "');";
		stmt.executeUpdate(sql);
	}
	
	public static void main( String args[] ) throws SQLException
	{
		Connection c = null;
		Statement stmt = null;

		SqlWrapper sql = new SqlWrapper();
		sql.InsertItem("www.test.com", "this is a wall of text", "<HTML>test test</HTML>");
		sql.Close();
	}
}

package GLock;

import java.net.InetAddress;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

class sqlConnect {
	
	private static sqlConnect sc = new sqlConnect(); 
	
	Connection con;
	Statement stmt;
	ResultSet rs;
	String url;
	String dbId = "pi";
	String dbPwd = "raspberry";
	
	String user_index;
	String id;
	String pwd;
	
	
	
	private sqlConnect() {
		// TODO Auto-generated constructor stub
		// a url which indicates server/dbname
		url = "jdbc:mysql://218.150.181.86:3306/kanglab_db?useSSL=false";
		con = null;
		stmt = null;
		rs = null;
	}
	
	public static sqlConnect getInstance(){ return sc; }
	
	public void setId(String id){
		this.id = id;
	}
	
	public void setPwd(String pwd){
		this.pwd = pwd;
	}
	
	public String getId(){ return id; }
	public String getPwd(){ return pwd; }

	// Need to call this function when compile this program on linux
	// does not need on windows
	public void jdbcDriverLoad()
	{
		try{
			Class.forName("org.gjt.mm.mysql.Driver");
			System.out.println("jdbc driver load Successed!!!");
		} catch(ClassNotFoundException e){
			System.out.println(e.getMessage());
		}
	}
	
	
	public void connectToMysql()
	{
		try{
			
			System.out.println("try-catch test statement!");
			
			// database id and password
			con = (Connection) DriverManager.getConnection(url, dbId, dbPwd);
			System.out.println("mysql access Successed!!!");
			stmt = (Statement) con.createStatement();
			
			// query test code
//			
//			ResultSet rs = stmt.executeQuery("select * from test");
//			System.out.println("Got result : ");
//			while(rs.next()) {
//				String no = rs.getString(2);
//				String tblname = rs.getString(1);
//				System.out.println("1 : " + no + "\n");
//				System.out.println("2 : " + tblname + "\n");
//			}

		} catch(java.lang.Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void closeConnection()
	{
		try {
			stmt.close();
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	
	public SecurityBean getSecureDate()
	{
		
		SecurityBean sb = new SecurityBean();
		String query = "select * from security_time where id ='" + id + "';";
		String[] split = null;
		
		try {

			rs = stmt.executeQuery(query);
			
			if(rs.next()){
				for(int i = 3; i < 16; i = i + 2) {
					
					String startTime = rs.getString(i);
					String endTime = rs.getString(i);
	
					if(startTime.equals("0:0") && endTime.equals("0:0"))
					{
						sb.day.put(i/2 + "", false);
						System.out.println(i/2 + " : false");
					}
					else
					{
						sb.day.put(i/2 + "", true);
						System.out.println(i/2 + " : true");
					}
					
					split = startTime.split(":");
					sb.setsStartHour(Integer.parseInt(split[0]), i/2-1);
					sb.setsStartMin(Integer.parseInt(split[1]), i/2-1);
					System.out.println(i/2 + " start Time : " + split[0] + " "+ split[1]);
					

					split = endTime.split(":");
					sb.setsEndHour(Integer.parseInt(split[0]),i/2-1); 
					sb.setsEndMin(Integer.parseInt(split[1]),i/2-1); 
					System.out.println(i/2 + " end Time : " + split[0] + " "+ split[1]);
					
				}
			}
			
			rs.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sb;
	}
	
	
	// send local date to db
	public void sendLog(boolean success)
	{
		
		try {
			
			if(success)
				stmt.executeUpdate("insert into log (access_check, user_index) " 
						+ "values(1, '" + user_index + "');");
			
			else
				stmt.executeUpdate("insert into log (access_check, user_index) " 
						+ "values(0, '" + user_index + "');");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	// send local ip to db
	public void sendIp()
	{
		
		try {
			
			String localIp = InetAddress.getLocalHost().getHostAddress();
			stmt.executeUpdate("update users set ip_address = '" + localIp + "' where id = '" + id + "';");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void removeIp()
	{
		try {
			
			stmt.executeUpdate("update users set ip_address = 'NULL' where id = '" + id + "';");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
		
	}
	
	//check user's id and password
	public boolean isJoinedUser(){
		
		int result = 0;
		String query = "select count(*) from users where id ='" + id + "' and id_password = '" + pwd + "';";
		String idQuery = "select index_user from users where id = '" + id + "';";
		
		try {
			
			rs = stmt.executeQuery(query);
			if(rs.next())
				result = rs.getInt(1);
			
		} catch (SQLException e) {	
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		// if id and passwords correct then true
		if(result>0)
		{
			// Get user index
			try {
				
				rs = stmt.executeQuery(idQuery);
				
				if(rs.next())
					user_index = rs.getString(1);
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return true;
		
		}
		
		// else false
		return false;
	}
	
	
	
}// class sqlConnect end;
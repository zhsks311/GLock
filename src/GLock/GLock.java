package GLock;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.SimpleAttributeSet;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

public class GLock extends JFrame {

	private JButton[] buttons;
	private JPanel Panel;
	private String input="";
	private String id;
	// password is a password for user's id
	private String password;
	// lPwd is a password for doorlock
	private String lPwd;
	sqlConnect sc;
	List<String> disposablePwd = new ArrayList<>();
	
	public GLock() {
		
		
		this.setSize(500, 500);
		
		Panel = new JPanel(new GridLayout(3, 4));
		buttons = getButtons(12);
		layoutButtons();

		this.add(Panel);
		this.add(getCheckButton(), BorderLayout.EAST);

		this.setVisible(true);
		
		// initializing
		// jdbcDriverLoad is needed when we use this program in Linux
		sc = new sqlConnect();
		
		defaultIdSet();
		
		sc.connectToMysql();
		
		//sc.jdbcDriverLoad
		// if there's no correct user, then exit program
		if(!sc.isJoinedUser())
			System.exit(0);
		
		// Update ip to command to doorlock
		sc.sendIp();
		sc.closeConnection();
		
		
	}
	
	public static void main(String[] args) {
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new GLock();
			}
		});
		
	}
	
	public void defaultIdSet()
	{
		setId("kang");
		setPwd("kang");
		setLpwd("1234");
			
	}
	
	
	public void setId(String i)
	{
		id = i;
	}
	
	public void setPwd(String pwd)
	{
		password = pwd;
	}
	
	public void setLpwd(String pwd)
	{
		lPwd = pwd;
	}
	
	public String getId() { return id; }
	public String getPwd() { return password; }
	public String getLpwd() { return lPwd; }
	
	
	// shuffle buttons
	public void ShuffleButtons() {
		if (buttons != null) {
			Collections.shuffle(Arrays.asList(buttons));
			layoutButtons();
		}
	}
	
	
	public void layoutButtons() {
		Panel.removeAll();
		for (JButton button : buttons) {
			Panel.add(button);
		}
		Panel.revalidate();
		Panel.repaint();
	}
	
	// create buttons
	private JButton[] getButtons(int size) {
		JButton[] buttons = new JButton[size];
		String strs[] = { "*", "#", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0" };
		List<String> list;
		list = Arrays.asList(strs);
		Collections.shuffle(list);
		
		for (int i = 0; i < size; i++) {
			final JButton button = new JButton(/* ""+i */);
			
			button.setText("" + strs[i]);
			button.setBackground(Color.BLACK);
			button.setForeground(Color.white);
				
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// System.out.println("Button " + button.getText() + "
					// pressed");
					System.out.println(e.getActionCommand());
					input = input + e.getActionCommand();
					
				}
			});
			buttons[i] = button;
			
		}
		
		return buttons;
	}

	// Check Passwords when OK button clicked
	private JButton getCheckButton() {
		JButton check = new JButton("OK");
		check.setBackground(Color.black);
	
		check.setForeground(Color.white);

		check.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.print("\n"+input);
				if(input.equals(lPwd)){
					ShuffleButtons();
					input="";
					sc.connectToMysql();
					sc.sendDate();
					sc.closeConnection();
					JOptionPane.showMessageDialog(null, "OPEN");
				}else{
					JOptionPane.showMessageDialog(null, "Passwords incorrected.");
					ShuffleButtons();
					input="";
				}
				
			}
		});
		return check;
	}

	// make disposable password for friend
	public int createDisposablePwd()
	{
		int randomKey = (int) (Math.random()*8999990+1000000);
		disposablePwd.add(String.valueOf(randomKey));
		return randomKey;
	}
	
	// find disposable password
	public boolean searchDisposablePwd(int key)
	{
		int pwdIndex=0;
		if((pwdIndex = disposablePwd.indexOf(String.valueOf(key)))>=0)
		{
			disposablePwd.remove(pwdIndex);
			return true;
		}
		
		return false;
		
	}
	
	public void exitProgram()
	{
		sc.removeIp();
		System.exit(0);
		
	}
	

	class sqlConnect {
		
		Connection con;
		Statement stmt;
		ResultSet rs;
		String url;
		
		public sqlConnect() {
			// TODO Auto-generated constructor stub
			// a url which indicates server/dbname
			url = "jdbc:mysql://192.168.0.9:3306/test";
			con = null;
			stmt = null;
			rs = null;
		}
		
	
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
				con = (Connection) DriverManager.getConnection(url,"pi","raspberry");
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
		
		// send local date to db
		public void sendDate()
		{
			try {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String date = format.format(new Date());
				stmt.executeUpdate("insert into test (name) values('" + date + "');");
				
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
				stmt.executeUpdate("update user set ip = '" + localIp + "' where userId = '" + getId() + "';");
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
			
		
		}
		public void removeIp()
		{
			try {
				
				stmt.executeUpdate("update user set ip = 'NULL' where userId = '" + getId() + "';");
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
			
			
		}
		
		//check user's id and password
		public boolean isJoinedUser(){
			
			int result = 0;
			String query = "select count(*) from user where userId ='" + getId() + "' and userPwd = '" + getPwd() + "';";
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
				return true;
			
			// else false
			return false;
		}
		
	}// class sqlConnect end;
	

}



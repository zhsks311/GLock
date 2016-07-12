package GLock;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
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
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.SimpleAttributeSet;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

/*
 * For Linux
//pi4j
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
*/


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
	
	// counting how many number button pressed
	int btnPressCnt = 0;
	
	boolean linuxFlag = false;
	private int falseCount = 0;
	
	String srcPath = "/home/pi/project/";
	String imageSrcPath = srcPath + "image/";

	String date;
	
	// variable for secure time
	HashMap<String, Boolean> day = new HashMap<String, Boolean>();
	int sStartHour = 0;
	int sEndHour = 0;

	public GLock() {
		
		//networking.sockClient();
		networking.runServer();
		
		this.setSize(480, 320); 
		
		Panel = new JPanel(new GridLayout(3, 4));
		buttons = getButtons(12);
		layoutButtons();

		this.add(Panel);
		this.add(getCheckButton(), BorderLayout.EAST);

		this.setVisible(true);
		
		/*
		 * For Linux
		// set Gpio for door control
		setGpio();
		*/
		
		// test initialization
		// need to get data from server later
		// and input data to variable
		day.put("Mon", true);
		day.put("Tue", true);
		day.put("Wed", true);
		day.put("Thu", false);
		day.put("Fri", false);
		day.put("Sat", false);
		day.put("Sun", false);
		
		// initializing
		// jdbcDriverLoad is needed when we use this program in Linux
		sc = new sqlConnect();
		defaultIdSet();
		sc.connectToMysql();
		
		//sc.jdbcDriverLoad
		// if there's no correct user, then exit program
		if(!sc.isJoinedUser(getId(), getPwd()))
			System.exit(0);
		
		// Update ip to command to doorlock
		sc.sendIp(getId());
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
					
					if((isSecureTime() || falseCount > 2) && btnPressCnt++ < 1)
					{
						if(linuxFlag)
							takePicture();

					}

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
					
					/*
					 * For Linux
					 * openDoor();
					 */
					
					// reset the counts that is added when input wrong password
					falseCount = 0;
					JOptionPane.showMessageDialog(null, "OPEN");
				
				}else{
					
					// increase the counts for password incorrect 
					falseCount++;
					JOptionPane.showMessageDialog(null, "Passwords incorrected.");
				}
				
				if(isSecureTime() || falseCount > 2)
				{

					// send a picture
					if(linuxFlag)
						networking.uploadFile(imageSrcPath + getId() + "_" + networking.getTime(); + ".jpg");
				
					
				}
				
				input="";
				sc.connectToMysql();
				sc.sendLog();
				sc.closeConnection();
				
				ShuffleButtons();
				
				// btnPressCnt is for timing to take a picture
				// it is used to check
				// Does number button be pressed first
				btnPressCnt=0;
				
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
	
	public void takePicture()
	{
		date = networking.getTime();
		System.out.println( imageSrcPath + getId() + "_" + date + ".jpg");
		networking.executeCommand("raspistill -t 100 -o " + imageSrcPath + getId() + "_" + date + ".jpg");

	}
	
	public boolean isSecureTime(){
		
		String today = networking.getDay();
		
		// hour - the hour-of-day to represent, from 0 to 23
		java.time.LocalTime systemTime = java.time.LocalTime.now();
		int hour = systemTime.getHour(); 

		

		if(day.get(today))
			return false;
		
		if(!(sStartHour <= hour && sEndHour > hour))
			return false;
		
		
		return true;
	}

	
/*
 * For Linux
	public void setGpio()
	{
		// create gpio controller
		gpio = GpioFactory.getInstance();

		//provision gpio pin #01 as an output pin and turn off
		pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_27, "MyLED", PinState.HIGH);

		// set shutdown state for this pin
	        pin.setShutdownOptions(true, PinState.HIGH);
	}

	public void openDoor()
	{

		// gpio low
		pin.low();

		try{
		// wait for next work
		Thread.sleep(100);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		// gpio high for maintain normal state
		pin.high();

	}
*/
	
	public void exitProgram()
	{
		sc.removeIp(getId());
		System.exit(0);
		
	}
	
}



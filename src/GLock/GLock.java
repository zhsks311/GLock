package GLock;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
import GLock.networking.TCPServer.Listener;

/* For Linux
//pi4j
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
*/

public class GLock extends JPanel {

	private static GLock glock;
	
	private JButton[] buttons;
	private JPanel Panel;
	private String input="";

	// lPwd is a password for doorlock
	private String lPwd;
	sqlConnect sc = sqlConnect.getInstance();
	List<String> disposablePwd = new ArrayList<>();
	
	// counting how many number button pressed
	int btnPressCnt = 0;

	/* For Linux
	GpioPinDigitalOutput pin;
	GpioPinDigitalOutput pin2;
	GpioController gpio;
	 */
	boolean linuxFlag = true;
	private int falseCount = 0;
	
	String srcPath = "/home/pi/project/";
	String imageSrcPath = srcPath + "image/";

	String date;
		
	// variable for secure time
	SecurityBean sb;
	
	private GLock() { }
	
	
	public static synchronized GLock getInstance()
	{		
		
		if(glock == null){
			glock = new GLock();
			glock.setGUI();
		}
		return glock;
		
	}
	
	
	public void callInitailData()
	{

		defaultIdSet();
		/* For Linux
		setGpio();
		*/
		sc.jdbcDriverLoad();
		sc.connectToMysql();
		
		// initializing variables about security time
		// need to get data from server later
		// and input data to variable		
		sb = SecurityBean.getInstance();
		sb = sc.getSecureDate();
		sc.getDisposablePwd();
		sc.getUid();
		
		// Update ip to command to doohttps://www.youtube.com/watch?v=dET0YZCp-xYrlock
		
		sc.sendIp();
		sc.closeConnection();
		
	}
	
	public void setGUI()
	{
		
		this.setSize(480, 320); 
		this.setLayout(new BorderLayout());
		Panel = new JPanel(new GridLayout(3, 4));
		buttons = getButtons(12);
		layoutButtons();

		this.add(Panel);
		this.add(getCheckButton(), BorderLayout.EAST);

		this.setVisible(true);
			
	}

	
	public static void main(String[] args) {
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
//				new GLock();
				mainFrame mf = new mainFrame();
			}
		});
		
	}
	
	
	public void defaultIdSet()
	{
		setLpwd("1234");
	}
	
	public void setLpwd(String pwd)
	{
		lPwd = pwd;
	}

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
					
					if(( falseCount > 2 && btnPressCnt < 1) || (isSecureTime()&& btnPressCnt < 1))
					{
						/* For Linux
						if(linuxFlag)
						{
							Picture p = new Picture();
							p.start();
							
						}
						*/
					} 
					btnPressCnt++;
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
				
				System.out.println("\n"+input);
				ShuffleButtons();
				
				if(input.equals(lPwd) || sb.isValidTempPwd(input))
					procDoor(true);
				else
					procDoor(false);
				
				
			}
		});
		return check;
	}
	
	public void procDoor(Boolean truth)
	{
		Boolean isTempPwd = sb.isValidTempPwd(input);
		if(truth){
			
		/* For Linux
			  openDoor();
		 */
			JOptionPane.showMessageDialog(null, "OPEN");
			
			// reset the counts that is added when input wrong password
			falseCount = 0;
			
			// send log
			try{
				sc.connectToMysql();
				if(isTempPwd)
				{
					int tempPwdIndex = sb.findTempPwdIndex(input);
					sb.removeTempPwd(tempPwdIndex);	
					sc.removeDisposablePwd(input);
				}
				
				sc.sendLog(true);
				sc.closeConnection();
			} catch(Exception ex){
				ex.printStackTrace();
			}
			
		}else{
			
			// increase the counts for password incorrect 
			falseCount++;
			JOptionPane.showMessageDialog(null, "Passwords incorrected.");
			
			// send log
			try{
				sc.connectToMysql();
				sc.sendLog(false);
				sc.closeConnection();
			} catch(Exception ex){
				ex.printStackTrace();
			}
		}
		
		// if three times password wrong or the time is on secured time, 
		// call push
		if(falseCount > 2 || isSecureTime())
		{
			networking.callPush(sc.getId());
		}
		
		
		System.out.println("false count : " + falseCount);
		
		
		
		input="";
		
		// btnPressCnt is for timing to take a picture
		// it is used to check
		// Does number button be pressed first
		btnPressCnt = 0;
		
		
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

	public boolean isSecureTime(){
		
		int today = 1;
		today = networking.getDay();
		
		// hour - the hour-of-day to represent, from 0 to 23
		java.time.LocalTime systemTime = java.time.LocalTime.now();
		int hour = systemTime.getHour(); 
		
		// search by key.
		// if value is false, return false.
		// first, check a day is security set day. 
		if(!sb.day.get(today + "")){
			System.out.println("Not Secure Time");
			return false;
		}
		
		// if set day, check time.
		if(!(sb.sStartHour[today] <= hour && sb.sEndHour[today] > hour))
		{
			System.out.println("Not Secure Time");
			return false;
		}
		
		// else it's secured time
		System.out.println("Secure Time");
		return true;
	}
	
	/*For Linux

	public void setGpio()
	{
		// create gpio controller
		gpio = GpioFactory.getInstance();

		//provision gpio pin #01 as an output pin and turn off
		pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_27, "MyLED", PinState.HIGH);
		pin2 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_26, "GROUND", PinState.LOW);
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

	
	public void exitProgram()
	{
		sc.removeIp();
		System.exit(0);
		
	}
	

	class Picture extends  Thread{
		String path="";
        public  Picture()  
        {
                
        }

        public  void  run()  {
        	path = imageSrcPath + sc.getId() + "_" + networking.getDay() + "_" + networking.getTime() + ".jpg";
        	takePicture();
        	networking.uploadFile(path, sc.getId());
        }

    	public void takePicture()
    	{
    		
    		System.out.println(path);
    		networking.executeCommand("raspistill -t 100 -o " + path);
    		try{
    			Thread.sleep(300);
    		} catch(Exception ex){
				ex.printStackTrace();
			}
    	}
    	

	}//server
	*/
}



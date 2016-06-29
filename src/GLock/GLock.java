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
	int btnPressCnt = 0;
	boolean linuxFlag = false;
	
	String srcPath = "/home/pi/project/";
	String imageSrcPath = srcPath + "image/";
	// get date
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	String date;
	
	
	
	public GLock() {
		
		//networking.sockClient();
		networking.runServer();
		
		date = format.format(new Date());
		
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

					if(btnPressCnt < 1)
					{
						if(linuxFlag)
							takePicture();

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
				
				System.out.print("\n"+input);
				if(input.equals(lPwd)){
					
					input="";
					
					JOptionPane.showMessageDialog(null, "OPEN");
					sc.connectToMysql();
					sc.sendDate();
					sc.closeConnection();
					
				}else{
					JOptionPane.showMessageDialog(null, "Passwords incorrected.");
					
					input="";
				}
				ShuffleButtons();
				btnPressCnt=0;
				if(linuxFlag)
					networking.uploadFile(imageSrcPath + getId() + "_" + date + ".jpg");
			
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
			System.out.println( imageSrcPath + getId() + "_" + date + ".jpg");
			networking.executeCommand("raspistill -t 100 -o " + imageSrcPath + getId() + "_" + date + ".jpg");

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



package GLock;

import java.util.ArrayList;
import java.util.HashMap;

public class SecurityBean {

	static private SecurityBean sb;
	
	// variables for security time
	HashMap<String, Boolean> day = new HashMap<String, Boolean>();
	int[] sStartHour = new int[7];
	int[] sEndHour = new int[7];
	int[] sStartMin = new int[7];
	int[] sEndMin = new int[7];
	String lPwd;
	// a variable for uid check 
	HashMap<String, Boolean> uid = new HashMap<String, Boolean>();
	
	// a variable for disposable password
	ArrayList<String> tempPwd = new ArrayList<String>();
	

	
	public SecurityBean()
	{
		
	}

	static public SecurityBean getInstance()
	{		
		
		if(sb == null)
			sb = new SecurityBean();
		return sb;
		
	}
		
	public Boolean getUid(String key) {
		System.out.println(key);
		System.out.println(uid.get(key));
		return uid.get(key) == null ? false : true;
	}
	
	public String getLPwd() {
		return lPwd;
	}

	public void setTempPwd(String lPwd) {
		this.lPwd = lPwd;
	}

	public ArrayList<String> getTempPwd() {
		return tempPwd;
	}

	public void setTempPwd(ArrayList<String> tempPwd) {
		this.tempPwd = tempPwd;
	}

	
	public Boolean isValidTempPwd(String key)
	{
		return sb.tempPwd.contains(key);
	}
	
	public int findTempPwdIndex(String key)
	{
		return tempPwd.indexOf(key);
	}
	
	public void removeTempPwd(int index)
	{
		tempPwd.remove(index);
	}
	
	public void setUid(String value, Boolean check) {
		System.out.println(value + ", " + check);
		uid.put(value, check);
	}
	
	public void setDay(HashMap<String, Boolean> day) {
		this.day = day;
	}
	public HashMap<String, Boolean> getDay() {
		return day;
	}
	public int[] getsStartHour() {
		return sStartHour;
	}
	public int[] getsEndHour() {
		return sEndHour;
	}
	public int[] getsStartMin() {
		return sStartMin;
	}
	public int[] getsEndMin() {
		return sEndMin;
	}
	public void setsStartHour(int sStartHour, int i) {
		this.sStartHour[i] = sStartHour;
	}
	public void setsEndHour(int sEndHour, int i) {
		this.sEndHour[i] = sEndHour;
	}
	public void setsStartMin(int sStartMin, int i) {
		this.sStartMin[i] = sStartMin;
	}
	public void setsEndMin(int sEndMin, int i) {
		this.sEndMin[i] = sEndMin;
	}
	
	
}

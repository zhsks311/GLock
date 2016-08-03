package GLock;

import java.util.HashMap;

public class SecurityBean {

	HashMap<String, Boolean> day = new HashMap<String, Boolean>();
	int[] sStartHour = new int[7];
	int[] sEndHour = new int[7];
	int[] sStartMin = new int[7];
	int[] sEndMin = new int[7];
	
	public SecurityBean()
	{
		
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

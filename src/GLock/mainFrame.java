package GLock;

import java.awt.CardLayout;
import javax.swing.JFrame;

public class mainFrame extends JFrame {
	
	private CardLayout cards = new CardLayout();
	
    public mainFrame() {
    	this.setSize(480, 320); 
        getContentPane().setLayout(cards);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
         
//        getContentPane().add("One", new Login(this));
//        getContentPane().add("Two", new GLock(this));
        
        setVisible(true);
    }
     
    
    public void changePanel() {
        cards.next(this.getContentPane());
    }
    
    public CardLayout getCardLayout() {
    	 return cards;
    	}


}

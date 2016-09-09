package GLock;

import java.awt.CardLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;

public class mainFrame extends JFrame {
	
	private CardLayout cards = new CardLayout();
	private GLock glock = GLock.getInstance();
    public mainFrame() {
    	this.setSize(480, 320); 
        getContentPane().setLayout(cards);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
         
        getContentPane().add("One", new Login(this));
        getContentPane().add("Two", glock);
        
//        // full screening code
//        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//        GraphicsDevice gd = ge.getDefaultScreenDevice();
//        this.setUndecorated(true);
//        gd.setFullScreenWindow(this);
        
        setVisible(true);
    }
     
    
    public void changePanel() {
        cards.next(this.getContentPane());
    }
    
    public CardLayout getCardLayout() {
    	 return cards;
    	}


}

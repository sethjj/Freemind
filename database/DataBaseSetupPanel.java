/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freemind.database;

import freemind.database.actions.SpringUtilities;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

/**
 *
 * @author woo
 */
class DataBaseSetupPanel extends JDialog
        implements ActionListener,
        PropertyChangeListener {

    private JOptionPane optionPane;
    private String btnString1 = "Enter";
    private String btnString2 = "Cancel";
    private boolean cancelled = false;
    //
    private JTextField driverNameField  = new JTextField(50);
    private JTextField databaseUrlField  = new JTextField(50);
    private JTextField databaseUserIdField  = new JTextField(50);
    private JTextField databaseUserPasswordField  = new JTextField(50);
    //
    private String driverName = "";
    private String url = "";
    private String userId = "";
    private String passwd = "";
    //
    private JTextField[] fields = new JTextField[4];
    private String[] labels = {"Driver classname:","Database url:","Database userId:","Database password:"};
    private static final int TEXT_WIDTH = 20;
    private static final String[] driverNames = {"com.mysql.jdbc.Driver",
        "jdbc.odbc.JdbcOdbcDriver"};
    
    /** Creates the reusable dialog. */
    public DataBaseSetupPanel(Frame aFrame,
            String databaseClassname,
            String databaseUrl,
            String databaseUserId,
            String databasePassword) {
        super(aFrame, true);
        driverName = databaseClassname;
        url = databaseUrl;
        userId = databaseUserId;
        passwd = databasePassword;
        setPreferredSize(new Dimension(600, 250));
        setTitle("Database Setup");

        //Create an array of the text and components to be displayed.
        String msgString1 = "Enter database access parameters";
        JPanel optionPanel = createSetupPanel();
        //optionPanel.setPreferredSize(new Dimension(400, 700));
        Object[] array = {msgString1, optionPanel};

        //Create an array specifying the number of dialog buttons
        //and their text.
        Object[] options = {btnString1, btnString2};

        //Create the JOptionPane.
        optionPane = new JOptionPane(array,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION,
                null,
                options,
                options[0]);

        //Make this dialog display it.
        setContentPane(optionPane);

        //Handle window closing correctly.
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent we) {
                /*
                 * Instead of directly closing the window,
                 * we're going to change the JOptionPane's
                 * value property.
                 */
                optionPane.setValue(new Integer(
                        JOptionPane.CLOSED_OPTION));
            }
        });

        //Ensure the text field always gets the first focus.
        addComponentListener(new ComponentAdapter() {

            public void componentShown(ComponentEvent ce) {
                driverNameField.requestFocusInWindow();
            }
        });

        //Register an event handler that reacts to option pane state changes.
        optionPane.addPropertyChangeListener(this);
        pack();
    }

    /** This method handles events for the button choices field. */
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        pack();
    }

    /** This method reacts to state changes in the option pane. */
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();

        if (isVisible()
                && (e.getSource() == optionPane)
                && (JOptionPane.VALUE_PROPERTY.equals(prop)
                || JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
            Object value = optionPane.getValue();

            if (value == JOptionPane.UNINITIALIZED_VALUE) {
                //ignore reset
                return;
            }

            //Reset the JOptionPane's value.
            //If you don't do this, then if the user
            //presses the same button next time, no
            //property change event will be fired.
            optionPane.setValue(
                    JOptionPane.UNINITIALIZED_VALUE);

            if (btnString1.equals(value)) {
                driverName = this.driverNameField.getText();
                url = this.databaseUrlField.getText();
                userId = this.databaseUserIdField.getText();
                passwd = this.databaseUserPasswordField.getText();
                System.out.println("DriverName: "+driverName);
                setVisible(false);
            } else { //user closed dialog or clicked cancel
                clearAndHide();
            }
        }
    }

    /** This method clears the dialog and hides it. */
    public void clearAndHide() {
        cancelled = true;
        setVisible(false);
    }


    private static void createAndShowGUI() {
        //Create and set up the window.
        final JFrame frame = new JFrame("Test Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        DataBaseSetupPanel cop = new DataBaseSetupPanel(frame,"A","B","C","D");
        cop.setVisible(true);
        System.out.println("DriverName: "+cop.getDriverName());
        System.out.println("Url:        "+cop.getUrl());
        System.out.println("UserId:     "+cop.getUserId());
        System.out.println("Password:   "+cop.getPasswd());
        System.out.println("All Done!\n");
        System.exit(0);
        //Display the window.
     }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                createAndShowGUI();
            }
        });
    }

    public JPanel createSetupPanel() {
        JPanel setupPanel = new JPanel(); 
        setupPanel.setLayout(new SpringLayout());
        driverNameField = new JTextField(TEXT_WIDTH);
        databaseUrlField = new JTextField(TEXT_WIDTH);
        databaseUserIdField = new JTextField(TEXT_WIDTH);
        databaseUserPasswordField = new JTextField(TEXT_WIDTH);
        fields[0] = driverNameField;
        fields[1] = databaseUrlField;
        fields[2] = databaseUserIdField;
        fields[3] = databaseUserPasswordField;
        driverNameField.setText(driverName);
        databaseUrlField.setText(url);
        databaseUserIdField.setText(userId);
        databaseUserPasswordField.setText(passwd);
        for( int i=0; i<labels.length; i++ )    {
            setupPanel.add(new JLabel(labels[i], JLabel.TRAILING));
            setupPanel.add(fields[i]);
        }
        SpringUtilities.makeCompactGrid(setupPanel,
                labels.length, 2, // rows, cols
                6, 6,             // initX, initY
                6, 6);            // xPad, yPad
        return setupPanel;
    } 
    
    public String getDriverName()   {   return driverName;           }
    public String getUrl()          {   return url;          }
    public String getUserId()       {   return userId;       }
    public String getPasswd()       {   return passwd; }

    boolean cancelled() {
        return cancelled;
    }

}

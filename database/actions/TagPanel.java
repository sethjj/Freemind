/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freemind.database.actions;

import freemind.database.DataBaseReader;
import freemind.database.DataBaseTransactionException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author woo
 */
class TagPanel extends JDialog
        implements ActionListener,
        PropertyChangeListener {

    private JOptionPane optionPane;
    private String btnString1 = "Tag";
    private String btnString2 = "Cancel";
    private String mapName;
    private JFrame frame;
    private DataBaseReader dbr;
    private JCheckBox versionCheckBox = new JCheckBox("Version");
    private JCheckBox releaseCheckBox = new JCheckBox("Release");
    private JComboBox majorVersionCombo;
    private boolean newMajorVersion = false;
    private boolean newMajorRelease = false;
    private JLabel minorVersionLabel;
    private JComboBox majorReleaseCombo;
    private JLabel minorReleaseLabel;
    private JTextArea jta;
    private String dataBaseVersionNumber;
    private String dataBaseReleaseNumber;
    private final String userId;

    public TagPanel(JFrame aFrame, String mapName, DataBaseReader dbr, String userId) {
        super(aFrame, true);
        this.frame = (JFrame) aFrame;
        this.dbr = dbr;
        this.mapName = mapName;
        this.userId = userId;
        setPreferredSize(new Dimension(800, 400));
 
        setTitle("Tagging");


        //Create an array of the text and components to be displayed.
        String msgString1 = "Tag "+mapName;
        String msgString2 = "as:";
        JPanel versionPanel = createVersionPanel();
        JPanel releasePanel = createReleasePanel();
        JPanel commentPanel = createCommentPanel();
        //optionPanel.setPreferredSize(new Dimension(400, 700));
        Object[] array = {msgString1, msgString2, versionPanel, releasePanel, commentPanel};

        //Create an array specifying the number of dialog buttons
        //and their text.
        Object[] options = {btnString1, btnString2};

        //Create the JOptionPane.
        optionPane = new JOptionPane(array,
                JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.OK_OPTION,
                null,
                options,
                options[1]);

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
                //optionPane.setValue(new Integer(
                //        JOptionPane.CLOSED_OPTION));
            }
        });

        //Register an event handler that reacts to option pane state changes.
        optionPane.addPropertyChangeListener(this);
        pack();

    }

    @Override
    public void actionPerformed(ActionEvent ae) {
         pack();
    }

    @Override
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

            String versionStr = "";
            String releaseStr = "";
            if (btnString1.equals(value)) {
                if( !releaseCheckBox.isSelected() && !versionCheckBox.isSelected() )    {
                    JOptionPane.showMessageDialog(frame, "You have not selected which tag(s) to apply!");
                    return;
                }
                if( getComment() == null || getComment().length() == 0 )    {
                    JOptionPane.showMessageDialog(frame, "You must provide a comment!");
                    return;
                }
                System.out.println("Tag the sucker!");
                if( versionCheckBox.isSelected() )  {
                    versionStr = majorVersionCombo.getSelectedItem()
                            +"."+minorVersionLabel.getText();
                 }
                if( releaseCheckBox.isSelected() )  {
                    releaseStr = majorReleaseCombo.getSelectedItem()
                            +"."+minorReleaseLabel.getText();
                    
                }
                System.out.println("Comment: "+getComment());
                Connection c = null;
                String tagMsg = mapName +" successfully tagged as\n";
                try {
                    c = dbr.beginTransaction(mapName, userId); // update version and release ids if necessary
                    if( versionCheckBox.isSelected())   {
                    if( majorVersionCombo.getSelectedItem().equals(dataBaseVersionNumber) )
                    {
                        dbr.updateNextValueForMap(c, mapName, "nextMajorVersionId"); // auto updates minor
                    } else { // minor update only
                        dbr.updateNextValueForMap(c, mapName, "nextMinorVersionId");
                    }
                    } else  {
                        if( majorReleaseCombo.getSelectedItem().equals(dataBaseReleaseNumber) )
                        {
                            dbr.updateNextValueForMap(c, mapName, "nextMajorReleaseId"); // auto updates minor
                        } else  { // minor update only
                            dbr.updateNextValueForMap(c, mapName, "nextMinorReleaseId");  
                        }
                    }
                    if( versionStr != null && versionStr.length() > 0 )
                        tagMsg += "Version: "+versionStr;
                    if( releaseStr != null && releaseStr.length() > 0 ) {
                        if( tagMsg.contains("Version:") )
                            tagMsg += ", ";
                            tagMsg += "Release: "+releaseStr;
                    }
                    dbr.endTransaction(c, mapName, getComment(), "", versionStr, releaseStr, tagMsg);
                } catch( Exception ex ) {
                    try {
                        ex.printStackTrace();
                        dbr.rollBack(c);
                    } catch (DataBaseTransactionException ex1) {
                        Logger.getLogger(TagPanel.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
                System.out.println("Tagging completed successfully?");
                setVisible(false);
            } else if( btnString2.equals(value) ) {
                System.out.println("Cancel");
                setVisible(false);
            }
        }
    }

    private JPanel createVersionPanel() {
        JPanel panel = new JPanel();
        panel.add(versionCheckBox);
        Connection c = null;
        try {
            c = dbr.getConnection();
        } catch( Exception ex ) {
            ex.printStackTrace();
            return panel;
        }
        int currentVersion = dbr.getNextValueForMap(c, mapName, "nextMajorVersionId"); // 1
        dataBaseVersionNumber = ""+currentVersion--; // 1
        String current = ""+currentVersion;  // 0 for current version
        String[] majorNumbers = {current, dataBaseVersionNumber}; // from database and 1+ minor
        majorVersionCombo = new JComboBox(majorNumbers);
        majorVersionCombo.setSelectedIndex(0);
        panel.add(majorVersionCombo);
        panel.add(new JLabel("."));
        final String minorVersionNumber = ""+(dbr.getNextValueForMap(c, mapName, "nextMinorVersionId")-1); // 2
        minorVersionLabel = new JLabel(minorVersionNumber); // 1
        panel.add(minorVersionLabel);
        majorVersionCombo.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                if( majorVersionCombo.getSelectedIndex() != 0 ) {
                    newMajorVersion = true;
                    minorVersionLabel.setText("0"); 
                } else  {
                    minorVersionLabel.setText(minorVersionNumber);
                }
            }
        });
        return panel;
    }

    private JPanel createReleasePanel() {
        JPanel panel = new JPanel();
        panel.add(releaseCheckBox);
         Connection c = null;
        try {
            c = dbr.getConnection();
        } catch( Exception ex ) {
            ex.printStackTrace();
            return panel;
        }
        int currentRelease = dbr.getNextValueForMap(c, mapName, "nextMajorReleaseId"); // 2
        dataBaseReleaseNumber = ""+currentRelease--; // 2
        String current = ""+currentRelease; // 1
        String[] majorNumbers = {current, dataBaseReleaseNumber}; // from database and 1+ minor
        majorReleaseCombo = new JComboBox(majorNumbers);
        majorReleaseCombo.setSelectedIndex(0);
        panel.add(majorReleaseCombo);
        panel.add(new JLabel("."));
        final String minorReleaseNumber = ""+(dbr.getNextValueForMap(c, mapName, "nextMinorReleaseId")-1); // 1
         minorReleaseLabel = new JLabel(minorReleaseNumber); // 0
        panel.add(minorReleaseLabel);
        majorReleaseCombo.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                if( majorReleaseCombo.getSelectedIndex() != 0 ) {
                    newMajorRelease = true;
                    minorReleaseLabel.setText("0"); 
                } else  {
                    minorReleaseLabel.setText(minorReleaseNumber);
                }
            }
        });
        return panel;
    }
    
    public static void main(String[] args)  {
        TagPanel tp = new TagPanel(new JFrame("Test"), "output.mm", new DataBaseReader(null), "testUserId");
        tp.setVisible(true);
        
    }

        private JPanel createCommentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        jta = new JTextArea(5, 50);
        JScrollPane scrollPane = new JScrollPane(jta);
        panel.add(jta, BorderLayout.CENTER);
        panel.add(new JLabel("Comment"), BorderLayout.NORTH);
        return panel;
     }

    private String getComment() {
        return jta.getText();
    }
    
}



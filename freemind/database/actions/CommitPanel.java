/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freemind.database.actions;

import java.awt.Dimension;
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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author woo
 */
public class CommitPanel extends JDialog
        implements ActionListener, PropertyChangeListener {

    String mapName;
    final JOptionPane optionPane;
    String btnString1 = "Cancel";
    String btnString2 = "Commit";
    private JTextArea jta;
    private JTextField changeRequestField;
    private String comment = null;

    public CommitPanel(JFrame frame, String mapName) {
        super(frame, true);
        this.mapName = mapName;
        setPreferredSize(new Dimension(500, 300));

        setTitle("Archive Commit");

        //Create an array of the text and components to be displayed.
        String msgString2 = "Comment";
        String msgString3 = "Change Request Number";
        jta = new JTextArea(5, 50);
        JScrollPane scrollPane = new JScrollPane(jta);
        changeRequestField = new JTextField("");
        Object[] array = {new JLabel(mapName), msgString2, scrollPane,msgString3, changeRequestField};

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
                jta.requestFocusInWindow();
            }
        });

        //Register an event handler that reacts to option pane state changes.
        optionPane.addPropertyChangeListener(this);
        pack();

    }

    public boolean wasCancelled() {
        return (comment == null || comment.length() == 0);
    }

    public String getComment() {
         return comment;
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

            if (btnString2.equals(value)) {
                if (jta.getText() == null || jta.getText().length() == 0) {
                    JOptionPane.showMessageDialog(this, "You must enter a comment");
                    return;
                }
                comment = jta.getText();
                setVisible(false);
            } else { //user closed dialog or clicked cancel
                clearAndHide();
            }
        }
    }

    /** This method clears the dialog and hides it. */
    public void clearAndHide() {
        comment = null;
        setVisible(false);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        
    }

    String getChangeRequestNumber() {
        return changeRequestField.getText();
    }
}

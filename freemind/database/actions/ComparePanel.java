/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freemind.database.actions;

import freemind.database.ComboObject;
import freemind.database.DataBaseReader;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;

/**
 *
 * @author woo
 */
class ComparePanel extends JDialog
        implements ActionListener,
        PropertyChangeListener {

    private JOptionPane optionPane;
    private String btnString1 = "Enter";
    private String btnString2 = "Cancel";
    private String map;
    private String selectedDate;
    private final static String[] options = {"Current", "BaseLine", "Date", "Version", "Release"};
    JRadioButton currentButton;
    JRadioButton baseLineButton;
    JRadioButton byDateButton;
    JRadioButton byVersionButton;
    JRadioButton byReleaseButton;
    ComboObject current;
    ComboObject baseLine;
    JComboBox datesCombo;
    JComboBox versionCombo;
    JComboBox releaseCombo;
    JComboBox[] comboBoxes = new JComboBox[3];
    private String selectedOption = "Current";
    private ButtonGroup group;
    private JComponent selectionComponent = new JLabel();
    private JPanel checkoutPanel;
    private DataBaseReader dbr ;
    private JFrame frame;

    /** Creates the reusable dialog. */
    public ComparePanel(Frame aFrame, String map, DataBaseReader dbr) {
        super(aFrame, true);
        this.frame = (JFrame) aFrame;
        setPreferredSize(new Dimension(500, 400));
        this.map = map;
        this.dbr = dbr;

        setTitle("Archive Compare");


        //Create an array of the text and components to be displayed.
        String msgString2 = "Compare With";
        JPanel optionPanel = createSelectionPanel();
        //optionPanel.setPreferredSize(new Dimension(400, 700));
        Object[] array = {new JLabel(map), msgString2, optionPanel};

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
                currentButton.requestFocusInWindow();
            }
        });

        //Register an event handler that reacts to option pane state changes.
        optionPane.addPropertyChangeListener(this);
        pack();

    }

    /** This method handles events for the button choices field. */
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        checkoutPanel.remove(selectionComponent);
        if (command.equals(options[2])) {
            Object[] dates = dbr.getDatesForMap(getMap());
            datesCombo = new JComboBox(dates);
            comboBoxes[0] = datesCombo;
            selectionComponent = datesCombo;
        } else if (command.equals(options[3])) {
            Object[] versions = dbr.getVersionsForMap(getMap());
            versionCombo = new JComboBox(versions);
            comboBoxes[1] = versionCombo;
            selectionComponent = versionCombo;
        } else if (command.equals(options[4])) {
            Object[] releases = dbr.getReleasesForMap(getMap());
            releaseCombo = new JComboBox(releases);
            comboBoxes[2] = releaseCombo;
            selectionComponent = releaseCombo;
        } else {
            selectionComponent = new JLabel();
        }
        checkoutPanel.add(selectionComponent);
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
                System.out.println("selected was '" + getMap() + "'");
                int caseNum = 0;
                if (currentButton.isSelected()) {
                    selectedDate = current.getDateStr();
                    selectedOption = options[0];
                } else if (baseLineButton.isSelected()) {
                    selectedDate = baseLine.getDateStr();
                    selectedOption = options[1];
                } else if (byDateButton.isSelected()) {
                    selectedDate = ((ComboObject) comboBoxes[0].getSelectedItem()).getDateStr();
                    selectedOption = options[2];
                } else if (byVersionButton.isSelected()) {
                    selectedDate = ((ComboObject) comboBoxes[1].getSelectedItem()).getDateStr();
                    selectedOption = options[3];
                } else if (byReleaseButton.isSelected()) {
                    selectedDate = ((ComboObject) comboBoxes[2].getSelectedItem()).getDateStr();
                    selectedOption = options[4];
                }


                System.out.println("Archive type was '" + selectedOption + "' and the date was '" + getSelectedDate());
                setVisible(false);
            } else { //user closed dialog or clicked cancel
                selectedDate = null;
                selectedOption = null;
                clearAndHide();
            }
        }
    }

    /** This method clears the dialog and hides it. */
    public void clearAndHide() {
        setVisible(false);
    }

    String getMap() {
        return map;
    }

    public String getSelectedDate() {
        return selectedDate;
    }

    public JPanel createSelectionPanel() {
        checkoutPanel = new JPanel();
        GridLayout gl = new GridLayout(0, 1);
        checkoutPanel.setLayout(gl);
        JPanel aPanel = new JPanel(new SpringLayout());
        //In initialization code:
        //Create the radio buttons.
        currentButton = new JRadioButton("Current");
        currentButton.setActionCommand("Current");
        currentButton.setSelected(true);

        baseLineButton = new JRadioButton("BaseLine");
        baseLineButton.setActionCommand("BaseLine");

        byDateButton = new JRadioButton("Date");
        byDateButton.setActionCommand("Date");

        byVersionButton = new JRadioButton("Version");
        byVersionButton.setActionCommand("Version");

        byReleaseButton = new JRadioButton("Release");
        byReleaseButton.setActionCommand("Release");

        //Group the radio buttons.
        group = new ButtonGroup();
        group.add(currentButton);
        group.add(baseLineButton);
        group.add(byDateButton);
        group.add(byVersionButton);
        group.add(byReleaseButton);

        //Register a listener for the radio buttons.
        currentButton.addActionListener(this);
        baseLineButton.addActionListener(this);
        byDateButton.addActionListener(this);
        byReleaseButton.addActionListener(this);
        byVersionButton.addActionListener(this);
        // Get DataBaseReader

        aPanel.add(currentButton);
        current = dbr.getCurrentDateForMap(map);
        aPanel.add(new JLabel(current.getDateStr()));
        //
        aPanel.add(baseLineButton);
        baseLine = dbr.getBaseLineDateForMap(getMap());
        aPanel.add(new JLabel(baseLine.getDateStr()));
        //
        JPanel comboPanel = new JPanel();
        comboPanel.setLayout(new FlowLayout());
        //
        comboPanel.add(byDateButton);
        //
        comboPanel.add(byVersionButton);
        //
        comboPanel.add(byReleaseButton);
        SpringUtilities.makeCompactGrid(aPanel,
                2, 2,
                6, 6,
                6, 6);
        checkoutPanel.add(aPanel);
        checkoutPanel.add(comboPanel);
        checkoutPanel.add(selectionComponent);
        return checkoutPanel;
    }
}

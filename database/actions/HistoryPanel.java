/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freemind.database.actions;

import freemind.database.ComboObject;
import freemind.database.DataBaseReader;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

/**
 *
 * @author woo
 */
class HistoryPanel extends JDialog
        implements ActionListener,
        PropertyChangeListener {

    private JOptionPane optionPane;
    private JComboBox mapCombo;
    private String btnString1 = "Close";
    private String btnString2 = "Cancel";
    private String map;
    private JFrame frame;
    private DataBaseReader dbr;
    private JScrollPane tablePanel;
    private JPanel optionPanel;

    public HistoryPanel(JFrame aFrame, String[] maps, DataBaseReader dbr) {
        super(aFrame, true);
        this.frame = (JFrame) aFrame;
        this.dbr = dbr;
        //setPreferredSize(new Dimension(800, 400));
        mapCombo = new JComboBox(maps);
        mapCombo.addActionListener(this);

        setTitle("History");


        //Create an array of the text and components to be displayed.
        String msgString1 = "Select a Map";
        String msgString2 = "History";
        optionPanel = new JPanel();
        //optionPanel.setPreferredSize(new Dimension(400, 700));
        Object[] array = {msgString1, mapCombo, msgString2, optionPanel};

        //Create an array specifying the number of dialog buttons
        //and their text.
        Object[] options = {btnString1};

        //Create the JOptionPane.
        optionPane = new JOptionPane(array,
                JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.OK_OPTION,
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
        Object[] historyObject = dbr.getDatesForMap((String) mapCombo.getSelectedItem());
        if (tablePanel != null) {
            optionPanel.remove(tablePanel);
        }
        tablePanel = createHistoryTable(historyObject);
        optionPanel.add(tablePanel);
        tablePanel.setPreferredSize(new Dimension(750,350));
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


            if (btnString1.equals(value)) {
                setVisible(false);
            }
        }
    }
    private int[] colWidths = {150, 50, 75, 50, 50, 300};

    public JScrollPane createHistoryTable(Object[] items) {
        Object[][] data = new Object[items.length][6];
        for (int i = 0; i < items.length; i++) {
            ComboObject co = (ComboObject) items[i];
            data[i][0] = co.getDateStr();
            data[i][1] = co.getCommitter();
            data[i][2] = co.getChangeRequest();
            data[i][3] = co.getVersion();
            data[i][4] = co.getReleaseId();
            data[i][5] = co.getComment();
        }
        JTable table = new JTable(new MyTableModel(data));
        JTableHeader header = table.getTableHeader();
        header.setBackground(Color.yellow);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane scrollPane = new JScrollPane(table);
        //scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        adjustColWidths(table, colWidths);
        return scrollPane;
    }

    private void adjustColWidths(JTable table, int[] colWidths) {
        for( int vColIndex = 0; vColIndex < colWidths.length; vColIndex++ ) {
            TableColumn col = table.getColumnModel().getColumn(vColIndex);
            col.setPreferredWidth(colWidths[vColIndex]);
        }
    }
}

class MyTableModel extends AbstractTableModel {

    private String[] columnNames = {"Date", "Committer", "CR #", "Version", "Release", "Comment"};
    private Object[][] data;
    private Class[] classes = {String.class, String.class, String.class, String.class, String.class, String.class};

    public MyTableModel(Object[][] data) {
        super();
        this.data = data;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    public Class getColumnClass(int c) {
        return classes[c];
    }
  
    public static final String[] testMaps = {"1","2"};
 
    public static void main(String[] args)  {
        JFrame frame = new JFrame("Test Scrolling");
        JFrame hpFrame = new JFrame("History Panel frame");
        ComboObject[] objects = new ComboObject[5];
        HistoryPanel hp = new HistoryPanel(hpFrame, testMaps, null);
        for( int i=0; i<objects.length; i++ )   {
            objects[i] = new ComboObject("2100-10-31 hh:mm:ss", "Comment", "woo",
                    ""+i+"."+(objects.length-i), ""+(objects.length-i)+"."+i,"CR:00"+i);
        }
        JScrollPane panel = hp.createHistoryTable(objects);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
        //hp.setVisible(true);
    }
}

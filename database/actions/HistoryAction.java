/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freemind.database.actions;

import freemind.database.DataBaseReader;
import freemind.modes.mindmapmode.MindMapController;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;

/**
 * The CheckoutAction produces a panel to obtain which archived instance of a map to checkout.  It then
 * uses the DataBaseReader class to obtain the "map" from the nodes appropriate to the selected date.
 * Those nodes are converted into a proper xml formed map and used to create a new on-screen map.
 * The title of the screen version should be set to the name and date of the checkout.
 * 
 * @author woo
 * @date 110928
 * @version Checked into cvs under src/src unfortunately
 */
public class HistoryAction extends AbstractDatabaseAction {

    public HistoryAction(MindMapController controller) {
        super(controller.getText("history"));
        this.controller = controller;
        if( dbr == null )
            dbr = new DataBaseReader(controller);
    }

    public void actionPerformed(ActionEvent e) {

        String[] maps = dbr.getMapAreas();
        HistoryPanel hp = new HistoryPanel((JFrame) controller.getFrame(), maps, dbr);
        hp.setVisible(true);
        return;
    }
}

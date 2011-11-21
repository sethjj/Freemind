/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freemind.database.actions;

import freemind.database.DataBaseReader;
import freemind.database.DataBaseTransactionException;
import freemind.database.MajorMinorOrderedMap;
import freemind.modes.MindMap;
import freemind.modes.mindmapmode.MindMapController;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author woo
 */
public class CommitAction extends AbstractDatabaseAction {

    public CommitAction(MindMapController controller) {
        super(controller.getText("commit"));
        this.controller = controller;
        if (dbr == null) {
            dbr = new DataBaseReader(controller);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (!isValid()) {
            return;
        }
        Connection c = null;
        boolean commitNew = false;
        try {
            c = DataBaseReader.getConnection();
            int mapId = dbr.getMapId(c, controller.getMap().getMapName());
            if (mapId == -1) { // This map has never been committed.
                int retVal = JOptionPane.showConfirmDialog((JFrame) controller.getFrame(),
                        "The map with name '" + controller.getMap().getMapName() + "' has never been committed.\n"
                        + "Do you want to commit it?");
                if (retVal == JOptionPane.NO_OPTION || retVal == JOptionPane.CANCEL_OPTION) {
                    return;
                }
                String s = (String) JOptionPane.showInputDialog(
                        (JFrame) controller.getFrame(),
                        "Name of map to be committed",
                        "Name Dialog",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        controller.getMap().getMapName());
                if (s == null || s.length() == 0) {
                    return;
                }
                controller.getMap().setMapName(s);
                commitNew = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                c.close();
            } catch (SQLException ex) {
                Logger.getLogger(CommitAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        String checkedoutDateStr = "";
        if (!commitNew) {
            MindMap mm = controller.getMap();
            checkedoutDateStr = " with checkout date '" + mm.getRootNode().getAttribute("dateStr")
                    + "'.";
            if (checkedoutDateStr.contains("null")) {
                JOptionPane.showMessageDialog((JFrame) controller.getFrame(), "This map has not been checked out!\n"
                        + "Check out a map before editing and committing.");
                return;
            }
        }
        CommitPanel cp = new CommitPanel((JFrame) controller.getFrame(), controller.getMap().getMapName());
        cp.setVisible(true);
        if (cp.wasCancelled()) {
            return;
        }
        String comment = cp.getComment();
        String changeRequestNumber = cp.getChangeRequestNumber();
        System.out.println("The value of comment is '" + comment + "'");
        // TODO: Get nextMajorId for Map and use for root node if "-99"
        // Then go through memory nodes and replace each -99.whatever with
        // the getNextNodeIdForMap(map) and set it's parentId correctly also.
        DiffAction da = new DiffAction(controller);
        MajorMinorOrderedMap memoryNodes = dbr.updateIds(da.getMemoryMap(controller),
                controller.getMap().getMapName());
        da.commitNew = commitNew;
        da.computeChanges(memoryNodes, dbr, false);
        List newNodes = DiffAction.getNewNodes(memoryNodes);
        List editedNodes = DiffAction.getEditedNodes(memoryNodes, controller.getMap().getMapName(), dbr);
        List removedNodes = DiffAction.getFakeNodes(memoryNodes);

        if ((newNodes.size() == 0)
                && (editedNodes.size() == 0)
                && (removedNodes.size() == 0)) {
            int val = JOptionPane.showConfirmDialog((JFrame) controller.getFrame(), "There are no changes.\n"
                    + "Do you still want to commit?");
            if (val == JOptionPane.NO_OPTION || val == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        System.out.println("We are going to commit map '" + controller.getMap().getMapName() + "'"
                + checkedoutDateStr);
        // Mark removed nodes as deleted
        try {
            c = dbr.beginTransaction(controller.getMap().getMapName(), userId);
            if (removedNodes.size() > 0) {
                dbr.markAsDeleted(c, removedNodes, controller.getMap().getMapName());
            }
            if (editedNodes.size() > 0) {
                dbr.updateEditedNodes(c, editedNodes, controller.getMap().getMapName());
            }
            if (newNodes.size() > 0) {
                dbr.addNewNodes(c, newNodes, controller.getMap().getMapName());
            }
            dbr.endTransaction(c, controller.getMap().getMapName(), comment, changeRequestNumber, "", "", "");
        } catch (Exception ex) {
            try {
                dbr.rollBack(c);
            } catch (DataBaseTransactionException ex1) {
                Logger.getLogger(CommitAction.class.getName()).log(Level.SEVERE, ex1.getMessage());
            }
        }

        // TODO: Remember to refresh screen and put up committed message

    }
}

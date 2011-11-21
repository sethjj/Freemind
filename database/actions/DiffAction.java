/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freemind.database.actions;

import freemind.database.ComboObject;
import freemind.database.TreeNode;
import freemind.database.DataBaseReader;
import freemind.database.MajorMinorOrderedMap;
import freemind.modes.ControllerAdapter;
import freemind.modes.MindMapNode;
import freemind.modes.attributes.NodeAttributeTableModel;
import freemind.modes.mindmapmode.MindMapController;
import freemind.modes.mindmapmode.MindMapNodeModel;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author woo
 */
public class DiffAction extends AbstractDatabaseAction {

    public static String updateAttributes(MindMapNode node, String parentIdStr) {
        node.createAttributeTableModel();
        NodeAttributeTableModel attr = node.getAttributes();
        int rowCount = attr.getRowCount();
        for (int i = rowCount - 1; i >= 0; i--) {
            attr.removeRow(i);
        }
        String idStr = DataBaseReader.getTempId();
        attr.insertRow(0, "id", idStr);
        attr.insertRow(1, "parentId", parentIdStr);
        return idStr;
    }
    public boolean commitNew = false;

    public DiffAction(MindMapController controller) {
        super(controller.getText("diff"));
        this.controller = controller;
        if (dbr == null) {
            dbr = new DataBaseReader(controller);
        }
    }

    /**
     * Read in the tree from the database and compare it to the tree in memory
     * @param e 
     */
    public void actionPerformed(ActionEvent e) {
        MajorMinorOrderedMap memoryNodes = getMemoryMap(controller);
        computeChanges(memoryNodes, dbr, true);
    }

    public void computeChanges(MajorMinorOrderedMap memoryNodes, DataBaseReader dbr, boolean showPanel) {
        int newNodes = 0;
        int changedNodes = 0;
        int removedNodes = 0;

        // Get the list of nodes from the reader
        // Get the list of nodes from the map in memory
        // do a compare by node ids.
        // Mark new, deleted, and edited in database list.
        // dbr.printTree();
        //  Test to see if I can put something into display frame
        // TODO the date aDate is obtained from a panel like checkout panel determining what date
        // they are diffing with!
        String mapName = controller.getMap().getMapName();
        int loc = mapName.indexOf(":");
        if (loc != -1) {
            mapName = mapName.substring(0, loc);
        }
        System.out.println("Check if mapName '" + mapName + "' exists");
        ComboObject currentDateObject = dbr.getCurrentDateForMap(mapName);
        if (currentDateObject == null && !commitNew) {
            JOptionPane.showMessageDialog((JFrame) controller.getFrame(), "The map '" + mapName + "' has not been committed\n"
                    + "and cannot be compared with this name.\n"
                    + "It must be committed before comparison can be done.");
            /*String s = (String)JOptionPane.showInputDialog(
            (JFrame)controller.getFrame(),
            "Compare "+mapName+" to:",
            "Name Dialog",
            JOptionPane.PLAIN_MESSAGE,
            null,
            null,
            controller.getMap().getMapName());
            if( s == null || s.length() == 0 )
            return;
            mapName = s;
             */
            JOptionPane.showMessageDialog((JFrame) controller.getFrame(), "It might be possible to compare it to another map\n by renaming it to another maps name!");
            return;
        }
        String selectedDate = null;
        if (currentDateObject != null) {
            selectedDate = currentDateObject.getDateStr();
        } else {
            selectedDate = dbr.getCurrentTime();
        }
        if (showPanel && (currentDateObject != null)) {
            ComparePanel cop = new ComparePanel((JFrame) controller.getFrame(), mapName, dbr);
            cop.setVisible(true);
            selectedDate = cop.getSelectedDate();
            if (selectedDate == null) {
                return;
            }
            // Create temp file.
        }
        if (!mapExists()) {
            System.out.println("The map " + controller.getMap().getMapName() + " does not exist in the database!");
        }
        System.out.println("Selected Date is for comparison is '" + selectedDate + "'");
        MajorMinorOrderedMap dataBaseNodes = dbr.getNodes(controller.getMap().getMapName(), selectedDate);
        System.out.println("Diff called.  There are " + dataBaseNodes.size() + " entries in database for "
                + controller.getMap().getMapName());
        System.out.println("\t and there are " + memoryNodes.size() + " entries in memory.");
        removeFakeNodes(memoryNodes, controller);

        Iterator it = memoryNodes.iterator();

        while (it.hasNext()) {
            String key = (String) it.next();
            MindMapNode mmtn = (MindMapNode) memoryNodes.get(key);
            TreeNode tn = (TreeNode) dataBaseNodes.get(key);
            if (tn == null) { // In memory, not in database, so it's new
                mmtn.setBackgroundColor(Color.green);
                ((ControllerAdapter) controller).nodeChanged(mmtn);
                System.out.println("Marked " + mmtn + " as green");
                newNodes++;
            } else if (!nodesEqual(tn, mmtn)) {
                mmtn.setBackgroundColor(Color.yellow);
                ((ControllerAdapter) controller).nodeChanged(mmtn);
                System.out.println("Marked " + mmtn + " as yellow");
                changedNodes++;
            } else {
                mmtn.setBackgroundColor(Color.white);
                ((ControllerAdapter) controller).nodeChanged(mmtn);
            }
        }
        it = dataBaseNodes.iterator();
        while (it.hasNext()) { // looping through keys in database
            String key = (String) it.next();
            MindMapNode mmn = (MindMapNode) memoryNodes.get(key);
            if (mmn == null) { // a node in the database isn't in memory
                System.out.println("*** The node in the database " + key + " has been deleted in memory!");
                // This should then create a node marked deleted and put in place on the map in red
                // get the node's parentid, look up the parent and add it as a node in the map
                TreeNode tn = (TreeNode) dataBaseNodes.get(key);
                // mmn = new MindMapNodeModel(controller.getFrame(), controller.getMap());
                // mmn.setText(tn.getLabel());

                // mmn.setBackgroundColor(Color.red);
                // Map attributes = tn.getAttributes();

                // Now find the omitted nodes parent
                String losersParentId = tn.getAttribute("parentId");
                MindMapNode hisParent = getParentWithId(memoryNodes, losersParentId);
                if (hisParent != null) {
                    //NewChildAction nca = new NewChildAction(controller);
                    //MindMapNode newChild = nca.addNewNode(hisParent, 0);
                    MindMapNode newChild = controller.addNewNode(hisParent, 0, true);
                    newChild.setBackgroundColor(Color.red);
                    removedNodes++;
                    newChild.setText(tn.getLabel());
                    newChild.createAttributeTableModel();
                    NodeAttributeTableModel attr = newChild.getAttributes();
                    int rowCount = attr.getRowCount();
                    for (int i = rowCount - 1; i >= 0; i--) {
                        attr.removeRow(i);
                    }
                    System.out.println("Rows in " + tn.getLabel() + "= " + attr.getRowCount());
                    Map ats = tn.getAttributes();
                    int k = 0;
                    Iterator jt = ats.keySet().iterator();
                    String idValue = null;
                    while (jt.hasNext()) {
                        String theKey = (String) jt.next();
                        String value = (String) ats.get(theKey);
                        attr.insertRow(k++, theKey, value);
                        if (theKey.equals("id")) {
                            idValue = value;
                        }
                    }
                    memoryNodes.put(idValue, newChild);
                    ((ControllerAdapter) controller).nodeChanged(newChild);
                }
                //  New child needs database versions attributes ***TODO***

            }
        }
        System.out.println("Summary: \n\tNew Nodes: " + newNodes + "\n\tChanged Nodes: " + changedNodes
                + "\n\tRemoved Nodes: " + removedNodes);
    }

    public static void removeFakeNodes(MajorMinorOrderedMap memoryNodes, MindMapController controller) {
        List fakeNodes = getFakeNodes(memoryNodes);
        int kount = fakeNodes.size();
        for (int i = 0; i < kount; i++) {
            MindMapNode mmtn = (MindMapNode) fakeNodes.get(i);
            controller.deleteNode(mmtn);
        }
    }

    public static List getFakeNodes(MajorMinorOrderedMap memoryNodes) {
        Iterator it = memoryNodes.iterator();
        // First remove any "fake nodes placed there earlier to show removed nodes"
        List fakeNodes = new ArrayList();
        while (it.hasNext()) {
            String key = (String) it.next();
            MindMapNode mmtn = (MindMapNode) memoryNodes.get(key);
            if (mmtn.getBackgroundColor() != null && mmtn.getBackgroundColor().equals(Color.red)) {
                fakeNodes.add(mmtn);
            }
        }
        return fakeNodes;
    }

    public static List getNewNodes(MajorMinorOrderedMap memoryNodes) {
        Iterator it = memoryNodes.iterator();
        // First remove any "fake nodes placed there earlier to show removed nodes"
        List newNodes = new ArrayList();
        while (it.hasNext()) {
            String key = (String) it.next();
            MindMapNode mmtn = (MindMapNode) memoryNodes.get(key);
            if (mmtn.getBackgroundColor() != null && mmtn.getBackgroundColor().equals(Color.green)) {
                newNodes.add(mmtn);
            }
        }
        return newNodes;
    }

    public static List getEditedNodes(MajorMinorOrderedMap memoryNodes, String mapName, DataBaseReader dbr) {
        Iterator it = memoryNodes.iterator();
        // First remove any "fake nodes placed there earlier to show removed nodes"
        List editedNodes = new ArrayList();
        while (it.hasNext()) {
            String key = (String) it.next();
            MindMapNode mmtn = (MindMapNode) memoryNodes.get(key);
            if (mmtn.getBackgroundColor() != null && mmtn.getBackgroundColor().equals(Color.yellow)) {
                String idStr = mmtn.getAttribute("id");
                TreeNode tn = dbr.getNode(idStr, mapName);
                if (tn != null) {
                    editedNodes.add(mmtn);
                }
            }
        }
        return editedNodes;
    }

    private MindMapNode getParentWithId(MajorMinorOrderedMap memoryNodes, String losersParentId) {
        Iterator it = memoryNodes.iterator();
        while (it.hasNext()) {
            Object key = it.next();
            MindMapNode mmn = (MindMapNode) memoryNodes.get(key);
            String id = mmn.getAttribute("id");
            if (losersParentId.equals(id)) {
                return mmn;
            }
        }
        return null;
    }

    public static MajorMinorOrderedMap getMemoryMap(MindMapController controller) {
        // TODO This needs to get the id and version and put them into
        // the node attributes as it traverses the tree for new nodes.
        MajorMinorOrderedMap mm = new MajorMinorOrderedMap();
        MindMapNode root = controller.getRootNode();
        String idStr = root.getAttribute("id");
        if (idStr == null || idStr.length() == 0) {
            idStr = updateAttributes(root, "-1");
        }
        mm.put(idStr, root);
        mm = traverse(root, mm);
        return mm;
    }

    private static MajorMinorOrderedMap traverse(MindMapNode parentNode, MajorMinorOrderedMap aMap) {
        if (parentNode == null) {
            return aMap;
        }
        String parentIdStr = parentNode.getAttribute("id");
        List childNodes = parentNode.getChildren();
        Iterator it = childNodes.iterator();
        while (it.hasNext()) {
            MindMapNode node = (MindMapNode) it.next();
            String idStr = node.getAttribute("id");
            if (idStr == null || idStr.length() == 0) {
                idStr = updateAttributes(node, parentIdStr);
            }
            aMap.put(idStr, node);
            //System.out.println(idStr+", "+node);
            aMap = traverse(node, aMap);
        }
        return aMap;
    }

    private boolean nodesEqual(TreeNode tn, MindMapNode mmtn) {
        String parentIdStr = "" + tn.getParentId();
        // if changed parent
        if (!(parentIdStr.equals(mmtn.getAttribute("parentId")))) {
            System.out.println("No match parentIdStr: " + parentIdStr + " on " + tn.getId() + ", mmtn: " + mmtn.getAttribute("parentId") + " on " + mmtn.getAttribute("id"));
            return false;
        }
        // if changed label
        int loc = mmtn.getText().indexOf(":");
        if (loc != -1) { // was already modified for display
            mmtn.setText(mmtn.getText().substring(0, loc));
        }
        if (!(tn.getLabel().trim().equals(mmtn.getText().trim()))) {
            System.out.println("label tn " + tn.getLabel() + " on " + tn.getId() + " doesn't match " + mmtn.getText() + " on " + mmtn.getAttribute("id"));
            mmtn.setText(mmtn.getText() + ": was " + tn.getLabel());
            return false;
        }
        return true;
    }

    public MindMapNodeModel renumberNodes(MindMapNodeModel node, MindMapNode parent) {
        System.out.println("{renumberNodes} - node's parent is - " + parent);
        boolean exists = mapContains(node, parent);
        System.out.println("{renumberNodes} - mapContains(" + node + "," + parent + ") = " + exists);
        if (exists && isCopy(node)) {
            Map replaceMap = new HashMap();
            String id = node.getAttribute("id");
            updateAttributes(node, parent.getAttribute("id"));
            replaceMap.put(id, node.getAttribute("id"));
            ListIterator li = node.childrenUnfolded();
            while (li.hasNext()) {
                MindMapNode mmn = (MindMapNode) li.next();
                String oldId = mmn.getAttribute("id");
                String parentId = (String) replaceMap.get(mmn.getAttribute("parentId"));
                updateAttributes(mmn, parentId);
                replaceMap.put(oldId, mmn.getAttribute("id"));
            }
        }
        return node;
    }

    private boolean mapContains(MindMapNode node, MindMapNode parent) {
        MajorMinorOrderedMap mm = getMemoryMap(controller);
        String parentId = parent.getAttribute("id");
        String id = node.getAttribute("id");
        Iterator keyIterator = mm.iterator();
        while (keyIterator.hasNext()) {
            Object key = keyIterator.next();
            MindMapNode aNode = (MindMapNode) mm.get(key);
            String nodeId = aNode.getAttribute("id");
            if (nodeId.equals(id) && !aNode.getParentNode().getAttribute("id").equals("parentId")) {
                System.out.println("existing parent of found node = " + aNode.getParentNode());
                return true;
            }
        }
        return false;
    }

    private boolean isCopy(MindMapNodeModel node) {
        String text = node.getText();
        if( text.endsWith(":copied") )  {
            int loc = text.indexOf(":copied");
            node.setText(text.substring(0,loc));
            return true;
        }
        return false;
    }
}

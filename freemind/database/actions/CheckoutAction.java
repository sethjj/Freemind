/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freemind.database.actions;

import freemind.database.DataBaseReader;
import freemind.modes.ControllerAdapter;
import freemind.modes.MapAdapter;
import freemind.modes.MindMap;
import freemind.modes.attributes.Attribute;
import freemind.modes.attributes.AttributeTableLayoutModel;
import freemind.modes.attributes.NodeAttributeTableModel;
import freemind.modes.mindmapmode.MindMapController;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
public class CheckoutAction extends AbstractDatabaseAction {
    
public CheckoutAction(MindMapController controller) {
        super(controller.getText("checkout"));
        this.controller = controller;
         if( dbr == null )
            dbr = new DataBaseReader(controller);
   }

    public void actionPerformed(ActionEvent e) {
        File temp = null;
        
        MindMapController newController = new MindMapController(controller.getModeController().getMode());
        ControllerAdapter newControllerAdapter = (ControllerAdapter) newController;
        try {
            String[] maps = dbr.getMapAreas();
            CheckOutPanel cop = new CheckOutPanel((JFrame)controller.getFrame(), maps, dbr);
            cop.setVisible(true);
            String map = cop.getMap();
            String selectedDate = cop.getSelectedDate();
            if( selectedDate == null )
                return;
            System.out.println("Selected Date to checkout map "+map+" is '"+selectedDate+"'");
            File lastCurrentDir = controller.getLastCurrentDir();
            if( lastCurrentDir == null )    {
                lastCurrentDir = new File(System.getProperty("user.home","."));
            }
             // Create temp file.
            temp = File.createTempFile(map+":", ".mm");

            // Delete temp file when program exits.
            temp.deleteOnExit();
           if (map != null && map.length() > 0) {
                // Write to temp file
                BufferedWriter out = new BufferedWriter(new FileWriter(temp));
                out.write(dbr.getXML(map, selectedDate));
                out.close();
                MapAdapter newModel = newController.newModel(newControllerAdapter);
                newModel.load(temp);
                newControllerAdapter.newMap(newModel);
                MindMap mm = newControllerAdapter.getMap();
                // store the date of the map checked out, indicating this is checked out
                if( mm.getRootNode().getAttributeTableLength() < 4 )    {
                    NodeAttributeTableModel at = mm.getRootNode().getAttributes();
                    at.addRowNoUndo(new Attribute("dateStr", selectedDate));
                }
                else
                    mm.getRootNode().setAttribute(4, new Attribute("dateStr", selectedDate));
                mm.setMapName(map);
                mm.getRegistry().getAttributes().setAttributeViewType(AttributeTableLayoutModel.HIDE_ALL);
               newController.getFrame().setTitle(cop.getMap());
               controller.setLastCurrentDir(lastCurrentDir);
               System.out.print("lastCurrentDir should be set to: "+lastCurrentDir.getPath()+"\n");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

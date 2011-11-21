/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freemind.database.actions;

import freemind.database.DataBaseReader;
import freemind.modes.mindmapmode.MindMapController;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author woo
 */
class GenerateAction extends AbstractDatabaseAction {

    public GenerateAction(MindMapController controller) {
        super(controller.getText("generate"));
        this.controller = controller;
         if( dbr == null )
            dbr = new DataBaseReader(controller);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        JOptionPane.showMessageDialog((JFrame)controller.getFrame(),"Not yet implemented.");
    }
    
}

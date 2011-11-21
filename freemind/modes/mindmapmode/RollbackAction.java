/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freemind.modes.mindmapmode;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;

/**
 *
 * @author woo
 */
class RollbackAction implements Action {

    public RollbackAction(MindMapController aThis) {
        System.out.println("Hello World!  We're going to roll back to previous version in database!");
    }

    @Override
    public Object getValue(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void putValue(String string, Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setEnabled(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isEnabled() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}

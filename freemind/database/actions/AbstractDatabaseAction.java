/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freemind.database.actions;

import java.sql.Connection;
import freemind.database.DataBaseReader;
import freemind.modes.mindmapmode.MindMapController;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 *
 * @author woo
 */
public abstract class AbstractDatabaseAction extends AbstractAction {

    public JMenuItem menuItem;
    protected DataBaseReader dbr = null; // sets a date/time for this session
    protected static boolean isValid = false;
    private static String password;
    protected static String userId;
    protected MindMapController controller = null;
  
    public AbstractDatabaseAction(String description)   {
        super(description);
    }
    public void setEnabled(boolean val) {
        menuItem.setEnabled(val);
    }
    
    public boolean isEnabled()  {
        return menuItem.isEnabled();
    }
    
    public boolean mapExists()  {
        Connection c = null;
        try {
            c = dbr.getConnection();
            return (dbr.getMapId(c, controller.getMap().getMapName()) != -1 );
        } catch( Exception ex ) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Could not get mapId", ex);
        } finally   {
            try {
                c.close();
            } catch (SQLException ex) {
                Logger.getLogger(AbstractDatabaseAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    public boolean isValid()    {
        userId = System.getProperty("user.name");
        if( !isValid )  {
                password = (String)JOptionPane.showInputDialog(
                    (JFrame)controller.getFrame(),
                    "Enter password for "+userId,
                    "Password Dialog",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "");
                if( password == null || password.length() == 0 )
                    return false;
        } 
        if( !dbr.isValidCommitter(userId, password))  {
           JOptionPane.showMessageDialog((JFrame)controller.getFrame(), "User '"+userId+"' is not authorized to tag");
           password = "";
           return false;
        }
      isValid = true;
      return true;
    }

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freemind.database.actions;

import freemind.database.DataBaseReader;
import freemind.modes.ModeController;
import freemind.modes.mindmapmode.MindMapController;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author woo
 */
public class ArchiveMenuExtender {

    private JMenuBar menuBar;
    private boolean visible = false;

    public ArchiveMenuExtender(JMenuBar menuBar) {
        super();
        this.menuBar = menuBar;
        try {
            visible = DataBaseReader.hasDatabase();
        } catch (Throwable t) {
            Logger.getLogger(ArchiveMenuExtender.class.getName()).log(Level.WARN, "No datbase available");
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public JMenu createArchiveMenu(ModeController modeController) {
        //archiveMenu = new JMenu("Archive"); 
        //JMenuItem checkout = new JMenuItem("Checkout...");
        //ActionListener mal = new MyActionListener(newModeController);
        //checkout.addActionListener(mal);
        //JMenuItem commit = new JMenuItem("Commit...");
        //menuB.add(checkout);
        //menuB.add(commit); 
        //archiveMenu = menuHolder.addMenu(new JMenu("Archive"), ARCHIVE_MENU+"."); 
        //menuHolder.addCategory(ARCHIVE_MENU+"checkout");
        //menuHolder.addCategory(ARCHIVE_MENU+"commit");
        //menuHolder.addMenu(archiveMenu,ARCHIVE_MENU+".");

        JMenu archiveMenu = new JMenu("Archive");
        JMenuItem co = new JMenuItem("Checkout...");
        co.addActionListener(new CheckoutAction((MindMapController) modeController));
        archiveMenu.add(co);
        JMenuItem diff = new JMenuItem("Compare...");
        diff.addActionListener(new DiffAction((MindMapController) modeController));
        archiveMenu.add(diff);
        JMenuItem history = new JMenuItem("History");
        history.addActionListener(new HistoryAction((MindMapController) modeController));
        archiveMenu.add(history);
        boolean isValid = DataBaseReader.isValidUser(System.getProperty("user.name"));
        if ( isValid ) {
            archiveMenu.add(new JSeparator());
            JMenuItem commit = new JMenuItem("Commit...");
            commit.addActionListener(new CommitAction((MindMapController) modeController));
            archiveMenu.add(commit);
            JMenuItem tag = new JMenuItem("Tag...");
            tag.addActionListener(new TagAction((MindMapController) modeController));
            archiveMenu.add(tag);
            JMenuItem generate = new JMenuItem("Generate...");
            generate.addActionListener(new GenerateAction((MindMapController) modeController));
            archiveMenu.add(generate);
        }
        return archiveMenu;
    }
}

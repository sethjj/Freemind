/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freemind.database;

import com.mysql.jdbc.Driver;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import freemind.database.actions.DiffAction;
import freemind.main.FeedBack;
import freemind.main.FreeMind;
import freemind.main.FreeMindCommon;
import freemind.main.FreeMindSplashModern;
import freemind.main.FreeMindStarter;
import freemind.main.IFreeMindSplash;
import freemind.main.Tools;
import freemind.modes.ControllerAdapter;
import freemind.modes.MapAdapter;
import freemind.modes.MindMap;
import freemind.modes.MindMapNode;
import freemind.modes.ModeController;
import freemind.modes.attributes.Attribute;
import freemind.modes.attributes.AttributeTableLayoutModel;
import freemind.modes.mindmapmode.MindMapController;
import freemind.view.mindmapview.NodeView;
import java.awt.Color;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
//import javax.mail.internet.MimeUtility;

/**
 *
 * 
 * @author woo
 */
public class DataBaseReader {

    static final String JAVA_VERSION = System.getProperty("java.version");
    private static int tempId = 1;
    private static String driverName = "com.mysql.jdbc.Driver";
    private static String url = "jdbc:mysql://localhost:3306/mm_test";
    private static String dataBaseUserId = "root";
    private static String passwd = "root";
    private static File autoPropertiesFile;

    public static boolean checkConnection() throws SQLException {
        Connection c = null;
        boolean retVal = false;
        try {
            c = getConnection();
            if (c != null) {
                retVal = true;
            }
        } catch (Exception e) {
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return retVal;
    }

    /**
     * parse out the Major and replace it witn Minor
     * @param nextId
     * @return 
     */
    private static String getMinorIdName(String nextId) {
        StringBuffer retStr = new StringBuffer();
        int loc = nextId.indexOf("Major");
        retStr.append(nextId.substring(0, loc));
        retStr.append("Minor");
        retStr.append(nextId.substring(loc + 5));
        return retStr.toString();
    }
    protected static MindMapController controller;
    private String userId;

    /**
     * getNextValueForMap is responsible for getting the nextMajorVersion, minorVersion, MajorRelease, and Minor release
     * ids for a given map
     * @param c
     * @param mapName
     * @param nextId
     * @return 
     */
    public int getNextValueForMap(Connection c, String mapName, String nextId) {
        int retVal = -1;
        try {
            String sqlStatement = "SELECT " + nextId + " FROM maps where mapName = \"" + mapName + "\"";
            PreparedStatement pps = c.prepareStatement(sqlStatement);
            ResultSet rsl = pps.executeQuery();
            System.out.println("{getNextValueForMap} - " + pps.toString());
            while (rsl.next()) {
                retVal = rsl.getInt(1);
            }
            if (retVal == -1) { // entry is not in constants table
                System.out.println("WARNING, " + mapName + " is not in maps table");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("{getNextValueForMap} - retVal = " + retVal);
        return retVal;
    }

    public void testGettingIds() {
        Connection c = null;
        try {
            c = DataBaseReader.getConnection();


            testSettingNextId(c, "Minor", "Version", "test_short.mm"); // should become 1           
            int nextMajorVersionId = testGettingNextId(c, "Major", "Version", "test_short.mm"); // get a 2
            int nextMajorReleaseId = testGettingNextId(c, "Major", "Release", "test_short.mm");
            int nextMinorVersionId = testGettingNextId(c, "Minor", "Version", "test_short.mm");
            int nextMinorReleaseId = testGettingNextId(c, "Minor", "Release", "test_short.mm");
            System.out.println("(" + nextMajorVersionId + "," + nextMinorVersionId + ","
                    + nextMajorReleaseId + "," + nextMinorReleaseId + ")");
            testSettingNextId(c, "Major", "Version", "test_short.mm"); // should become 3
            if (testGettingNextId(c, "Major", "Version", "test_short.mm") != (nextMajorVersionId + 1)) {
                System.out.println("FAILURE TO CORRECTLY UPDATE ID!");
            }
            System.out.println("(" + testGettingNextId(c, "Major", "Version", "test_short.mm") + "," + testGettingNextId(c, "Minor", "Version", "test_short.mm") + ","
                    + nextMajorReleaseId + "," + nextMinorReleaseId + ")");

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                c.close();
            } catch (SQLException ex) {
                Logger.getLogger(DataBaseReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public int updateNextValueForMap(Connection c, String mapName, String nextId) {
        int retVal = -1;
        try {
            int val = getNextValueForMap(c, mapName, nextId);
            String minorResetString = "";
            if (nextId.contains("Major")) { // if resetting major id, set minor to 0
                minorResetString = getMinorIdName(nextId); // says nextMinorId
                minorResetString = ", " + minorResetString + "= 1 ";
            }
            String sqlStatement = "UPDATE maps set " + nextId + " = ? " + minorResetString + " where mapName = ?";
            PreparedStatement pps = c.prepareStatement(sqlStatement);
            retVal = ++val;
            pps.setInt(1, retVal); // this is the next value to be used.
            pps.setString(2, mapName);
            pps.executeUpdate();
            System.out.println("{updateNextValueForMap} - " + pps.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("{updateNextValueForMap} - retVal = " + retVal);
        return retVal;
    }

    private static int fetchAndIncrement(Connection c, String name) {
        int retVal = -1;
        try {
            String sqlStatement = "SELECT value FROM constants where name = \"" + name + "\"";
            PreparedStatement pps = c.prepareStatement(sqlStatement);
            ResultSet rsl = pps.executeQuery();
            while (rsl.next()) {
                retVal = rsl.getInt(1);
            }
            if (retVal == -1) { // entry is not in constants table
                System.out.println("WARNING, " + name + " is not in constants table");

                // add the textStr to the dataDictionary with the nextLabelId, then increment that.
                String sql = "insert into constants (name, value)"
                        + " values (\"" + name + "\", 1 )";
                System.out.println("sql for fetchAndIncrement is '" + sql + "'");
                PreparedStatement stmt = c.prepareStatement(sql);
                //
                stmt.executeUpdate();
                retVal = 1;
            } else { // update the value already there and just retrieved
                String sql = "update constants set value = ? where name = ?";
                PreparedStatement stmt = c.prepareStatement(sql);
                stmt.setInt(1, retVal + 1);
                stmt.setString(2, name);
                //
                stmt.executeUpdate();

            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return retVal;
        }
        return retVal;
    }

    public static String getTempId() {
        return "-99." + tempId++;
    }

    public boolean isValidCommitter(String userId, String password) {
        String sql = "SELECT last_login, time(last_login), roles from users where userId = \"" + userId + "\""
                + " and password = \"" + password + "\"";
        Connection c = null;
        String retVal = null;
        String lastTime = null;
        try {
            c = getConnection();
            ResultSet rs = executeSql(c, sql);
            while (rs.next()) {
                try {
                    java.sql.Date date = rs.getDate(1);
                    java.sql.Time time = rs.getTime(2);
                    if (date != null && time != null) {
                        lastTime = date.toString() + " " + time.toString();
                    }
                } catch (Exception dateValueEx) {
                    lastTime = "NEVER";
                }
                retVal = rs.getString(3);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                c.close();
            } catch (SQLException ex) {
                Logger.getLogger(DataBaseReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        boolean val = (retVal != null && (retVal.equals("admin") || retVal.equals("committer")));
        if (val) {
            System.out.println("Last login time of '" + userId + "' was " + lastTime);
        }
        return val;
    }

        public static boolean isValidUser(String userId) {
        String sql = "SELECT last_login, time(last_login), roles from users where userId = \"" + userId + "\"";
        Connection c = null;
        String retVal = null;
        String lastTime = null;
        try {
            c = getConnection();
            PreparedStatement pps = c.prepareStatement(sql);
            ResultSet rs = pps.executeQuery();
            while (rs.next()) {
                try {
                    java.sql.Date date = rs.getDate(1);
                    java.sql.Time time = rs.getTime(2);
                    if (date != null && time != null) {
                        lastTime = date.toString() + " " + time.toString();
                    }
                } catch (Exception dateValueEx) {
                    lastTime = "NEVER";
                }
                retVal = rs.getString(3);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                c.close();
            } catch (SQLException ex) {
                Logger.getLogger(DataBaseReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        boolean val = (retVal != null && (retVal.equals("admin") || retVal.equals("committer")));
        if (val) {
            System.out.println("Last login time of '" + userId + "' was " + lastTime);
        }
        return val;
    }

    /**
     * New nodes should have a leading -99.  These need to be replaced with
     * new node ids and where they are in the parentIds also, then they need to be replaced
     * with proper renumbered parentId.
     * @param newNodes
     * @param map 
     */
    public void addNewNodes(Connection c, List newNodes, String map) {
        try {
            int mapId = getMapId(c, map, (MindMapNode) newNodes.get(0));
            for (int i = 0; i < newNodes.size(); i++) {
                MindMapNode mmn = (MindMapNode) newNodes.get(i);
                String textStr = mmn.getText();
                int textId = getTextId(c, textStr);
                String theId = mmn.getAttribute("id");
                String parentId = mmn.getAttribute("parentId");
                // TODO uuencode label when inserting
                // prolly Mime.encodeText(textStr)
                //textStr = MimeUtility.encodeText(textStr);
                String sql = "insert into links (id,mapId,parentId,labelId,created)"
                        + " values (\"" + theId + "\"," + mapId + ",\"" + parentId + "\"," + textId + ",\"" + currentTime + "\")";
                System.out.println("Add new node sql: '" + sql + "'");
                PreparedStatement stmt = c.prepareStatement(sql);

                //
                stmt.executeUpdate();
                Logger.getLogger(DataBaseReader.class.getName()).log(Level.INFO, stmt.toString());
                System.out.println("{addNewNodes} - " + stmt);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void markAsDeleted(Connection c, List removedNodes, String mapName) {
        try {
            int mapId = getMapId(c, mapName);
            if (mapId == -1) {
                Logger.getLogger(DataBaseReader.class.getName()).log(Level.WARNING, "Cannot mark deleted items, no mapId for " + mapName);
                return;
            }
            for (int i = 0; i < removedNodes.size(); i++) {
                MindMapNode mmn = (MindMapNode) removedNodes.get(i);
                String oidStr = (String) mmn.getAttribute("oid");
                int oid = Integer.parseInt(oidStr);
                String sql = "update links set modified = ? where oid = ? ";
                PreparedStatement stmt = c.prepareStatement(sql);
                stmt.setString(1, currentTime);
                stmt.setInt(2, oid);
                //
                stmt.executeUpdate();
                Logger.getLogger(DataBaseReader.class.getName()).log(Level.INFO, stmt.toString());
                System.out.println("{markAsDeleted} - " + stmt);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateEditedNodes(Connection c, List nodes, String mapName) {
        try {
            int mapId = getMapId(c, mapName);
            for (int i = 0; i < nodes.size(); i++) {
                MindMapNode mmn = (MindMapNode) nodes.get(i);
                String strId = (String) mmn.getAttribute("id");
                String parentId = mmn.getAttribute("parentId");
                String oidStr = (String) mmn.getAttribute("oid");
                int oid = Integer.parseInt(oidStr);
                //TreeNode theNode = getNode(strId, mapName);
                //if (theNode == null) {
                //    continue;
                //}
                String sql = "update links set modified = ? where oid = ?";
                PreparedStatement stmt = c.prepareStatement(sql);
                stmt.setString(1, currentTime);
                stmt.setInt(2, oid);
                //
                stmt.executeUpdate();
                Logger.getLogger(DataBaseReader.class.getName()).log(Level.INFO, "Mark as deleted " + stmt.toString());
                System.out.println("{updateEditedNodes} - " + stmt);
                //
                String textStr = mmn.getText();
                int loc = textStr.lastIndexOf(":");
                if (loc != -1) {
                    textStr = textStr.substring(0, loc); // trim off previous value
                }
                int labelId = getTextId(c, textStr);
                //textStr = MimeUtility.encodeText(textStr);
                // TODO uuencode label when inserting
                sql = "insert into links (id, mapId, parentId,labelId, created)"
                        + " values (\"" + strId + "\", " + mapId + ", \"" + parentId + "\"," + labelId + ", \"" + currentTime + "\")";
                Logger.getLogger(DataBaseReader.class.getName()).log(Level.INFO, sql);
                System.out.println("{updateEditedNodes} - " + sql);
                stmt = c.prepareStatement(sql);
                //
                stmt.executeUpdate();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void printNodes(String title, List nodes) {
        System.out.println(title);
        for (int i = 0; i < nodes.size(); i++) {
            TreeNode mmn = (TreeNode) nodes.get(i);
            String label = mmn.getLabel();
            System.out.println("\t" + i + ") " + label);
            List keys = mmn.getAttributeKeys();
            for (int j = 0; j < keys.size(); j++) {
                String key = (String) keys.get(j);
                System.out.println("\t\t" + j + ") " + key + "->" + mmn.getAttribute(key));
            }
        }
    }

    public Connection beginTransaction(String mapName, String userId)
            throws DataBaseTransactionException, Exception {
        System.out.println("\t\t\t\t\tbegin Transaction");
        Connection c = getConnection();
        modDate = new java.util.Date(); // corrects getting new time when starting transaction
        currentTime = sdf.format(modDate);
        this.userId = userId;
        c.setAutoCommit(false);
        String sql = "START TRANSACTION";
        ResultSet rs = executeSql(c, sql);
        sql = "SELECT VERSION()";
        rs = executeSql(c, sql);
        while (rs.next()) {
               String retVal = rs.getString(1);
               System.out.println("MySQL version: "+retVal);
            }
        return c;
    }

    public void endTransaction(Connection c, String mapName, String comment, String crNumber,
            String version, String release, String msg)
            throws DataBaseTransactionException, Exception {
        updateIds(mapName, comment, crNumber, version, release);
        updateUserAccess(userId);
        System.out.println("\t\t\t\t\tend Transaction");
        c.commit();
        c.close();
        JOptionPane.showMessageDialog((JFrame) controller.getFrame(), "Transaction successful\n" + msg);
    }

    public void rollBack(Connection c) throws DataBaseTransactionException {
        System.out.println("\t\t\t\t\t\t* * * * * * * * roll back * * * * * * ");
        String msg = "successfully ";
        try {
            c.rollback();
        } catch (Exception ex) {
            msg = "un"+msg;
            Logger.getLogger(DataBaseReader.class.getName()).log(Level.SEVERE, null, ex);
        } finally   {
            try {
                JOptionPane.showMessageDialog((JFrame) controller.getFrame(), "Transaction failed and was "+msg+"rolled back");
                c.close();
            } catch (SQLException ex) {
                Logger.getLogger(DataBaseReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * getTextId will look in the dataDictionary for the textStr.
     * If it finds it, it will return the id in the dataDictionary of the textStr.
     * If it does not, it will add it to the dataDictionary, assign an id, and
     * return the id of the new entry.
     * N.B. - It is important that at the end of the "session" that the next id for
     * the dataDictionary be updated.
     * 
     * @param c
     * @param textStr
     * @return 
     */
    private static int getTextId(Connection c, String textStr) {
        int retVal = -1;
        int nextLabelId = -1;
        try {
            String sqlStatement = "SELECT id FROM dataDictionary where label = \"" + textStr + "\"";
            PreparedStatement pps = c.prepareStatement(sqlStatement);
            ResultSet rsl = pps.executeQuery();
            while (rsl.next()) {
                retVal = rsl.getInt(1);
            }
            if (retVal == -1) { // entry is not in dataDictionary
                // determine if the nextTextId has been fetched yet.  If not fetch it.
                nextLabelId = fetchNextLabelId(c);
                // add the textStr to the dataDictionary with the nextLabelId, then increment that.
                String sql = "insert into dataDictionary (id,label)"
                        + " values (" + nextLabelId + ",\"" + textStr + "\")";
                PreparedStatement stmt = c.prepareStatement(sql);
                //
                stmt.executeUpdate();
                retVal = nextLabelId++;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return retVal;
    }

    /**
     * getMapId here returns the id of an existing map
     * 
     * @param c
     * @param mapName
     * @return 
     */
    public int getMapId(Connection c, String mapName) {
        int retVal = -1;
        try {
            String sqlStatement = "SELECT mapId FROM maps where mapName = \"" + mapName + "\"";
            ResultSet rsl = executeSql(c, sqlStatement);
            while (rsl.next()) {
                retVal = rsl.getInt(1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return retVal;

    }

    /**
     * getMapId will look in the maps table to see if a map exists for this string.
     * If it does, it will return that string.
     * If it does not, it will create an entry in the maps table, and return the id of the 
     * new entry.
     * N.B. - It is important that at the end of the "session" that the next id for the
     * maps table is updated.
     * @param c
     * @param map
     * @return 
     */
    private int getMapId(Connection c, String map, MindMapNode root) {
        int retVal = -1;
        int nextMapId = -1;
        String rootId = root.getAttribute("parentId");
        try {
            retVal = getMapId(c, map);
            if (retVal == -1) { // entry is not in dataDictionary
                // determine if the nextTextId has been fetched yet.  If not fetch it.
                nextMapId = fetchNextMapId(c);
                // add the textStr to the dataDictionary with the nextLabelId, then increment that.
                String sql = "insert into maps (mapId,mapName, rootId)"
                        + " values (" + nextMapId + ",\"" + map + "\",\"" + rootId + "\")";
                PreparedStatement stmt = c.prepareStatement(sql);
                //
                stmt.executeUpdate();
                retVal = nextMapId++;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return retVal;
    }

    /**
     * for testing, this will just return 0, then will actually get from database
     */
    private static int fetchNextLabelId(Connection c) {
        return fetchAndIncrement(c, "nextLabelId");
    }

    /**
     * After database operations are completed and before the transaction is closed,
     * update the Ids.  This is an operation provided automatically by the endTransaction
     */
    private void updateIds(String mapName, String comment, String crNumber,
            String version, String release)
            throws Exception { //TODO here is where we insert the date time into the version control
        System.out.println("insert version control information before closing transaction");
        String versionStr = "NULL";
        String releaseStr = "NULL";
        if (version != null && version.length() > 0) {
            versionStr = "\"" + version + "\"";
        }
        if (release != null && release.length() > 0) {
            releaseStr = "\"" + release + "\"";
        }
        Connection c = null;
        try {
            c = getConnection();
            int mapId = getMapId(c, mapName);
            if (mapId == -1) {
                Logger.getLogger(DataBaseReader.class.getName()).log(Level.SEVERE, "No mapId for " + mapName);
                throw new Exception("No mapId for " + mapName);
            }
            String userId = System.getProperty("user.name");
            String sql = "insert into versionControl (mapId,changeDate,committer, comment, changeRequest, version, releaseId)"
                    + " values (" + mapId + ",\"" + currentTime + "\",\"" + userId + "\",\""
                    + comment + "\", \"" + crNumber + "\"," + versionStr + "," + releaseStr + ")";
            System.out.println("sql: '" + sql + "'");
            PreparedStatement stmt = c.prepareStatement(sql);
            Logger.getLogger(DataBaseReader.class.getName()).log(Level.INFO, stmt.toString());

            //
            stmt.executeUpdate();

        } catch (Exception ex) {
            throw new Exception(ex);
        } finally {
            try {
                c.close();
            } catch (SQLException ex) {
                Logger.getLogger(DataBaseReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static int fetchNextMapId(Connection c) {
        return fetchAndIncrement(c, "nextMapId");
    }

    public int fetchNextMajorId(Connection c, String mapName) {
        int retVal = -1;
        try {
            String sqlStatement = "SELECT nextMajorId FROM maps where mapName = \"" + mapName + "\"";
            PreparedStatement pps = c.prepareStatement(sqlStatement);
            ResultSet rsl = pps.executeQuery();
            while (rsl.next()) {
                retVal = rsl.getInt(1);
            }
            if (retVal == -1) { // entry is not in maps table
                System.out.println("WARNING, " + mapName + " is not in maps table");
                int mapId = fetchAndIncrement(c, "nextMapId");
                // add the mapName to the maps table incrementing the nextMapId, then increment that.
                String sql = "insert into maps (mapId, mapName)"
                        + " values (" + mapId + ", \"" + mapName + "\")";
                PreparedStatement stmt = c.prepareStatement(sql);
                //
                stmt.executeUpdate();
                retVal = 1;
            } else { // update the value already there and just retrieved
                String sql = "update maps set nextMajorId = ? where mapName = ?";
                PreparedStatement stmt = c.prepareStatement(sql);
                stmt.setInt(1, retVal + 1);
                stmt.setString(2, mapName);
                //
                stmt.executeUpdate();

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return retVal;
    }

    public Object[] getDatesForMap(String mapName) {
        Connection c = null;
        Object[] result = null;
        try {
            c = getConnection();
            result = getDatesForMap(c, mapName);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                c.close();
            } catch (SQLException ex) {
                Logger.getLogger(DataBaseReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }

    public Object[] getDatesForMap(Connection c, String mapName) {
        int mapId = getMapId(c, mapName);
        List aList = new ArrayList();
        try {
            String sql = "select changeDate, time(changeDate), comment, committer, version, releaseId, changeRequest from versionControl where mapId = " + mapId + " order by changeDate";
            ResultSet rsl = executeSql(c, sql);
            while (rsl.next()) {
                java.sql.Date date = rsl.getDate(1);
                java.sql.Time time = rsl.getTime(2);
                String comment = rsl.getString(3);
                String dateStr = date.toString() + " " + time.toString();
                String committer = rsl.getString(4);
                String version = rsl.getString(5);
                String releaseId = rsl.getString(6);
                String changeRequest = rsl.getString(7);
                //System.out.println("Date for query = '" + dateStr + "'");
                aList.add(new ComboObject(dateStr, comment, committer, version, releaseId, changeRequest));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return aList.toArray();
    }

    public String[] getListOfMaps() {
        String sql = "select mapName from maps";
        String[] result = null;
        Connection c = null;
        try {
            c = getConnection();
            ResultSet rsl = executeSql(c, sql);
            List theList = new ArrayList();
            while (rsl.next()) {
                String mapName = rsl.getString(1);
                theList.add(mapName);
            }
            result = new String[theList.size()];
            return (String[]) theList.toArray(result);
        } catch (Exception cex) {
            cex.printStackTrace();
        } finally {
            try {
                c.close();
            } catch (SQLException ex) {
                Logger.getLogger(DataBaseReader.class.getName()).log(Level.SEVERE, null, ex);
            }
            ;
        }
        return result;
    }

    public ResultSet executeSql(Connection c, String sql) {
        try {
            PreparedStatement pps = c.prepareStatement(sql);
            ResultSet rsl = pps.executeQuery();
            return rsl;
        } catch (Exception sex) {
            sex.printStackTrace();
        }
        return null;
    }

    public ComboObject getVersionControlInfoForMap(Connection c, String mapName) {
        ComboObject result = null;
        try {
            int mapId = getMapId(c, mapName);
            String sql = "select version, releaseId from versionControl where mapId = " + mapId + " order by changeDate DESC LIMIT 1";
            ResultSet rs = executeSql(c, sql);
            while (rs.next()) {
                String version = rs.getString(1);
                String release = rs.getString(2);
                result = new ComboObject("", "", "", version, release, "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Object[] getVersionsForMap(String mapName) {
        Connection c = null;
        Object[] result = null;
        try {
            c = getConnection();
            result = getVersionsForMap(c, mapName);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                c.close();
            } catch (SQLException ex) {
                Logger.getLogger(DataBaseReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;

    }

    public Object[] getVersionsForMap(Connection c, String mapName) {
        int mapId = getMapId(c, mapName);
        List aList = new ArrayList();
        try {
            String sql = "select changeDate, time(changeDate), version, comment from versionControl where version is not NUll and mapId = " + mapId + " order by changeDate";
            ResultSet rsl = executeSql(c, sql);
            while (rsl.next()) {
                java.sql.Date date = rsl.getDate(1);
                java.sql.Time time = rsl.getTime(2);
                String version = rsl.getString(3);
                String comment = rsl.getString(4);
                String dateStr = date.toString() + " " + time.toString();
                //System.out.println("Date for query = '" + dateStr + "'");
                aList.add(new ComboObject(dateStr, version, comment));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return aList.toArray();
    }

    public Object[] getReleasesForMap(String mapName) {
        Connection c = null;
        Object[] result = null;
        try {
            c = getConnection();
            result = getReleasesForMap(c, mapName);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                c.close();
            } catch (SQLException ex) {
                Logger.getLogger(DataBaseReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;

    }

    public Object[] getReleasesForMap(Connection c, String mapName) {
        int mapId = getMapId(c, mapName);
        List aList = new ArrayList();
        try {
            String sql = "select changeDate, time(changeDate), releaseId, comment from versionControl where releaseId is not NUll and mapId = " + mapId + " order by changeDate";
            ResultSet rsl = executeSql(c, sql);
            while (rsl.next()) {
                java.sql.Date date = rsl.getDate(1);
                java.sql.Time time = rsl.getTime(2);
                String version = rsl.getString(3);
                String comment = rsl.getString(4);
                String dateStr = date.toString() + " " + time.toString();
                //System.out.println("Date for query = '" + dateStr + "'");
                aList.add(new ComboObject(dateStr, version, comment));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return aList.toArray();
    }

    public ComboObject getCurrentDateForMap(String mapName) {
        Connection c = null;
        ComboObject result = null;
        try {
            c = getConnection();
            result = getCurrentDateForMap(c, mapName);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                c.close();
            } catch (SQLException ex) {
                Logger.getLogger(DataBaseReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }

    public ComboObject getCurrentDateForMap(Connection c, String mapName) {
        int mapId = getMapId(c, mapName);
        ComboObject result = null;
        try {
            String sql = "select changeDate, time(changeDate), comment from versionControl where mapId = " + mapId + " order by changeDate DESC LIMIT 1";
            ResultSet rsl = executeSql(c, sql);
            while (rsl.next()) {
                java.sql.Date date = rsl.getDate(1);
                java.sql.Time time = rsl.getTime(2);
                String comment = rsl.getString(3);
                String dateStr = date.toString() + " " + time.toString();
                //System.out.println("Date for query = '" + dateStr + "'");
                result = new ComboObject(dateStr, comment);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public ComboObject getBaseLineDateForMap(String mapName) {
        Connection c = null;
        ComboObject result = null;
        try {
            c = getConnection();
            result = getBaseLineDateForMap(c, mapName);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                c.close();
            } catch (SQLException ex) {
                Logger.getLogger(DataBaseReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }

    public ComboObject getBaseLineDateForMap(Connection c, String mapName) {
        int mapId = getMapId(c, mapName);
        ComboObject result = null;
        try {
            String sql = "select changeDate, time(changeDate),  comment from versionControl where mapId = " + mapId + " order by changeDate LIMIT 1";
            ResultSet rsl = executeSql(c, sql);
            while (rsl.next()) {
                java.sql.Date date = rsl.getDate(1);
                java.sql.Time time = rsl.getTime(2);
                String comment = rsl.getString(3);
                String dateStr = date.toString() + " " + time.toString();
                //System.out.println("Date for query = '" + dateStr + "'");
                result = new ComboObject(dateStr, comment);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    private List traverseNodes(List results, List treeNodes, int i) {

        TreeNode tn = (TreeNode) treeNodes.get(i);
        results.add(tn);
        String parentId = tn.getParentId();
        if (parentId.contains("-1")) {
            return results;
        }
        int j = locateNode(parentId, treeNodes);
        if (j == -1) {
            return results;
        }
        return traverseNodes(results, treeNodes, j);
    }

    private int locateNode(String parentId, List treeNodes) {
        for (int i = 0; i < treeNodes.size(); i++) {
            TreeNode tn = (TreeNode) treeNodes.get(i);
            String id = tn.getId();
            if (id.equals(parentId)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * The branches must be ordered so that a, b1, c1, d5 comes before a, b2!
     * The input looks like
     * D6, C1, B1, A
     * D5, C1, B1, A
     * C2, B1, A
     * C1, B1, A
     * B2, A
     * B1, A
     * A
     * 
     * @param treeData
     * @return 
     */
    private List orderBranches(List treeData) {
        List retList = new ArrayList();
        List root = (List) treeData.get(treeData.size() - 1);
        retList.add(root);
        TreeNode rootNode = (TreeNode) root.get(0);
        String rootId = rootNode.getId();
        return recurseChildren(retList, treeData, rootId);
    }

    private List getChildrenOf(List treeData, String id) {
        //System.out.println("Get children of " + id);
        List children = new ArrayList();
        for (int i = treeData.size() - 1; i >= 0; i--) {
            List aBranch = (List) treeData.get(i);
            TreeNode leafNode = (TreeNode) aBranch.get(0);
            if (id.equals(leafNode.getParentId())) {
                children.add(aBranch);
            }
        }
        //System.out.println("\tchildren are " + children);
        return children;
    }

    private List recurseChildren(List retList, List treeData, String id) {
        List children = getChildrenOf(treeData, id); // returns B1, B2 as chidren
        for (int i = 0; i < children.size(); i++) {
            List child = (List) children.get(i); // gets B1 branch
            retList.add(child);
            TreeNode childNode = (TreeNode) child.get(0);
            recurseChildren(retList, treeData, childNode.getId());
        }
        return retList;
    }

    public void updateUserAccess(String userId) {
        Connection c = null;
        try {
            c = getConnection();
            String sql = "update users set last_login = ? where userid = ?";
            PreparedStatement stmt = c.prepareStatement(sql);
            stmt.setString(1, currentTime);
            stmt.setString(2, userId);
            //
            stmt.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                c.close();
            } catch (SQLException ex) {
                Logger.getLogger(DataBaseReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void testIncrementingConstants(MindMapController newController) {
        Connection c = null;
        try {
            c = getConnection();
            System.out.println("Value of nextRalph is: " + fetchAndIncrement(c, "nextRalph"));
            System.out.println("Value of nextRalph is: " + fetchAndIncrement(c, "nextRalph"));
            System.out.println("Value of nextRalph is: " + fetchAndIncrement(c, "nextRalph"));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                c.close();
            } catch (SQLException ex) {
                Logger.getLogger(DataBaseReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void testGettingNodeIds(MindMapController newController) {
        Connection c = null;
        try {
            c = getConnection();
            System.out.println("next id for test_short.mm is " + getNextNodeIdForMap(c, "test_short.mm"));
            System.out.println("next id for test_short.mm is " + getNextNodeIdForMap(c, "test_short.mm"));
            System.out.println("next id for test_short.mm is " + getNextNodeIdForMap(c, "test_short.mm"));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                c.close();
            } catch (SQLException ex) {
                Logger.getLogger(DataBaseReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public String getNextNodeIdForMap(Connection c, String mapName) {
        int theNextId = getTheNextMajorForMap(c, mapName);
        int theNextMinorId = Integer.parseInt((String) nextMinorId.get(mapName));
        String retVal = "" + theNextId + "." + theNextMinorId++;
        nextMinorId.put(mapName, "" + theNextMinorId); // for next time
        return retVal;
    }

    private int getTheNextMajorForMap(Connection c, String mapName) {
        if (nextMajorId == null) {
            nextMajorId = new TreeMap();
            nextMinorId = new TreeMap();
            int theNextId = fetchNextMajorId(c, mapName);
            nextMajorId.put(mapName, "" + theNextId);
            nextMinorId.put(mapName, "1");
        }
        String idStr = (String) nextMajorId.get(mapName);
        int theNextId = -1;
        if (idStr == null) { // majorId not fetched yet, get and update it
            theNextId = fetchNextMajorId(c, mapName);
            nextMajorId.put(mapName, "" + theNextId);
            nextMinorId.put(mapName, "1");
        } else {
            theNextId = Integer.parseInt(idStr);
        }
        return theNextId;

    }

    public String fixNodeNumber(Connection c, String mapName, TreeMap nodeIdMap, String idStr) { // get something like -99.-3
        String newNodeId = null;
        if (idStr.endsWith("-1") && idStr.indexOf(".") == -1) { // root node, handle special
            newNodeId = "" + getTheNextMajorForMap(c, mapName) + "." + idStr;
        } else {
            newNodeId = (String) nodeIdMap.get(idStr);
            if (newNodeId == null && idStr.startsWith("-")) {
                newNodeId = getNextNodeIdForMap(c, mapName);
            }
        }
        nodeIdMap.put(idStr, newNodeId);
        return newNodeId;
    }

    public MajorMinorOrderedMap updateIds(MajorMinorOrderedMap mm, String mapName) {
        TreeMap nodeIdMap = new TreeMap();
        Connection c = null;
        try {
            c = getConnection();
            Iterator it = mm.iterator();
            while (it.hasNext()) {
                String idStr = (String) it.next();
                MindMapNode mmn = (MindMapNode) mm.get(idStr);
                fixNode(mmn, c, mapName, nodeIdMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                c.close();
            } catch (SQLException ex) {
                Logger.getLogger(DataBaseReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return mm;
    }
    private static final String[] attrs = {"id", "parentId"};

    private void fixNode(MindMapNode mmn, Connection c, String mapName, TreeMap nodeIdMap) {
        System.out.println("\n\nNode [" + mmn.getText() + "] had ids " + mmn.getAttribute("id") + "," + mmn.getAttribute("parentId"));

        for (int i = 0; i < attrs.length; i++) {
            String idold = mmn.getAttribute(attrs[i]);
            String id = fixNodeNumber(c, mapName, nodeIdMap, idold);
            int pos = mmn.getAttributePosition(attrs[i]);
            if (id != null) {
                mmn.setAttribute(pos, new Attribute(attrs[i], id));
            }
        }
        System.out.println("Node [" + mmn.getText() + "] now has ids " + mmn.getAttribute("id") + "," + mmn.getAttribute("parentId") + " and rowcount of " + mmn.getAttributeTableLength());
    }

    private int testGettingNextId(Connection c, String majorMinor, String typeName, String mapName) {
        return getNextValueForMap(c, mapName, "next" + majorMinor + typeName + "Id");
    }

    private int testSettingNextId(Connection c, String majorMinor, String typeName, String mapName) {
        return updateNextValueForMap(c, mapName, "next" + majorMinor + typeName + "Id");
    }
    MajorMinorOrderedMap values = null;
    private int lastIndentLevel = 0;
    public static Map nextMajorId = new TreeMap();
    public static Map nextMinorId = new TreeMap();
    private boolean regressionTest = true;
    private String regressionTestFilePath = "/Users/woo/Development/Dropbox/Freemind/Perl_DB/test_short.mm";
    private java.util.Date modDate = new java.util.Date();
    private String currentTime;
    private java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //
    private static Properties userPreferences;

    /**
     * Return a list of nodes starting at root and ending at leaves
     * @param c
     * @param mapArea
     * @param dateStr
     * @return
     * @throws SQLException
     * @throws Exception 
     */
    public List getTreeNodes(Connection c, String mapArea, String dateStr) throws SQLException, Exception {
        List nodes = new ArrayList();
        // test
        //return getSampleNodes();
        int mapId = -1;
        String modifier = "";
        if (mapArea != null) {
            mapId = getMapId(c, mapArea);
            if (mapId == -1) {
                System.out.println("Did not find mapId for mapName '" + mapArea + "'");
            }
            modifier = " where mapId = " + mapId + " ";
        }

        // TODO - do select using join to get label values
        String sqlStatement = "select links.id, links.parentId, dataDictionary.label, links.oid "
                + " from links join dataDictionary "
                + " on links.labelId = dataDictionary.id " + modifier
                + " and created <= \"" + dateStr + "\" "
                + " and ( modified is NULL or modified > \"" + dateStr + "\")"
                + " order by links.parentId, links.id";
        System.out.println("{getTreeNodes} - " + sqlStatement);
        PreparedStatement pps = c.prepareStatement(sqlStatement);
        ResultSet rsl = pps.executeQuery();
        // Storing away the nodes by parentId for lookup later
        TreeNode root = null;
        while (rsl.next()) {
            String id = rsl.getString(1);
            String parentId = rsl.getString(2);
            // TODO uudecode string from database
            String label = rsl.getString(3);
            //label = MimeUtility.decodeText(label);
            int oid = rsl.getInt(4);
            TreeNode tn = new TreeNode(oid, id, parentId, label);
            if (parentId.contains("-1")) {
                root = tn;
            }
            //System.out.println("id = " + id + ", parentId = " + parentId + ", label = " + label);
            nodes.add(tn);
        }
        return nodes;
    }

    public void renumberChildren(Connection c, String mapName, MindMapNode node) {
        // find the child whose parent is node
        ListIterator it = node.childrenFolded();
        String nodeId = getNextNodeIdForMap(c, mapName);
        node.setAttribute(0, new Attribute("id", nodeId));
        while (it.hasNext()) {
            MindMapNode childNode = (MindMapNode) it.next();
            if (childNode.hasChildren()) {
                renumberChildren(c, mapName, childNode);
            }
            String childId = getNextNodeIdForMap(c, mapName);
            childNode.setAttribute(0, new Attribute("id", childId));
            childNode.setAttribute(0, new Attribute("parentId", nodeId));
        }
    }

    public static boolean hasDatabase() throws Throwable {
        Properties defaultPreferences = null;
        if (userPreferences == null) {
            System.out.println("Reading user preferences");
            FreeMindStarter starter = new FreeMindStarter();
            defaultPreferences = starter.readDefaultPreferences();
            userPreferences = starter.readUsersPreferences(defaultPreferences);
            if (userPreferences.containsKey("driverName")) {
                System.out.println("Found defaults for database properties");
                driverName = userPreferences.getProperty("driverName");
                url = userPreferences.getProperty("databaseUrl");
                dataBaseUserId = userPreferences.getProperty("databaseUserId");
                passwd = userPreferences.getProperty("databasePassword");
            }
        }
        boolean retVal = checkConnection();
        if (!retVal) {
            DataBaseSetupPanel dbsp = new DataBaseSetupPanel(new JFrame("Setup"),
                    driverName, url, dataBaseUserId, passwd);
           
            dbsp.setVisible(true);
            boolean cancelled = dbsp.cancelled();
            while (!cancelled && !retVal) { // Entered some data?
                driverName = dbsp.getDriverName();
                url = dbsp.getUrl();
                dataBaseUserId = dbsp.getUserId();
                passwd = dbsp.getPasswd();
                retVal = checkConnection();
                if (!retVal) {
                    int ret = JOptionPane.showConfirmDialog(null, "Connection info not valid. Enter another or cancel");
                    if (ret == JOptionPane.NO_OPTION || ret == JOptionPane.CANCEL_OPTION ) {
                        cancelled = true;
                    } else {
                        dbsp.setVisible(true);
                        cancelled = dbsp.cancelled();
                    }
                }
            }
        }
        if (retVal) { // Have valid connection information
            System.out.println("Setting database properties in userPreferences");
            userPreferences.setProperty("driverName", driverName);
            userPreferences.setProperty("databaseUrl", url);
            userPreferences.setProperty("databaseUserId", dataBaseUserId);
            userPreferences.setProperty("databasePassword", passwd);
            String freemindDirectory = getFreeMindDirectory(defaultPreferences);
            File userPropertiesFolder = new File(freemindDirectory);
            autoPropertiesFile = new File(userPropertiesFolder, defaultPreferences.getProperty("autoproperties"));
            saveProperties(userPreferences);
        }
        return retVal;

    }

    public static Connection getConnection() throws Exception {
        // load the Oracle JDBC Driver
        // Class.forName(driverName);
        // define database connection parameters
        return DriverManager.getConnection(url, dataBaseUserId,
                passwd);
    }

    private static String getFreeMindDirectory(Properties defaultPreferences) {
        return System.getProperty("user.home") + File.separator + defaultPreferences.getProperty("properties_folder");
    }

    private static void saveProperties(Properties props) {
        System.out.println("Saving properties to: " + autoPropertiesFile.getAbsolutePath());
        try {
            OutputStream out = new FileOutputStream(autoPropertiesFile);
            final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out, "8859_1");
            outputStreamWriter.write("#FreeMind ");
            outputStreamWriter.write(FreeMind.VERSION.toString());
            outputStreamWriter.write('\n');
            outputStreamWriter.flush();
            // auto.store(out,null);//to save as few props as possible.
            props.store(out, null);
            out.close();
            System.out.println("Successful save of properties");
        } catch (Exception ex) {
            System.err.println("Exception saving properties " + ex);
        }
    }

    public DataBaseReader(MindMapController theController) {
        super();
        controller = theController;
        currentTime = sdf.format(modDate);
       /* Enumeration e = DriverManager.getDrivers();
        System.out.println("Available Drivers");
        int k = 0;
        while( e.hasMoreElements() )    {
            Object obj = e.nextElement();
            Driver aDriver = (Driver)obj;
            System.out.println(""+k+": "+obj.getClass().getName()+"->"+aDriver.getMajorVersion()+":"+aDriver.getMinorVersion());
        }*/
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public MajorMinorOrderedMap getNodes(String mapName, String aDate) {
        values = new MajorMinorOrderedMap();
        Connection c = null;
        try {
            c = getConnection();
            List nodes = getTreeNodes(c, mapName, aDate);
            for (int i = 0; i < nodes.size(); i++) {
                TreeNode tn = (TreeNode) nodes.get(i);
                values.put(tn.getId(), tn);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                c.close();
            } catch (SQLException ex) {
                Logger.getLogger(DataBaseReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return values;
    }

    public TreeNode getNode(String theId, String mapName) {
        TreeNode node = null;
        try {
            Connection c = getConnection();
            String modifier = "";
            if (mapName != null) {
                int mapId = getMapId(c, mapName);
                modifier = " where mapId = " + mapId + " ";
            }
            String sqlStatement = "select links.id, links.parentId, dataDictionary.label, links.modified, links.oid from links join dataDictionary"
                    + " on links.labelId = dataDictionary.id " + modifier + " order by id";
            PreparedStatement pps = c.prepareStatement(sqlStatement);
            ResultSet rsl = pps.executeQuery();
            // Storing away the nodes by parentId for lookup later

            while (rsl.next()) {
                String id = rsl.getString(1);
                String parentId = rsl.getString(2);
                // TODO uudecode string from database
                String label = rsl.getString(3);
                //label = MimeUtility.decodeText(label);
                int oid = rsl.getInt(4);
                node = new TreeNode(oid, id, parentId, label);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return node;

    }

    /**
     * values is a Map of the TreeNodes keyed by id
     * results is a Map of the branches from leaf to root.
     * @return
     */
    public String getXML(String mapArea, String aDate) {
        StringBuffer buffer = new StringBuffer();
        StringBuffer closedNodeString = new StringBuffer();
        List results = orderBranches(getTreeData(mapArea, aDate)); // This is a map of the branches keyed by id.
        Stack closedNodeStack = new Stack();
        //printTreeData(results); // prints nodes from database call
        //printValues();
        List lastBranch = new ArrayList();
        int j = 0;
        buffer.append("<map version=\"0.9.0\">\n");
        for (int k = 0; k < results.size(); k++) {

            // Create a new list and replace id with the xml for the node with that id
            List theList = (List) results.get(k);
            List aList = new ArrayList();
            for (int i = 0; i < theList.size(); i++) {
                aList.add(((TreeNode) theList.get(i)).getXML());
            }
            // At this point, we have a branch assembled from leaf to root
            // output the branch in reverse order
            if (aList.size() < lastIndentLevel) {
                closedNodeStack.push("</node>\n");
                for (int i = aList.size(); i <= lastIndentLevel; i++) {
                    if (!closedNodeStack.empty()) {
                        buffer.append(closedNodeStack.pop());
                    }
                }
            }
            if (aList.size() > lastIndentLevel) {
                closedNodeStack.push("</node>\n");
            }
            if (aList.size() == lastIndentLevel) {
                if (!closedNodeStack.empty()) {
                    buffer.append(closedNodeStack.pop());
                }
                closedNodeStack.push("</node>\n");
            }
            buffer.append("\n");
            int kount = 0;
            for (int indentLevel = 1; indentLevel <= aList.size() - 1; indentLevel++) {
                buffer.append("\t");
                kount++;
            }
            Collections.reverse(aList);
            //System.out.println("The reversed list: " + aList);
            for (int i = kount; i < aList.size(); i++) {
                buffer.append(aList.get(i));
            }
            lastIndentLevel = aList.size();
            lastBranch = aList;
            j++;
        }
        while (!closedNodeStack.empty()) {
            buffer.append(closedNodeStack.pop());
        }
        buffer.append("\n</map>\n");
        //System.out.print("\nXML generated\n" + buffer);
        return buffer.toString();
    }

    /**
     * getTreeData should provide a MajorMinorOrderedMap such that the branches 
     * are ordered by their child/parent relationships, i.e.
     * A
     * A, B1
     * A, B1, C1
     * A, B1, C1, D5
     * A, B1, C1, D6
     * A, B1, C2
     * 
     * only reversed in this case
     * 
     * @param mapArea
     * @param aDate
     * @return 
     */
    public List getTreeData(String mapArea, String aDate) {
        List parents = new ArrayList();
        values = new MajorMinorOrderedMap();
        Connection c = null;
        try {
            c = getConnection();
            List treeNodes = getTreeNodes(c, mapArea, aDate);
            // values = getSampleNodes();
            // Now have to construct the branches of the tree
            System.out.println("\n\n");
            // Starting with root, find children, etc.
            int theCount = treeNodes.size();
            for (int i = theCount - 1; i >= 0; i--) {
                List out = new ArrayList();
                List aBranch = traverseNodes(out, treeNodes, i);
                if (aBranch != null && aBranch.size() > 0) {
                    TreeNode tn = (TreeNode) aBranch.get(0);
                    String id = tn.getId();
                    parents.add(aBranch);
                    values.put(id, tn);
                }
            }

            c.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (c != null) {
            System.out.println("Got a connection!");
        }
        return parents;
    }

    private void printTreeData(List results) {
        System.out.println("TreeData");
        Iterator it = results.iterator();
        while (it.hasNext()) {
            Object data = it.next();
            System.out.println(data);
        }
    }

    private void printValues() {
        System.out.println("Values");
        Iterator it = values.iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object data = values.get(key);
            System.out.println("key: " + key + "->" + data);
        }
    }

    public String[] getMapAreas() {
        Connection c = null;
        List areas = new ArrayList();
        try {
            c = getConnection();
            String sqlStatement = "SELECT mapName FROM maps order by mapName";
            PreparedStatement pps = c.prepareStatement(sqlStatement);
            ResultSet rsl = pps.executeQuery();
            // Storing away the nodes by parentId for lookup later

            while (rsl.next()) {
                String value = rsl.getString(1);
                areas.add(value);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                c.close();
            } catch (SQLException ex) {
                Logger.getLogger(DataBaseReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        String[] retStrings = new String[areas.size()];
        for (int i = 0; i < areas.size(); i++) {
            retStrings[i] = (String) areas.get(i);
        }
        return retStrings;
    }

    /**
     * During testing, this is used to load a map into memory without having to use
     * the GUI portion, if the xml File is given
     * @param frame
     * @param controller 
     */
    public MindMapController loadAMap(File xmlFile, MindMapController controller) {
        MindMapController newController = new MindMapController(controller.getModeController().getMode());
        ControllerAdapter newControllerAdapter = (ControllerAdapter) newController;
        try {
            if (xmlFile != null) {
                MapAdapter newModel = newController.newModel(newControllerAdapter);
                newModel.load(xmlFile);
                newControllerAdapter.newMap(newModel);
                MindMap mm = newControllerAdapter.getMap();
                mm.setMapName(xmlFile.getName());
                MindMapNode root = newModel.getRootNode();
                mm.getRegistry().getAttributes().setAttributeViewType(AttributeTableLayoutModel.HIDE_ALL);
                //newControllerAdapter.invokeHooksRecursively(root., newModel);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return newController;
    }

    public static void main(String[] args) {
        try {
            DataBaseReader dbr = new DataBaseReader(null);
            FreeMindStarter starter = new FreeMindStarter();
            // First check version of Java
            dbr.checkJavaVersion();
            Properties defaultPreferences = dbr.readDefaultPreferences();
            dbr.createUserDirectory(defaultPreferences);
            Properties userPreferences = dbr.readUsersPreferences(defaultPreferences);
            dbr.setDefaultLocale(userPreferences);
            final FreeMind frame = new FreeMind(defaultPreferences, userPreferences, dbr.getUserPreferencesFile(defaultPreferences));
            IFreeMindSplash splash = null;
            final FeedBack feedBack;
            // change here, if you don't like the splash
            if (true) {
                splash = new FreeMindSplashModern(frame);
                splash.setVisible(true);
                feedBack = splash.getFeedBack();
                frame.mWindowIcon = splash.getWindowIcon();
            } else {
                feedBack = new FeedBack() {

                    int value = 0;

                    public int getActualValue() {
                        return value;
                    }

                    public void increase(String messageId) {
                        progress(getActualValue() + 1, messageId);
                    }

                    public void progress(int act, String messageId) {
                        frame.logger.info("Beginnig task:" + messageId);
                    }

                    public void setMaximumValue(int max) {
                    }
                };
                frame.mWindowIcon = new ImageIcon(frame.getResource("images/FreeMindWindowIcon.png"));
            }
            feedBack.setMaximumValue(9);
            frame.init(feedBack);

            feedBack.increase("FreeMind.progress.startCreateController");
            final ModeController ctrl = frame.createModeController(args);

            feedBack.increase("FreeMind.progress.loadMaps");
            // This could be improved.

            //frame.loadMaps(args, ctrl);
            Tools.waitForEventQueue();
            feedBack.increase("FreeMind.progress.endStartup");
            // focus fix after startup.
            frame.addWindowFocusListener(new WindowFocusListener() {

                public void windowLostFocus(WindowEvent e) {
                }

                public void windowGainedFocus(WindowEvent e) {
                    NodeView selectedView = ctrl.getSelectedView();
                    if (selectedView != null) {
                        selectedView.requestFocus();
                        MindMapNode selected = ctrl.getSelected();
                        if (selected != null) {
                            ctrl.centerNode(selected);
                        }
                    }
                    frame.removeWindowFocusListener(this);
                }
            });
            frame.setVisible(true);
            if (splash != null) {
                splash.setVisible(false);
            }
            dbr.runTests(frame, ctrl);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "freemind.main.FreeMind can't be started", "Startup problem", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

    }

    private void checkJavaVersion() {
        System.out.println("Checking Java Version...");
        if (JAVA_VERSION.compareTo("1.4.0") < 0) {
            String message = "Warning: FreeMind requires version Java 1.4.0 or higher (your version: "
                    + JAVA_VERSION
                    + ", installed in "
                    + System.getProperty("java.home") + ").";
            System.err.println(message);
            JOptionPane.showMessageDialog(null, message, "FreeMind",
                    JOptionPane.WARNING_MESSAGE);
            System.exit(1);
        }
    }

    private void createUserDirectory(Properties pDefaultProperties) {
        File userPropertiesFolder = new File(getFreeMindDirectory(pDefaultProperties));
        try {
            // create user directory:
            if (!userPropertiesFolder.exists()) {
                userPropertiesFolder.mkdir();
            }
        } catch (Exception e) {
            // exception is logged to console as we don't have a logger
            e.printStackTrace();
            System.err.println("Cannot create folder for user properties and logging: '"
                    + userPropertiesFolder.getAbsolutePath() + "'");

        }
    }

    /**
     * @param pProperties 
     */
    private void setDefaultLocale(Properties pProperties) {
        String lang = pProperties.getProperty(FreeMindCommon.RESOURCE_LANGUAGE);
        if (lang == null) {
            return;
        }
        Locale localeDef = null;
        switch (lang.length()) {
            case 2:
                localeDef = new Locale(lang);
                break;
            case 5:
                localeDef = new Locale(lang.substring(0, 1), lang.substring(3, 4));
                break;
            default:
                return;
        }
        Locale.setDefault(localeDef);
    }

    private Properties readUsersPreferences(Properties defaultPreferences) {
        Properties auto = null;
        auto = new Properties(defaultPreferences);
        try {
            InputStream in = null;
            File autoPropertiesFile = getUserPreferencesFile(defaultPreferences);
            in = new FileInputStream(autoPropertiesFile);
            auto.load(in);
            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Panic! Error while loading default properties.");
        }
        return auto;
    }

    private File getUserPreferencesFile(Properties defaultPreferences) {
        if (defaultPreferences == null) {
            System.err.println("Panic! Error while loading default properties.");
            System.exit(1);
        }
        String freemindDirectory = getFreeMindDirectory(defaultPreferences);
        File userPropertiesFolder = new File(freemindDirectory);
        File autoPropertiesFile = new File(userPropertiesFolder, defaultPreferences.getProperty("autoproperties"));
        return autoPropertiesFile;
    }

    public Properties readDefaultPreferences() {
        String propsLoc = "freemind.properties";
        URL defaultPropsURL = this.getClass().getClassLoader().getResource(propsLoc);
        Properties props = new Properties();
        try {
            InputStream in = defaultPropsURL.openStream();
            props.load(in);
            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Panic! Error while loading default properties.");
        }
        return props;
    }

    private void runTests(FreeMind frame, ModeController ctrl) {
        File xmlFile = null;
        if (!regressionTest) {
            JFileChooser fc = new JFileChooser();
            fc.showOpenDialog(frame);
            xmlFile = fc.getSelectedFile();
        } else {
            xmlFile = new File(regressionTestFilePath);
        }
        if (xmlFile != null) {
            MindMapController newController = loadAMap(xmlFile, (MindMapController) ctrl);
            //testSavingNewMap(newController, xmlFile.getName());
            // Run tests of constants incrementers, id generators, etc.
            testIncrementingConstants(newController);
            testGettingNodeIds(newController);
            testGettingIds();
            DataBaseReader dbr = new DataBaseReader(newController);
            testSuccessfulCommit(dbr);
            testFailedCommit(dbr);
            System.out.println("All Done!");
        } else {
            System.out.println("Failed to load xmlFile\nAll Done!");
        }
        System.exit(0);
    }

    private void testSavingNewMap(MindMapController controller, String mapName) {
        DiffAction da = new DiffAction(controller);
        MajorMinorOrderedMap memoryNodes = da.getMemoryMap(controller);
        // modify the nodes so that they all appear new
        Iterator it = memoryNodes.iterator();
        while (it.hasNext()) {
            Integer key = (Integer) it.next();
            MindMapNode node = (MindMapNode) memoryNodes.get(key);
            node.setBackgroundColor(Color.green);
        }
        List newNodes = DiffAction.getNewNodes(memoryNodes);

        // Mark removed nodes as deleted
        Connection c = null;
        try {
            DataBaseReader dbr = new DataBaseReader(null);
            c = dbr.beginTransaction(mapName, "testUserId");
            dbr.addNewNodes(c, newNodes, mapName);
            dbr.endTransaction(c, mapName, "A comment", "", "", "", "testSavingNewMap");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private List getList(MajorMinorOrderedMap nodes) {
        List aList = new ArrayList();
        Iterator it = nodes.iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            aList.add(nodes.get(key));
        }
        return aList;
    }

    private void testSuccessfulCommit(DataBaseReader dbr) {
        try {
            Connection c = dbr.beginTransaction("test_short.mm", userId);
            // update something
            String sql = "insert into vendors (vendorId, vendorName) values (0,\"vendor0\")";
            PreparedStatement stmt = c.prepareStatement(sql);
            stmt.executeUpdate();
            sql = "insert into vendors (vendorId, vendorName) values (1,\"vendor1\")";
            stmt = c.prepareStatement(sql);
            stmt.executeUpdate();
           dbr.endTransaction(c, "test_short.mm", "test commit", null, null, null, "A Message");
        } catch (DataBaseTransactionException ex) {
            Logger.getLogger(DataBaseReader.class.getName()).log(Level.SEVERE, ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(DataBaseReader.class.getName()).log(Level.SEVERE, ex.getMessage());
        }
    }

    private void testFailedCommit(DataBaseReader dbr) {
        Connection c = null;
        try {
            c = dbr.beginTransaction("test_short.mm", userId);
            // update something
            String sql = "insert into vendors (vendorId, vendorName) values (2,\"vendor2\")";
            java.sql.Statement stmt = c.createStatement();
            stmt.executeUpdate(sql);
            sql = "insert into vendors (vendorJd, vendorName) values (3,\"vendor3\")";
            stmt.executeUpdate(sql);
           dbr.endTransaction(c, "test_short.mm", "test commit", null, null, null, "A Message");
        } catch (DataBaseTransactionException ex) {
            Logger.getLogger(DataBaseReader.class.getName()).log(Level.SEVERE, ex.getMessage());
        } catch (Exception ex) {
            try {
                dbr.rollBack(c);
            } catch (DataBaseTransactionException ex1) {
                Logger.getLogger(DataBaseReader.class.getName()).log(Level.SEVERE, ex1.getMessage());
            }
            Logger.getLogger(DataBaseReader.class.getName()).log(Level.SEVERE, ex.getMessage());
        }
    }
}

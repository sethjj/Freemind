/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freemind.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author woo
 */
public class TreeNode {
    
    private int oid;
    private String id;
    private String parentId;
    private String label;
    private Map attributes = new HashMap();

    public TreeNode(int oid, String id, String parentId, String label) {
        super();
        this.oid = oid;
        this.id = id;
        this.parentId = parentId;
        this.label = label;
        setAttribute("oid", ""+oid);
        setAttribute("id", ""+id);
        setAttribute("parentId", ""+parentId);
    }
    
    public void setAttribute(String name, String value) {
        attributes.put(name, value);
    }
    
    public String getAttribute(String name) {
        return (String)attributes.get(name);
    }
    
    public int getOid() {
        return oid;
    }
    public String getId()  {
        return id;
    }
    
    public String getParentId()    {
        return parentId;
    }
    
    public String getLabel()    {
        return label;
    }
    /**
     * Returns a non-terminated xml string from this TreeNode
     * @return 
     */
    public String getXML()  {
        return getXML(false);
    }
    /**
     * Returns an xml string with an optional termination (terminate = true)
     * @param terminate
     * @return 
     */
    public String getXML(boolean terminate)  {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<node");
        buffer.append(" TEXT=\"");
        buffer.append(label);
        buffer.append("\" ");
        buffer.append(" id=\""+id+"\" ");
        buffer.append(">");
        buffer.append(attributesToXML());
        if( terminate )
            buffer.append("</node>");
        return buffer.toString();
    }
    
    private String attributesToXML()  {
        StringBuffer buffer = new StringBuffer();
            Iterator it = attributes.keySet().iterator();
            while( it.hasNext() )   {
                String name = (String)it.next();
                String value = (String)attributes.get(name);
                buffer.append("\n\t<attribute NAME=\"");
                buffer.append(name);
                buffer.append("\" VALUE=\"");
                buffer.append(value);
                buffer.append("\" />");
            }
        return buffer.toString();
    }
    
    public String toString()    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("("+oid);
        buffer.append(","+id);
        buffer.append(", "+parentId);
        buffer.append(", "+label);
        buffer.append(")");
        return buffer.toString();
    }

    public Map getAttributes() {
        return attributes;
    }

    List getAttributeKeys() {
        List keys = new ArrayList();
        Iterator it = attributes.keySet().iterator();
        while( it.hasNext() )   {
            String key = (String)it.next();
            if( !keys.contains(key) )
                keys.add(key);
        }
        return keys;
    }
    
}

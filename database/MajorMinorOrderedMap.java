/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package freemind.database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * The MajorMinorOrderedMap extends TreeMap so that keys which are composed of
 * major.minor.anything else, where major and minor are integers and are often
 * of the form 2.1, 2.2. 3.1, 3.2, etc. for database efficiency, can be stored
 * and retrieved in the order intended, i.e. 2.1 before 22.3, etc.
 * 
 * The implementation will convert the leading two integers into an integer
 * and store internally, with the iterator being extracted from the internal
 * converted indices and used directly instead of keySet().iterator()
 *
 * @author woo
 */
public class MajorMinorOrderedMap extends TreeMap {
    
    private TreeMap majorMinorKeyMap = new TreeMap();
    private static final int[] powers = {10, 100, 1000, 10000, 100000};
    public static final String[] testValues1 = {"1.1", "1.2", "1.3", "2.1", "2.2"};
    public static final String[] testValues2 = {"6.37.20", "6.38.30", "10.1.10", "10.2.20"};
    
    public MajorMinorOrderedMap() {
        super();
    }
   
    public Object put( Object key, Object value)  {
        majorMinorKeyMap.put(majorMinorKey(key), key);
        return super.put(key, value);
    }

    public Iterator iterator()    {
        List keyset = new ArrayList();
        Iterator it = majorMinorKeyMap.keySet().iterator();
        while( it.hasNext() )   {
            Object key = it.next();
            keyset.add(majorMinorKeyMap.get(key));
        }
        return keyset.iterator();
    }
    
    private Integer majorMinorKey(Object key) {
        String keyStr = key.toString();
        Integer theKey = null;
        StringTokenizer tokens = new StringTokenizer(keyStr, ".");
        int value = 0;
        int count = tokens.countTokens();
        if( count == 0 )
            return theKey;
        value = Integer.parseInt(tokens.nextToken());
        String decimalPart = tokens.nextToken();
        int places = decimalPart.length();
        value = value*100000 + Integer.parseInt(decimalPart);
        return new Integer(value);
    }
    
    public static void main(String[] args)  {
        MajorMinorOrderedMap mmom = new MajorMinorOrderedMap();
        System.out.println("testValues1 using MajorMinorOrderedMap");
        for( int i=0; i<testValues1.length; i++ )   {
            mmom.put(testValues1[i], testValues1[i]);
        }
        Iterator it = mmom.iterator();
        while( it.hasNext() )   {
            String key = (String)it.next();
            String value = (String)mmom.get(key);
            System.out.println("key, value = "+key+", "+value);
        }
        System.out.println("\ntestValues2 using MajorMinorOrderedMap");
        mmom = new MajorMinorOrderedMap();
        for( int i=0; i<testValues2.length; i++ )   {
            mmom.put(testValues2[i], testValues2[i]);
        }
        it = mmom.iterator();
        while( it.hasNext() )   {
            String key = (String)it.next();
            String value = (String)mmom.get(key);
            System.out.println("key, value = "+key+", "+value);
        }
        System.out.println("\nCompare to TreeMap testValues2");
        TreeMap tm = new TreeMap();
        for( int i=0; i<testValues2.length; i++ )   {
            tm.put(testValues2[i], testValues2[i]);
        }
        it = tm.keySet().iterator();
        while( it.hasNext() )   {
            String key = (String)it.next();
            String value = (String)tm.get(key);
            System.out.println("key, value = "+key+", "+value);
        }
        System.out.println("\nAll Done!");
    }
    
}


package com.openexchange.admin;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import junit.framework.TestCase;

/**
 *
 * @author cutmasta
 */
public abstract class AbstractAdminTest extends TestCase{
    
    protected  static String TEST_DOMAIN = "example.org";
    protected  static String change_suffix = "_changed";
    
    @SuppressWarnings("unchecked")
    protected static void parseResponse(Vector resp) throws Exception{
        if(resp.size()==0){
            throw new Exception("Invalid Response:");
        }else{
            if(!(resp.get(0)!=null && resp.get(0).toString().equals("OK"))){                
                if(resp.size()>1){                
                    throw new Exception ("Error: "+resp.get(1));
                }else{
                    throw new Exception("Error without message");
                }
            }
        }
    }
    
    protected static String checkHost(String host) throws Exception{
        if(!host.startsWith("rmi://")){
            host = "rmi://"+host;
        }
        if(!host.endsWith("/")){
            host = host+"/";
        }
        return host;
    }
    
    protected static void log(Object obj){
        System.out.println(""+obj);
    }
    
    protected static void compareStringArray(String[] a,String[]b){
        if(a==null){
            assertNotNull("expected null array",b);
        }else{
            assertNotNull("array is null",b);
        }
        assertEquals("expected same size",a.length,b.length);
        SortedSet<String> aa = new TreeSet<String>();
        SortedSet<String> bb = new TreeSet<String>();
        for(int cc = 0;cc<a.length;cc++){
            aa.add(a[cc]);
            bb.add(b[cc]);
        }
//        boolean test = aa.(bb);
        assertEquals(aa,bb);
    }
   
}

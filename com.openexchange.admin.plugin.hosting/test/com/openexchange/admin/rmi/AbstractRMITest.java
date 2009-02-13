
package com.openexchange.admin.rmi;

import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;

public abstract class AbstractRMITest extends AbstractTest {

    public Credentials testCredentials;
    public Credentials adminCredentials;
    public Context testContext;
    public Context adminContext;

    @Before
    public void setUp() throws Exception {
        testCredentials = DummyCredentials();
        testContext = getTestContextObject(testCredentials);
        adminCredentials = new Credentials("oxadminmaster","secret");
        adminContext = getTestContextObject(adminCredentials);
    }

    @After
    public void tearDown() throws Exception {
        
    }

    /**
     * compares two user arrays by retrieving all the IDs they contain
     * an checking if they match. Ignores duplicate entries, ignores
     * users without an ID at all.
     */
    public void assertIDsAreEqual(User[] arr1, User[] arr2) {
        Set<Integer> set1 = new HashSet<Integer>();
        for(int i = 0; i < arr1.length; i++){
            set1.add( arr1[i].getId() );
        }
        Set<Integer> set2 = new HashSet<Integer>();
        for(int i = 0; i < arr1.length; i++){
            set2.add( arr2[i].getId() );
        }
        
        assertEquals("Both arrays should return the same IDs", set1, set2 );
    
    }

    public Integer getContextID() {
        return new Integer(1);
    }

    public Credentials getCredentials() {
        return new Credentials("oxadmin","secret");
    }

    public String getHostName() {
        return "localhost";
    }

    /**
     * Creates a URL that can be used for testing locally.
     * Needs a RMI-class name added to it for lookup, though.
     * @return
     */
    public String getRMIHostUrl(String classname) {
        String host = "localhost";
        
        if(System.getProperty("host")!=null){
            host = System.getProperty("host");
        }        
        
        if(!host.startsWith("rmi://")){
            host = "rmi://"+host;
        }
        if(!host.endsWith("/")){
            host = host+"/";
        }
        return host + classname;
    }

    /**
     * Initializes a new {@link AbstractRMITest}.
     */
    public AbstractRMITest() {
        super();
    }
    
    /*** ANY & friends ***/
    protected interface Verifier<T>{
        public boolean verify(T obj1, Object obj2);
    }
    
    public <T> boolean any(Collection<T> collection, Object searched, Verifier<T> verifier){
        for(T elem: collection){
            if(verifier.verify(elem, searched))
                return true;
        }
        return false;
    }

    public <T> boolean any(T[] collection, Object searched, Verifier<T> verifier){
        return any(Arrays.asList(collection), searched, verifier);
    }

}

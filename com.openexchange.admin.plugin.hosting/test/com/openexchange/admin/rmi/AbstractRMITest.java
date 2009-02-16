
package com.openexchange.admin.rmi;

import static org.junit.Assert.assertEquals;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchResourceException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public abstract class AbstractRMITest extends AbstractTest {

    public Credentials testCredentials;
    public Credentials adminCredentials;
    public Context testContext;
    public Context adminContext;
    public User adminUser;
    public User testUser;
    protected Resource testResource;

    @Before
    public void setUp() throws Exception {
        testCredentials = DummyCredentials();
        testContext = getTestContextObject(testCredentials);

        adminUser = newUser("oxadminmaster","secret","ContextCreatingAdmin","Ad","Min","adminmaster@ox.invalid");
        adminCredentials = new Credentials(adminUser.getName(),adminUser.getPassword());
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
    
    /**
     * Checks whether users have the same mandatory fields. 
     * See #newUser to know which fields are mandatory. 
     * @param expected
     * @param actual
     */
    public void assertUserEquals(User expected, User actual){
        assertEquals("Name should be equal", expected.getName(), actual.getName() );
        assertEquals("Display name should be equal", expected.getDisplay_name(), actual.getDisplay_name() );
        assertEquals("Given name should be equal", expected.getGiven_name(), actual.getGiven_name() );
        assertEquals("Surname should be equal", expected.getSur_name(), actual.getSur_name() );
        assertEquals("Primary E-Mail should be equal", expected.getPrimaryEmail(), actual.getPrimaryEmail() );
        assertEquals("E-Mail #1 should be equal", expected.getEmail1(), actual.getEmail1() );
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
    public void assertUserWasCreatedProperly(User expected, Context context, Credentials credentials) throws Exception{
        OXUserInterface userInterface = getUserInterface();
        User lookupUser = new User();
        lookupUser.setId( expected.getId() );
        lookupUser = userInterface.getData(context, lookupUser, credentials);
        assertUserEquals(expected, lookupUser);
    }
    
    /**
     * Creates a user with all mandatory fields set.
     * @param name
     * @param passwd
     * @param displayName
     * @param givenName
     * @param surname
     * @param email
     * @return
     */
    public User newUser(String name, String passwd, String displayName, String givenName, String surname, String email){
        User user = new User();
        user.setName(name);
        user.setPassword(passwd);
        user.setDisplay_name(displayName);
        user.setGiven_name(givenName);
        user.setSur_name(surname);
        user.setPrimaryEmail(email);
        user.setEmail1(email);
        return user;
    }
    
    /*** Interfaces ***/
    
    public OXGroupInterface getGroupInterface() throws MalformedURLException, RemoteException, NotBoundException{
         return (OXGroupInterface) Naming.lookup( getRMIHostUrl( OXGroupInterface.RMI_NAME ) );
    }
    
    public OXUserInterface getUserInterface() throws MalformedURLException, RemoteException, NotBoundException{
         return (OXUserInterface) Naming.lookup( getRMIHostUrl( OXUserInterface.RMI_NAME ) );
    }
    
    public OXContextInterface getContextInterface() throws MalformedURLException, RemoteException, NotBoundException{
        return (OXContextInterface) Naming.lookup( getRMIHostUrl( OXContextInterface.RMI_NAME ) );
    }
    
    public OXResourceInterface getResourceInterface() throws MalformedURLException, RemoteException, NotBoundException{
        return (OXResourceInterface) Naming.lookup( getRMIHostUrl( OXResourceInterface.RMI_NAME ) );
    }


    /**
     * Creates a URL that can be used for testing locally.
     * Needs a RMI-class name added to it for lookup, though.
     * @return
     */
    public String getRMIHostUrl(String classname) {
        String host = getHostName();
        
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
    protected interface Verifier<T,S>{
        public boolean verify(T obj1, S obj2);
    }
    
    public <T,S> boolean any(Collection<T> collection, S searched, Verifier<T,S> verifier){
        for(T elem: collection){
            if(verifier.verify(elem, searched))
                return true;
        }
        return false;
    }

    public <T,S> boolean any(T[] collection, S searched, Verifier<T,S> verifier){
        return any(Arrays.asList(collection), searched, verifier);
    }
    
    /*** Creating test objects on the server ***/
    
    public Resource getTestResource(){
        if(testResource != null && testResource.getId() != null)
            return testResource;
        Resource res = new Resource();
        res.setName("Testresource");
        res.setEmail("test-resource@testsystem.invalid");
        res.setDisplayname("The test resource");
        return res;
    }
    /** Create a test resource on the server. Always remove this via #removeTestResource() afterwards!
     * 
     * @throws DatabaseUpdateException 
     * @throws InvalidDataException 
     * @throws NoSuchContextException 
     * @throws InvalidCredentialsException 
     * @throws StorageException 
     * @throws RemoteException 
     * @throws NotBoundException 
     * @throws MalformedURLException
     * */ 
    public Resource createTestResource() throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, MalformedURLException, NotBoundException{
        OXResourceInterface resInterface = (OXResourceInterface) Naming.lookup( getRMIHostUrl( OXResourceInterface.RMI_NAME ) );
        testResource = resInterface.create(testContext, getTestResource(), testCredentials);
        return testResource;
    }

    public void removeTestResource() throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, MalformedURLException, NotBoundException{
        OXResourceInterface resInterface = (OXResourceInterface) Naming.lookup( getRMIHostUrl( OXResourceInterface.RMI_NAME ) );
        try {
            resInterface.delete(testContext, testResource, testCredentials);
        } catch (NoSuchResourceException e) {
            // don't do anything, has been removed already, right?
            System.out.println("Resource was removed already");
        }
    }
}

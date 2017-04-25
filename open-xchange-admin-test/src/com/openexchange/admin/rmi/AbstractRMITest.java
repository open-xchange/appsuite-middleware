
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
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchResourceException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.user.copy.rmi.OXUserCopyInterface;

public abstract class AbstractRMITest extends AbstractTest {

    public static final String ADMIN_USER = "oxadmin";

    public static final String ADMIN_PW = "secret";

    public static final String MASTER_PW = "secret";

    public static final String OXADMINMASTER = "oxadminmaster";

    public static String ctxid = "666";

    public String prefix = "/opt/open-xchange/sbin/";

    public Credentials adminCredentials;
    public Credentials superAdminCredentials;
    public Context adminContext;
    public Context superAdminContext;
    public User superAdmin;
    public User testUser;
    protected Resource testResource;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        adminCredentials = DummyCredentials();
        adminContext = getTestContextObject(adminCredentials);

        superAdmin = newUser(OXADMINMASTER, MASTER_PW, "ContextCreatingAdmin", "Ad", "Min", "adminmaster@ox.invalid");
        superAdminCredentials = DummyMasterCredentials();
        superAdminContext = getTestContextObject(superAdminCredentials);
    }

    public Integer getContextID() {
        return new Integer(1);
    }

    public Credentials getCredentials() {
        return new Credentials(ADMIN_USER, ADMIN_PW);
    }

    public User getAdminData() throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchUserException, DatabaseUpdateException, MalformedURLException, NotBoundException{
        User admin = new User();
        admin.setId(Integer.valueOf(2));
        OXUserInterface userInterface = getUserInterface();
        admin = userInterface.getData(adminContext, admin, adminCredentials);
        return admin;
    }

    /**
     * compares two user arrays by retrieving all the IDs they contain
     * an checking if they match. Ignores duplicate entries, ignores
     * users without an ID at all.
     */
    public void assertIDsAreEqual(User[] arr1, User[] arr2) {
        Set<Integer> set1 = new HashSet<Integer>();
        for (User element : arr1) {
            set1.add( element.getId() );
        }
        Set<Integer> set2 = new HashSet<Integer>();
        for(int i = 0; i < arr1.length; i++){
            set2.add( arr2[i].getId() );
        }

        assertEquals("Both arrays should return the same IDs", set1, set2 );
    }

    /*** Asserts for mandatory fields ***/

    public void assertUserEquals(User expected, User actual){
        assertEquals("Name should match", expected.getName(), actual.getName() );
        assertEquals("Display name should match", expected.getDisplay_name(), actual.getDisplay_name() );
        assertEquals("Given name should match", expected.getGiven_name(), actual.getGiven_name() );
        assertEquals("Surname should match", expected.getSur_name(), actual.getSur_name() );
        assertEquals("Primary E-Mail should match", expected.getPrimaryEmail(), actual.getPrimaryEmail() );
        assertEquals("E-Mail #1 should match", expected.getEmail1(), actual.getEmail1() );
    }

    public void assertGroupEquals(Group expected, Group actual){
        assertEquals("Display name should match", expected.getDisplayname(), actual.getDisplayname());
        assertEquals("Name should match", expected.getName(), actual.getName());
    }

    public void assertResourceEquals(Resource expected, Resource actual){
        assertEquals("Display name should match", expected.getDisplayname(), actual.getDisplayname());
        assertEquals("Name should match", expected.getName(), actual.getName());
        assertEquals("E-Mail should match", expected.getEmail(), actual.getEmail());
    }


    /*** Asserting proper creation by comparing data in the database with given one ***/

    public void assertUserWasCreatedProperly(User expected, Context context, Credentials credentials) throws Exception{
        OXUserInterface userInterface = getUserInterface();
        User lookupUser = new User();
        lookupUser.setId( expected.getId() );
        lookupUser = userInterface.getData(context, lookupUser, credentials);
        assertUserEquals(expected, lookupUser);
    }

    public void assertGroupWasCreatedProperly(Group expected, Context context, Credentials credentials) throws Exception{
        OXGroupInterface groupInterface = getGroupInterface();
        Group lookup = new Group();
        lookup.setId( expected.getId() );
        lookup = groupInterface.getData(context, lookup, credentials);
        assertGroupEquals(expected, lookup);
    }

    public void assertResourceWasCreatedProperly(Resource expected, Context context, Credentials credentials) throws Exception{
        OXResourceInterface resInterface = getResourceInterface();
        Resource lookup = new Resource();
        lookup.setId( expected.getId() );
        lookup = resInterface.getData(context, lookup, credentials);
        assertResourceEquals(expected, lookup);
    }

    /*** pseudo constructors requiring mandatory fields ***/

    public static User newUser(String name, String passwd, String displayName, String givenName, String surname, String email){
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

    public static Group newGroup(String displayName, String name){
        Group group = new Group();
        group.setDisplayname(displayName);
        group.setName(name);
        return group;
    }

    public static Resource newResource(String name, String displayName, String email){
        Resource res = new Resource();
        res.setName(name);
        res.setDisplayname(displayName);
        res.setEmail(email);
        return res;
    }

    public static Context newContext(String name, int id){
        Context newContext = new Context();
        Filestore filestore = new Filestore();
        filestore.setSize(Long.valueOf(128l));
        newContext.setFilestoreId(filestore.getId());
        newContext.setName(name);
        newContext.setMaxQuota(filestore.getSize());
        newContext.setId( Integer.valueOf(id) );
        return newContext;
    }

    /*** Interfaces ***/

    public OXGroupInterface getGroupInterface() throws MalformedURLException, RemoteException, NotBoundException{
        return (OXGroupInterface) Naming.lookup(getRMIHostUrl() + OXGroupInterface.RMI_NAME);
    }

    public OXUserInterface getUserInterface() throws MalformedURLException, RemoteException, NotBoundException{
        return (OXUserInterface) Naming.lookup(getRMIHostUrl() + OXUserInterface.RMI_NAME);
    }

    public OXContextInterface getContextInterface() throws MalformedURLException, RemoteException, NotBoundException{
        return (OXContextInterface) Naming.lookup(getRMIHostUrl() + OXContextInterface.RMI_NAME);
    }

    public OXResourceInterface getResourceInterface() throws MalformedURLException, RemoteException, NotBoundException{
        return (OXResourceInterface) Naming.lookup(getRMIHostUrl() + OXResourceInterface.RMI_NAME);
    }

    public OXUserCopyInterface getUserCopyClient() throws MalformedURLException, RemoteException, NotBoundException {
        return (OXUserCopyInterface) Naming.lookup(getRMIHostUrl() + OXUserCopyInterface.RMI_NAME);
    }

    public OXUtilInterface getUtilInterface() throws MalformedURLException, RemoteException, NotBoundException {
        return (OXUtilInterface) Naming.lookup(getRMIHostUrl() + OXUtilInterface.RMI_NAME);
    }

    public OXTaskMgmtInterface getTaskInterface() throws MalformedURLException, RemoteException, NotBoundException {
        return (OXTaskMgmtInterface) Naming.lookup(getRMIHostUrl() + OXTaskMgmtInterface.RMI_NAME);
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
            if(verifier.verify(elem, searched)) {
                return true;
            }
        }
        return false;
    }

    public <T,S> boolean any(T[] collection, S searched, Verifier<T,S> verifier){
        return any(Arrays.asList(collection), searched, verifier);
    }

    /*** Creating test objects on the server ***/

    public Resource getTestResource(){
        if(testResource != null && testResource.getId() != null) {
            return testResource;
        }
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
        OXResourceInterface resInterface = getResourceInterface();
        testResource = resInterface.create(adminContext, getTestResource(), adminCredentials);
        return testResource;
    }

    public void removeTestResource() throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, MalformedURLException, NotBoundException{
        OXResourceInterface resInterface = getResourceInterface();
        try {
            resInterface.delete(adminContext, testResource, adminCredentials);
        } catch (NoSuchResourceException e) {
            // don't do anything, has been removed already, right?
            System.out.println("Resource was removed already");
        }
    }
}

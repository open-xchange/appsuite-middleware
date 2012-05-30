
package com.openexchange.admin.soap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import com.openexchange.admin.rmi.dataobjects.xsd.Credentials;
import com.openexchange.admin.soap.dataobjects.xsd.Context;
import com.openexchange.admin.soap.dataobjects.xsd.Database;
import com.openexchange.admin.soap.dataobjects.xsd.Filestore;
import com.openexchange.admin.soap.dataobjects.xsd.User;
import com.openexchange.admin.soap.dataobjects.xsd.UserModuleAccess;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.openexchange.admin.soap package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _EnableAllAuth_QNAME = new QName("http://soap.admin.openexchange.com", "auth");
    private final static QName _NoSuchContextExceptionNoSuchContextException_QNAME = new QName("http://soap.admin.openexchange.com", "NoSuchContextException");
    private final static QName _DowngradeCtx_QNAME = new QName("http://soap.admin.openexchange.com", "ctx");
    private final static QName _CreateAdminUser_QNAME = new QName("http://soap.admin.openexchange.com", "admin_user");
    private final static QName _CreateModuleAccessByNameAccessCombinationName_QNAME = new QName("http://soap.admin.openexchange.com", "access_combination_name");
    private final static QName _NoSuchReasonExceptionNoSuchReasonException_QNAME = new QName("http://soap.admin.openexchange.com", "NoSuchReasonException");
    private final static QName _MoveContextDatabaseDstDatabaseId_QNAME = new QName("http://soap.admin.openexchange.com", "dst_database_id");
    private final static QName _ListByFilestoreFs_QNAME = new QName("http://soap.admin.openexchange.com", "fs");
    private final static QName _CreateModuleAccessByNameResponseReturn_QNAME = new QName("http://soap.admin.openexchange.com", "return");
    private final static QName _InvalidCredentialsExceptionInvalidCredentialsException_QNAME = new QName("http://soap.admin.openexchange.com", "InvalidCredentialsException");
    private final static QName _ExceptionException_QNAME = new QName("http://soap.admin.openexchange.com", "Exception");
    private final static QName _ListByDatabaseDb_QNAME = new QName("http://soap.admin.openexchange.com", "db");
    private final static QName _MoveContextFilestoreDstFilestoreId_QNAME = new QName("http://soap.admin.openexchange.com", "dst_filestore_id");
    private final static QName _ChangeModuleAccessAccess_QNAME = new QName("http://soap.admin.openexchange.com", "access");
    private final static QName _InvalidDataExceptionInvalidDataException_QNAME = new QName("http://soap.admin.openexchange.com", "InvalidDataException");
    private final static QName _ListSearchPattern_QNAME = new QName("http://soap.admin.openexchange.com", "search_pattern");
    private final static QName _NoSuchDatabaseExceptionNoSuchDatabaseException_QNAME = new QName("http://soap.admin.openexchange.com", "NoSuchDatabaseException");
    private final static QName _StorageExceptionStorageException_QNAME = new QName("http://soap.admin.openexchange.com", "StorageException");
    private final static QName _ContextExistsExceptionContextExistsException_QNAME = new QName("http://soap.admin.openexchange.com", "ContextExistsException");
    private final static QName _DatabaseUpdateExceptionDatabaseUpdateException_QNAME = new QName("http://soap.admin.openexchange.com", "DatabaseUpdateException");
    private final static QName _RemoteExceptionRemoteException_QNAME = new QName("http://soap.admin.openexchange.com", "RemoteException");
    private final static QName _OXContextExceptionOXContextException_QNAME = new QName("http://soap.admin.openexchange.com", "OXContextException");
    private final static QName _NoSuchFilestoreExceptionNoSuchFilestoreException_QNAME = new QName("http://soap.admin.openexchange.com", "NoSuchFilestoreException");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.openexchange.admin.soap
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link com.openexchange.admin.soap.InvalidDataException }
     * 
     */
    public com.openexchange.admin.soap.InvalidDataException createInvalidDataException() {
        return new com.openexchange.admin.soap.InvalidDataException();
    }

    /**
     * Create an instance of {@link MoveContextDatabase }
     * 
     */
    public MoveContextDatabase createMoveContextDatabase() {
        return new MoveContextDatabase();
    }

    /**
     * Create an instance of {@link MoveContextFilestore }
     * 
     */
    public MoveContextFilestore createMoveContextFilestore() {
        return new MoveContextFilestore();
    }

    /**
     * Create an instance of {@link Change }
     * 
     */
    public Change createChange() {
        return new Change();
    }

    /**
     * Create an instance of {@link Enable }
     * 
     */
    public Enable createEnable() {
        return new Enable();
    }

    /**
     * Create an instance of {@link com.openexchange.admin.soap.NoSuchContextException }
     * 
     */
    public com.openexchange.admin.soap.NoSuchContextException createNoSuchContextException() {
        return new com.openexchange.admin.soap.NoSuchContextException();
    }

    /**
     * Create an instance of {@link ExistsResponse }
     * 
     */
    public ExistsResponse createExistsResponse() {
        return new ExistsResponse();
    }

    /**
     * Create an instance of {@link CreateModuleAccess }
     * 
     */
    public CreateModuleAccess createCreateModuleAccess() {
        return new CreateModuleAccess();
    }

    /**
     * Create an instance of {@link ListAll }
     * 
     */
    public ListAll createListAll() {
        return new ListAll();
    }

    /**
     * Create an instance of {@link ListByFilestore }
     * 
     */
    public ListByFilestore createListByFilestore() {
        return new ListByFilestore();
    }

    /**
     * Create an instance of {@link GetModuleAccess }
     * 
     */
    public GetModuleAccess createGetModuleAccess() {
        return new GetModuleAccess();
    }

    /**
     * Create an instance of {@link com.openexchange.admin.soap.StorageException }
     * 
     */
    public com.openexchange.admin.soap.StorageException createStorageException() {
        return new com.openexchange.admin.soap.StorageException();
    }

    /**
     * Create an instance of {@link GetAccessCombinationName }
     * 
     */
    public GetAccessCombinationName createGetAccessCombinationName() {
        return new GetAccessCombinationName();
    }

    /**
     * Create an instance of {@link ChangeModuleAccessByName }
     * 
     */
    public ChangeModuleAccessByName createChangeModuleAccessByName() {
        return new ChangeModuleAccessByName();
    }

    /**
     * Create an instance of {@link CreateModuleAccessByNameResponse }
     * 
     */
    public CreateModuleAccessByNameResponse createCreateModuleAccessByNameResponse() {
        return new CreateModuleAccessByNameResponse();
    }

    /**
     * Create an instance of {@link CreateResponse }
     * 
     */
    public CreateResponse createCreateResponse() {
        return new CreateResponse();
    }

    /**
     * Create an instance of {@link Delete }
     * 
     */
    public Delete createDelete() {
        return new Delete();
    }

    /**
     * Create an instance of {@link GetAdminId }
     * 
     */
    public GetAdminId createGetAdminId() {
        return new GetAdminId();
    }

    /**
     * Create an instance of {@link MoveContextDatabaseResponse }
     * 
     */
    public MoveContextDatabaseResponse createMoveContextDatabaseResponse() {
        return new MoveContextDatabaseResponse();
    }

    /**
     * Create an instance of {@link Disable }
     * 
     */
    public Disable createDisable() {
        return new Disable();
    }

    /**
     * Create an instance of {@link MoveContextFilestoreResponse }
     * 
     */
    public MoveContextFilestoreResponse createMoveContextFilestoreResponse() {
        return new MoveContextFilestoreResponse();
    }

    /**
     * Create an instance of {@link Downgrade }
     * 
     */
    public Downgrade createDowngrade() {
        return new Downgrade();
    }

    /**
     * Create an instance of {@link CreateModuleAccessResponse }
     * 
     */
    public CreateModuleAccessResponse createCreateModuleAccessResponse() {
        return new CreateModuleAccessResponse();
    }

    /**
     * Create an instance of {@link com.openexchange.admin.soap.RemoteException }
     * 
     */
    public com.openexchange.admin.soap.RemoteException createRemoteException() {
        return new com.openexchange.admin.soap.RemoteException();
    }

    /**
     * Create an instance of {@link com.openexchange.admin.soap.NoSuchDatabaseException }
     * 
     */
    public com.openexchange.admin.soap.NoSuchDatabaseException createNoSuchDatabaseException() {
        return new com.openexchange.admin.soap.NoSuchDatabaseException();
    }

    /**
     * Create an instance of {@link ListByFilestoreResponse }
     * 
     */
    public ListByFilestoreResponse createListByFilestoreResponse() {
        return new ListByFilestoreResponse();
    }

    /**
     * Create an instance of {@link com.openexchange.admin.soap.NoSuchReasonException }
     * 
     */
    public com.openexchange.admin.soap.NoSuchReasonException createNoSuchReasonException() {
        return new com.openexchange.admin.soap.NoSuchReasonException();
    }

    /**
     * Create an instance of {@link List }
     * 
     */
    public List createList() {
        return new List();
    }

    /**
     * Create an instance of {@link Create }
     * 
     */
    public Create createCreate() {
        return new Create();
    }

    /**
     * Create an instance of {@link GetAdminIdResponse }
     * 
     */
    public GetAdminIdResponse createGetAdminIdResponse() {
        return new GetAdminIdResponse();
    }

    /**
     * Create an instance of {@link com.openexchange.admin.soap.OXContextException }
     * 
     */
    public com.openexchange.admin.soap.OXContextException createOXContextException() {
        return new com.openexchange.admin.soap.OXContextException();
    }

    /**
     * Create an instance of {@link ListAllResponse }
     * 
     */
    public ListAllResponse createListAllResponse() {
        return new ListAllResponse();
    }

    /**
     * Create an instance of {@link ChangeModuleAccess }
     * 
     */
    public ChangeModuleAccess createChangeModuleAccess() {
        return new ChangeModuleAccess();
    }

    /**
     * Create an instance of {@link GetAccessCombinationNameResponse }
     * 
     */
    public GetAccessCombinationNameResponse createGetAccessCombinationNameResponse() {
        return new GetAccessCombinationNameResponse();
    }

    /**
     * Create an instance of {@link ListByDatabase }
     * 
     */
    public ListByDatabase createListByDatabase() {
        return new ListByDatabase();
    }

    /**
     * Create an instance of {@link ListByDatabaseResponse }
     * 
     */
    public ListByDatabaseResponse createListByDatabaseResponse() {
        return new ListByDatabaseResponse();
    }

    /**
     * Create an instance of {@link com.openexchange.admin.soap.ContextExistsException }
     * 
     */
    public com.openexchange.admin.soap.ContextExistsException createContextExistsException() {
        return new com.openexchange.admin.soap.ContextExistsException();
    }

    /**
     * Create an instance of {@link com.openexchange.admin.soap.InvalidCredentialsException }
     * 
     */
    public com.openexchange.admin.soap.InvalidCredentialsException createInvalidCredentialsException() {
        return new com.openexchange.admin.soap.InvalidCredentialsException();
    }

    /**
     * Create an instance of {@link GetData }
     * 
     */
    public GetData createGetData() {
        return new GetData();
    }

    /**
     * Create an instance of {@link Exists }
     * 
     */
    public Exists createExists() {
        return new Exists();
    }

    /**
     * Create an instance of {@link com.openexchange.admin.soap.NoSuchFilestoreException }
     * 
     */
    public com.openexchange.admin.soap.NoSuchFilestoreException createNoSuchFilestoreException() {
        return new com.openexchange.admin.soap.NoSuchFilestoreException();
    }

    /**
     * Create an instance of {@link ListResponse }
     * 
     */
    public ListResponse createListResponse() {
        return new ListResponse();
    }

    /**
     * Create an instance of {@link GetModuleAccessResponse }
     * 
     */
    public GetModuleAccessResponse createGetModuleAccessResponse() {
        return new GetModuleAccessResponse();
    }

    /**
     * Create an instance of {@link GetDataResponse }
     * 
     */
    public GetDataResponse createGetDataResponse() {
        return new GetDataResponse();
    }

    /**
     * Create an instance of {@link com.openexchange.admin.soap.DatabaseUpdateException }
     * 
     */
    public com.openexchange.admin.soap.DatabaseUpdateException createDatabaseUpdateException() {
        return new com.openexchange.admin.soap.DatabaseUpdateException();
    }

    /**
     * Create an instance of {@link CreateModuleAccessByName }
     * 
     */
    public CreateModuleAccessByName createCreateModuleAccessByName() {
        return new CreateModuleAccessByName();
    }

    /**
     * Create an instance of {@link DisableAll }
     * 
     */
    public DisableAll createDisableAll() {
        return new DisableAll();
    }

    /**
     * Create an instance of {@link EnableAll }
     * 
     */
    public EnableAll createEnableAll() {
        return new EnableAll();
    }

    /**
     * Create an instance of {@link Exception }
     * 
     */
    public Exception createException() {
        return new Exception();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = EnableAll.class)
    public JAXBElement<Credentials> createEnableAllAuth(Credentials value) {
        return new JAXBElement<Credentials>(_EnableAllAuth_QNAME, Credentials.class, EnableAll.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link com.openexchange.admin.rmi.exceptions.xsd.NoSuchContextException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "NoSuchContextException", scope = com.openexchange.admin.soap.NoSuchContextException.class)
    public JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.NoSuchContextException> createNoSuchContextExceptionNoSuchContextException(com.openexchange.admin.rmi.exceptions.xsd.NoSuchContextException value) {
        return new JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.NoSuchContextException>(_NoSuchContextExceptionNoSuchContextException_QNAME, com.openexchange.admin.rmi.exceptions.xsd.NoSuchContextException.class, com.openexchange.admin.soap.NoSuchContextException.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = Downgrade.class)
    public JAXBElement<Credentials> createDowngradeAuth(Credentials value) {
        return new JAXBElement<Credentials>(_EnableAllAuth_QNAME, Credentials.class, Downgrade.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = Downgrade.class)
    public JAXBElement<Context> createDowngradeCtx(Context value) {
        return new JAXBElement<Context>(_DowngradeCtx_QNAME, Context.class, Downgrade.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = Create.class)
    public JAXBElement<Credentials> createCreateAuth(Credentials value) {
        return new JAXBElement<Credentials>(_EnableAllAuth_QNAME, Credentials.class, Create.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link User }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "admin_user", scope = Create.class)
    public JAXBElement<User> createCreateAdminUser(User value) {
        return new JAXBElement<User>(_CreateAdminUser_QNAME, User.class, Create.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = Create.class)
    public JAXBElement<Context> createCreateCtx(Context value) {
        return new JAXBElement<Context>(_DowngradeCtx_QNAME, Context.class, Create.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "access_combination_name", scope = CreateModuleAccessByName.class)
    public JAXBElement<String> createCreateModuleAccessByNameAccessCombinationName(String value) {
        return new JAXBElement<String>(_CreateModuleAccessByNameAccessCombinationName_QNAME, String.class, CreateModuleAccessByName.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = CreateModuleAccessByName.class)
    public JAXBElement<Credentials> createCreateModuleAccessByNameAuth(Credentials value) {
        return new JAXBElement<Credentials>(_EnableAllAuth_QNAME, Credentials.class, CreateModuleAccessByName.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link User }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "admin_user", scope = CreateModuleAccessByName.class)
    public JAXBElement<User> createCreateModuleAccessByNameAdminUser(User value) {
        return new JAXBElement<User>(_CreateAdminUser_QNAME, User.class, CreateModuleAccessByName.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = CreateModuleAccessByName.class)
    public JAXBElement<Context> createCreateModuleAccessByNameCtx(Context value) {
        return new JAXBElement<Context>(_DowngradeCtx_QNAME, Context.class, CreateModuleAccessByName.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = GetModuleAccess.class)
    public JAXBElement<Credentials> createGetModuleAccessAuth(Credentials value) {
        return new JAXBElement<Credentials>(_EnableAllAuth_QNAME, Credentials.class, GetModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = GetModuleAccess.class)
    public JAXBElement<Context> createGetModuleAccessCtx(Context value) {
        return new JAXBElement<Context>(_DowngradeCtx_QNAME, Context.class, GetModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link com.openexchange.admin.rmi.exceptions.xsd.NoSuchReasonException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "NoSuchReasonException", scope = com.openexchange.admin.soap.NoSuchReasonException.class)
    public JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.NoSuchReasonException> createNoSuchReasonExceptionNoSuchReasonException(com.openexchange.admin.rmi.exceptions.xsd.NoSuchReasonException value) {
        return new JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.NoSuchReasonException>(_NoSuchReasonExceptionNoSuchReasonException_QNAME, com.openexchange.admin.rmi.exceptions.xsd.NoSuchReasonException.class, com.openexchange.admin.soap.NoSuchReasonException.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Database }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "dst_database_id", scope = MoveContextDatabase.class)
    public JAXBElement<Database> createMoveContextDatabaseDstDatabaseId(Database value) {
        return new JAXBElement<Database>(_MoveContextDatabaseDstDatabaseId_QNAME, Database.class, MoveContextDatabase.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = MoveContextDatabase.class)
    public JAXBElement<Credentials> createMoveContextDatabaseAuth(Credentials value) {
        return new JAXBElement<Credentials>(_EnableAllAuth_QNAME, Credentials.class, MoveContextDatabase.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = MoveContextDatabase.class)
    public JAXBElement<Context> createMoveContextDatabaseCtx(Context value) {
        return new JAXBElement<Context>(_DowngradeCtx_QNAME, Context.class, MoveContextDatabase.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = ListByFilestore.class)
    public JAXBElement<Credentials> createListByFilestoreAuth(Credentials value) {
        return new JAXBElement<Credentials>(_EnableAllAuth_QNAME, Credentials.class, ListByFilestore.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Filestore }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "fs", scope = ListByFilestore.class)
    public JAXBElement<Filestore> createListByFilestoreFs(Filestore value) {
        return new JAXBElement<Filestore>(_ListByFilestoreFs_QNAME, Filestore.class, ListByFilestore.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "return", scope = CreateModuleAccessByNameResponse.class)
    public JAXBElement<Context> createCreateModuleAccessByNameResponseReturn(Context value) {
        return new JAXBElement<Context>(_CreateModuleAccessByNameResponseReturn_QNAME, Context.class, CreateModuleAccessByNameResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = DisableAll.class)
    public JAXBElement<Credentials> createDisableAllAuth(Credentials value) {
        return new JAXBElement<Credentials>(_EnableAllAuth_QNAME, Credentials.class, DisableAll.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = Delete.class)
    public JAXBElement<Credentials> createDeleteAuth(Credentials value) {
        return new JAXBElement<Credentials>(_EnableAllAuth_QNAME, Credentials.class, Delete.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = Delete.class)
    public JAXBElement<Context> createDeleteCtx(Context value) {
        return new JAXBElement<Context>(_DowngradeCtx_QNAME, Context.class, Delete.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = Change.class)
    public JAXBElement<Credentials> createChangeAuth(Credentials value) {
        return new JAXBElement<Credentials>(_EnableAllAuth_QNAME, Credentials.class, Change.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = Change.class)
    public JAXBElement<Context> createChangeCtx(Context value) {
        return new JAXBElement<Context>(_DowngradeCtx_QNAME, Context.class, Change.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link com.openexchange.admin.rmi.exceptions.xsd.InvalidCredentialsException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "InvalidCredentialsException", scope = com.openexchange.admin.soap.InvalidCredentialsException.class)
    public JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.InvalidCredentialsException> createInvalidCredentialsExceptionInvalidCredentialsException(com.openexchange.admin.rmi.exceptions.xsd.InvalidCredentialsException value) {
        return new JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.InvalidCredentialsException>(_InvalidCredentialsExceptionInvalidCredentialsException_QNAME, com.openexchange.admin.rmi.exceptions.xsd.InvalidCredentialsException.class, com.openexchange.admin.soap.InvalidCredentialsException.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "Exception", scope = Exception.class)
    public JAXBElement<Object> createExceptionException(Object value) {
        return new JAXBElement<Object>(_ExceptionException_QNAME, Object.class, Exception.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = Exists.class)
    public JAXBElement<Credentials> createExistsAuth(Credentials value) {
        return new JAXBElement<Credentials>(_EnableAllAuth_QNAME, Credentials.class, Exists.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = Exists.class)
    public JAXBElement<Context> createExistsCtx(Context value) {
        return new JAXBElement<Context>(_DowngradeCtx_QNAME, Context.class, Exists.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = GetAccessCombinationName.class)
    public JAXBElement<Credentials> createGetAccessCombinationNameAuth(Credentials value) {
        return new JAXBElement<Credentials>(_EnableAllAuth_QNAME, Credentials.class, GetAccessCombinationName.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = GetAccessCombinationName.class)
    public JAXBElement<Context> createGetAccessCombinationNameCtx(Context value) {
        return new JAXBElement<Context>(_DowngradeCtx_QNAME, Context.class, GetAccessCombinationName.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = ListByDatabase.class)
    public JAXBElement<Credentials> createListByDatabaseAuth(Credentials value) {
        return new JAXBElement<Credentials>(_EnableAllAuth_QNAME, Credentials.class, ListByDatabase.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Database }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "db", scope = ListByDatabase.class)
    public JAXBElement<Database> createListByDatabaseDb(Database value) {
        return new JAXBElement<Database>(_ListByDatabaseDb_QNAME, Database.class, ListByDatabase.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UserModuleAccess }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "return", scope = GetModuleAccessResponse.class)
    public JAXBElement<UserModuleAccess> createGetModuleAccessResponseReturn(UserModuleAccess value) {
        return new JAXBElement<UserModuleAccess>(_CreateModuleAccessByNameResponseReturn_QNAME, UserModuleAccess.class, GetModuleAccessResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = MoveContextFilestore.class)
    public JAXBElement<Credentials> createMoveContextFilestoreAuth(Credentials value) {
        return new JAXBElement<Credentials>(_EnableAllAuth_QNAME, Credentials.class, MoveContextFilestore.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Filestore }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "dst_filestore_id", scope = MoveContextFilestore.class)
    public JAXBElement<Filestore> createMoveContextFilestoreDstFilestoreId(Filestore value) {
        return new JAXBElement<Filestore>(_MoveContextFilestoreDstFilestoreId_QNAME, Filestore.class, MoveContextFilestore.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = MoveContextFilestore.class)
    public JAXBElement<Context> createMoveContextFilestoreCtx(Context value) {
        return new JAXBElement<Context>(_DowngradeCtx_QNAME, Context.class, MoveContextFilestore.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = Disable.class)
    public JAXBElement<Credentials> createDisableAuth(Credentials value) {
        return new JAXBElement<Credentials>(_EnableAllAuth_QNAME, Credentials.class, Disable.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = Disable.class)
    public JAXBElement<Context> createDisableCtx(Context value) {
        return new JAXBElement<Context>(_DowngradeCtx_QNAME, Context.class, Disable.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UserModuleAccess }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "access", scope = ChangeModuleAccess.class)
    public JAXBElement<UserModuleAccess> createChangeModuleAccessAccess(UserModuleAccess value) {
        return new JAXBElement<UserModuleAccess>(_ChangeModuleAccessAccess_QNAME, UserModuleAccess.class, ChangeModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = ChangeModuleAccess.class)
    public JAXBElement<Credentials> createChangeModuleAccessAuth(Credentials value) {
        return new JAXBElement<Credentials>(_EnableAllAuth_QNAME, Credentials.class, ChangeModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = ChangeModuleAccess.class)
    public JAXBElement<Context> createChangeModuleAccessCtx(Context value) {
        return new JAXBElement<Context>(_DowngradeCtx_QNAME, Context.class, ChangeModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "return", scope = CreateResponse.class)
    public JAXBElement<Context> createCreateResponseReturn(Context value) {
        return new JAXBElement<Context>(_CreateModuleAccessByNameResponseReturn_QNAME, Context.class, CreateResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link com.openexchange.admin.rmi.exceptions.xsd.InvalidDataException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "InvalidDataException", scope = com.openexchange.admin.soap.InvalidDataException.class)
    public JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.InvalidDataException> createInvalidDataExceptionInvalidDataException(com.openexchange.admin.rmi.exceptions.xsd.InvalidDataException value) {
        return new JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.InvalidDataException>(_InvalidDataExceptionInvalidDataException_QNAME, com.openexchange.admin.rmi.exceptions.xsd.InvalidDataException.class, com.openexchange.admin.soap.InvalidDataException.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = List.class)
    public JAXBElement<Credentials> createListAuth(Credentials value) {
        return new JAXBElement<Credentials>(_EnableAllAuth_QNAME, Credentials.class, List.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "search_pattern", scope = List.class)
    public JAXBElement<String> createListSearchPattern(String value) {
        return new JAXBElement<String>(_ListSearchPattern_QNAME, String.class, List.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = GetAdminId.class)
    public JAXBElement<Credentials> createGetAdminIdAuth(Credentials value) {
        return new JAXBElement<Credentials>(_EnableAllAuth_QNAME, Credentials.class, GetAdminId.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = GetAdminId.class)
    public JAXBElement<Context> createGetAdminIdCtx(Context value) {
        return new JAXBElement<Context>(_DowngradeCtx_QNAME, Context.class, GetAdminId.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link com.openexchange.admin.rmi.exceptions.xsd.NoSuchDatabaseException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "NoSuchDatabaseException", scope = com.openexchange.admin.soap.NoSuchDatabaseException.class)
    public JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.NoSuchDatabaseException> createNoSuchDatabaseExceptionNoSuchDatabaseException(com.openexchange.admin.rmi.exceptions.xsd.NoSuchDatabaseException value) {
        return new JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.NoSuchDatabaseException>(_NoSuchDatabaseExceptionNoSuchDatabaseException_QNAME, com.openexchange.admin.rmi.exceptions.xsd.NoSuchDatabaseException.class, com.openexchange.admin.soap.NoSuchDatabaseException.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "return", scope = CreateModuleAccessResponse.class)
    public JAXBElement<Context> createCreateModuleAccessResponseReturn(Context value) {
        return new JAXBElement<Context>(_CreateModuleAccessByNameResponseReturn_QNAME, Context.class, CreateModuleAccessResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link com.openexchange.admin.rmi.exceptions.xsd.StorageException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "StorageException", scope = com.openexchange.admin.soap.StorageException.class)
    public JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.StorageException> createStorageExceptionStorageException(com.openexchange.admin.rmi.exceptions.xsd.StorageException value) {
        return new JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.StorageException>(_StorageExceptionStorageException_QNAME, com.openexchange.admin.rmi.exceptions.xsd.StorageException.class, com.openexchange.admin.soap.StorageException.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link com.openexchange.admin.rmi.exceptions.xsd.ContextExistsException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ContextExistsException", scope = com.openexchange.admin.soap.ContextExistsException.class)
    public JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.ContextExistsException> createContextExistsExceptionContextExistsException(com.openexchange.admin.rmi.exceptions.xsd.ContextExistsException value) {
        return new JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.ContextExistsException>(_ContextExistsExceptionContextExistsException_QNAME, com.openexchange.admin.rmi.exceptions.xsd.ContextExistsException.class, com.openexchange.admin.soap.ContextExistsException.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "return", scope = GetAccessCombinationNameResponse.class)
    public JAXBElement<String> createGetAccessCombinationNameResponseReturn(String value) {
        return new JAXBElement<String>(_CreateModuleAccessByNameResponseReturn_QNAME, String.class, GetAccessCombinationNameResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = GetData.class)
    public JAXBElement<Credentials> createGetDataAuth(Credentials value) {
        return new JAXBElement<Credentials>(_EnableAllAuth_QNAME, Credentials.class, GetData.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = GetData.class)
    public JAXBElement<Context> createGetDataCtx(Context value) {
        return new JAXBElement<Context>(_DowngradeCtx_QNAME, Context.class, GetData.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = ListAll.class)
    public JAXBElement<Credentials> createListAllAuth(Credentials value) {
        return new JAXBElement<Credentials>(_EnableAllAuth_QNAME, Credentials.class, ListAll.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link com.openexchange.admin.rmi.exceptions.xsd.DatabaseUpdateException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "DatabaseUpdateException", scope = com.openexchange.admin.soap.DatabaseUpdateException.class)
    public JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.DatabaseUpdateException> createDatabaseUpdateExceptionDatabaseUpdateException(com.openexchange.admin.rmi.exceptions.xsd.DatabaseUpdateException value) {
        return new JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.DatabaseUpdateException>(_DatabaseUpdateExceptionDatabaseUpdateException_QNAME, com.openexchange.admin.rmi.exceptions.xsd.DatabaseUpdateException.class, com.openexchange.admin.soap.DatabaseUpdateException.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link java.rmi.xsd.RemoteException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "RemoteException", scope = com.openexchange.admin.soap.RemoteException.class)
    public JAXBElement<java.rmi.xsd.RemoteException> createRemoteExceptionRemoteException(java.rmi.xsd.RemoteException value) {
        return new JAXBElement<java.rmi.xsd.RemoteException>(_RemoteExceptionRemoteException_QNAME, java.rmi.xsd.RemoteException.class, com.openexchange.admin.soap.RemoteException.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = Enable.class)
    public JAXBElement<Credentials> createEnableAuth(Credentials value) {
        return new JAXBElement<Credentials>(_EnableAllAuth_QNAME, Credentials.class, Enable.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = Enable.class)
    public JAXBElement<Context> createEnableCtx(Context value) {
        return new JAXBElement<Context>(_DowngradeCtx_QNAME, Context.class, Enable.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "access_combination_name", scope = ChangeModuleAccessByName.class)
    public JAXBElement<String> createChangeModuleAccessByNameAccessCombinationName(String value) {
        return new JAXBElement<String>(_CreateModuleAccessByNameAccessCombinationName_QNAME, String.class, ChangeModuleAccessByName.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = ChangeModuleAccessByName.class)
    public JAXBElement<Credentials> createChangeModuleAccessByNameAuth(Credentials value) {
        return new JAXBElement<Credentials>(_EnableAllAuth_QNAME, Credentials.class, ChangeModuleAccessByName.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = ChangeModuleAccessByName.class)
    public JAXBElement<Context> createChangeModuleAccessByNameCtx(Context value) {
        return new JAXBElement<Context>(_DowngradeCtx_QNAME, Context.class, ChangeModuleAccessByName.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link com.openexchange.admin.rmi.exceptions.xsd.OXContextException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "OXContextException", scope = com.openexchange.admin.soap.OXContextException.class)
    public JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.OXContextException> createOXContextExceptionOXContextException(com.openexchange.admin.rmi.exceptions.xsd.OXContextException value) {
        return new JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.OXContextException>(_OXContextExceptionOXContextException_QNAME, com.openexchange.admin.rmi.exceptions.xsd.OXContextException.class, com.openexchange.admin.soap.OXContextException.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link com.openexchange.admin.rmi.exceptions.xsd.NoSuchFilestoreException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "NoSuchFilestoreException", scope = com.openexchange.admin.soap.NoSuchFilestoreException.class)
    public JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.NoSuchFilestoreException> createNoSuchFilestoreExceptionNoSuchFilestoreException(com.openexchange.admin.rmi.exceptions.xsd.NoSuchFilestoreException value) {
        return new JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.NoSuchFilestoreException>(_NoSuchFilestoreExceptionNoSuchFilestoreException_QNAME, com.openexchange.admin.rmi.exceptions.xsd.NoSuchFilestoreException.class, com.openexchange.admin.soap.NoSuchFilestoreException.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UserModuleAccess }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "access", scope = CreateModuleAccess.class)
    public JAXBElement<UserModuleAccess> createCreateModuleAccessAccess(UserModuleAccess value) {
        return new JAXBElement<UserModuleAccess>(_ChangeModuleAccessAccess_QNAME, UserModuleAccess.class, CreateModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = CreateModuleAccess.class)
    public JAXBElement<Credentials> createCreateModuleAccessAuth(Credentials value) {
        return new JAXBElement<Credentials>(_EnableAllAuth_QNAME, Credentials.class, CreateModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link User }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "admin_user", scope = CreateModuleAccess.class)
    public JAXBElement<User> createCreateModuleAccessAdminUser(User value) {
        return new JAXBElement<User>(_CreateAdminUser_QNAME, User.class, CreateModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = CreateModuleAccess.class)
    public JAXBElement<Context> createCreateModuleAccessCtx(Context value) {
        return new JAXBElement<Context>(_DowngradeCtx_QNAME, Context.class, CreateModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "return", scope = GetDataResponse.class)
    public JAXBElement<Context> createGetDataResponseReturn(Context value) {
        return new JAXBElement<Context>(_CreateModuleAccessByNameResponseReturn_QNAME, Context.class, GetDataResponse.class, value);
    }

}

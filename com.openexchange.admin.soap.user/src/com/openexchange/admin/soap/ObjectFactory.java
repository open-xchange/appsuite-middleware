
package com.openexchange.admin.soap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import com.openexchange.admin.rmi.dataobjects.xsd.Credentials;
import com.openexchange.admin.soap.dataobjects.xsd.Context;
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

    private final static QName _DuplicateExtensionExceptionDuplicateExtensionException_QNAME = new QName("http://soap.admin.openexchange.com", "DuplicateExtensionException");
    private final static QName _ChangeModuleAccessGlobalAuth_QNAME = new QName("http://soap.admin.openexchange.com", "auth");
    private final static QName _ChangeModuleAccessGlobalFilter_QNAME = new QName("http://soap.admin.openexchange.com", "filter");
    private final static QName _ChangeModuleAccessGlobalRemoveAccess_QNAME = new QName("http://soap.admin.openexchange.com", "removeAccess");
    private final static QName _ChangeModuleAccessGlobalAddAccess_QNAME = new QName("http://soap.admin.openexchange.com", "addAccess");
    private final static QName _StorageExceptionStorageException_QNAME = new QName("http://soap.admin.openexchange.com", "StorageException");
    private final static QName _ChangeCtx_QNAME = new QName("http://soap.admin.openexchange.com", "ctx");
    private final static QName _ChangeUsrdata_QNAME = new QName("http://soap.admin.openexchange.com", "usrdata");
    private final static QName _ExceptionException_QNAME = new QName("http://soap.admin.openexchange.com", "Exception");
    private final static QName _GetAccessCombinationNameResponseReturn_QNAME = new QName("http://soap.admin.openexchange.com", "return");
    private final static QName _ListSearchPattern_QNAME = new QName("http://soap.admin.openexchange.com", "search_pattern");
    private final static QName _ExistsUser_QNAME = new QName("http://soap.admin.openexchange.com", "user");
    private final static QName _ChangeByModuleAccessModuleAccess_QNAME = new QName("http://soap.admin.openexchange.com", "moduleAccess");
    private final static QName _CreateByModuleAccessNameAccessCombinationName_QNAME = new QName("http://soap.admin.openexchange.com", "access_combination_name");
    private final static QName _NoSuchUserExceptionNoSuchUserException_QNAME = new QName("http://soap.admin.openexchange.com", "NoSuchUserException");
    private final static QName _RemoteExceptionRemoteException_QNAME = new QName("http://soap.admin.openexchange.com", "RemoteException");
    private final static QName _DatabaseUpdateExceptionDatabaseUpdateException_QNAME = new QName("http://soap.admin.openexchange.com", "DatabaseUpdateException");
    private final static QName _CreateByModuleAccessAccess_QNAME = new QName("http://soap.admin.openexchange.com", "access");
    private final static QName _InvalidCredentialsExceptionInvalidCredentialsException_QNAME = new QName("http://soap.admin.openexchange.com", "InvalidCredentialsException");
    private final static QName _NoSuchContextExceptionNoSuchContextException_QNAME = new QName("http://soap.admin.openexchange.com", "NoSuchContextException");
    private final static QName _InvalidDataExceptionInvalidDataException_QNAME = new QName("http://soap.admin.openexchange.com", "InvalidDataException");

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
     * Create an instance of {@link ChangeByModuleAccessName }
     * 
     */
    public ChangeByModuleAccessName createChangeByModuleAccessName() {
        return new ChangeByModuleAccessName();
    }

    /**
     * Create an instance of {@link Change }
     * 
     */
    public Change createChange() {
        return new Change();
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
     * Create an instance of {@link ListAll }
     * 
     */
    public ListAll createListAll() {
        return new ListAll();
    }

    /**
     * Create an instance of {@link GetMultipleData }
     * 
     */
    public GetMultipleData createGetMultipleData() {
        return new GetMultipleData();
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
     * Create an instance of {@link com.openexchange.admin.soap.NoSuchUserException }
     * 
     */
    public com.openexchange.admin.soap.NoSuchUserException createNoSuchUserException() {
        return new com.openexchange.admin.soap.NoSuchUserException();
    }

    /**
     * Create an instance of {@link ListCaseInsensitive }
     * 
     */
    public ListCaseInsensitive createListCaseInsensitive() {
        return new ListCaseInsensitive();
    }

    /**
     * Create an instance of {@link GetContextAdminResponse }
     * 
     */
    public GetContextAdminResponse createGetContextAdminResponse() {
        return new GetContextAdminResponse();
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
     * Create an instance of {@link CreateByModuleAccess }
     * 
     */
    public CreateByModuleAccess createCreateByModuleAccess() {
        return new CreateByModuleAccess();
    }

    /**
     * Create an instance of {@link CreateByModuleAccessNameResponse }
     * 
     */
    public CreateByModuleAccessNameResponse createCreateByModuleAccessNameResponse() {
        return new CreateByModuleAccessNameResponse();
    }

    /**
     * Create an instance of {@link com.openexchange.admin.soap.RemoteException }
     * 
     */
    public com.openexchange.admin.soap.RemoteException createRemoteException() {
        return new com.openexchange.admin.soap.RemoteException();
    }

    /**
     * Create an instance of {@link CreateByModuleAccessName }
     * 
     */
    public CreateByModuleAccessName createCreateByModuleAccessName() {
        return new CreateByModuleAccessName();
    }

    /**
     * Create an instance of {@link GetMultipleDataResponse }
     * 
     */
    public GetMultipleDataResponse createGetMultipleDataResponse() {
        return new GetMultipleDataResponse();
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
     * Create an instance of {@link DeleteMultiple }
     * 
     */
    public DeleteMultiple createDeleteMultiple() {
        return new DeleteMultiple();
    }

    /**
     * Create an instance of {@link ListAllResponse }
     * 
     */
    public ListAllResponse createListAllResponse() {
        return new ListAllResponse();
    }

    /**
     * Create an instance of {@link ChangeByModuleAccess }
     * 
     */
    public ChangeByModuleAccess createChangeByModuleAccess() {
        return new ChangeByModuleAccess();
    }

    /**
     * Create an instance of {@link com.openexchange.admin.soap.DuplicateExtensionException }
     * 
     */
    public com.openexchange.admin.soap.DuplicateExtensionException createDuplicateExtensionException() {
        return new com.openexchange.admin.soap.DuplicateExtensionException();
    }

    /**
     * Create an instance of {@link GetAccessCombinationNameResponse }
     * 
     */
    public GetAccessCombinationNameResponse createGetAccessCombinationNameResponse() {
        return new GetAccessCombinationNameResponse();
    }

    /**
     * Create an instance of {@link ChangeModuleAccessGlobal }
     * 
     */
    public ChangeModuleAccessGlobal createChangeModuleAccessGlobal() {
        return new ChangeModuleAccessGlobal();
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
     * Create an instance of {@link GetContextAdmin }
     * 
     */
    public GetContextAdmin createGetContextAdmin() {
        return new GetContextAdmin();
    }

    /**
     * Create an instance of {@link ListCaseInsensitiveResponse }
     * 
     */
    public ListCaseInsensitiveResponse createListCaseInsensitiveResponse() {
        return new ListCaseInsensitiveResponse();
    }

    /**
     * Create an instance of {@link Exists }
     * 
     */
    public Exists createExists() {
        return new Exists();
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
     * Create an instance of {@link CreateByModuleAccessResponse }
     * 
     */
    public CreateByModuleAccessResponse createCreateByModuleAccessResponse() {
        return new CreateByModuleAccessResponse();
    }

    /**
     * Create an instance of {@link com.openexchange.admin.soap.DatabaseUpdateException }
     * 
     */
    public com.openexchange.admin.soap.DatabaseUpdateException createDatabaseUpdateException() {
        return new com.openexchange.admin.soap.DatabaseUpdateException();
    }

    /**
     * Create an instance of {@link GetDataResponse }
     * 
     */
    public GetDataResponse createGetDataResponse() {
        return new GetDataResponse();
    }

    /**
     * Create an instance of {@link Exception }
     * 
     */
    public Exception createException() {
        return new Exception();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link com.openexchange.admin.rmi.exceptions.xsd.DuplicateExtensionException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "DuplicateExtensionException", scope = com.openexchange.admin.soap.DuplicateExtensionException.class)
    public JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.DuplicateExtensionException> createDuplicateExtensionExceptionDuplicateExtensionException(com.openexchange.admin.rmi.exceptions.xsd.DuplicateExtensionException value) {
        return new JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.DuplicateExtensionException>(_DuplicateExtensionExceptionDuplicateExtensionException_QNAME, com.openexchange.admin.rmi.exceptions.xsd.DuplicateExtensionException.class, com.openexchange.admin.soap.DuplicateExtensionException.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = ChangeModuleAccessGlobal.class)
    public JAXBElement<Credentials> createChangeModuleAccessGlobalAuth(Credentials value) {
        return new JAXBElement<Credentials>(_ChangeModuleAccessGlobalAuth_QNAME, Credentials.class, ChangeModuleAccessGlobal.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "filter", scope = ChangeModuleAccessGlobal.class)
    public JAXBElement<String> createChangeModuleAccessGlobalFilter(String value) {
        return new JAXBElement<String>(_ChangeModuleAccessGlobalFilter_QNAME, String.class, ChangeModuleAccessGlobal.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UserModuleAccess }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "removeAccess", scope = ChangeModuleAccessGlobal.class)
    public JAXBElement<UserModuleAccess> createChangeModuleAccessGlobalRemoveAccess(UserModuleAccess value) {
        return new JAXBElement<UserModuleAccess>(_ChangeModuleAccessGlobalRemoveAccess_QNAME, UserModuleAccess.class, ChangeModuleAccessGlobal.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UserModuleAccess }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "addAccess", scope = ChangeModuleAccessGlobal.class)
    public JAXBElement<UserModuleAccess> createChangeModuleAccessGlobalAddAccess(UserModuleAccess value) {
        return new JAXBElement<UserModuleAccess>(_ChangeModuleAccessGlobalAddAccess_QNAME, UserModuleAccess.class, ChangeModuleAccessGlobal.class, value);
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
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = Change.class)
    public JAXBElement<Credentials> createChangeAuth(Credentials value) {
        return new JAXBElement<Credentials>(_ChangeModuleAccessGlobalAuth_QNAME, Credentials.class, Change.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = Change.class)
    public JAXBElement<Context> createChangeCtx(Context value) {
        return new JAXBElement<Context>(_ChangeCtx_QNAME, Context.class, Change.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link User }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "usrdata", scope = Change.class)
    public JAXBElement<User> createChangeUsrdata(User value) {
        return new JAXBElement<User>(_ChangeUsrdata_QNAME, User.class, Change.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = DeleteMultiple.class)
    public JAXBElement<Credentials> createDeleteMultipleAuth(Credentials value) {
        return new JAXBElement<Credentials>(_ChangeModuleAccessGlobalAuth_QNAME, Credentials.class, DeleteMultiple.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = DeleteMultiple.class)
    public JAXBElement<Context> createDeleteMultipleCtx(Context value) {
        return new JAXBElement<Context>(_ChangeCtx_QNAME, Context.class, DeleteMultiple.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = Create.class)
    public JAXBElement<Credentials> createCreateAuth(Credentials value) {
        return new JAXBElement<Credentials>(_ChangeModuleAccessGlobalAuth_QNAME, Credentials.class, Create.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = Create.class)
    public JAXBElement<Context> createCreateCtx(Context value) {
        return new JAXBElement<Context>(_ChangeCtx_QNAME, Context.class, Create.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link User }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "usrdata", scope = Create.class)
    public JAXBElement<User> createCreateUsrdata(User value) {
        return new JAXBElement<User>(_ChangeUsrdata_QNAME, User.class, Create.class, value);
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
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "return", scope = GetAccessCombinationNameResponse.class)
    public JAXBElement<String> createGetAccessCombinationNameResponseReturn(String value) {
        return new JAXBElement<String>(_GetAccessCombinationNameResponseReturn_QNAME, String.class, GetAccessCombinationNameResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link User }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "return", scope = CreateByModuleAccessNameResponse.class)
    public JAXBElement<User> createCreateByModuleAccessNameResponseReturn(User value) {
        return new JAXBElement<User>(_GetAccessCombinationNameResponseReturn_QNAME, User.class, CreateByModuleAccessNameResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = List.class)
    public JAXBElement<Credentials> createListAuth(Credentials value) {
        return new JAXBElement<Credentials>(_ChangeModuleAccessGlobalAuth_QNAME, Credentials.class, List.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = List.class)
    public JAXBElement<Context> createListCtx(Context value) {
        return new JAXBElement<Context>(_ChangeCtx_QNAME, Context.class, List.class, value);
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
     * Create an instance of {@link JAXBElement }{@code <}{@link User }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "user", scope = Exists.class)
    public JAXBElement<User> createExistsUser(User value) {
        return new JAXBElement<User>(_ExistsUser_QNAME, User.class, Exists.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = Exists.class)
    public JAXBElement<Credentials> createExistsAuth(Credentials value) {
        return new JAXBElement<Credentials>(_ChangeModuleAccessGlobalAuth_QNAME, Credentials.class, Exists.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = Exists.class)
    public JAXBElement<Context> createExistsCtx(Context value) {
        return new JAXBElement<Context>(_ChangeCtx_QNAME, Context.class, Exists.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = ListAll.class)
    public JAXBElement<Credentials> createListAllAuth(Credentials value) {
        return new JAXBElement<Credentials>(_ChangeModuleAccessGlobalAuth_QNAME, Credentials.class, ListAll.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = ListAll.class)
    public JAXBElement<Context> createListAllCtx(Context value) {
        return new JAXBElement<Context>(_ChangeCtx_QNAME, Context.class, ListAll.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link User }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "user", scope = ChangeByModuleAccess.class)
    public JAXBElement<User> createChangeByModuleAccessUser(User value) {
        return new JAXBElement<User>(_ExistsUser_QNAME, User.class, ChangeByModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = ChangeByModuleAccess.class)
    public JAXBElement<Credentials> createChangeByModuleAccessAuth(Credentials value) {
        return new JAXBElement<Credentials>(_ChangeModuleAccessGlobalAuth_QNAME, Credentials.class, ChangeByModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = ChangeByModuleAccess.class)
    public JAXBElement<Context> createChangeByModuleAccessCtx(Context value) {
        return new JAXBElement<Context>(_ChangeCtx_QNAME, Context.class, ChangeByModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UserModuleAccess }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "moduleAccess", scope = ChangeByModuleAccess.class)
    public JAXBElement<UserModuleAccess> createChangeByModuleAccessModuleAccess(UserModuleAccess value) {
        return new JAXBElement<UserModuleAccess>(_ChangeByModuleAccessModuleAccess_QNAME, UserModuleAccess.class, ChangeByModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link User }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "return", scope = CreateResponse.class)
    public JAXBElement<User> createCreateResponseReturn(User value) {
        return new JAXBElement<User>(_GetAccessCombinationNameResponseReturn_QNAME, User.class, CreateResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link User }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "user", scope = Delete.class)
    public JAXBElement<User> createDeleteUser(User value) {
        return new JAXBElement<User>(_ExistsUser_QNAME, User.class, Delete.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = Delete.class)
    public JAXBElement<Credentials> createDeleteAuth(Credentials value) {
        return new JAXBElement<Credentials>(_ChangeModuleAccessGlobalAuth_QNAME, Credentials.class, Delete.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = Delete.class)
    public JAXBElement<Context> createDeleteCtx(Context value) {
        return new JAXBElement<Context>(_ChangeCtx_QNAME, Context.class, Delete.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "access_combination_name", scope = CreateByModuleAccessName.class)
    public JAXBElement<String> createCreateByModuleAccessNameAccessCombinationName(String value) {
        return new JAXBElement<String>(_CreateByModuleAccessNameAccessCombinationName_QNAME, String.class, CreateByModuleAccessName.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = CreateByModuleAccessName.class)
    public JAXBElement<Credentials> createCreateByModuleAccessNameAuth(Credentials value) {
        return new JAXBElement<Credentials>(_ChangeModuleAccessGlobalAuth_QNAME, Credentials.class, CreateByModuleAccessName.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = CreateByModuleAccessName.class)
    public JAXBElement<Context> createCreateByModuleAccessNameCtx(Context value) {
        return new JAXBElement<Context>(_ChangeCtx_QNAME, Context.class, CreateByModuleAccessName.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link User }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "usrdata", scope = CreateByModuleAccessName.class)
    public JAXBElement<User> createCreateByModuleAccessNameUsrdata(User value) {
        return new JAXBElement<User>(_ChangeUsrdata_QNAME, User.class, CreateByModuleAccessName.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = GetMultipleData.class)
    public JAXBElement<Credentials> createGetMultipleDataAuth(Credentials value) {
        return new JAXBElement<Credentials>(_ChangeModuleAccessGlobalAuth_QNAME, Credentials.class, GetMultipleData.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = GetMultipleData.class)
    public JAXBElement<Context> createGetMultipleDataCtx(Context value) {
        return new JAXBElement<Context>(_ChangeCtx_QNAME, Context.class, GetMultipleData.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link com.openexchange.admin.rmi.exceptions.xsd.NoSuchUserException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "NoSuchUserException", scope = com.openexchange.admin.soap.NoSuchUserException.class)
    public JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.NoSuchUserException> createNoSuchUserExceptionNoSuchUserException(com.openexchange.admin.rmi.exceptions.xsd.NoSuchUserException value) {
        return new JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.NoSuchUserException>(_NoSuchUserExceptionNoSuchUserException_QNAME, com.openexchange.admin.rmi.exceptions.xsd.NoSuchUserException.class, com.openexchange.admin.soap.NoSuchUserException.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link User }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "user", scope = GetAccessCombinationName.class)
    public JAXBElement<User> createGetAccessCombinationNameUser(User value) {
        return new JAXBElement<User>(_ExistsUser_QNAME, User.class, GetAccessCombinationName.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = GetAccessCombinationName.class)
    public JAXBElement<Credentials> createGetAccessCombinationNameAuth(Credentials value) {
        return new JAXBElement<Credentials>(_ChangeModuleAccessGlobalAuth_QNAME, Credentials.class, GetAccessCombinationName.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = GetAccessCombinationName.class)
    public JAXBElement<Context> createGetAccessCombinationNameCtx(Context value) {
        return new JAXBElement<Context>(_ChangeCtx_QNAME, Context.class, GetAccessCombinationName.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = GetContextAdmin.class)
    public JAXBElement<Credentials> createGetContextAdminAuth(Credentials value) {
        return new JAXBElement<Credentials>(_ChangeModuleAccessGlobalAuth_QNAME, Credentials.class, GetContextAdmin.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = GetContextAdmin.class)
    public JAXBElement<Context> createGetContextAdminCtx(Context value) {
        return new JAXBElement<Context>(_ChangeCtx_QNAME, Context.class, GetContextAdmin.class, value);
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
     * Create an instance of {@link JAXBElement }{@code <}{@link com.openexchange.admin.rmi.exceptions.xsd.DatabaseUpdateException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "DatabaseUpdateException", scope = com.openexchange.admin.soap.DatabaseUpdateException.class)
    public JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.DatabaseUpdateException> createDatabaseUpdateExceptionDatabaseUpdateException(com.openexchange.admin.rmi.exceptions.xsd.DatabaseUpdateException value) {
        return new JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.DatabaseUpdateException>(_DatabaseUpdateExceptionDatabaseUpdateException_QNAME, com.openexchange.admin.rmi.exceptions.xsd.DatabaseUpdateException.class, com.openexchange.admin.soap.DatabaseUpdateException.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = ListCaseInsensitive.class)
    public JAXBElement<Credentials> createListCaseInsensitiveAuth(Credentials value) {
        return new JAXBElement<Credentials>(_ChangeModuleAccessGlobalAuth_QNAME, Credentials.class, ListCaseInsensitive.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = ListCaseInsensitive.class)
    public JAXBElement<Context> createListCaseInsensitiveCtx(Context value) {
        return new JAXBElement<Context>(_ChangeCtx_QNAME, Context.class, ListCaseInsensitive.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "search_pattern", scope = ListCaseInsensitive.class)
    public JAXBElement<String> createListCaseInsensitiveSearchPattern(String value) {
        return new JAXBElement<String>(_ListSearchPattern_QNAME, String.class, ListCaseInsensitive.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link User }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "return", scope = GetContextAdminResponse.class)
    public JAXBElement<User> createGetContextAdminResponseReturn(User value) {
        return new JAXBElement<User>(_GetAccessCombinationNameResponseReturn_QNAME, User.class, GetContextAdminResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UserModuleAccess }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "access", scope = CreateByModuleAccess.class)
    public JAXBElement<UserModuleAccess> createCreateByModuleAccessAccess(UserModuleAccess value) {
        return new JAXBElement<UserModuleAccess>(_CreateByModuleAccessAccess_QNAME, UserModuleAccess.class, CreateByModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = CreateByModuleAccess.class)
    public JAXBElement<Credentials> createCreateByModuleAccessAuth(Credentials value) {
        return new JAXBElement<Credentials>(_ChangeModuleAccessGlobalAuth_QNAME, Credentials.class, CreateByModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = CreateByModuleAccess.class)
    public JAXBElement<Context> createCreateByModuleAccessCtx(Context value) {
        return new JAXBElement<Context>(_ChangeCtx_QNAME, Context.class, CreateByModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link User }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "usrdata", scope = CreateByModuleAccess.class)
    public JAXBElement<User> createCreateByModuleAccessUsrdata(User value) {
        return new JAXBElement<User>(_ChangeUsrdata_QNAME, User.class, CreateByModuleAccess.class, value);
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
     * Create an instance of {@link JAXBElement }{@code <}{@link User }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "return", scope = GetDataResponse.class)
    public JAXBElement<User> createGetDataResponseReturn(User value) {
        return new JAXBElement<User>(_GetAccessCombinationNameResponseReturn_QNAME, User.class, GetDataResponse.class, value);
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
     * Create an instance of {@link JAXBElement }{@code <}{@link User }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "return", scope = CreateByModuleAccessResponse.class)
    public JAXBElement<User> createCreateByModuleAccessResponseReturn(User value) {
        return new JAXBElement<User>(_GetAccessCombinationNameResponseReturn_QNAME, User.class, CreateByModuleAccessResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link User }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "user", scope = GetData.class)
    public JAXBElement<User> createGetDataUser(User value) {
        return new JAXBElement<User>(_ExistsUser_QNAME, User.class, GetData.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = GetData.class)
    public JAXBElement<Credentials> createGetDataAuth(Credentials value) {
        return new JAXBElement<Credentials>(_ChangeModuleAccessGlobalAuth_QNAME, Credentials.class, GetData.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = GetData.class)
    public JAXBElement<Context> createGetDataCtx(Context value) {
        return new JAXBElement<Context>(_ChangeCtx_QNAME, Context.class, GetData.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link User }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "user", scope = GetModuleAccess.class)
    public JAXBElement<User> createGetModuleAccessUser(User value) {
        return new JAXBElement<User>(_ExistsUser_QNAME, User.class, GetModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = GetModuleAccess.class)
    public JAXBElement<Credentials> createGetModuleAccessAuth(Credentials value) {
        return new JAXBElement<Credentials>(_ChangeModuleAccessGlobalAuth_QNAME, Credentials.class, GetModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = GetModuleAccess.class)
    public JAXBElement<Context> createGetModuleAccessCtx(Context value) {
        return new JAXBElement<Context>(_ChangeCtx_QNAME, Context.class, GetModuleAccess.class, value);
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
     * Create an instance of {@link JAXBElement }{@code <}{@link UserModuleAccess }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "return", scope = GetModuleAccessResponse.class)
    public JAXBElement<UserModuleAccess> createGetModuleAccessResponseReturn(UserModuleAccess value) {
        return new JAXBElement<UserModuleAccess>(_GetAccessCombinationNameResponseReturn_QNAME, UserModuleAccess.class, GetModuleAccessResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link User }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "user", scope = ChangeByModuleAccessName.class)
    public JAXBElement<User> createChangeByModuleAccessNameUser(User value) {
        return new JAXBElement<User>(_ExistsUser_QNAME, User.class, ChangeByModuleAccessName.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "access_combination_name", scope = ChangeByModuleAccessName.class)
    public JAXBElement<String> createChangeByModuleAccessNameAccessCombinationName(String value) {
        return new JAXBElement<String>(_CreateByModuleAccessNameAccessCombinationName_QNAME, String.class, ChangeByModuleAccessName.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = ChangeByModuleAccessName.class)
    public JAXBElement<Credentials> createChangeByModuleAccessNameAuth(Credentials value) {
        return new JAXBElement<Credentials>(_ChangeModuleAccessGlobalAuth_QNAME, Credentials.class, ChangeByModuleAccessName.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = ChangeByModuleAccessName.class)
    public JAXBElement<Context> createChangeByModuleAccessNameCtx(Context value) {
        return new JAXBElement<Context>(_ChangeCtx_QNAME, Context.class, ChangeByModuleAccessName.class, value);
    }

}

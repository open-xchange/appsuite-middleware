
package com.openexchange.admin.soap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import com.openexchange.admin.rmi.dataobjects.xsd.Credentials;
import com.openexchange.admin.soap.dataobjects.xsd.Context;
import com.openexchange.admin.soap.dataobjects.xsd.Resource;


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

    private final static QName _DatabaseUpdateExceptionDatabaseUpdateException_QNAME = new QName("http://soap.admin.openexchange.com", "DatabaseUpdateException");
    private final static QName _GetDataAuth_QNAME = new QName("http://soap.admin.openexchange.com", "auth");
    private final static QName _GetDataCtx_QNAME = new QName("http://soap.admin.openexchange.com", "ctx");
    private final static QName _GetDataRes_QNAME = new QName("http://soap.admin.openexchange.com", "res");
    private final static QName _GetDataResponseReturn_QNAME = new QName("http://soap.admin.openexchange.com", "return");
    private final static QName _NoSuchContextExceptionNoSuchContextException_QNAME = new QName("http://soap.admin.openexchange.com", "NoSuchContextException");
    private final static QName _RemoteExceptionRemoteException_QNAME = new QName("http://soap.admin.openexchange.com", "RemoteException");
    private final static QName _StorageExceptionStorageException_QNAME = new QName("http://soap.admin.openexchange.com", "StorageException");
    private final static QName _ExceptionException_QNAME = new QName("http://soap.admin.openexchange.com", "Exception");
    private final static QName _InvalidCredentialsExceptionInvalidCredentialsException_QNAME = new QName("http://soap.admin.openexchange.com", "InvalidCredentialsException");
    private final static QName _InvalidDataExceptionInvalidDataException_QNAME = new QName("http://soap.admin.openexchange.com", "InvalidDataException");
    private final static QName _NoSuchResourceExceptionNoSuchResourceException_QNAME = new QName("http://soap.admin.openexchange.com", "NoSuchResourceException");
    private final static QName _ListPattern_QNAME = new QName("http://soap.admin.openexchange.com", "pattern");

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
     * Create an instance of {@link GetMultipleDataResponse }
     * 
     */
    public GetMultipleDataResponse createGetMultipleDataResponse() {
        return new GetMultipleDataResponse();
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
     * Create an instance of {@link ListAll }
     * 
     */
    public ListAll createListAll() {
        return new ListAll();
    }

    /**
     * Create an instance of {@link ListAllResponse }
     * 
     */
    public ListAllResponse createListAllResponse() {
        return new ListAllResponse();
    }

    /**
     * Create an instance of {@link com.openexchange.admin.soap.NoSuchResourceException }
     * 
     */
    public com.openexchange.admin.soap.NoSuchResourceException createNoSuchResourceException() {
        return new com.openexchange.admin.soap.NoSuchResourceException();
    }

    /**
     * Create an instance of {@link GetMultipleData }
     * 
     */
    public GetMultipleData createGetMultipleData() {
        return new GetMultipleData();
    }

    /**
     * Create an instance of {@link com.openexchange.admin.soap.StorageException }
     * 
     */
    public com.openexchange.admin.soap.StorageException createStorageException() {
        return new com.openexchange.admin.soap.StorageException();
    }

    /**
     * Create an instance of {@link GetData }
     * 
     */
    public GetData createGetData() {
        return new GetData();
    }

    /**
     * Create an instance of {@link com.openexchange.admin.soap.InvalidCredentialsException }
     * 
     */
    public com.openexchange.admin.soap.InvalidCredentialsException createInvalidCredentialsException() {
        return new com.openexchange.admin.soap.InvalidCredentialsException();
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
     * Create an instance of {@link ListResponse }
     * 
     */
    public ListResponse createListResponse() {
        return new ListResponse();
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
     * Create an instance of {@link com.openexchange.admin.soap.RemoteException }
     * 
     */
    public com.openexchange.admin.soap.RemoteException createRemoteException() {
        return new com.openexchange.admin.soap.RemoteException();
    }

    /**
     * Create an instance of {@link Exception }
     * 
     */
    public Exception createException() {
        return new Exception();
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
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = GetData.class)
    public JAXBElement<Credentials> createGetDataAuth(Credentials value) {
        return new JAXBElement<Credentials>(_GetDataAuth_QNAME, Credentials.class, GetData.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = GetData.class)
    public JAXBElement<Context> createGetDataCtx(Context value) {
        return new JAXBElement<Context>(_GetDataCtx_QNAME, Context.class, GetData.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Resource }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "res", scope = GetData.class)
    public JAXBElement<Resource> createGetDataRes(Resource value) {
        return new JAXBElement<Resource>(_GetDataRes_QNAME, Resource.class, GetData.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = GetMultipleData.class)
    public JAXBElement<Credentials> createGetMultipleDataAuth(Credentials value) {
        return new JAXBElement<Credentials>(_GetDataAuth_QNAME, Credentials.class, GetMultipleData.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = GetMultipleData.class)
    public JAXBElement<Context> createGetMultipleDataCtx(Context value) {
        return new JAXBElement<Context>(_GetDataCtx_QNAME, Context.class, GetMultipleData.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = Create.class)
    public JAXBElement<Credentials> createCreateAuth(Credentials value) {
        return new JAXBElement<Credentials>(_GetDataAuth_QNAME, Credentials.class, Create.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = Create.class)
    public JAXBElement<Context> createCreateCtx(Context value) {
        return new JAXBElement<Context>(_GetDataCtx_QNAME, Context.class, Create.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Resource }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "res", scope = Create.class)
    public JAXBElement<Resource> createCreateRes(Resource value) {
        return new JAXBElement<Resource>(_GetDataRes_QNAME, Resource.class, Create.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Resource }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "return", scope = GetDataResponse.class)
    public JAXBElement<Resource> createGetDataResponseReturn(Resource value) {
        return new JAXBElement<Resource>(_GetDataResponseReturn_QNAME, Resource.class, GetDataResponse.class, value);
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
     * Create an instance of {@link JAXBElement }{@code <}{@link java.rmi.xsd.RemoteException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "RemoteException", scope = com.openexchange.admin.soap.RemoteException.class)
    public JAXBElement<java.rmi.xsd.RemoteException> createRemoteExceptionRemoteException(java.rmi.xsd.RemoteException value) {
        return new JAXBElement<java.rmi.xsd.RemoteException>(_RemoteExceptionRemoteException_QNAME, java.rmi.xsd.RemoteException.class, com.openexchange.admin.soap.RemoteException.class, value);
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
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "Exception", scope = Exception.class)
    public JAXBElement<Object> createExceptionException(Object value) {
        return new JAXBElement<Object>(_ExceptionException_QNAME, Object.class, Exception.class, value);
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
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = Change.class)
    public JAXBElement<Credentials> createChangeAuth(Credentials value) {
        return new JAXBElement<Credentials>(_GetDataAuth_QNAME, Credentials.class, Change.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = Change.class)
    public JAXBElement<Context> createChangeCtx(Context value) {
        return new JAXBElement<Context>(_GetDataCtx_QNAME, Context.class, Change.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Resource }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "res", scope = Change.class)
    public JAXBElement<Resource> createChangeRes(Resource value) {
        return new JAXBElement<Resource>(_GetDataRes_QNAME, Resource.class, Change.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = ListAll.class)
    public JAXBElement<Credentials> createListAllAuth(Credentials value) {
        return new JAXBElement<Credentials>(_GetDataAuth_QNAME, Credentials.class, ListAll.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = ListAll.class)
    public JAXBElement<Context> createListAllCtx(Context value) {
        return new JAXBElement<Context>(_GetDataCtx_QNAME, Context.class, ListAll.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Resource }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "return", scope = CreateResponse.class)
    public JAXBElement<Resource> createCreateResponseReturn(Resource value) {
        return new JAXBElement<Resource>(_GetDataResponseReturn_QNAME, Resource.class, CreateResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link com.openexchange.admin.rmi.exceptions.xsd.NoSuchResourceException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "NoSuchResourceException", scope = com.openexchange.admin.soap.NoSuchResourceException.class)
    public JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.NoSuchResourceException> createNoSuchResourceExceptionNoSuchResourceException(com.openexchange.admin.rmi.exceptions.xsd.NoSuchResourceException value) {
        return new JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.NoSuchResourceException>(_NoSuchResourceExceptionNoSuchResourceException_QNAME, com.openexchange.admin.rmi.exceptions.xsd.NoSuchResourceException.class, com.openexchange.admin.soap.NoSuchResourceException.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = List.class)
    public JAXBElement<Credentials> createListAuth(Credentials value) {
        return new JAXBElement<Credentials>(_GetDataAuth_QNAME, Credentials.class, List.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = List.class)
    public JAXBElement<Context> createListCtx(Context value) {
        return new JAXBElement<Context>(_GetDataCtx_QNAME, Context.class, List.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "pattern", scope = List.class)
    public JAXBElement<String> createListPattern(String value) {
        return new JAXBElement<String>(_ListPattern_QNAME, String.class, List.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Credentials }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "auth", scope = Delete.class)
    public JAXBElement<Credentials> createDeleteAuth(Credentials value) {
        return new JAXBElement<Credentials>(_GetDataAuth_QNAME, Credentials.class, Delete.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Context }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "ctx", scope = Delete.class)
    public JAXBElement<Context> createDeleteCtx(Context value) {
        return new JAXBElement<Context>(_GetDataCtx_QNAME, Context.class, Delete.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Resource }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://soap.admin.openexchange.com", name = "res", scope = Delete.class)
    public JAXBElement<Resource> createDeleteRes(Resource value) {
        return new JAXBElement<Resource>(_GetDataRes_QNAME, Resource.class, Delete.class, value);
    }

}

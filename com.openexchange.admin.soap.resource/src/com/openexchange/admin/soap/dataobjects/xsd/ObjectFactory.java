
package com.openexchange.admin.soap.dataobjects.xsd;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.openexchange.admin.soap.dataobjects.xsd package. 
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

    private final static QName _EntryValue_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "value");
    private final static QName _EntryKey_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "key");
    private final static QName _ResourceId_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "id");
    private final static QName _ResourceEmail_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "email");
    private final static QName _ResourceName_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "name");
    private final static QName _ResourceDescription_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "description");
    private final static QName _ResourceAvailable_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "available");
    private final static QName _ResourceDisplayname_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "displayname");
    private final static QName _ContextEnabled_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "enabled");
    private final static QName _ContextWriteDatabase_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "writeDatabase");
    private final static QName _ContextFilestoreId_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "filestoreId");
    private final static QName _ContextUsedQuota_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "usedQuota");
    private final static QName _ContextMaxQuota_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "maxQuota");
    private final static QName _ContextReadDatabase_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "readDatabase");
    private final static QName _ContextFilestoreName_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "filestore_name");
    private final static QName _ContextAverageSize_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "average_size");
    private final static QName _ContextUserAttributes_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "userAttributes");
    private final static QName _DatabaseMasterId_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "masterId");
    private final static QName _DatabaseMaxUnits_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "maxUnits");
    private final static QName _DatabasePoolMax_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "poolMax");
    private final static QName _DatabaseCurrentUnits_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "currentUnits");
    private final static QName _DatabasePassword_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "password");
    private final static QName _DatabaseReadId_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "read_id");
    private final static QName _DatabaseUrl_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "url");
    private final static QName _DatabaseMaster_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "master");
    private final static QName _DatabaseScheme_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "scheme");
    private final static QName _DatabasePoolInitial_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "poolInitial");
    private final static QName _DatabaseDriver_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "driver");
    private final static QName _DatabaseLogin_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "login");
    private final static QName _DatabasePoolHardLimit_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "poolHardLimit");
    private final static QName _DatabaseClusterWeight_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "clusterWeight");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.openexchange.admin.soap.dataobjects.xsd
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Resource }
     * 
     */
    public Resource createResource() {
        return new Resource();
    }

    /**
     * Create an instance of {@link Context }
     * 
     */
    public Context createContext() {
        return new Context();
    }

    /**
     * Create an instance of {@link Entry }
     * 
     */
    public Entry createEntry() {
        return new Entry();
    }

    /**
     * Create an instance of {@link SOAPStringMap }
     * 
     */
    public SOAPStringMap createSOAPStringMap() {
        return new SOAPStringMap();
    }

    /**
     * Create an instance of {@link SOAPStringMapMap }
     * 
     */
    public SOAPStringMapMap createSOAPStringMapMap() {
        return new SOAPStringMapMap();
    }

    /**
     * Create an instance of {@link Database }
     * 
     */
    public Database createDatabase() {
        return new Database();
    }

    /**
     * Create an instance of {@link SOAPMapEntry }
     * 
     */
    public SOAPMapEntry createSOAPMapEntry() {
        return new SOAPMapEntry();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "value", scope = Entry.class)
    public JAXBElement<String> createEntryValue(String value) {
        return new JAXBElement<String>(_EntryValue_QNAME, String.class, Entry.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "key", scope = Entry.class)
    public JAXBElement<String> createEntryKey(String value) {
        return new JAXBElement<String>(_EntryKey_QNAME, String.class, Entry.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "id", scope = Resource.class)
    public JAXBElement<Integer> createResourceId(Integer value) {
        return new JAXBElement<Integer>(_ResourceId_QNAME, Integer.class, Resource.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "email", scope = Resource.class)
    public JAXBElement<String> createResourceEmail(String value) {
        return new JAXBElement<String>(_ResourceEmail_QNAME, String.class, Resource.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "name", scope = Resource.class)
    public JAXBElement<String> createResourceName(String value) {
        return new JAXBElement<String>(_ResourceName_QNAME, String.class, Resource.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "description", scope = Resource.class)
    public JAXBElement<String> createResourceDescription(String value) {
        return new JAXBElement<String>(_ResourceDescription_QNAME, String.class, Resource.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "available", scope = Resource.class)
    public JAXBElement<Boolean> createResourceAvailable(Boolean value) {
        return new JAXBElement<Boolean>(_ResourceAvailable_QNAME, Boolean.class, Resource.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "displayname", scope = Resource.class)
    public JAXBElement<String> createResourceDisplayname(String value) {
        return new JAXBElement<String>(_ResourceDisplayname_QNAME, String.class, Resource.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "enabled", scope = Context.class)
    public JAXBElement<Boolean> createContextEnabled(Boolean value) {
        return new JAXBElement<Boolean>(_ContextEnabled_QNAME, Boolean.class, Context.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "id", scope = Context.class)
    public JAXBElement<Integer> createContextId(Integer value) {
        return new JAXBElement<Integer>(_ResourceId_QNAME, Integer.class, Context.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Database }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "writeDatabase", scope = Context.class)
    public JAXBElement<Database> createContextWriteDatabase(Database value) {
        return new JAXBElement<Database>(_ContextWriteDatabase_QNAME, Database.class, Context.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "filestoreId", scope = Context.class)
    public JAXBElement<Integer> createContextFilestoreId(Integer value) {
        return new JAXBElement<Integer>(_ContextFilestoreId_QNAME, Integer.class, Context.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "name", scope = Context.class)
    public JAXBElement<String> createContextName(String value) {
        return new JAXBElement<String>(_ResourceName_QNAME, String.class, Context.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "usedQuota", scope = Context.class)
    public JAXBElement<Long> createContextUsedQuota(Long value) {
        return new JAXBElement<Long>(_ContextUsedQuota_QNAME, Long.class, Context.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "maxQuota", scope = Context.class)
    public JAXBElement<Long> createContextMaxQuota(Long value) {
        return new JAXBElement<Long>(_ContextMaxQuota_QNAME, Long.class, Context.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Database }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "readDatabase", scope = Context.class)
    public JAXBElement<Database> createContextReadDatabase(Database value) {
        return new JAXBElement<Database>(_ContextReadDatabase_QNAME, Database.class, Context.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "filestore_name", scope = Context.class)
    public JAXBElement<String> createContextFilestoreName(String value) {
        return new JAXBElement<String>(_ContextFilestoreName_QNAME, String.class, Context.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "average_size", scope = Context.class)
    public JAXBElement<Long> createContextAverageSize(Long value) {
        return new JAXBElement<Long>(_ContextAverageSize_QNAME, Long.class, Context.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SOAPStringMapMap }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "userAttributes", scope = Context.class)
    public JAXBElement<SOAPStringMapMap> createContextUserAttributes(SOAPStringMapMap value) {
        return new JAXBElement<SOAPStringMapMap>(_ContextUserAttributes_QNAME, SOAPStringMapMap.class, Context.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "masterId", scope = Database.class)
    public JAXBElement<Integer> createDatabaseMasterId(Integer value) {
        return new JAXBElement<Integer>(_DatabaseMasterId_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "maxUnits", scope = Database.class)
    public JAXBElement<Integer> createDatabaseMaxUnits(Integer value) {
        return new JAXBElement<Integer>(_DatabaseMaxUnits_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "poolMax", scope = Database.class)
    public JAXBElement<Integer> createDatabasePoolMax(Integer value) {
        return new JAXBElement<Integer>(_DatabasePoolMax_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "currentUnits", scope = Database.class)
    public JAXBElement<Integer> createDatabaseCurrentUnits(Integer value) {
        return new JAXBElement<Integer>(_DatabaseCurrentUnits_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "password", scope = Database.class)
    public JAXBElement<String> createDatabasePassword(String value) {
        return new JAXBElement<String>(_DatabasePassword_QNAME, String.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "read_id", scope = Database.class)
    public JAXBElement<Integer> createDatabaseReadId(Integer value) {
        return new JAXBElement<Integer>(_DatabaseReadId_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "url", scope = Database.class)
    public JAXBElement<String> createDatabaseUrl(String value) {
        return new JAXBElement<String>(_DatabaseUrl_QNAME, String.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "master", scope = Database.class)
    public JAXBElement<Boolean> createDatabaseMaster(Boolean value) {
        return new JAXBElement<Boolean>(_DatabaseMaster_QNAME, Boolean.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "id", scope = Database.class)
    public JAXBElement<Integer> createDatabaseId(Integer value) {
        return new JAXBElement<Integer>(_ResourceId_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "scheme", scope = Database.class)
    public JAXBElement<String> createDatabaseScheme(String value) {
        return new JAXBElement<String>(_DatabaseScheme_QNAME, String.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "name", scope = Database.class)
    public JAXBElement<String> createDatabaseName(String value) {
        return new JAXBElement<String>(_ResourceName_QNAME, String.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "poolInitial", scope = Database.class)
    public JAXBElement<Integer> createDatabasePoolInitial(Integer value) {
        return new JAXBElement<Integer>(_DatabasePoolInitial_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "driver", scope = Database.class)
    public JAXBElement<String> createDatabaseDriver(String value) {
        return new JAXBElement<String>(_DatabaseDriver_QNAME, String.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "login", scope = Database.class)
    public JAXBElement<String> createDatabaseLogin(String value) {
        return new JAXBElement<String>(_DatabaseLogin_QNAME, String.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "poolHardLimit", scope = Database.class)
    public JAXBElement<Integer> createDatabasePoolHardLimit(Integer value) {
        return new JAXBElement<Integer>(_DatabasePoolHardLimit_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "clusterWeight", scope = Database.class)
    public JAXBElement<Integer> createDatabaseClusterWeight(Integer value) {
        return new JAXBElement<Integer>(_DatabaseClusterWeight_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SOAPStringMap }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "value", scope = SOAPMapEntry.class)
    public JAXBElement<SOAPStringMap> createSOAPMapEntryValue(SOAPStringMap value) {
        return new JAXBElement<SOAPStringMap>(_EntryValue_QNAME, SOAPStringMap.class, SOAPMapEntry.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "key", scope = SOAPMapEntry.class)
    public JAXBElement<String> createSOAPMapEntryKey(String value) {
        return new JAXBElement<String>(_EntryKey_QNAME, String.class, SOAPMapEntry.class, value);
    }

}

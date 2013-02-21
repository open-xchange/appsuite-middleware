
package com.openexchange.admin.soap.reseller.context.rmi.dataobjects;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the com.openexchange.admin.soap.reseller.context.rmi.dataobjects package.
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

    private final static QName _DatabaseReadIdset_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "read_idset");
    private final static QName _DatabasePoolMaxset_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "poolMaxset");
    private final static QName _DatabaseMandatoryMembersRegister_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "mandatoryMembersRegister");
    private final static QName _DatabaseClusterWeightset_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "clusterWeightset");
    private final static QName _DatabaseDriverset_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "driverset");
    private final static QName _DatabaseClusterWeight_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "clusterWeight");
    private final static QName _DatabasePoolHardLimit_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "poolHardLimit");
    private final static QName _DatabaseLoginset_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "loginset");
    private final static QName _DatabaseMaxUnitsset_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "maxUnitsset");
    private final static QName _DatabasePoolMax_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "poolMax");
    private final static QName _DatabaseIdset_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "idset");
    private final static QName _DatabaseUrl_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "url");
    private final static QName _DatabasePoolInitialset_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "poolInitialset");
    private final static QName _DatabaseMaster_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "master");
    private final static QName _DatabaseMandatoryMembersCreate_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "mandatoryMembersCreate");
    private final static QName _DatabaseMasterset_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "masterset");
    private final static QName _DatabaseId_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "id");
    private final static QName _DatabaseUrlset_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "urlset");
    private final static QName _DatabaseMandatoryMembersChange_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "mandatoryMembersChange");
    private final static QName _DatabasePoolInitial_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "poolInitial");
    private final static QName _DatabaseName_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "name");
    private final static QName _DatabaseScheme_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "scheme");
    private final static QName _DatabaseLogin_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "login");
    private final static QName _DatabaseDriver_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "driver");
    private final static QName _DatabasePoolHardLimitset_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "poolHardLimitset");
    private final static QName _DatabaseCurrentUnitsset_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "currentUnitsset");
    private final static QName _DatabaseMandatoryMembersDelete_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "mandatoryMembersDelete");
    private final static QName _DatabaseMaxUnits_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "maxUnits");
    private final static QName _DatabaseMasterIdset_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "masterIdset");
    private final static QName _DatabaseMasterId_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "masterId");
    private final static QName _DatabaseSchemeset_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "schemeset");
    private final static QName _DatabaseCurrentUnits_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "currentUnits");
    private final static QName _DatabaseReadId_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "read_id");
    private final static QName _DatabaseNameset_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "nameset");
    private final static QName _DatabasePasswordset_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "passwordset");
    private final static QName _DatabasePassword_QNAME = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", "password");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.openexchange.admin.soap.reseller.context.rmi.dataobjects
     *
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Filestore }
     *
     */
    public Filestore createFilestore() {
        return new Filestore();
    }

    /**
     * Create an instance of {@link Database }
     *
     */
    public Database createDatabase() {
        return new Database();
    }

    /**
     * Create an instance of {@link EnforceableDataObject }
     *
     */
    public EnforceableDataObject createEnforceableDataObject() {
        return new EnforceableDataObject();
    }

    /**
     * Create an instance of {@link Credentials }
     *
     */
    public Credentials createCredentials() {
        return new Credentials();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "read_idset", scope = Database.class)
    public JAXBElement<Boolean> createDatabaseReadIdset(Boolean value) {
        return new JAXBElement<Boolean>(_DatabaseReadIdset_QNAME, Boolean.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "poolMaxset", scope = Database.class)
    public JAXBElement<Boolean> createDatabasePoolMaxset(Boolean value) {
        return new JAXBElement<Boolean>(_DatabasePoolMaxset_QNAME, Boolean.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "mandatoryMembersRegister", scope = Database.class)
    public JAXBElement<String> createDatabaseMandatoryMembersRegister(String value) {
        return new JAXBElement<String>(_DatabaseMandatoryMembersRegister_QNAME, String.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "clusterWeightset", scope = Database.class)
    public JAXBElement<Boolean> createDatabaseClusterWeightset(Boolean value) {
        return new JAXBElement<Boolean>(_DatabaseClusterWeightset_QNAME, Boolean.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "driverset", scope = Database.class)
    public JAXBElement<Boolean> createDatabaseDriverset(Boolean value) {
        return new JAXBElement<Boolean>(_DatabaseDriverset_QNAME, Boolean.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "clusterWeight", scope = Database.class)
    public JAXBElement<Integer> createDatabaseClusterWeight(Integer value) {
        return new JAXBElement<Integer>(_DatabaseClusterWeight_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "poolHardLimit", scope = Database.class)
    public JAXBElement<Integer> createDatabasePoolHardLimit(Integer value) {
        return new JAXBElement<Integer>(_DatabasePoolHardLimit_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "loginset", scope = Database.class)
    public JAXBElement<Boolean> createDatabaseLoginset(Boolean value) {
        return new JAXBElement<Boolean>(_DatabaseLoginset_QNAME, Boolean.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "maxUnitsset", scope = Database.class)
    public JAXBElement<Boolean> createDatabaseMaxUnitsset(Boolean value) {
        return new JAXBElement<Boolean>(_DatabaseMaxUnitsset_QNAME, Boolean.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "poolMax", scope = Database.class)
    public JAXBElement<Integer> createDatabasePoolMax(Integer value) {
        return new JAXBElement<Integer>(_DatabasePoolMax_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "idset", scope = Database.class)
    public JAXBElement<Boolean> createDatabaseIdset(Boolean value) {
        return new JAXBElement<Boolean>(_DatabaseIdset_QNAME, Boolean.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "url", scope = Database.class)
    public JAXBElement<String> createDatabaseUrl(String value) {
        return new JAXBElement<String>(_DatabaseUrl_QNAME, String.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "poolInitialset", scope = Database.class)
    public JAXBElement<Boolean> createDatabasePoolInitialset(Boolean value) {
        return new JAXBElement<Boolean>(_DatabasePoolInitialset_QNAME, Boolean.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "master", scope = Database.class)
    public JAXBElement<Boolean> createDatabaseMaster(Boolean value) {
        return new JAXBElement<Boolean>(_DatabaseMaster_QNAME, Boolean.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "mandatoryMembersCreate", scope = Database.class)
    public JAXBElement<String> createDatabaseMandatoryMembersCreate(String value) {
        return new JAXBElement<String>(_DatabaseMandatoryMembersCreate_QNAME, String.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "masterset", scope = Database.class)
    public JAXBElement<Boolean> createDatabaseMasterset(Boolean value) {
        return new JAXBElement<Boolean>(_DatabaseMasterset_QNAME, Boolean.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "id", scope = Database.class)
    public JAXBElement<Integer> createDatabaseId(Integer value) {
        return new JAXBElement<Integer>(_DatabaseId_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "urlset", scope = Database.class)
    public JAXBElement<Boolean> createDatabaseUrlset(Boolean value) {
        return new JAXBElement<Boolean>(_DatabaseUrlset_QNAME, Boolean.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "mandatoryMembersChange", scope = Database.class)
    public JAXBElement<String> createDatabaseMandatoryMembersChange(String value) {
        return new JAXBElement<String>(_DatabaseMandatoryMembersChange_QNAME, String.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "poolInitial", scope = Database.class)
    public JAXBElement<Integer> createDatabasePoolInitial(Integer value) {
        return new JAXBElement<Integer>(_DatabasePoolInitial_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "name", scope = Database.class)
    public JAXBElement<String> createDatabaseName(String value) {
        return new JAXBElement<String>(_DatabaseName_QNAME, String.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "scheme", scope = Database.class)
    public JAXBElement<String> createDatabaseScheme(String value) {
        return new JAXBElement<String>(_DatabaseScheme_QNAME, String.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "login", scope = Database.class)
    public JAXBElement<String> createDatabaseLogin(String value) {
        return new JAXBElement<String>(_DatabaseLogin_QNAME, String.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "driver", scope = Database.class)
    public JAXBElement<String> createDatabaseDriver(String value) {
        return new JAXBElement<String>(_DatabaseDriver_QNAME, String.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "poolHardLimitset", scope = Database.class)
    public JAXBElement<Boolean> createDatabasePoolHardLimitset(Boolean value) {
        return new JAXBElement<Boolean>(_DatabasePoolHardLimitset_QNAME, Boolean.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "currentUnitsset", scope = Database.class)
    public JAXBElement<Boolean> createDatabaseCurrentUnitsset(Boolean value) {
        return new JAXBElement<Boolean>(_DatabaseCurrentUnitsset_QNAME, Boolean.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "mandatoryMembersDelete", scope = Database.class)
    public JAXBElement<String> createDatabaseMandatoryMembersDelete(String value) {
        return new JAXBElement<String>(_DatabaseMandatoryMembersDelete_QNAME, String.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "maxUnits", scope = Database.class)
    public JAXBElement<Integer> createDatabaseMaxUnits(Integer value) {
        return new JAXBElement<Integer>(_DatabaseMaxUnits_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "masterIdset", scope = Database.class)
    public JAXBElement<Boolean> createDatabaseMasterIdset(Boolean value) {
        return new JAXBElement<Boolean>(_DatabaseMasterIdset_QNAME, Boolean.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "masterId", scope = Database.class)
    public JAXBElement<Integer> createDatabaseMasterId(Integer value) {
        return new JAXBElement<Integer>(_DatabaseMasterId_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "schemeset", scope = Database.class)
    public JAXBElement<Boolean> createDatabaseSchemeset(Boolean value) {
        return new JAXBElement<Boolean>(_DatabaseSchemeset_QNAME, Boolean.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "currentUnits", scope = Database.class)
    public JAXBElement<Integer> createDatabaseCurrentUnits(Integer value) {
        return new JAXBElement<Integer>(_DatabaseCurrentUnits_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "read_id", scope = Database.class)
    public JAXBElement<Integer> createDatabaseReadId(Integer value) {
        return new JAXBElement<Integer>(_DatabaseReadId_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "nameset", scope = Database.class)
    public JAXBElement<Boolean> createDatabaseNameset(Boolean value) {
        return new JAXBElement<Boolean>(_DatabaseNameset_QNAME, Boolean.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "passwordset", scope = Database.class)
    public JAXBElement<Boolean> createDatabasePasswordset(Boolean value) {
        return new JAXBElement<Boolean>(_DatabasePasswordset_QNAME, Boolean.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", name = "password", scope = Database.class)
    public JAXBElement<String> createDatabasePassword(String value) {
        return new JAXBElement<String>(_DatabasePassword_QNAME, String.class, Database.class, value);
    }

}

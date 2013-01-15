
package com.openexchange.admin.soap.reseller.context.reseller.rmi.dataobjects;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the com.openexchange.admin.soap.reseller.context.reseller.rmi.dataobjects package.
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

    private final static QName _RestrictionMandatoryMembersRegister_QNAME = new QName("http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", "mandatoryMembersRegister");
    private final static QName _RestrictionMandatoryMembersCreate_QNAME = new QName("http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", "mandatoryMembersCreate");
    private final static QName _RestrictionId_QNAME = new QName("http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", "id");
    private final static QName _RestrictionMandatoryMembersChange_QNAME = new QName("http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", "mandatoryMembersChange");
    private final static QName _RestrictionName_QNAME = new QName("http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", "name");
    private final static QName _RestrictionValue_QNAME = new QName("http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", "value");
    private final static QName _RestrictionMandatoryMembersDelete_QNAME = new QName("http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", "mandatoryMembersDelete");
    private final static QName _ResellerAdminParentId_QNAME = new QName("http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", "parentId");
    private final static QName _ResellerAdminPasswordMechset_QNAME = new QName("http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", "passwordMechset");
    private final static QName _ResellerAdminDisplayname_QNAME = new QName("http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", "displayname");
    private final static QName _ResellerAdminRestrictionsset_QNAME = new QName("http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", "restrictionsset");
    private final static QName _ResellerAdminParentIdset_QNAME = new QName("http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", "parentIdset");
    private final static QName _ResellerAdminRestrictions_QNAME = new QName("http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", "restrictions");
    private final static QName _ResellerAdminDisplaynameset_QNAME = new QName("http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", "displaynameset");
    private final static QName _ResellerAdminIdset_QNAME = new QName("http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", "idset");
    private final static QName _ResellerAdminPasswordset_QNAME = new QName("http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", "passwordset");
    private final static QName _ResellerAdminPasswordMech_QNAME = new QName("http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", "passwordMech");
    private final static QName _ResellerAdminNameset_QNAME = new QName("http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", "nameset");
    private final static QName _ResellerAdminPassword_QNAME = new QName("http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", "password");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.openexchange.admin.soap.reseller.context.reseller.rmi.dataobjects
     *
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ResellerAdmin }
     *
     */
    public ResellerAdmin createResellerAdmin() {
        return new ResellerAdmin();
    }

    /**
     * Create an instance of {@link Restriction }
     *
     */
    public Restriction createRestriction() {
        return new Restriction();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", name = "mandatoryMembersRegister", scope = Restriction.class)
    public JAXBElement<String> createRestrictionMandatoryMembersRegister(String value) {
        return new JAXBElement<String>(_RestrictionMandatoryMembersRegister_QNAME, String.class, Restriction.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", name = "mandatoryMembersCreate", scope = Restriction.class)
    public JAXBElement<String> createRestrictionMandatoryMembersCreate(String value) {
        return new JAXBElement<String>(_RestrictionMandatoryMembersCreate_QNAME, String.class, Restriction.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", name = "id", scope = Restriction.class)
    public JAXBElement<Integer> createRestrictionId(Integer value) {
        return new JAXBElement<Integer>(_RestrictionId_QNAME, Integer.class, Restriction.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", name = "mandatoryMembersChange", scope = Restriction.class)
    public JAXBElement<String> createRestrictionMandatoryMembersChange(String value) {
        return new JAXBElement<String>(_RestrictionMandatoryMembersChange_QNAME, String.class, Restriction.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", name = "name", scope = Restriction.class)
    public JAXBElement<String> createRestrictionName(String value) {
        return new JAXBElement<String>(_RestrictionName_QNAME, String.class, Restriction.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", name = "value", scope = Restriction.class)
    public JAXBElement<String> createRestrictionValue(String value) {
        return new JAXBElement<String>(_RestrictionValue_QNAME, String.class, Restriction.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", name = "mandatoryMembersDelete", scope = Restriction.class)
    public JAXBElement<String> createRestrictionMandatoryMembersDelete(String value) {
        return new JAXBElement<String>(_RestrictionMandatoryMembersDelete_QNAME, String.class, Restriction.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", name = "mandatoryMembersCreate", scope = ResellerAdmin.class)
    public JAXBElement<String> createResellerAdminMandatoryMembersCreate(String value) {
        return new JAXBElement<String>(_RestrictionMandatoryMembersCreate_QNAME, String.class, ResellerAdmin.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", name = "mandatoryMembersRegister", scope = ResellerAdmin.class)
    public JAXBElement<String> createResellerAdminMandatoryMembersRegister(String value) {
        return new JAXBElement<String>(_RestrictionMandatoryMembersRegister_QNAME, String.class, ResellerAdmin.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", name = "id", scope = ResellerAdmin.class)
    public JAXBElement<Integer> createResellerAdminId(Integer value) {
        return new JAXBElement<Integer>(_RestrictionId_QNAME, Integer.class, ResellerAdmin.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", name = "parentId", scope = ResellerAdmin.class)
    public JAXBElement<Integer> createResellerAdminParentId(Integer value) {
        return new JAXBElement<Integer>(_ResellerAdminParentId_QNAME, Integer.class, ResellerAdmin.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", name = "mandatoryMembersChange", scope = ResellerAdmin.class)
    public JAXBElement<String> createResellerAdminMandatoryMembersChange(String value) {
        return new JAXBElement<String>(_RestrictionMandatoryMembersChange_QNAME, String.class, ResellerAdmin.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", name = "name", scope = ResellerAdmin.class)
    public JAXBElement<String> createResellerAdminName(String value) {
        return new JAXBElement<String>(_RestrictionName_QNAME, String.class, ResellerAdmin.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", name = "passwordMechset", scope = ResellerAdmin.class)
    public JAXBElement<Boolean> createResellerAdminPasswordMechset(Boolean value) {
        return new JAXBElement<Boolean>(_ResellerAdminPasswordMechset_QNAME, Boolean.class, ResellerAdmin.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", name = "displayname", scope = ResellerAdmin.class)
    public JAXBElement<String> createResellerAdminDisplayname(String value) {
        return new JAXBElement<String>(_ResellerAdminDisplayname_QNAME, String.class, ResellerAdmin.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", name = "mandatoryMembersDelete", scope = ResellerAdmin.class)
    public JAXBElement<String> createResellerAdminMandatoryMembersDelete(String value) {
        return new JAXBElement<String>(_RestrictionMandatoryMembersDelete_QNAME, String.class, ResellerAdmin.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", name = "restrictionsset", scope = ResellerAdmin.class)
    public JAXBElement<Boolean> createResellerAdminRestrictionsset(Boolean value) {
        return new JAXBElement<Boolean>(_ResellerAdminRestrictionsset_QNAME, Boolean.class, ResellerAdmin.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", name = "parentIdset", scope = ResellerAdmin.class)
    public JAXBElement<Boolean> createResellerAdminParentIdset(Boolean value) {
        return new JAXBElement<Boolean>(_ResellerAdminParentIdset_QNAME, Boolean.class, ResellerAdmin.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Restriction }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", name = "restrictions", scope = ResellerAdmin.class)
    public JAXBElement<Restriction> createResellerAdminRestrictions(Restriction value) {
        return new JAXBElement<Restriction>(_ResellerAdminRestrictions_QNAME, Restriction.class, ResellerAdmin.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", name = "displaynameset", scope = ResellerAdmin.class)
    public JAXBElement<Boolean> createResellerAdminDisplaynameset(Boolean value) {
        return new JAXBElement<Boolean>(_ResellerAdminDisplaynameset_QNAME, Boolean.class, ResellerAdmin.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", name = "idset", scope = ResellerAdmin.class)
    public JAXBElement<Boolean> createResellerAdminIdset(Boolean value) {
        return new JAXBElement<Boolean>(_ResellerAdminIdset_QNAME, Boolean.class, ResellerAdmin.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", name = "passwordset", scope = ResellerAdmin.class)
    public JAXBElement<Boolean> createResellerAdminPasswordset(Boolean value) {
        return new JAXBElement<Boolean>(_ResellerAdminPasswordset_QNAME, Boolean.class, ResellerAdmin.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", name = "passwordMech", scope = ResellerAdmin.class)
    public JAXBElement<String> createResellerAdminPasswordMech(String value) {
        return new JAXBElement<String>(_ResellerAdminPasswordMech_QNAME, String.class, ResellerAdmin.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", name = "nameset", scope = ResellerAdmin.class)
    public JAXBElement<Boolean> createResellerAdminNameset(Boolean value) {
        return new JAXBElement<Boolean>(_ResellerAdminNameset_QNAME, Boolean.class, ResellerAdmin.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://dataobjects.rmi.reseller.admin.openexchange.com/xsd", name = "password", scope = ResellerAdmin.class)
    public JAXBElement<String> createResellerAdminPassword(String value) {
        return new JAXBElement<String>(_ResellerAdminPassword_QNAME, String.class, ResellerAdmin.class, value);
    }

}

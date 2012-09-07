
package com.openexchange.admin.soap.reseller.context.rmi.dataobjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * <p>Java-Klasse f\u00fcr Database complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Database">
 *   &lt;complexContent>
 *     &lt;extension base="{http://dataobjects.rmi.admin.openexchange.com/xsd}EnforceableDataObject">
 *       &lt;sequence>
 *         &lt;element name="clusterWeight" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="clusterWeightset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="currentUnits" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="currentUnitsset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="driver" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="driverset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="idset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="login" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="loginset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="mandatoryMembersChange" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="mandatoryMembersCreate" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="mandatoryMembersDelete" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="mandatoryMembersRegister" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="master" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="masterId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="masterIdset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="masterset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="maxUnits" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="maxUnitsset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="nameset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="password" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="passwordset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="poolHardLimit" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="poolHardLimitset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="poolInitial" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="poolInitialset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="poolMax" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="poolMaxset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="read_id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="read_idset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="scheme" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="schemeset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="urlset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Database", propOrder = {
    "rest"
})
public class Database
    extends EnforceableDataObject
{

    @XmlElementRefs({
        @XmlElementRef(name = "driverset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "urlset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "scheme", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "poolHardLimitset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "currentUnitsset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "nameset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "read_idset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "id", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "poolMax", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "mandatoryMembersChange", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "maxUnits", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "idset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "login", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "poolInitialset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "name", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "password", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "driver", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "mandatoryMembersRegister", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "masterset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "read_id", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "mandatoryMembersCreate", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "poolHardLimit", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "loginset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "mandatoryMembersDelete", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "poolMaxset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "passwordset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "master", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "url", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "clusterWeight", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "masterId", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "schemeset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "clusterWeightset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "poolInitial", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "currentUnits", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "maxUnitsset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class),
        @XmlElementRef(name = "masterIdset", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", type = JAXBElement.class)
    })
    protected List<JAXBElement<? extends Serializable>> rest;

    /**
     * Ruft das restliche Contentmodell ab. 
     * 
     * <p>
     * Sie rufen diese "catch-all"-Eigenschaft aus folgendem Grund ab: 
     * Der Feldname "MandatoryMembersChange" wird von zwei verschiedenen Teilen eines Schemas verwendet. Siehe: 
     * Zeile 0 von http://192.168.32.167/servlet/axis2/services/OXResellerContextService?wsdl#types4
     * Zeile 0 von http://192.168.32.167/servlet/axis2/services/OXResellerContextService?wsdl#types4
     * <p>
     * Um diese Eigenschaft zu entfernen, wenden Sie eine Eigenschaftenanpassung f\u00fcr eine
     * der beiden folgenden Deklarationen an, um deren Namen zu \u00e4ndern: 
     * Gets the value of the rest property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rest property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRest().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends Serializable>> getRest() {
        if (rest == null) {
            rest = new ArrayList<JAXBElement<? extends Serializable>>();
        }
        return this.rest;
    }

    private <V extends Serializable> V getByName(final String name, final V defaultValue) {
        for (final JAXBElement<? extends Serializable> jaxbElement : getRest()) {
            if (name.equals(jaxbElement.getName().getLocalPart())) {
                return (V) (jaxbElement.isNil() ? defaultValue : jaxbElement.getValue());
            }
        }
        return null;
    }

    private <V extends Serializable> void setByName(final String name, final Class<V> clazz, final V value) {
        final List<JAXBElement<? extends Serializable>> rests = getRest();
        for (final JAXBElement<? extends Serializable> jaxbElement : rests) {
            if (name.equals(jaxbElement.getName().getLocalPart())) {
                final JAXBElement<V> cur = (JAXBElement<V>) jaxbElement;
                cur.setValue(value);
                return;
            }
        }
        final QName qname = new QName("http://dataobjects.rmi.admin.openexchange.com/xsd", name);
        rests.add(new JAXBElement<V>(qname, clazz, value));
    }
    
    /**
     * Ruft den Wert der clusterWeight-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getClusterWeight() {
        return getByName("clusterWeight", null);
    }
    
    /**
     * Legt den Wert der clusterWeight-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setClusterWeight(final Integer value) {
        setByName("clusterWeight", Integer.class, value);
    }
    
    /**
     * Ruft den Wert der currentUnits-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCurrentUnits() {
        return getByName("currentUnits", null);
    }
    
    /**
     * Legt den Wert der currentUnits-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCurrentUnits(final Integer value) {
        setByName("currentUnits", Integer.class, value);
    }
    
    /**
     * Ruft den Wert der driver-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDriver() {
        return getByName("driver", null);
    }
    
    /**
     * Legt den Wert der driver-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDriver(final String value) {
        setByName("drive", String.class, value);
    }
    
    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getId() {
        return getByName("id", null);
    }
    
    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setId(final Integer value) {
        setByName("id", Integer.class, value);
    }
    
    /**
     * Ruft den Wert der login-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogin() {
        return getByName("login", null);
    }
    
    /**
     * Legt den Wert der login-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogin(final String value) {
        setByName("login", String.class, value);
    }
    
    /**
     * Ruft den Wert der master-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isMaster() {
        return getByName("master", null);
    }
    
    /**
     * Legt den Wert der master-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMaster(final Boolean value) {
        setByName("master", Boolean.class, value);
    }
    
    /**
     * Ruft den Wert der masterId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMasterId() {
        return getByName("masterId", null);
    }
    
    /**
     * Legt den Wert der masterId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMasterId(final Integer value) {
        setByName("masterId", Integer.class, value);
    }
    
    /**
     * Ruft den Wert der maxUnits-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMaxUnits() {
        return getByName("maxUnits", null);
    }
    
    /**
     * Legt den Wert der maxUnits-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMaxUnits(final Integer value) {
        setByName("maxUnits", Integer.class, value);
    }
    
    /**
     * Ruft den Wert der name-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return getByName("name", null);
    }
    
    /**
     * Legt den Wert der name-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(final String value) {
        setByName("name", String.class, value);
    }
    
    /**
     * Ruft den Wert der password-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassword() {
        return getByName("password", null);
    }
    
    /**
     * Legt den Wert der password-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassword(final String value) {
        setByName("password", String.class, value);
    }
    
    /**
     * Ruft den Wert der poolHardLimit-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPoolHardLimit() {
        return getByName("poolHardLimit", null);
    }
    
    /**
     * Legt den Wert der poolHardLimit-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPoolHardLimit(final Integer value) {
        setByName("poolHardLimit", Integer.class, value);
    }
    
    /**
     * Ruft den Wert der poolInitial-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPoolInitial() {
        return getByName("poolInitial", null);
    }
    
    /**
     * Legt den Wert der poolInitial-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPoolInitial(final Integer value) {
        setByName("poolInitial", Integer.class, value);
    }
    
    /**
     * Ruft den Wert der poolMax-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPoolMax() {
        return getByName("poolMax", null);
    }
    
    /**
     * Legt den Wert der poolMax-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPoolMax(final Integer value) {
        setByName("poolMax", Integer.class, value);
    }
    
    /**
     * Ruft den Wert der readId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getReadId() {
        return getByName("readId", null);
    }
    
    /**
     * Legt den Wert der readId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setReadId(final Integer value) {
        setByName("readId", Integer.class, value);
    }
    
    /**
     * Ruft den Wert der scheme-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScheme() {
        return getByName("scheme", null);
    }
    
    /**
     * Legt den Wert der scheme-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScheme(final String value) {
        setByName("scheme", String.class, value);
    }
    
    /**
     * Ruft den Wert der url-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUrl() {
        return getByName("url", null);
    }
    
    /**
     * Legt den Wert der url-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUrl(final String value) {
        setByName("url", String.class, value);
    }
}

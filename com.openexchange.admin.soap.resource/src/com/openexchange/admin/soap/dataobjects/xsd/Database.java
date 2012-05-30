
package com.openexchange.admin.soap.dataobjects.xsd;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für Database complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Database">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="clusterWeight" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="currentUnits" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="driver" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="login" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="master" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="masterId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="maxUnits" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="password" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="poolHardLimit" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="poolInitial" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="poolMax" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="read_id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="scheme" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Database", propOrder = {
    "clusterWeight",
    "currentUnits",
    "driver",
    "id",
    "login",
    "master",
    "masterId",
    "maxUnits",
    "name",
    "password",
    "poolHardLimit",
    "poolInitial",
    "poolMax",
    "readId",
    "scheme",
    "url"
})
public class Database {

    @XmlElementRef(name = "clusterWeight", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Integer> clusterWeight;
    @XmlElementRef(name = "currentUnits", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Integer> currentUnits;
    @XmlElementRef(name = "driver", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> driver;
    @XmlElementRef(name = "id", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Integer> id;
    @XmlElementRef(name = "login", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> login;
    @XmlElementRef(name = "master", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> master;
    @XmlElementRef(name = "masterId", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Integer> masterId;
    @XmlElementRef(name = "maxUnits", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Integer> maxUnits;
    @XmlElementRef(name = "name", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> name;
    @XmlElementRef(name = "password", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> password;
    @XmlElementRef(name = "poolHardLimit", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Integer> poolHardLimit;
    @XmlElementRef(name = "poolInitial", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Integer> poolInitial;
    @XmlElementRef(name = "poolMax", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Integer> poolMax;
    @XmlElementRef(name = "read_id", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Integer> readId;
    @XmlElementRef(name = "scheme", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> scheme;
    @XmlElementRef(name = "url", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> url;

    /**
     * Ruft den Wert der clusterWeight-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getClusterWeight() {
        return clusterWeight;
    }

    /**
     * Legt den Wert der clusterWeight-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setClusterWeight(JAXBElement<Integer> value) {
        this.clusterWeight = value;
    }

    /**
     * Ruft den Wert der currentUnits-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getCurrentUnits() {
        return currentUnits;
    }

    /**
     * Legt den Wert der currentUnits-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setCurrentUnits(JAXBElement<Integer> value) {
        this.currentUnits = value;
    }

    /**
     * Ruft den Wert der driver-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getDriver() {
        return driver;
    }

    /**
     * Legt den Wert der driver-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setDriver(JAXBElement<String> value) {
        this.driver = value;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setId(JAXBElement<Integer> value) {
        this.id = value;
    }

    /**
     * Ruft den Wert der login-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getLogin() {
        return login;
    }

    /**
     * Legt den Wert der login-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setLogin(JAXBElement<String> value) {
        this.login = value;
    }

    /**
     * Ruft den Wert der master-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getMaster() {
        return master;
    }

    /**
     * Legt den Wert der master-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setMaster(JAXBElement<Boolean> value) {
        this.master = value;
    }

    /**
     * Ruft den Wert der masterId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getMasterId() {
        return masterId;
    }

    /**
     * Legt den Wert der masterId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setMasterId(JAXBElement<Integer> value) {
        this.masterId = value;
    }

    /**
     * Ruft den Wert der maxUnits-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getMaxUnits() {
        return maxUnits;
    }

    /**
     * Legt den Wert der maxUnits-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setMaxUnits(JAXBElement<Integer> value) {
        this.maxUnits = value;
    }

    /**
     * Ruft den Wert der name-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getName() {
        return name;
    }

    /**
     * Legt den Wert der name-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setName(JAXBElement<String> value) {
        this.name = value;
    }

    /**
     * Ruft den Wert der password-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getPassword() {
        return password;
    }

    /**
     * Legt den Wert der password-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setPassword(JAXBElement<String> value) {
        this.password = value;
    }

    /**
     * Ruft den Wert der poolHardLimit-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getPoolHardLimit() {
        return poolHardLimit;
    }

    /**
     * Legt den Wert der poolHardLimit-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setPoolHardLimit(JAXBElement<Integer> value) {
        this.poolHardLimit = value;
    }

    /**
     * Ruft den Wert der poolInitial-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getPoolInitial() {
        return poolInitial;
    }

    /**
     * Legt den Wert der poolInitial-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setPoolInitial(JAXBElement<Integer> value) {
        this.poolInitial = value;
    }

    /**
     * Ruft den Wert der poolMax-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getPoolMax() {
        return poolMax;
    }

    /**
     * Legt den Wert der poolMax-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setPoolMax(JAXBElement<Integer> value) {
        this.poolMax = value;
    }

    /**
     * Ruft den Wert der readId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getReadId() {
        return readId;
    }

    /**
     * Legt den Wert der readId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setReadId(JAXBElement<Integer> value) {
        this.readId = value;
    }

    /**
     * Ruft den Wert der scheme-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getScheme() {
        return scheme;
    }

    /**
     * Legt den Wert der scheme-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setScheme(JAXBElement<String> value) {
        this.scheme = value;
    }

    /**
     * Ruft den Wert der url-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUrl() {
        return url;
    }

    /**
     * Legt den Wert der url-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUrl(JAXBElement<String> value) {
        this.url = value;
    }

}

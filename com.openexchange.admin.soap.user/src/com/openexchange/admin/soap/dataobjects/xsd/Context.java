
package com.openexchange.admin.soap.dataobjects.xsd;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für Context complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Context">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="average_size" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="enabled" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="filestoreId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="filestore_name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="loginMappings" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="maxQuota" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="readDatabase" type="{http://dataobjects.soap.admin.openexchange.com/xsd}Database" minOccurs="0"/>
 *         &lt;element name="usedQuota" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="userAttributes" type="{http://dataobjects.soap.admin.openexchange.com/xsd}SOAPStringMapMap" minOccurs="0"/>
 *         &lt;element name="writeDatabase" type="{http://dataobjects.soap.admin.openexchange.com/xsd}Database" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Context", propOrder = {
    "averageSize",
    "enabled",
    "filestoreId",
    "filestoreName",
    "id",
    "loginMappings",
    "maxQuota",
    "name",
    "readDatabase",
    "usedQuota",
    "userAttributes",
    "writeDatabase"
})
public class Context {

    @XmlElementRef(name = "average_size", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Long> averageSize;
    @XmlElementRef(name = "enabled", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> enabled;
    @XmlElementRef(name = "filestoreId", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Integer> filestoreId;
    @XmlElementRef(name = "filestore_name", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> filestoreName;
    @XmlElementRef(name = "id", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Integer> id;
    @XmlElement(nillable = true)
    protected List<String> loginMappings;
    @XmlElementRef(name = "maxQuota", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Long> maxQuota;
    @XmlElementRef(name = "name", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> name;
    @XmlElementRef(name = "readDatabase", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Database> readDatabase;
    @XmlElementRef(name = "usedQuota", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Long> usedQuota;
    @XmlElementRef(name = "userAttributes", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<SOAPStringMapMap> userAttributes;
    @XmlElementRef(name = "writeDatabase", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Database> writeDatabase;

    /**
     * Ruft den Wert der averageSize-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Long }{@code >}
     *     
     */
    public JAXBElement<Long> getAverageSize() {
        return averageSize;
    }

    /**
     * Legt den Wert der averageSize-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Long }{@code >}
     *     
     */
    public void setAverageSize(JAXBElement<Long> value) {
        this.averageSize = value;
    }

    /**
     * Ruft den Wert der enabled-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getEnabled() {
        return enabled;
    }

    /**
     * Legt den Wert der enabled-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setEnabled(JAXBElement<Boolean> value) {
        this.enabled = value;
    }

    /**
     * Ruft den Wert der filestoreId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getFilestoreId() {
        return filestoreId;
    }

    /**
     * Legt den Wert der filestoreId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setFilestoreId(JAXBElement<Integer> value) {
        this.filestoreId = value;
    }

    /**
     * Ruft den Wert der filestoreName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getFilestoreName() {
        return filestoreName;
    }

    /**
     * Legt den Wert der filestoreName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setFilestoreName(JAXBElement<String> value) {
        this.filestoreName = value;
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
     * Gets the value of the loginMappings property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the loginMappings property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLoginMappings().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getLoginMappings() {
        if (loginMappings == null) {
            loginMappings = new ArrayList<String>();
        }
        return this.loginMappings;
    }

    /**
     * Ruft den Wert der maxQuota-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Long }{@code >}
     *     
     */
    public JAXBElement<Long> getMaxQuota() {
        return maxQuota;
    }

    /**
     * Legt den Wert der maxQuota-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Long }{@code >}
     *     
     */
    public void setMaxQuota(JAXBElement<Long> value) {
        this.maxQuota = value;
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
     * Ruft den Wert der readDatabase-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Database }{@code >}
     *     
     */
    public JAXBElement<Database> getReadDatabase() {
        return readDatabase;
    }

    /**
     * Legt den Wert der readDatabase-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Database }{@code >}
     *     
     */
    public void setReadDatabase(JAXBElement<Database> value) {
        this.readDatabase = value;
    }

    /**
     * Ruft den Wert der usedQuota-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Long }{@code >}
     *     
     */
    public JAXBElement<Long> getUsedQuota() {
        return usedQuota;
    }

    /**
     * Legt den Wert der usedQuota-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Long }{@code >}
     *     
     */
    public void setUsedQuota(JAXBElement<Long> value) {
        this.usedQuota = value;
    }

    /**
     * Ruft den Wert der userAttributes-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link SOAPStringMapMap }{@code >}
     *     
     */
    public JAXBElement<SOAPStringMapMap> getUserAttributes() {
        return userAttributes;
    }

    /**
     * Legt den Wert der userAttributes-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link SOAPStringMapMap }{@code >}
     *     
     */
    public void setUserAttributes(JAXBElement<SOAPStringMapMap> value) {
        this.userAttributes = value;
    }

    /**
     * Ruft den Wert der writeDatabase-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Database }{@code >}
     *     
     */
    public JAXBElement<Database> getWriteDatabase() {
        return writeDatabase;
    }

    /**
     * Legt den Wert der writeDatabase-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Database }{@code >}
     *     
     */
    public void setWriteDatabase(JAXBElement<Database> value) {
        this.writeDatabase = value;
    }

}

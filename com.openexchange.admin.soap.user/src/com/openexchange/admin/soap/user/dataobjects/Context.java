
package com.openexchange.admin.soap.user.dataobjects;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse f\u00fcr Context complex type.
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

    @XmlElement(name = "average_size", nillable = true)
    protected Long averageSize;
    @XmlElement(nillable = true)
    protected Boolean enabled;
    @XmlElement(nillable = true)
    protected Integer filestoreId;
    @XmlElement(name = "filestore_name", nillable = true)
    protected String filestoreName;
    @XmlElement(nillable = true)
    protected Integer id;
    @XmlElement(nillable = true)
    protected List<String> loginMappings;
    @XmlElement(nillable = true)
    protected Long maxQuota;
    @XmlElement(nillable = true)
    protected String name;
    @XmlElement(nillable = true)
    protected Database readDatabase;
    @XmlElement(nillable = true)
    protected Long usedQuota;
    @XmlElement(nillable = true)
    protected SOAPStringMapMap userAttributes;
    @XmlElement(nillable = true)
    protected Database writeDatabase;

    /**
     * Ruft den Wert der averageSize-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public Long getAverageSize() {
        return averageSize;
    }

    /**
     * Legt den Wert der averageSize-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setAverageSize(Long value) {
        this.averageSize = value;
    }

    /**
     * Ruft den Wert der enabled-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isEnabled() {
        return enabled;
    }

    /**
     * Legt den Wert der enabled-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setEnabled(Boolean value) {
        this.enabled = value;
    }

    /**
     * Ruft den Wert der filestoreId-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getFilestoreId() {
        return filestoreId;
    }

    /**
     * Legt den Wert der filestoreId-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setFilestoreId(Integer value) {
        this.filestoreId = value;
    }

    /**
     * Ruft den Wert der filestoreName-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFilestoreName() {
        return filestoreName;
    }

    /**
     * Legt den Wert der filestoreName-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFilestoreName(String value) {
        this.filestoreName = value;
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
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setId(Integer value) {
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
     * Sets the loginMappings
     *
     * @param loginMappings The loginMappings to set
     */
    public void setLoginMappings(List<String> loginMappings) {
        this.loginMappings = loginMappings;
    }

    /**
     * Ruft den Wert der maxQuota-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public Long getMaxQuota() {
        return maxQuota;
    }

    /**
     * Legt den Wert der maxQuota-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setMaxQuota(Long value) {
        this.maxQuota = value;
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
        return name;
    }

    /**
     * Legt den Wert der name-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Ruft den Wert der readDatabase-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Database }
     *
     */
    public Database getReadDatabase() {
        return readDatabase;
    }

    /**
     * Legt den Wert der readDatabase-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Database }
     *
     */
    public void setReadDatabase(Database value) {
        this.readDatabase = value;
    }

    /**
     * Ruft den Wert der usedQuota-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public Long getUsedQuota() {
        return usedQuota;
    }

    /**
     * Legt den Wert der usedQuota-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setUsedQuota(Long value) {
        this.usedQuota = value;
    }

    /**
     * Ruft den Wert der userAttributes-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link SOAPStringMapMap }
     *
     */
    public SOAPStringMapMap getUserAttributes() {
        return userAttributes;
    }

    /**
     * Legt den Wert der userAttributes-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link SOAPStringMapMap }
     *
     */
    public void setUserAttributes(SOAPStringMapMap value) {
        this.userAttributes = value;
    }

    /**
     * Ruft den Wert der writeDatabase-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Database }
     *
     */
    public Database getWriteDatabase() {
        return writeDatabase;
    }

    /**
     * Legt den Wert der writeDatabase-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Database }
     *
     */
    public void setWriteDatabase(Database value) {
        this.writeDatabase = value;
    }

}

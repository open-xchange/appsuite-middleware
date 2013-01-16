
package com.openexchange.admin.soap.reseller.context.rmi.dataobjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse f\u00fcr Filestore complex type.
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 *
 * <pre>
 * &lt;complexType name="Filestore">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="currentContexts" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="currentContextsset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="idset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="maxContexts" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="maxContextsset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="reserved" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="reservedset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="size" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="sizeset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="urlset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="used" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="usedset" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Filestore", propOrder = {
    "currentContexts",
    "currentContextsset",
    "id",
    "idset",
    "maxContexts",
    "maxContextsset",
    "reserved",
    "reservedset",
    "size",
    "sizeset",
    "url",
    "urlset",
    "used",
    "usedset"
})
public class Filestore {

    @XmlElement(nillable = true)
    protected Integer currentContexts;
    protected Boolean currentContextsset;
    @XmlElement(nillable = true)
    protected Integer id;
    protected Boolean idset;
    @XmlElement(nillable = true)
    protected Integer maxContexts;
    protected Boolean maxContextsset;
    @XmlElement(nillable = true)
    protected Long reserved;
    protected Boolean reservedset;
    @XmlElement(nillable = true)
    protected Long size;
    protected Boolean sizeset;
    @XmlElement(nillable = true)
    protected String url;
    protected Boolean urlset;
    @XmlElement(nillable = true)
    protected Long used;
    protected Boolean usedset;

    /**
     * Ruft den Wert der currentContexts-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getCurrentContexts() {
        return currentContexts;
    }

    /**
     * Legt den Wert der currentContexts-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setCurrentContexts(Integer value) {
        this.currentContexts = value;
    }

    /**
     * Ruft den Wert der currentContextsset-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isCurrentContextsset() {
        return currentContextsset;
    }

    /**
     * Legt den Wert der currentContextsset-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setCurrentContextsset(Boolean value) {
        this.currentContextsset = value;
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
     * Ruft den Wert der idset-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isIdset() {
        return idset;
    }

    /**
     * Legt den Wert der idset-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setIdset(Boolean value) {
        this.idset = value;
    }

    /**
     * Ruft den Wert der maxContexts-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getMaxContexts() {
        return maxContexts;
    }

    /**
     * Legt den Wert der maxContexts-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setMaxContexts(Integer value) {
        this.maxContexts = value;
    }

    /**
     * Ruft den Wert der maxContextsset-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isMaxContextsset() {
        return maxContextsset;
    }

    /**
     * Legt den Wert der maxContextsset-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setMaxContextsset(Boolean value) {
        this.maxContextsset = value;
    }

    /**
     * Ruft den Wert der reserved-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public Long getReserved() {
        return reserved;
    }

    /**
     * Legt den Wert der reserved-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setReserved(Long value) {
        this.reserved = value;
    }

    /**
     * Ruft den Wert der reservedset-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isReservedset() {
        return reservedset;
    }

    /**
     * Legt den Wert der reservedset-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setReservedset(Boolean value) {
        this.reservedset = value;
    }

    /**
     * Ruft den Wert der size-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public Long getSize() {
        return size;
    }

    /**
     * Legt den Wert der size-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setSize(Long value) {
        this.size = value;
    }

    /**
     * Ruft den Wert der sizeset-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isSizeset() {
        return sizeset;
    }

    /**
     * Legt den Wert der sizeset-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setSizeset(Boolean value) {
        this.sizeset = value;
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
        return url;
    }

    /**
     * Legt den Wert der url-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUrl(String value) {
        this.url = value;
    }

    /**
     * Ruft den Wert der urlset-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isUrlset() {
        return urlset;
    }

    /**
     * Legt den Wert der urlset-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setUrlset(Boolean value) {
        this.urlset = value;
    }

    /**
     * Ruft den Wert der used-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public Long getUsed() {
        return used;
    }

    /**
     * Legt den Wert der used-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setUsed(Long value) {
        this.used = value;
    }

    /**
     * Ruft den Wert der usedset-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isUsedset() {
        return usedset;
    }

    /**
     * Legt den Wert der usedset-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setUsedset(Boolean value) {
        this.usedset = value;
    }

}


package com.openexchange.admin.soap.context.dataobjects;

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
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="maxContexts" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="reserved" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="size" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="used" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
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
    "id",
    "maxContexts",
    "reserved",
    "size",
    "url",
    "used"
})
public class Filestore {

    @XmlElement(nillable = true)
    protected Integer currentContexts;
    @XmlElement(nillable = true)
    protected Integer id;
    @XmlElement(nillable = true)
    protected Integer maxContexts;
    @XmlElement(nillable = true)
    protected Long reserved;
    @XmlElement(nillable = true)
    protected Long size;
    @XmlElement(nillable = true)
    protected String url;
    @XmlElement(nillable = true)
    protected Long used;

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

}

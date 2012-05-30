
package com.openexchange.admin.soap.dataobjects.xsd;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für Filestore complex type.
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

    @XmlElementRef(name = "currentContexts", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Integer> currentContexts;
    @XmlElementRef(name = "id", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Integer> id;
    @XmlElementRef(name = "maxContexts", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Integer> maxContexts;
    @XmlElementRef(name = "reserved", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Long> reserved;
    @XmlElementRef(name = "size", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Long> size;
    @XmlElementRef(name = "url", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> url;
    @XmlElementRef(name = "used", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Long> used;

    /**
     * Ruft den Wert der currentContexts-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getCurrentContexts() {
        return currentContexts;
    }

    /**
     * Legt den Wert der currentContexts-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setCurrentContexts(JAXBElement<Integer> value) {
        this.currentContexts = value;
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
     * Ruft den Wert der maxContexts-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getMaxContexts() {
        return maxContexts;
    }

    /**
     * Legt den Wert der maxContexts-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setMaxContexts(JAXBElement<Integer> value) {
        this.maxContexts = value;
    }

    /**
     * Ruft den Wert der reserved-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Long }{@code >}
     *     
     */
    public JAXBElement<Long> getReserved() {
        return reserved;
    }

    /**
     * Legt den Wert der reserved-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Long }{@code >}
     *     
     */
    public void setReserved(JAXBElement<Long> value) {
        this.reserved = value;
    }

    /**
     * Ruft den Wert der size-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Long }{@code >}
     *     
     */
    public JAXBElement<Long> getSize() {
        return size;
    }

    /**
     * Legt den Wert der size-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Long }{@code >}
     *     
     */
    public void setSize(JAXBElement<Long> value) {
        this.size = value;
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

    /**
     * Ruft den Wert der used-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Long }{@code >}
     *     
     */
    public JAXBElement<Long> getUsed() {
        return used;
    }

    /**
     * Legt den Wert der used-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Long }{@code >}
     *     
     */
    public void setUsed(JAXBElement<Long> value) {
        this.used = value;
    }

}

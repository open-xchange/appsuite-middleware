
package com.openexchange.admin.soap.dataobjects.xsd;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für SOAPMapEntry complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="SOAPMapEntry">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="value" type="{http://dataobjects.soap.admin.openexchange.com/xsd}SOAPStringMap" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SOAPMapEntry", propOrder = {
    "key",
    "value"
})
public class SOAPMapEntry {

    @XmlElementRef(name = "key", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> key;
    @XmlElementRef(name = "value", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<SOAPStringMap> value;

    /**
     * Ruft den Wert der key-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getKey() {
        return key;
    }

    /**
     * Legt den Wert der key-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setKey(JAXBElement<String> value) {
        this.key = value;
    }

    /**
     * Ruft den Wert der value-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link SOAPStringMap }{@code >}
     *     
     */
    public JAXBElement<SOAPStringMap> getValue() {
        return value;
    }

    /**
     * Legt den Wert der value-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link SOAPStringMap }{@code >}
     *     
     */
    public void setValue(JAXBElement<SOAPStringMap> value) {
        this.value = value;
    }

}


package com.openexchange.admin.soap.resource.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="NoSuchResourceException" type="{http://exceptions.rmi.admin.openexchange.com/xsd}NoSuchResourceException" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "noSuchResourceException"
})
@XmlRootElement(name = "NoSuchResourceException")
public class NoSuchResourceException {

    @XmlElement(name = "NoSuchResourceException", nillable = true)
    protected com.openexchange.admin.soap.resource.exceptions.NoSuchResourceException noSuchResourceException;

    /**
     * Ruft den Wert der noSuchResourceException-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link com.openexchange.admin.soap.resource.exceptions.NoSuchResourceException }
     *     
     */
    public com.openexchange.admin.soap.resource.exceptions.NoSuchResourceException getNoSuchResourceException() {
        return noSuchResourceException;
    }

    /**
     * Legt den Wert der noSuchResourceException-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link com.openexchange.admin.soap.resource.exceptions.NoSuchResourceException }
     *     
     */
    public void setNoSuchResourceException(com.openexchange.admin.soap.resource.exceptions.NoSuchResourceException value) {
        this.noSuchResourceException = value;
    }

}

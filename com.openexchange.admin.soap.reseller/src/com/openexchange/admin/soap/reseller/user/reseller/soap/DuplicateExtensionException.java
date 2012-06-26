
package com.openexchange.admin.soap.reseller.user.reseller.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse f\u00fcr anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DuplicateExtensionException" type="{http://exceptions.rmi.admin.openexchange.com/xsd}DuplicateExtensionException" minOccurs="0"/>
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
    "duplicateExtensionException"
})
@XmlRootElement(name = "DuplicateExtensionException")
public class DuplicateExtensionException {

    @XmlElement(name = "DuplicateExtensionException", nillable = true)
    protected com.openexchange.admin.soap.reseller.user.rmi.exceptions.DuplicateExtensionException duplicateExtensionException;

    /**
     * Ruft den Wert der duplicateExtensionException-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link com.openexchange.admin.soap.reseller.user.rmi.exceptions.DuplicateExtensionException }
     *     
     */
    public com.openexchange.admin.soap.reseller.user.rmi.exceptions.DuplicateExtensionException getDuplicateExtensionException() {
        return duplicateExtensionException;
    }

    /**
     * Legt den Wert der duplicateExtensionException-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link com.openexchange.admin.soap.reseller.user.rmi.exceptions.DuplicateExtensionException }
     *     
     */
    public void setDuplicateExtensionException(com.openexchange.admin.soap.reseller.user.rmi.exceptions.DuplicateExtensionException value) {
        this.duplicateExtensionException = value;
    }

}

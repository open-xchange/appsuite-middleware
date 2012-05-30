
package com.openexchange.admin.soap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
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
 *         &lt;element name="NoSuchUserException" type="{http://exceptions.rmi.admin.openexchange.com/xsd}NoSuchUserException" minOccurs="0"/>
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
    "noSuchUserException"
})
@XmlRootElement(name = "NoSuchUserException")
public class NoSuchUserException {

    @XmlElementRef(name = "NoSuchUserException", namespace = "http://soap.admin.openexchange.com", type = JAXBElement.class)
    protected JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.NoSuchUserException> noSuchUserException;

    /**
     * Ruft den Wert der noSuchUserException-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link com.openexchange.admin.rmi.exceptions.xsd.NoSuchUserException }{@code >}
     *     
     */
    public JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.NoSuchUserException> getNoSuchUserException() {
        return noSuchUserException;
    }

    /**
     * Legt den Wert der noSuchUserException-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link com.openexchange.admin.rmi.exceptions.xsd.NoSuchUserException }{@code >}
     *     
     */
    public void setNoSuchUserException(JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.NoSuchUserException> value) {
        this.noSuchUserException = value;
    }

}

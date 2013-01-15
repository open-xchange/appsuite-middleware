
package com.openexchange.admin.soap.reseller.context.reseller.soap;

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
 *         &lt;element name="InvalidCredentialsException" type="{http://exceptions.rmi.admin.openexchange.com/xsd}InvalidCredentialsException" minOccurs="0"/>
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
    "invalidCredentialsException"
})
@XmlRootElement(name = "InvalidCredentialsException")
public class InvalidCredentialsException {

    @XmlElement(name = "InvalidCredentialsException", nillable = true)
    protected com.openexchange.admin.soap.reseller.context.rmi.exceptions.InvalidCredentialsException invalidCredentialsException;

    /**
     * Ruft den Wert der invalidCredentialsException-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link com.openexchange.admin.soap.reseller.context.rmi.exceptions.InvalidCredentialsException }
     *
     */
    public com.openexchange.admin.soap.reseller.context.rmi.exceptions.InvalidCredentialsException getInvalidCredentialsException() {
        return invalidCredentialsException;
    }

    /**
     * Legt den Wert der invalidCredentialsException-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link com.openexchange.admin.soap.reseller.context.rmi.exceptions.InvalidCredentialsException }
     *
     */
    public void setInvalidCredentialsException(com.openexchange.admin.soap.reseller.context.rmi.exceptions.InvalidCredentialsException value) {
        this.invalidCredentialsException = value;
    }

}

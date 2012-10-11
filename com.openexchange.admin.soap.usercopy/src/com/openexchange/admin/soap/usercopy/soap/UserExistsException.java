
package com.openexchange.admin.soap.usercopy.soap;

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
 *         &lt;element name="UserExistsException" type="{http://exceptions.rmi.admin.openexchange.com/xsd}UserExistsException" minOccurs="0"/>
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
    "userExistsException"
})
@XmlRootElement(name = "UserExistsException")
public class UserExistsException {

    @XmlElement(name = "UserExistsException", nillable = true)
    protected com.openexchange.admin.soap.usercopy.exceptions.UserExistsException userExistsException;

    /**
     * Ruft den Wert der userExistsException-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link com.openexchange.admin.soap.usercopy.exceptions.UserExistsException }
     *     
     */
    public com.openexchange.admin.soap.usercopy.exceptions.UserExistsException getUserExistsException() {
        return userExistsException;
    }

    /**
     * Legt den Wert der userExistsException-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link com.openexchange.admin.soap.usercopy.exceptions.UserExistsException }
     *     
     */
    public void setUserExistsException(com.openexchange.admin.soap.usercopy.exceptions.UserExistsException value) {
        this.userExistsException = value;
    }

}

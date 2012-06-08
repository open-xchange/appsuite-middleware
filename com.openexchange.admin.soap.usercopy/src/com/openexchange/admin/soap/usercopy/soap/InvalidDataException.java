
package com.openexchange.admin.soap.usercopy.soap;

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
 *         &lt;element name="InvalidDataException" type="{http://exceptions.rmi.admin.openexchange.com/xsd}InvalidDataException" minOccurs="0"/>
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
    "invalidDataException"
})
@XmlRootElement(name = "InvalidDataException")
public class InvalidDataException {

    @XmlElement(name = "InvalidDataException", nillable = true)
    protected com.openexchange.admin.soap.usercopy.exceptions.InvalidDataException invalidDataException;

    /**
     * Ruft den Wert der invalidDataException-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link com.openexchange.admin.soap.usercopy.exceptions.InvalidDataException }
     *     
     */
    public com.openexchange.admin.soap.usercopy.exceptions.InvalidDataException getInvalidDataException() {
        return invalidDataException;
    }

    /**
     * Legt den Wert der invalidDataException-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link com.openexchange.admin.soap.usercopy.exceptions.InvalidDataException }
     *     
     */
    public void setInvalidDataException(com.openexchange.admin.soap.usercopy.exceptions.InvalidDataException value) {
        this.invalidDataException = value;
    }

}

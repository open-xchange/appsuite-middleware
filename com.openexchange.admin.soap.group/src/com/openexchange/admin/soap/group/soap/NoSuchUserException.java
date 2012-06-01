
package com.openexchange.admin.soap.group.soap;

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

    @XmlElement(name = "NoSuchUserException", nillable = true)
    protected com.openexchange.admin.soap.group.exceptions.NoSuchUserException noSuchUserException;

    /**
     * Ruft den Wert der noSuchUserException-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link com.openexchange.admin.soap.group.exceptions.NoSuchUserException }
     *     
     */
    public com.openexchange.admin.soap.group.exceptions.NoSuchUserException getNoSuchUserException() {
        return noSuchUserException;
    }

    /**
     * Legt den Wert der noSuchUserException-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link com.openexchange.admin.soap.group.exceptions.NoSuchUserException }
     *     
     */
    public void setNoSuchUserException(com.openexchange.admin.soap.group.exceptions.NoSuchUserException value) {
        this.noSuchUserException = value;
    }

}

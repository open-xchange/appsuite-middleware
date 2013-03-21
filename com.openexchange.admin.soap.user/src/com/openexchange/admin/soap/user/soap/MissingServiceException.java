
package com.openexchange.admin.soap.user.soap;

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
 *         &lt;element name="MissingServiceException" type="{http://exceptions.rmi.admin.openexchange.com/xsd}MissingServiceException" minOccurs="0"/>
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
    "missingServiceException"
})
@XmlRootElement(name = "MissingServiceException")
public class MissingServiceException {

    @XmlElement(name = "MissingServiceException", nillable = true)
    protected com.openexchange.admin.soap.user.exceptions.MissingServiceException missingServiceException;

    /**
     * Ruft den Wert der missingServiceException-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link com.openexchange.admin.soap.user.exceptions.MissingServiceException }
     *
     */
    public com.openexchange.admin.soap.user.exceptions.MissingServiceException getMissingServiceException() {
        return missingServiceException;
    }

    /**
     * Legt den Wert der missingServiceException-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link com.openexchange.admin.soap.user.exceptions.MissingServiceException }
     *
     */
    public void setMissingServiceException(com.openexchange.admin.soap.user.exceptions.MissingServiceException value) {
        this.missingServiceException = value;
    }

}

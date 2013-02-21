
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
 *         &lt;element name="NoSuchContextException" type="{http://exceptions.rmi.admin.openexchange.com/xsd}NoSuchContextException" minOccurs="0"/>
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
    "noSuchContextException"
})
@XmlRootElement(name = "NoSuchContextException")
public class NoSuchContextException {

    @XmlElement(name = "NoSuchContextException", nillable = true)
    protected com.openexchange.admin.soap.reseller.user.rmi.exceptions.NoSuchContextException noSuchContextException;

    /**
     * Ruft den Wert der noSuchContextException-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link com.openexchange.admin.soap.reseller.user.rmi.exceptions.NoSuchContextException }
     *
     */
    public com.openexchange.admin.soap.reseller.user.rmi.exceptions.NoSuchContextException getNoSuchContextException() {
        return noSuchContextException;
    }

    /**
     * Legt den Wert der noSuchContextException-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link com.openexchange.admin.soap.reseller.user.rmi.exceptions.NoSuchContextException }
     *
     */
    public void setNoSuchContextException(com.openexchange.admin.soap.reseller.user.rmi.exceptions.NoSuchContextException value) {
        this.noSuchContextException = value;
    }

}


package com.openexchange.admin.soap.reseller.service.reseller.soap;

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
 *         &lt;element name="OXResellerException" type="{http://exceptions.rmi.reseller.admin.openexchange.com/xsd}OXResellerException" minOccurs="0"/>
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
    "oxResellerException"
})
@XmlRootElement(name = "OXResellerException")
public class OXResellerException {

    @XmlElement(name = "OXResellerException", nillable = true)
    protected com.openexchange.admin.soap.reseller.service.reseller.rmi.exceptions.OXResellerException oxResellerException;

    /**
     * Ruft den Wert der oxResellerException-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link com.openexchange.admin.soap.reseller.service.reseller.rmi.exceptions.OXResellerException }
     *
     */
    public com.openexchange.admin.soap.reseller.service.reseller.rmi.exceptions.OXResellerException getOXResellerException() {
        return oxResellerException;
    }

    /**
     * Legt den Wert der oxResellerException-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link com.openexchange.admin.soap.reseller.service.reseller.rmi.exceptions.OXResellerException }
     *
     */
    public void setOXResellerException(com.openexchange.admin.soap.reseller.service.reseller.rmi.exceptions.OXResellerException value) {
        this.oxResellerException = value;
    }

}

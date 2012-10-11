
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
 *         &lt;element name="OXContextException" type="{http://exceptions.rmi.admin.openexchange.com/xsd}OXContextException" minOccurs="0"/>
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
    "oxContextException"
})
@XmlRootElement(name = "OXContextException")
public class OXContextException {

    @XmlElement(name = "OXContextException", nillable = true)
    protected com.openexchange.admin.soap.reseller.context.rmi.exceptions.OXContextException oxContextException;

    /**
     * Ruft den Wert der oxContextException-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link com.openexchange.admin.soap.reseller.context.rmi.exceptions.OXContextException }
     *     
     */
    public com.openexchange.admin.soap.reseller.context.rmi.exceptions.OXContextException getOXContextException() {
        return oxContextException;
    }

    /**
     * Legt den Wert der oxContextException-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link com.openexchange.admin.soap.reseller.context.rmi.exceptions.OXContextException }
     *     
     */
    public void setOXContextException(com.openexchange.admin.soap.reseller.context.rmi.exceptions.OXContextException value) {
        this.oxContextException = value;
    }

}

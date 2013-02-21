
package com.openexchange.admin.soap.context.soap;

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
 *         &lt;element name="ContextExistsException" type="{http://exceptions.rmi.admin.openexchange.com/xsd}ContextExistsException" minOccurs="0"/>
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
    "contextExistsException"
})
@XmlRootElement(name = "ContextExistsException")
public class ContextExistsException {

    @XmlElement(name = "ContextExistsException", nillable = true)
    protected com.openexchange.admin.soap.context.exceptions.ContextExistsException contextExistsException;

    /**
     * Ruft den Wert der contextExistsException-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link com.openexchange.admin.soap.context.exceptions.ContextExistsException }
     *
     */
    public com.openexchange.admin.soap.context.exceptions.ContextExistsException getContextExistsException() {
        return contextExistsException;
    }

    /**
     * Legt den Wert der contextExistsException-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link com.openexchange.admin.soap.context.exceptions.ContextExistsException }
     *
     */
    public void setContextExistsException(com.openexchange.admin.soap.context.exceptions.ContextExistsException value) {
        this.contextExistsException = value;
    }

}

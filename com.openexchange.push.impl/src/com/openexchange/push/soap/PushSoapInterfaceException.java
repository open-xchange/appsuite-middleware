
package com.openexchange.push.soap;

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
 *         &lt;element name="PushSoapInterfaceException" type="{http://soap.push.openexchange.com}Exception" minOccurs="0"/>
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
    "pushSoapInterfaceException"
})
@XmlRootElement(name = "PushSoapInterfaceException")
public class PushSoapInterfaceException {

    @XmlElement(name = "PushSoapInterfaceException", nillable = true)
    protected Exception pushSoapInterfaceException;

    /**
     * Ruft den Wert der pushSoapInterfaceException-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Exception }
     *     
     */
    public Exception getPushSoapInterfaceException() {
        return pushSoapInterfaceException;
    }

    /**
     * Legt den Wert der pushSoapInterfaceException-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Exception }
     *     
     */
    public void setPushSoapInterfaceException(Exception value) {
        this.pushSoapInterfaceException = value;
    }

}

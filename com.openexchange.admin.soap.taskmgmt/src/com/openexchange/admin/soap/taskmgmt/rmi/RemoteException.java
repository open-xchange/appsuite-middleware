
package com.openexchange.admin.soap.taskmgmt.rmi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.taskmgmt.io.IOException;


/**
 * <p>Java-Klasse f\u00fcr RemoteException complex type.
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 *
 * <pre>
 * &lt;complexType name="RemoteException">
 *   &lt;complexContent>
 *     &lt;extension base="{http://io.java/xsd}IOException">
 *       &lt;sequence>
 *         &lt;element name="cause" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
 *         &lt;element name="message" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RemoteException", propOrder = {
    "cause",
    "message"
})
public class RemoteException
    extends IOException
{

    @XmlElement(nillable = true)
    protected Object cause;
    @XmlElement(nillable = true)
    protected String message;

    /**
     * Ruft den Wert der cause-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Object }
     *
     */
    public Object getCause() {
        return cause;
    }

    /**
     * Legt den Wert der cause-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Object }
     *
     */
    public void setCause(Object value) {
        this.cause = value;
    }

    /**
     * Ruft den Wert der message-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMessage() {
        return message;
    }

    /**
     * Legt den Wert der message-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMessage(String value) {
        this.message = value;
    }

}

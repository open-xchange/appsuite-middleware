
package com.openexchange.admin.soap.resource;

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
 *         &lt;element name="RemoteException" type="{http://rmi.java/xsd}RemoteException" minOccurs="0"/>
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
    "remoteException"
})
@XmlRootElement(name = "RemoteException")
public class RemoteException {

    @XmlElement(name = "RemoteException", nillable = true)
    protected java.rmi.xsd.RemoteException remoteException;

    /**
     * Ruft den Wert der remoteException-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link java.rmi.xsd.RemoteException }
     *     
     */
    public java.rmi.xsd.RemoteException getRemoteException() {
        return remoteException;
    }

    /**
     * Legt den Wert der remoteException-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link java.rmi.xsd.RemoteException }
     *     
     */
    public void setRemoteException(java.rmi.xsd.RemoteException value) {
        this.remoteException = value;
    }

}

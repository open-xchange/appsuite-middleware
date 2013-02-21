
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
    protected com.openexchange.admin.soap.reseller.service.rmi.RemoteException remoteException;

    /**
     * Ruft den Wert der remoteException-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link com.openexchange.admin.soap.reseller.service.rmi.RemoteException }
     *
     */
    public com.openexchange.admin.soap.reseller.service.rmi.RemoteException getRemoteException() {
        return remoteException;
    }

    /**
     * Legt den Wert der remoteException-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link com.openexchange.admin.soap.reseller.service.rmi.RemoteException }
     *
     */
    public void setRemoteException(com.openexchange.admin.soap.reseller.service.rmi.RemoteException value) {
        this.remoteException = value;
    }

}

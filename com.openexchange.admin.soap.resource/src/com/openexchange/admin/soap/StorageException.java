
package com.openexchange.admin.soap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
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
 *         &lt;element name="StorageException" type="{http://exceptions.rmi.admin.openexchange.com/xsd}StorageException" minOccurs="0"/>
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
    "storageException"
})
@XmlRootElement(name = "StorageException")
public class StorageException {

    @XmlElementRef(name = "StorageException", namespace = "http://soap.admin.openexchange.com", type = JAXBElement.class)
    protected JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.StorageException> storageException;

    /**
     * Ruft den Wert der storageException-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link com.openexchange.admin.rmi.exceptions.xsd.StorageException }{@code >}
     *     
     */
    public JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.StorageException> getStorageException() {
        return storageException;
    }

    /**
     * Legt den Wert der storageException-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link com.openexchange.admin.rmi.exceptions.xsd.StorageException }{@code >}
     *     
     */
    public void setStorageException(JAXBElement<com.openexchange.admin.rmi.exceptions.xsd.StorageException> value) {
        this.storageException = value;
    }

}

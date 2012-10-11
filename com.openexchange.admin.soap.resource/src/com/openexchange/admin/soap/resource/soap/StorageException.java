
package com.openexchange.admin.soap.resource.soap;

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

    @XmlElement(name = "StorageException", nillable = true)
    protected com.openexchange.admin.soap.resource.exceptions.StorageException storageException;

    /**
     * Ruft den Wert der storageException-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link com.openexchange.admin.soap.resource.exceptions.StorageException }
     *     
     */
    public com.openexchange.admin.soap.resource.exceptions.StorageException getStorageException() {
        return storageException;
    }

    /**
     * Legt den Wert der storageException-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link com.openexchange.admin.soap.resource.exceptions.StorageException }
     *     
     */
    public void setStorageException(com.openexchange.admin.soap.resource.exceptions.StorageException value) {
        this.storageException = value;
    }

}

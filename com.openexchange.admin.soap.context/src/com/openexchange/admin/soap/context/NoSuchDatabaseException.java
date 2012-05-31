
package com.openexchange.admin.soap.context;

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
 *         &lt;element name="NoSuchDatabaseException" type="{http://exceptions.rmi.admin.openexchange.com/xsd}NoSuchDatabaseException" minOccurs="0"/>
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
    "noSuchDatabaseException"
})
@XmlRootElement(name = "NoSuchDatabaseException")
public class NoSuchDatabaseException {

    @XmlElement(name = "NoSuchDatabaseException", nillable = true)
    protected com.openexchange.admin.rmi.exceptions.xsd.NoSuchDatabaseException noSuchDatabaseException;

    /**
     * Ruft den Wert der noSuchDatabaseException-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link com.openexchange.admin.rmi.exceptions.xsd.NoSuchDatabaseException }
     *     
     */
    public com.openexchange.admin.rmi.exceptions.xsd.NoSuchDatabaseException getNoSuchDatabaseException() {
        return noSuchDatabaseException;
    }

    /**
     * Legt den Wert der noSuchDatabaseException-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link com.openexchange.admin.rmi.exceptions.xsd.NoSuchDatabaseException }
     *     
     */
    public void setNoSuchDatabaseException(com.openexchange.admin.rmi.exceptions.xsd.NoSuchDatabaseException value) {
        this.noSuchDatabaseException = value;
    }

}

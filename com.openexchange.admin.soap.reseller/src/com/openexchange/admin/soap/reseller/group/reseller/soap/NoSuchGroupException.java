
package com.openexchange.admin.soap.reseller.group.reseller.soap;

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
 *         &lt;element name="NoSuchGroupException" type="{http://exceptions.rmi.admin.openexchange.com/xsd}NoSuchGroupException" minOccurs="0"/>
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
    "noSuchGroupException"
})
@XmlRootElement(name = "NoSuchGroupException")
public class NoSuchGroupException {

    @XmlElement(name = "NoSuchGroupException", nillable = true)
    protected com.openexchange.admin.soap.reseller.group.rmi.exceptions.NoSuchGroupException noSuchGroupException;

    /**
     * Ruft den Wert der noSuchGroupException-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link com.openexchange.admin.soap.reseller.group.rmi.exceptions.NoSuchGroupException }
     *
     */
    public com.openexchange.admin.soap.reseller.group.rmi.exceptions.NoSuchGroupException getNoSuchGroupException() {
        return noSuchGroupException;
    }

    /**
     * Legt den Wert der noSuchGroupException-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link com.openexchange.admin.soap.reseller.group.rmi.exceptions.NoSuchGroupException }
     *
     */
    public void setNoSuchGroupException(com.openexchange.admin.soap.reseller.group.rmi.exceptions.NoSuchGroupException value) {
        this.noSuchGroupException = value;
    }

}

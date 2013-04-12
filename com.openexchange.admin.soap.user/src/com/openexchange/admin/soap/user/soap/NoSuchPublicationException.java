
package com.openexchange.admin.soap.user.soap;

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
 *         &lt;element name="NoSuchPublicationException" type="{http://exceptions.rmi.admin.openexchange.com/xsd}NoSuchPublicationException" minOccurs="0"/>
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
    "noSuchPublicationException"
})
@XmlRootElement(name = "NoSuchPublicationException")
public class NoSuchPublicationException {

    @XmlElement(name = "NoSuchPublicationException", nillable = true)
    protected com.openexchange.admin.soap.user.exceptions.NoSuchPublicationException noSuchPublicationException;

    /**
     * Ruft den Wert der noSuchPublicationException-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link com.openexchange.admin.soap.user.exceptions.NoSuchPublicationException }
     *
     */
    public com.openexchange.admin.soap.user.exceptions.NoSuchPublicationException getNoSuchPublicationException() {
        return noSuchPublicationException;
    }

    /**
     * Legt den Wert der noSuchPublicationException-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link com.openexchange.admin.soap.user.exceptions.NoSuchPublicationException }
     *
     */
    public void setNoSuchPublicationException(com.openexchange.admin.soap.user.exceptions.NoSuchPublicationException value) {
        this.noSuchPublicationException = value;
    }

}

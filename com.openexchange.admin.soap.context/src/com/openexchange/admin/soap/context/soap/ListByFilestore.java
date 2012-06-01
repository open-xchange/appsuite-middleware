
package com.openexchange.admin.soap.context.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.context.dataobjects.Credentials;
import com.openexchange.admin.soap.context.dataobjects.Filestore;


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
 *         &lt;element name="fs" type="{http://dataobjects.soap.admin.openexchange.com/xsd}Filestore" minOccurs="0"/>
 *         &lt;element name="auth" type="{http://dataobjects.rmi.admin.openexchange.com/xsd}Credentials" minOccurs="0"/>
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
    "fs",
    "auth"
})
@XmlRootElement(name = "listByFilestore")
public class ListByFilestore {

    @XmlElement(nillable = true)
    protected Filestore fs;
    @XmlElement(nillable = true)
    protected Credentials auth;

    /**
     * Ruft den Wert der fs-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Filestore }
     *     
     */
    public Filestore getFs() {
        return fs;
    }

    /**
     * Legt den Wert der fs-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Filestore }
     *     
     */
    public void setFs(Filestore value) {
        this.fs = value;
    }

    /**
     * Ruft den Wert der auth-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Credentials }
     *     
     */
    public Credentials getAuth() {
        return auth;
    }

    /**
     * Legt den Wert der auth-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Credentials }
     *     
     */
    public void setAuth(Credentials value) {
        this.auth = value;
    }

}

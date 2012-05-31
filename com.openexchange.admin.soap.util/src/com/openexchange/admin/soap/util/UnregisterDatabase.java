
package com.openexchange.admin.soap.util;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.rmi.dataobjects.xsd.Credentials;
import com.openexchange.admin.soap.dataobjects.xsd.Database;


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
 *         &lt;element name="dbhandle" type="{http://dataobjects.soap.admin.openexchange.com/xsd}Database" minOccurs="0"/>
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
    "dbhandle",
    "auth"
})
@XmlRootElement(name = "unregisterDatabase")
public class UnregisterDatabase {

    @XmlElement(nillable = true)
    protected Database dbhandle;
    @XmlElement(nillable = true)
    protected Credentials auth;

    /**
     * Ruft den Wert der dbhandle-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Database }
     *     
     */
    public Database getDbhandle() {
        return dbhandle;
    }

    /**
     * Legt den Wert der dbhandle-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Database }
     *     
     */
    public void setDbhandle(Database value) {
        this.dbhandle = value;
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

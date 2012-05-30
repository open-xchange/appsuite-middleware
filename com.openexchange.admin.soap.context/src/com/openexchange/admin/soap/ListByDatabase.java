
package com.openexchange.admin.soap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
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
 *         &lt;element name="db" type="{http://dataobjects.soap.admin.openexchange.com/xsd}Database" minOccurs="0"/>
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
    "db",
    "auth"
})
@XmlRootElement(name = "listByDatabase")
public class ListByDatabase {

    @XmlElementRef(name = "db", namespace = "http://soap.admin.openexchange.com", type = JAXBElement.class)
    protected JAXBElement<Database> db;
    @XmlElementRef(name = "auth", namespace = "http://soap.admin.openexchange.com", type = JAXBElement.class)
    protected JAXBElement<Credentials> auth;

    /**
     * Ruft den Wert der db-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Database }{@code >}
     *     
     */
    public JAXBElement<Database> getDb() {
        return db;
    }

    /**
     * Legt den Wert der db-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Database }{@code >}
     *     
     */
    public void setDb(JAXBElement<Database> value) {
        this.db = value;
    }

    /**
     * Ruft den Wert der auth-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Credentials }{@code >}
     *     
     */
    public JAXBElement<Credentials> getAuth() {
        return auth;
    }

    /**
     * Legt den Wert der auth-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Credentials }{@code >}
     *     
     */
    public void setAuth(JAXBElement<Credentials> value) {
        this.auth = value;
    }

}

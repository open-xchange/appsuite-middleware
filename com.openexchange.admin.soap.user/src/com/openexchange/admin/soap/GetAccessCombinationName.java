
package com.openexchange.admin.soap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.rmi.dataobjects.xsd.Credentials;
import com.openexchange.admin.soap.dataobjects.xsd.Context;
import com.openexchange.admin.soap.dataobjects.xsd.User;


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
 *         &lt;element name="ctx" type="{http://dataobjects.soap.admin.openexchange.com/xsd}Context" minOccurs="0"/>
 *         &lt;element name="user" type="{http://dataobjects.soap.admin.openexchange.com/xsd}User" minOccurs="0"/>
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
    "ctx",
    "user",
    "auth"
})
@XmlRootElement(name = "getAccessCombinationName")
public class GetAccessCombinationName {

    @XmlElementRef(name = "ctx", namespace = "http://soap.admin.openexchange.com", type = JAXBElement.class)
    protected JAXBElement<Context> ctx;
    @XmlElementRef(name = "user", namespace = "http://soap.admin.openexchange.com", type = JAXBElement.class)
    protected JAXBElement<User> user;
    @XmlElementRef(name = "auth", namespace = "http://soap.admin.openexchange.com", type = JAXBElement.class)
    protected JAXBElement<Credentials> auth;

    /**
     * Ruft den Wert der ctx-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Context }{@code >}
     *     
     */
    public JAXBElement<Context> getCtx() {
        return ctx;
    }

    /**
     * Legt den Wert der ctx-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Context }{@code >}
     *     
     */
    public void setCtx(JAXBElement<Context> value) {
        this.ctx = value;
    }

    /**
     * Ruft den Wert der user-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link User }{@code >}
     *     
     */
    public JAXBElement<User> getUser() {
        return user;
    }

    /**
     * Legt den Wert der user-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link User }{@code >}
     *     
     */
    public void setUser(JAXBElement<User> value) {
        this.user = value;
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

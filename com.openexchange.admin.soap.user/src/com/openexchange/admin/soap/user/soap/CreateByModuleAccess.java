
package com.openexchange.admin.soap.user.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.user.dataobjects.Context;
import com.openexchange.admin.soap.user.dataobjects.Credentials;
import com.openexchange.admin.soap.user.dataobjects.User;
import com.openexchange.admin.soap.user.dataobjects.UserModuleAccess;


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
 *         &lt;element name="ctx" type="{http://dataobjects.soap.admin.openexchange.com/xsd}Context" minOccurs="0"/>
 *         &lt;element name="usrdata" type="{http://dataobjects.soap.admin.openexchange.com/xsd}User" minOccurs="0"/>
 *         &lt;element name="access" type="{http://dataobjects.soap.admin.openexchange.com/xsd}UserModuleAccess" minOccurs="0"/>
 *         &lt;element name="auth" type="{http://dataobjects.rmi.admin.openexchange.com/xsd}Credentials" minOccurs="0"/>
 *         &lt;element name="primaryAccountName" type="{http://dataobjects.rmi.admin.openexchange.com/xsd}String" minOccurs="0"/>
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
    "usrdata",
    "access",
    "auth",
    "primaryAccountName"
})
@XmlRootElement(name = "createByModuleAccess")
public class CreateByModuleAccess {

    @XmlElement(nillable = true)
    protected Context ctx;
    @XmlElement(nillable = true)
    protected User usrdata;
    @XmlElement(nillable = true)
    protected UserModuleAccess access;
    @XmlElement(nillable = true)
    protected Credentials auth;
    @XmlElement(nillable = true)
    protected String primaryAccountName;


    /**
     * Gets the primaryAccountName
     *
     * @return The primaryAccountName
     */
    public String getPrimaryAccountName() {
        return primaryAccountName;
    }


    /**
     * Sets the primaryAccountName
     *
     * @param primaryAccountName The primaryAccountName to set
     */
    public void setPrimaryAccountName(String primaryAccountName) {
        this.primaryAccountName = primaryAccountName;
    }

    /**
     * Ruft den Wert der ctx-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Context }
     *
     */
    public Context getCtx() {
        return ctx;
    }

    /**
     * Legt den Wert der ctx-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Context }
     *
     */
    public void setCtx(Context value) {
        this.ctx = value;
    }

    /**
     * Ruft den Wert der usrdata-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link User }
     *
     */
    public User getUsrdata() {
        return usrdata;
    }

    /**
     * Legt den Wert der usrdata-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link User }
     *
     */
    public void setUsrdata(User value) {
        this.usrdata = value;
    }

    /**
     * Ruft den Wert der access-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link UserModuleAccess }
     *
     */
    public UserModuleAccess getAccess() {
        return access;
    }

    /**
     * Legt den Wert der access-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link UserModuleAccess }
     *
     */
    public void setAccess(UserModuleAccess value) {
        this.access = value;
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

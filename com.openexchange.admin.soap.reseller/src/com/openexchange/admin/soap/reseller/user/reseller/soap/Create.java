
package com.openexchange.admin.soap.reseller.user.reseller.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.reseller.user.reseller.soap.dataobjects.ResellerContext;
import com.openexchange.admin.soap.reseller.user.rmi.dataobjects.Credentials;
import com.openexchange.admin.soap.reseller.user.soap.dataobjects.User;


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
 *         &lt;element name="ctx" type="{http://dataobjects.soap.reseller.admin.openexchange.com/xsd}ResellerContext" minOccurs="0"/>
 *         &lt;element name="usrdata" type="{http://dataobjects.soap.admin.openexchange.com/xsd}User" minOccurs="0"/>
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
    "auth",
    "primaryAccountName"
})
@XmlRootElement(name = "create")
public class Create {

    @XmlElement(nillable = true)
    protected ResellerContext ctx;
    @XmlElement(nillable = true)
    protected User usrdata;
    @XmlElement(nillable = true)
    protected Credentials auth;
    @XmlElement(nillable = true)
    private String primaryAccountName;

    /**
     * Ruft den Wert der ctx-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link ResellerContext }
     *
     */
    public ResellerContext getCtx() {
        return ctx;
    }

    /**
     * Legt den Wert der ctx-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link ResellerContext }
     *
     */
    public void setCtx(ResellerContext value) {
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

}

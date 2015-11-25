
package com.openexchange.admin.soap.user.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.user.dataobjects.Context;
import com.openexchange.admin.soap.user.dataobjects.Credentials;


/**
 * <p>Java-Klasse f\u00fcr anonymous complex type.
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 *
 * <pre>
 * &lt;complexType>
 * &lt;complexContent>
 * &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 * &lt;sequence>
 * &lt;element name="ctx" type="{http://dataobjects.soap.admin.openexchange.com/xsd}Context" minOccurs="0"/>
 * &lt;element name="alias_domain" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 * &lt;element name="auth" type="{http://dataobjects.rmi.admin.openexchange.com/xsd}Credentials" minOccurs="0"/>
 * &lt;/sequence>
 * &lt;/restriction>
 * &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "ctx",
    "aliasDomain",
    "auth"
})
@XmlRootElement(name = "listByAliasDomain")
public class ListByAliasDomain {

    @XmlElement(nillable = true)
    protected Context ctx;
    @XmlElement(name = "alias_domain", nillable = true)
    protected String aliasDomain;
    @XmlElement(nillable = true)
    protected Credentials auth;

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
     * Ruft den Wert der aliasDomain-Eigenschaft ab.
     *
     * @return
     *         possible object is {@link String }
     *
     */
    public String getAliasDomain() {
        return aliasDomain;
    }

    /**
     * Legt den Wert der aliasDomain-Eigenschaft fest.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setAliasDomain(String value) {
        this.aliasDomain = value;
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

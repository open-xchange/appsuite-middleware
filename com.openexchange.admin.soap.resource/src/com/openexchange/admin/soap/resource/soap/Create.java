
package com.openexchange.admin.soap.resource.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.resource.dataobjects.Context;
import com.openexchange.admin.soap.resource.dataobjects.Credentials;
import com.openexchange.admin.soap.resource.dataobjects.Resource;


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
 *         &lt;element name="res" type="{http://dataobjects.soap.admin.openexchange.com/xsd}Resource" minOccurs="0"/>
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
    "res",
    "auth"
})
@XmlRootElement(name = "create")
public class Create {

    @XmlElement(nillable = true)
    protected Context ctx;
    @XmlElement(nillable = true)
    protected Resource res;
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
     * Ruft den Wert der res-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Resource }
     *     
     */
    public Resource getRes() {
        return res;
    }

    /**
     * Legt den Wert der res-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Resource }
     *     
     */
    public void setRes(Resource value) {
        this.res = value;
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


package com.openexchange.admin.soap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.rmi.dataobjects.xsd.Credentials;
import com.openexchange.admin.soap.dataobjects.xsd.Context;
import com.openexchange.admin.soap.dataobjects.xsd.Filestore;


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
 *         &lt;element name="dst_filestore_id" type="{http://dataobjects.soap.admin.openexchange.com/xsd}Filestore" minOccurs="0"/>
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
    "dstFilestoreId",
    "auth"
})
@XmlRootElement(name = "moveContextFilestore")
public class MoveContextFilestore {

    @XmlElementRef(name = "ctx", namespace = "http://soap.admin.openexchange.com", type = JAXBElement.class)
    protected JAXBElement<Context> ctx;
    @XmlElementRef(name = "dst_filestore_id", namespace = "http://soap.admin.openexchange.com", type = JAXBElement.class)
    protected JAXBElement<Filestore> dstFilestoreId;
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
     * Ruft den Wert der dstFilestoreId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Filestore }{@code >}
     *     
     */
    public JAXBElement<Filestore> getDstFilestoreId() {
        return dstFilestoreId;
    }

    /**
     * Legt den Wert der dstFilestoreId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Filestore }{@code >}
     *     
     */
    public void setDstFilestoreId(JAXBElement<Filestore> value) {
        this.dstFilestoreId = value;
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

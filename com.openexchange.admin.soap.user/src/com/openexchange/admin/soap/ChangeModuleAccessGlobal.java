
package com.openexchange.admin.soap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.rmi.dataobjects.xsd.Credentials;
import com.openexchange.admin.soap.dataobjects.xsd.UserModuleAccess;


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
 *         &lt;element name="filter" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="addAccess" type="{http://dataobjects.soap.admin.openexchange.com/xsd}UserModuleAccess" minOccurs="0"/>
 *         &lt;element name="removeAccess" type="{http://dataobjects.soap.admin.openexchange.com/xsd}UserModuleAccess" minOccurs="0"/>
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
    "filter",
    "addAccess",
    "removeAccess",
    "auth"
})
@XmlRootElement(name = "changeModuleAccessGlobal")
public class ChangeModuleAccessGlobal {

    @XmlElementRef(name = "filter", namespace = "http://soap.admin.openexchange.com", type = JAXBElement.class)
    protected JAXBElement<String> filter;
    @XmlElementRef(name = "addAccess", namespace = "http://soap.admin.openexchange.com", type = JAXBElement.class)
    protected JAXBElement<UserModuleAccess> addAccess;
    @XmlElementRef(name = "removeAccess", namespace = "http://soap.admin.openexchange.com", type = JAXBElement.class)
    protected JAXBElement<UserModuleAccess> removeAccess;
    @XmlElementRef(name = "auth", namespace = "http://soap.admin.openexchange.com", type = JAXBElement.class)
    protected JAXBElement<Credentials> auth;

    /**
     * Ruft den Wert der filter-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getFilter() {
        return filter;
    }

    /**
     * Legt den Wert der filter-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setFilter(JAXBElement<String> value) {
        this.filter = value;
    }

    /**
     * Ruft den Wert der addAccess-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link UserModuleAccess }{@code >}
     *     
     */
    public JAXBElement<UserModuleAccess> getAddAccess() {
        return addAccess;
    }

    /**
     * Legt den Wert der addAccess-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link UserModuleAccess }{@code >}
     *     
     */
    public void setAddAccess(JAXBElement<UserModuleAccess> value) {
        this.addAccess = value;
    }

    /**
     * Ruft den Wert der removeAccess-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link UserModuleAccess }{@code >}
     *     
     */
    public JAXBElement<UserModuleAccess> getRemoveAccess() {
        return removeAccess;
    }

    /**
     * Legt den Wert der removeAccess-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link UserModuleAccess }{@code >}
     *     
     */
    public void setRemoveAccess(JAXBElement<UserModuleAccess> value) {
        this.removeAccess = value;
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

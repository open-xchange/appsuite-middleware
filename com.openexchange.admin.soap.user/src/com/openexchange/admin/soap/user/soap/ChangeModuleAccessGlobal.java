
package com.openexchange.admin.soap.user.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.user.dataobjects.Credentials;
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

    @XmlElement(nillable = true)
    protected String filter;
    @XmlElement(nillable = true)
    protected UserModuleAccess addAccess;
    @XmlElement(nillable = true)
    protected UserModuleAccess removeAccess;
    @XmlElement(nillable = true)
    protected Credentials auth;

    /**
     * Ruft den Wert der filter-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Legt den Wert der filter-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFilter(String value) {
        this.filter = value;
    }

    /**
     * Ruft den Wert der addAccess-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link UserModuleAccess }
     *
     */
    public UserModuleAccess getAddAccess() {
        return addAccess;
    }

    /**
     * Legt den Wert der addAccess-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link UserModuleAccess }
     *
     */
    public void setAddAccess(UserModuleAccess value) {
        this.addAccess = value;
    }

    /**
     * Ruft den Wert der removeAccess-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link UserModuleAccess }
     *
     */
    public UserModuleAccess getRemoveAccess() {
        return removeAccess;
    }

    /**
     * Legt den Wert der removeAccess-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link UserModuleAccess }
     *
     */
    public void setRemoveAccess(UserModuleAccess value) {
        this.removeAccess = value;
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

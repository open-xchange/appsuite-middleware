
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
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ctx" type="{http://dataobjects.soap.admin.openexchange.com/xsd}Context" minOccurs="0"/>
 *         &lt;element name="search_pattern" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="auth" type="{http://dataobjects.rmi.admin.openexchange.com/xsd}Credentials" minOccurs="0"/>
 *         &lt;element name="include_guests" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="exclude_users" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
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
    "searchPattern",
    "auth",
    "includeGuests",
    "excludeUsers"
})
@XmlRootElement(name = "list")
public class List {

    @XmlElement(nillable = true)
    protected Context ctx;
    @XmlElement(name = "search_pattern", nillable = true)
    protected String searchPattern;
    @XmlElement(nillable = true)
    protected Credentials auth;
    @XmlElement(name = "include_guests", nillable = true)
    protected Boolean includeGuests;
    @XmlElement(name = "exclude_users", nillable = true)
    protected Boolean excludeUsers;
    

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
     * Ruft den Wert der searchPattern-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSearchPattern() {
        return searchPattern;
    }

    /**
     * Legt den Wert der searchPattern-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSearchPattern(String value) {
        this.searchPattern = value;
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

    
    public Boolean getIncludeGuests() {
        return includeGuests;
    }

    
    public void setIncludeGuests(Boolean includeGuests) {
        this.includeGuests = includeGuests;
    }

    
    public Boolean getExcludeUsers() {
        return excludeUsers;
    }

    
    public void setExcludeUsers(Boolean excludeUsers) {
        this.excludeUsers = excludeUsers;
    }

}

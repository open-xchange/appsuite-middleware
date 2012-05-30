
package com.openexchange.admin.soap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.rmi.dataobjects.xsd.Credentials;


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
 *         &lt;element name="search_pattern" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "searchPattern",
    "auth"
})
@XmlRootElement(name = "list")
public class List {

    @XmlElementRef(name = "search_pattern", namespace = "http://soap.admin.openexchange.com", type = JAXBElement.class)
    protected JAXBElement<String> searchPattern;
    @XmlElementRef(name = "auth", namespace = "http://soap.admin.openexchange.com", type = JAXBElement.class)
    protected JAXBElement<Credentials> auth;

    /**
     * Ruft den Wert der searchPattern-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getSearchPattern() {
        return searchPattern;
    }

    /**
     * Legt den Wert der searchPattern-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setSearchPattern(JAXBElement<String> value) {
        this.searchPattern = value;
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


package com.openexchange.oauth.provider.soap;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse f&uuml;r ClientData complex type.
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 *
 * <pre>
 * &lt;complexType name="ClientData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="contactAddress" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="defaultScope" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="icon" type="{http://soap.provider.oauth.openexchange.com}Icon" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="redirectURIs" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="website" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="groupId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClientData", propOrder = {
    "contactAddress",
    "defaultScope",
    "description",
    "icon",
    "name",
    "redirectURIs",
    "website",
    "groupId"
})
public class ClientData {

    @XmlElement(nillable = true)
    protected String contactAddress;
    @XmlElement(nillable = true)
    protected String defaultScope;
    @XmlElement(nillable = true)
    protected String description;
    @XmlElement(nillable = true)
    protected Icon icon;
    @XmlElement(nillable = true)
    protected String name;
    @XmlElement(nillable = true)
    protected List<String> redirectURIs;
    @XmlElement(nillable = true)
    protected String website;
    @XmlElement(nillable = true)
    protected String groupId;

    /**
     * Ruft den Wert der contactAddress-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getContactAddress() {
        return contactAddress;
    }

    /**
     * Legt den Wert der contactAddress-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setContactAddress(String value) {
        this.contactAddress = value;
    }

    /**
     * Ruft den Wert der defaultScope-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDefaultScope() {
        return defaultScope;
    }

    /**
     * Legt den Wert der defaultScope-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDefaultScope(String value) {
        this.defaultScope = value;
    }

    /**
     * Ruft den Wert der description-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDescription() {
        return description;
    }

    /**
     * Legt den Wert der description-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Ruft den Wert der icon-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Icon }
     *
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * Legt den Wert der icon-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Icon }
     *
     */
    public void setIcon(Icon value) {
        this.icon = value;
    }

    /**
     * Ruft den Wert der name-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Legt den Wert der name-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the redirectURIs property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the redirectURIs property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRedirectURIs().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     *
     *
     */
    public List<String> getRedirectURIs() {
        if (redirectURIs == null) {
            redirectURIs = new ArrayList<String>();
        }
        return this.redirectURIs;
    }

    /**
     * Ruft den Wert der website-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getWebsite() {
        return website;
    }

    /**
     * Legt den Wert der website-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setWebsite(String value) {
        this.website = value;
    }

    /**
     * Ruft den Wert der groupId-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Legt den Wert der groupId-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setGroupId(String value) {
        this.groupId = value;
    }
}

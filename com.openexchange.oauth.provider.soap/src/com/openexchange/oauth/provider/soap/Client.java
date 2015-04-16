
package com.openexchange.oauth.provider.soap;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Client complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="Client">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="contactAddress" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="defaultScope" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="enabled" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="icon" type="{http://soap.provider.oauth.openexchange.com}Icon"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="redirectURI" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *         &lt;element name="registrationDate" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *         &lt;element name="secret" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="website" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Client", propOrder = {
    "id",
    "name",
    "description",
    "contactAddress",
    "website",
    "defaultScope",
    "redirectURIs",
    "secret",
    "registrationDate",
    "enabled",
    "icon"
})
public class Client {

    @XmlElement(required = true, nillable = true)
    protected String contactAddress;
    @XmlElement(required = true, nillable = true)
    protected String defaultScope;
    @XmlElement(required = true, nillable = true)
    protected String description;
    protected boolean enabled;
    @XmlElement(required = true, nillable = true)
    protected Icon icon;
    @XmlElement(required = true, nillable = true)
    protected String id;
    @XmlElement(required = true, nillable = true)
    protected String name;
    @XmlElement(name = "redirectURI", required = true, nillable = true)
    protected List<String> redirectURIs;
    @XmlElement(required = true, nillable = true)
    protected long registrationDate;
    @XmlElement(required = true, nillable = true)
    protected String secret;
    @XmlElement(required = true, nillable = true)
    protected String website;

    /**
     * Gets the value of the contactAddress property.
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
     * Sets the value of the contactAddress property.
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
     * Gets the value of the defaultScope property.
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
     * Sets the value of the defaultScope property.
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
     * Gets the value of the description property.
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
     * Sets the value of the description property.
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
     * Gets the value of the enabled property.
     *
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the value of the enabled property.
     *
     */
    public void setEnabled(boolean value) {
        this.enabled = value;
    }

    /**
     * Gets the value of the icon property.
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
     * Sets the value of the icon property.
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
     * Gets the value of the id property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the name property.
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
     * Sets the value of the name property.
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
     * Gets the value of the redirectURI property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the redirectURI property.
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
     * Gets the value of the registrationDate property.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public long getRegistrationDate() {
        return registrationDate;
    }

    /**
     * Sets the value of the registrationDate property.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setRegistrationDate(long value) {
        this.registrationDate = value;
    }

    /**
     * Gets the value of the secret property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSecret() {
        return secret;
    }

    /**
     * Sets the value of the secret property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSecret(String value) {
        this.secret = value;
    }

    /**
     * Gets the value of the website property.
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
     * Sets the value of the website property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setWebsite(String value) {
        this.website = value;
    }

}

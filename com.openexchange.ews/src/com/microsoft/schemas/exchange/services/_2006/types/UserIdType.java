
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for UserIdType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UserIdType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PrimarySmtpAddress" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DisplayName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DistinguishedUser" type="{http://schemas.microsoft.com/exchange/services/2006/types}DistinguishedUserType" minOccurs="0"/>
 *         &lt;element name="ExternalUserIdentity" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserIdType", propOrder = {
    "sid",
    "primarySmtpAddress",
    "displayName",
    "distinguishedUser",
    "externalUserIdentity"
})
public class UserIdType {

    @XmlElement(name = "SID")
    protected String sid;
    @XmlElement(name = "PrimarySmtpAddress")
    protected String primarySmtpAddress;
    @XmlElement(name = "DisplayName")
    protected String displayName;
    @XmlElement(name = "DistinguishedUser")
    protected DistinguishedUserType distinguishedUser;
    @XmlElement(name = "ExternalUserIdentity")
    protected String externalUserIdentity;

    /**
     * Gets the value of the sid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSID() {
        return sid;
    }

    /**
     * Sets the value of the sid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSID(String value) {
        this.sid = value;
    }

    /**
     * Gets the value of the primarySmtpAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrimarySmtpAddress() {
        return primarySmtpAddress;
    }

    /**
     * Sets the value of the primarySmtpAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrimarySmtpAddress(String value) {
        this.primarySmtpAddress = value;
    }

    /**
     * Gets the value of the displayName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the value of the displayName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayName(String value) {
        this.displayName = value;
    }

    /**
     * Gets the value of the distinguishedUser property.
     * 
     * @return
     *     possible object is
     *     {@link DistinguishedUserType }
     *     
     */
    public DistinguishedUserType getDistinguishedUser() {
        return distinguishedUser;
    }

    /**
     * Sets the value of the distinguishedUser property.
     * 
     * @param value
     *     allowed object is
     *     {@link DistinguishedUserType }
     *     
     */
    public void setDistinguishedUser(DistinguishedUserType value) {
        this.distinguishedUser = value;
    }

    /**
     * Gets the value of the externalUserIdentity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExternalUserIdentity() {
        return externalUserIdentity;
    }

    /**
     * Sets the value of the externalUserIdentity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExternalUserIdentity(String value) {
        this.externalUserIdentity = value;
    }

}

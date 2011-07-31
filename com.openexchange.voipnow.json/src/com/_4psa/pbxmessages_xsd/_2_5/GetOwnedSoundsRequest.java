
package com._4psa.pbxmessages_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice minOccurs="0">
 *           &lt;element name="userID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *           &lt;element name="userIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;/choice>
 *         &lt;element name="folderID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;element name="languageID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;element name="musicOnHold" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="system" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="status" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
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
    "userID",
    "userIdentifier",
    "folderID",
    "languageID",
    "musicOnHold",
    "system",
    "status"
})
@XmlRootElement(name = "GetOwnedSoundsRequest")
public class GetOwnedSoundsRequest {

    protected BigInteger userID;
    protected String userIdentifier;
    protected BigInteger folderID;
    protected BigInteger languageID;
    @XmlElement(defaultValue = "-1")
    protected BigInteger musicOnHold;
    @XmlElement(defaultValue = "-1")
    protected BigInteger system;
    @XmlElement(defaultValue = "-1")
    protected BigInteger status;

    /**
     * Gets the value of the userID property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getUserID() {
        return userID;
    }

    /**
     * Sets the value of the userID property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setUserID(BigInteger value) {
        this.userID = value;
    }

    /**
     * Gets the value of the userIdentifier property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserIdentifier() {
        return userIdentifier;
    }

    /**
     * Sets the value of the userIdentifier property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserIdentifier(String value) {
        this.userIdentifier = value;
    }

    /**
     * Gets the value of the folderID property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getFolderID() {
        return folderID;
    }

    /**
     * Sets the value of the folderID property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setFolderID(BigInteger value) {
        this.folderID = value;
    }

    /**
     * Gets the value of the languageID property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getLanguageID() {
        return languageID;
    }

    /**
     * Sets the value of the languageID property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setLanguageID(BigInteger value) {
        this.languageID = value;
    }

    /**
     * Gets the value of the musicOnHold property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getMusicOnHold() {
        return musicOnHold;
    }

    /**
     * Sets the value of the musicOnHold property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setMusicOnHold(BigInteger value) {
        this.musicOnHold = value;
    }

    /**
     * Gets the value of the system property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getSystem() {
        return system;
    }

    /**
     * Sets the value of the system property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setSystem(BigInteger value) {
        this.system = value;
    }

    /**
     * Gets the value of the status property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setStatus(BigInteger value) {
        this.status = value;
    }

}

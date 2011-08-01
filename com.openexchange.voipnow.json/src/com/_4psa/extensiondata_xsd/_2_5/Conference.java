
package com._4psa.extensiondata_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.extensionmessages_xsd._2_5.SetConferenceRequest;
import com._4psa.extensionmessagesinfo_xsd._2_5.GetConferenceSettingsResponseType;


/**
 * Phone terminal conference function data
 *
 * <p>Java class for Conference complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="Conference">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="status" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="size" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="timeout" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="mohActive" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="PIN" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="close" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="announceUserCount" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="announceUser" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *           &lt;element name="identifier" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Conference", propOrder = {
    "status",
    "size",
    "timeout",
    "mohActive",
    "pin",
    "close",
    "announceUserCount",
    "announceUser",
    "id",
    "identifier"
})
@XmlSeeAlso({
    GetConferenceSettingsResponseType.class,
    SetConferenceRequest.class
})
public class Conference {

    protected Boolean status;
    protected BigInteger size;
    protected BigInteger timeout;
    protected Boolean mohActive;
    @XmlElement(name = "PIN")
    protected Boolean pin;
    protected Boolean close;
    protected Boolean announceUserCount;
    protected Boolean announceUser;
    @XmlElement(name = "ID")
    protected BigInteger id;
    protected String identifier;

    /**
     * Gets the value of the status property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setStatus(Boolean value) {
        this.status = value;
    }

    /**
     * Gets the value of the size property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getSize() {
        return size;
    }

    /**
     * Sets the value of the size property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setSize(BigInteger value) {
        this.size = value;
    }

    /**
     * Gets the value of the timeout property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getTimeout() {
        return timeout;
    }

    /**
     * Sets the value of the timeout property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setTimeout(BigInteger value) {
        this.timeout = value;
    }

    /**
     * Gets the value of the mohActive property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isMohActive() {
        return mohActive;
    }

    /**
     * Sets the value of the mohActive property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setMohActive(Boolean value) {
        this.mohActive = value;
    }

    /**
     * Gets the value of the pin property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isPIN() {
        return pin;
    }

    /**
     * Sets the value of the pin property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setPIN(Boolean value) {
        this.pin = value;
    }

    /**
     * Gets the value of the close property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isClose() {
        return close;
    }

    /**
     * Sets the value of the close property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setClose(Boolean value) {
        this.close = value;
    }

    /**
     * Gets the value of the announceUserCount property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isAnnounceUserCount() {
        return announceUserCount;
    }

    /**
     * Sets the value of the announceUserCount property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setAnnounceUserCount(Boolean value) {
        this.announceUserCount = value;
    }

    /**
     * Gets the value of the announceUser property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isAnnounceUser() {
        return announceUser;
    }

    /**
     * Sets the value of the announceUser property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setAnnounceUser(Boolean value) {
        this.announceUser = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getID() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setID(BigInteger value) {
        this.id = value;
    }

    /**
     * Gets the value of the identifier property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the value of the identifier property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setIdentifier(String value) {
        this.identifier = value;
    }

}

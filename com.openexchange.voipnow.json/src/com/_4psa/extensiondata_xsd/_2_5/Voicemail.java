
package com._4psa.extensiondata_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.common_xsd._2_5.UnlimitedUInt;
import com._4psa.extensionmessages_xsd._2_5.SetVoicemailRequest;
import com._4psa.extensionmessagesinfo_xsd._2_5.GetVoicemailSettingsResponseType;


/**
 * Phone terminal voicemail function data
 * 
 * <p>Java class for Voicemail complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Voicemail">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="status" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="storage" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedUInt" minOccurs="0"/>
 *         &lt;element name="autoDelDays" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="notify" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="alert"/>
 *               &lt;enumeration value="message"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="password" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="autoAnswer" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="autoAnswerTime" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="directory" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *           &lt;element name="identifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
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
@XmlType(name = "Voicemail", propOrder = {
    "status",
    "storage",
    "autoDelDays",
    "notify",
    "password",
    "autoAnswer",
    "autoAnswerTime",
    "directory",
    "id",
    "identifier"
})
@XmlSeeAlso({
    GetVoicemailSettingsResponseType.class,
    SetVoicemailRequest.class
})
public class Voicemail {

    @XmlElement(defaultValue = "1")
    protected Boolean status;
    protected UnlimitedUInt storage;
    protected BigInteger autoDelDays;
    @XmlElement(defaultValue = "message")
    protected String notify;
    protected String password;
    protected Boolean autoAnswer;
    protected BigInteger autoAnswerTime;
    @XmlElement(defaultValue = "0")
    protected Boolean directory;
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
     * Gets the value of the storage property.
     * 
     * @return
     *     possible object is
     *     {@link UnlimitedUInt }
     *     
     */
    public UnlimitedUInt getStorage() {
        return storage;
    }

    /**
     * Sets the value of the storage property.
     * 
     * @param value
     *     allowed object is
     *     {@link UnlimitedUInt }
     *     
     */
    public void setStorage(UnlimitedUInt value) {
        this.storage = value;
    }

    /**
     * Gets the value of the autoDelDays property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getAutoDelDays() {
        return autoDelDays;
    }

    /**
     * Sets the value of the autoDelDays property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setAutoDelDays(BigInteger value) {
        this.autoDelDays = value;
    }

    /**
     * Gets the value of the notify property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNotify() {
        return notify;
    }

    /**
     * Sets the value of the notify property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNotify(String value) {
        this.notify = value;
    }

    /**
     * Gets the value of the password property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the value of the password property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassword(String value) {
        this.password = value;
    }

    /**
     * Gets the value of the autoAnswer property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAutoAnswer() {
        return autoAnswer;
    }

    /**
     * Sets the value of the autoAnswer property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAutoAnswer(Boolean value) {
        this.autoAnswer = value;
    }

    /**
     * Gets the value of the autoAnswerTime property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getAutoAnswerTime() {
        return autoAnswerTime;
    }

    /**
     * Sets the value of the autoAnswerTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setAutoAnswerTime(BigInteger value) {
        this.autoAnswerTime = value;
    }

    /**
     * Gets the value of the directory property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDirectory() {
        return directory;
    }

    /**
     * Sets the value of the directory property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDirectory(Boolean value) {
        this.directory = value;
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


package com._4psa.extensiondata_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.common_xsd._2_5.UnlimitedUInt;
import com._4psa.extensionmessagesinfo_xsd._2_5.GetFaxCenterSettingsResponseType;


/**
 * Phone terminal fax center function data
 *
 * <p>Java class for FaxCenter complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="FaxCenter">
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
 *               &lt;enumeration value="fax"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="autoAnswer" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="autoAnswerSeconds" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="fax2voicemail" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="faxFromEmail" type="{http://4psa.com/Common.xsd/2.5.1}email" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="faxSndActive" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="faxSnd" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="identifier" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *           &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
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
@XmlType(name = "FaxCenter", propOrder = {
    "status",
    "storage",
    "autoDelDays",
    "notify",
    "autoAnswer",
    "autoAnswerSeconds",
    "fax2Voicemail",
    "faxFromEmail",
    "faxSndActive",
    "faxSnd",
    "identifier",
    "id"
})
@XmlSeeAlso({
    GetFaxCenterSettingsResponseType.class
})
public class FaxCenter {

    protected Boolean status;
    protected UnlimitedUInt storage;
    protected BigInteger autoDelDays;
    @XmlElement(defaultValue = "alert")
    protected String notify;
    protected Boolean autoAnswer;
    protected BigInteger autoAnswerSeconds;
    @XmlElement(name = "fax2voicemail")
    protected Boolean fax2Voicemail;
    protected List<String> faxFromEmail;
    protected Boolean faxSndActive;
    protected String faxSnd;
    protected String identifier;
    @XmlElement(name = "ID")
    protected BigInteger id;

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
     * Gets the value of the autoAnswerSeconds property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getAutoAnswerSeconds() {
        return autoAnswerSeconds;
    }

    /**
     * Sets the value of the autoAnswerSeconds property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setAutoAnswerSeconds(BigInteger value) {
        this.autoAnswerSeconds = value;
    }

    /**
     * Gets the value of the fax2Voicemail property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isFax2Voicemail() {
        return fax2Voicemail;
    }

    /**
     * Sets the value of the fax2Voicemail property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setFax2Voicemail(Boolean value) {
        this.fax2Voicemail = value;
    }

    /**
     * Gets the value of the faxFromEmail property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the faxFromEmail property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFaxFromEmail().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     *
     *
     */
    public List<String> getFaxFromEmail() {
        if (faxFromEmail == null) {
            faxFromEmail = new ArrayList<String>();
        }
        return this.faxFromEmail;
    }

    /**
     * Gets the value of the faxSndActive property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isFaxSndActive() {
        return faxSndActive;
    }

    /**
     * Sets the value of the faxSndActive property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setFaxSndActive(Boolean value) {
        this.faxSndActive = value;
    }

    /**
     * Gets the value of the faxSnd property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFaxSnd() {
        return faxSnd;
    }

    /**
     * Sets the value of the faxSnd property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFaxSnd(String value) {
        this.faxSnd = value;
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

}

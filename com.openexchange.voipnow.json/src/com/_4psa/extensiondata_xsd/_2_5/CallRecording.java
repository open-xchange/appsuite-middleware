
package com._4psa.extensiondata_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.common_xsd._2_5.UnlimitedUInt;
import com._4psa.extensionmessages_xsd._2_5.SetCallRecordingRequest;
import com._4psa.extensionmessagesinfo_xsd._2_5.GetCallRecordingSettingsResponseType;


/**
 * Phone terminal call recording function data
 * 
 * <p>Java class for CallRecording complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CallRecording">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="status" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="storage" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedUInt" minOccurs="0"/>
 *         &lt;element name="trigger" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
 *               &lt;enumeration value="0"/>
 *               &lt;enumeration value="1"/>
 *               &lt;enumeration value="2"/>
 *               &lt;enumeration value="3"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="recordSndActive" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="recordSnd" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
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
@XmlType(name = "CallRecording", propOrder = {
    "status",
    "storage",
    "trigger",
    "recordSndActive",
    "recordSnd",
    "id",
    "identifier"
})
@XmlSeeAlso({
    GetCallRecordingSettingsResponseType.class,
    SetCallRecordingRequest.class
})
public class CallRecording {

    protected Boolean status;
    protected UnlimitedUInt storage;
    protected Integer trigger;
    protected Boolean recordSndActive;
    protected String recordSnd;
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
     * Gets the value of the trigger property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTrigger() {
        return trigger;
    }

    /**
     * Sets the value of the trigger property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTrigger(Integer value) {
        this.trigger = value;
    }

    /**
     * Gets the value of the recordSndActive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isRecordSndActive() {
        return recordSndActive;
    }

    /**
     * Sets the value of the recordSndActive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRecordSndActive(Boolean value) {
        this.recordSndActive = value;
    }

    /**
     * Gets the value of the recordSnd property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRecordSnd() {
        return recordSnd;
    }

    /**
     * Sets the value of the recordSnd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRecordSnd(String value) {
        this.recordSnd = value;
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


package com._4psa.extensiondata_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.common_xsd._2_5.UnlimitedDate;
import com._4psa.common_xsd._2_5.UnlimitedUInt;
import com._4psa.extensionmessages_xsd._2_5.SetExtensionPLRequest;


/**
 * Extension permissions and limits data
 *
 * <p>Java class for ExtensionPLInfo complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ExtensionPLInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *           &lt;element name="identifier" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *         &lt;/choice>
 *         &lt;element name="soundManag" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="sipManag" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="multiUser" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="callAPIManag" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="callerIDManag" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="ProvisionManag" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *               &lt;enumeration value="0"/>
 *               &lt;enumeration value="2"/>
 *               &lt;enumeration value="4"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="IMManag" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="soundStorage" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedUInt" minOccurs="0"/>
 *         &lt;element name="mohStorage" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedUInt" minOccurs="0"/>
 *         &lt;element name="concurentCalls" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedUInt" minOccurs="0"/>
 *         &lt;element name="concurentInternalCalls" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedUInt" minOccurs="0"/>
 *         &lt;element name="concurentText2Speech" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedUInt" minOccurs="0"/>
 *         &lt;element name="queueMembersMax" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedUInt" minOccurs="0"/>
 *         &lt;element name="callCardCodesMax" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedUInt" minOccurs="0"/>
 *         &lt;element name="callbackCallerIDMax" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedUInt" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="accountExpire" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedDate" minOccurs="0"/>
 *           &lt;element name="accountExpireDays" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedUInt" minOccurs="0"/>
 *         &lt;/choice>
 *         &lt;sequence minOccurs="0">
 *           &lt;element name="level" minOccurs="0">
 *             &lt;simpleType>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                 &lt;enumeration value="none"/>
 *                 &lt;enumeration value="standard"/>
 *                 &lt;enumeration value="premium"/>
 *               &lt;/restriction>
 *             &lt;/simpleType>
 *           &lt;/element>
 *           &lt;element name="advertising" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *           &lt;element name="browserChat" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *           &lt;element name="activeCalls" minOccurs="0">
 *             &lt;simpleType>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                 &lt;enumeration value="own"/>
 *                 &lt;enumeration value="group"/>
 *                 &lt;enumeration value="all"/>
 *               &lt;/restriction>
 *             &lt;/simpleType>
 *           &lt;/element>
 *           &lt;element name="callOperations" minOccurs="0">
 *             &lt;simpleType>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                 &lt;enumeration value="own"/>
 *                 &lt;enumeration value="group"/>
 *                 &lt;enumeration value="all"/>
 *               &lt;/restriction>
 *             &lt;/simpleType>
 *           &lt;/element>
 *           &lt;element name="recordCalls" minOccurs="0">
 *             &lt;simpleType>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                 &lt;enumeration value="own"/>
 *                 &lt;enumeration value="group"/>
 *                 &lt;enumeration value="all"/>
 *               &lt;/restriction>
 *             &lt;/simpleType>
 *           &lt;/element>
 *           &lt;element name="whisperOnCall" minOccurs="0">
 *             &lt;simpleType>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                 &lt;enumeration value="own"/>
 *                 &lt;enumeration value="group"/>
 *                 &lt;enumeration value="all"/>
 *               &lt;/restriction>
 *             &lt;/simpleType>
 *           &lt;/element>
 *           &lt;element name="unparkCalls" minOccurs="0">
 *             &lt;simpleType>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                 &lt;enumeration value="parked_by_me"/>
 *                 &lt;enumeration value="parked_by_anyone"/>
 *               &lt;/restriction>
 *             &lt;/simpleType>
 *           &lt;/element>
 *           &lt;element name="pickupCalls" minOccurs="0">
 *             &lt;simpleType>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                 &lt;enumeration value="own"/>
 *                 &lt;enumeration value="group"/>
 *                 &lt;enumeration value="all"/>
 *               &lt;/restriction>
 *             &lt;/simpleType>
 *           &lt;/element>
 *         &lt;/sequence>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExtensionPLInfo", propOrder = {
    "id",
    "identifier",
    "soundManag",
    "sipManag",
    "multiUser",
    "callAPIManag",
    "callerIDManag",
    "provisionManag",
    "imManag",
    "soundStorage",
    "mohStorage",
    "concurentCalls",
    "concurentInternalCalls",
    "concurentText2Speech",
    "queueMembersMax",
    "callCardCodesMax",
    "callbackCallerIDMax",
    "accountExpire",
    "accountExpireDays",
    "level",
    "advertising",
    "browserChat",
    "activeCalls",
    "callOperations",
    "recordCalls",
    "whisperOnCall",
    "unparkCalls",
    "pickupCalls"
})
@XmlSeeAlso({
    com._4psa.extensionmessagesinfo_xsd._2_5.GetExtensionPLResponseType.ExtensionPL.class,
    SetExtensionPLRequest.class
})
public class ExtensionPLInfo {

    @XmlElement(name = "ID")
    protected BigInteger id;
    protected String identifier;
    protected Boolean soundManag;
    protected Boolean sipManag;
    protected Boolean multiUser;
    protected Boolean callAPIManag;
    protected Boolean callerIDManag;
    @XmlElement(name = "ProvisionManag", defaultValue = "0")
    protected BigInteger provisionManag;
    @XmlElement(name = "IMManag")
    protected Boolean imManag;
    protected UnlimitedUInt soundStorage;
    protected UnlimitedUInt mohStorage;
    protected UnlimitedUInt concurentCalls;
    protected UnlimitedUInt concurentInternalCalls;
    protected UnlimitedUInt concurentText2Speech;
    protected UnlimitedUInt queueMembersMax;
    protected UnlimitedUInt callCardCodesMax;
    protected UnlimitedUInt callbackCallerIDMax;
    protected UnlimitedDate accountExpire;
    protected UnlimitedUInt accountExpireDays;
    @XmlElement(defaultValue = "none")
    protected String level;
    @XmlElement(defaultValue = "false")
    protected Boolean advertising;
    @XmlElement(defaultValue = "false")
    protected Boolean browserChat;
    @XmlElement(defaultValue = "own")
    protected String activeCalls;
    @XmlElement(defaultValue = "own")
    protected String callOperations;
    @XmlElement(defaultValue = "own")
    protected String recordCalls;
    @XmlElement(defaultValue = "own")
    protected String whisperOnCall;
    @XmlElement(defaultValue = "parked_by_me")
    protected String unparkCalls;
    @XmlElement(defaultValue = "own")
    protected String pickupCalls;

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

    /**
     * Gets the value of the soundManag property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isSoundManag() {
        return soundManag;
    }

    /**
     * Sets the value of the soundManag property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setSoundManag(Boolean value) {
        this.soundManag = value;
    }

    /**
     * Gets the value of the sipManag property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isSipManag() {
        return sipManag;
    }

    /**
     * Sets the value of the sipManag property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setSipManag(Boolean value) {
        this.sipManag = value;
    }

    /**
     * Gets the value of the multiUser property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isMultiUser() {
        return multiUser;
    }

    /**
     * Sets the value of the multiUser property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setMultiUser(Boolean value) {
        this.multiUser = value;
    }

    /**
     * Gets the value of the callAPIManag property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isCallAPIManag() {
        return callAPIManag;
    }

    /**
     * Sets the value of the callAPIManag property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setCallAPIManag(Boolean value) {
        this.callAPIManag = value;
    }

    /**
     * Gets the value of the callerIDManag property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isCallerIDManag() {
        return callerIDManag;
    }

    /**
     * Sets the value of the callerIDManag property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setCallerIDManag(Boolean value) {
        this.callerIDManag = value;
    }

    /**
     * Gets the value of the provisionManag property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getProvisionManag() {
        return provisionManag;
    }

    /**
     * Sets the value of the provisionManag property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setProvisionManag(BigInteger value) {
        this.provisionManag = value;
    }

    /**
     * Gets the value of the imManag property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isIMManag() {
        return imManag;
    }

    /**
     * Sets the value of the imManag property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setIMManag(Boolean value) {
        this.imManag = value;
    }

    /**
     * Gets the value of the soundStorage property.
     *
     * @return
     *     possible object is
     *     {@link UnlimitedUInt }
     *
     */
    public UnlimitedUInt getSoundStorage() {
        return soundStorage;
    }

    /**
     * Sets the value of the soundStorage property.
     *
     * @param value
     *     allowed object is
     *     {@link UnlimitedUInt }
     *
     */
    public void setSoundStorage(UnlimitedUInt value) {
        this.soundStorage = value;
    }

    /**
     * Gets the value of the mohStorage property.
     *
     * @return
     *     possible object is
     *     {@link UnlimitedUInt }
     *
     */
    public UnlimitedUInt getMohStorage() {
        return mohStorage;
    }

    /**
     * Sets the value of the mohStorage property.
     *
     * @param value
     *     allowed object is
     *     {@link UnlimitedUInt }
     *
     */
    public void setMohStorage(UnlimitedUInt value) {
        this.mohStorage = value;
    }

    /**
     * Gets the value of the concurentCalls property.
     *
     * @return
     *     possible object is
     *     {@link UnlimitedUInt }
     *
     */
    public UnlimitedUInt getConcurentCalls() {
        return concurentCalls;
    }

    /**
     * Sets the value of the concurentCalls property.
     *
     * @param value
     *     allowed object is
     *     {@link UnlimitedUInt }
     *
     */
    public void setConcurentCalls(UnlimitedUInt value) {
        this.concurentCalls = value;
    }

    /**
     * Gets the value of the concurentInternalCalls property.
     *
     * @return
     *     possible object is
     *     {@link UnlimitedUInt }
     *
     */
    public UnlimitedUInt getConcurentInternalCalls() {
        return concurentInternalCalls;
    }

    /**
     * Sets the value of the concurentInternalCalls property.
     *
     * @param value
     *     allowed object is
     *     {@link UnlimitedUInt }
     *
     */
    public void setConcurentInternalCalls(UnlimitedUInt value) {
        this.concurentInternalCalls = value;
    }

    /**
     * Gets the value of the concurentText2Speech property.
     *
     * @return
     *     possible object is
     *     {@link UnlimitedUInt }
     *
     */
    public UnlimitedUInt getConcurentText2Speech() {
        return concurentText2Speech;
    }

    /**
     * Sets the value of the concurentText2Speech property.
     *
     * @param value
     *     allowed object is
     *     {@link UnlimitedUInt }
     *
     */
    public void setConcurentText2Speech(UnlimitedUInt value) {
        this.concurentText2Speech = value;
    }

    /**
     * Gets the value of the queueMembersMax property.
     *
     * @return
     *     possible object is
     *     {@link UnlimitedUInt }
     *
     */
    public UnlimitedUInt getQueueMembersMax() {
        return queueMembersMax;
    }

    /**
     * Sets the value of the queueMembersMax property.
     *
     * @param value
     *     allowed object is
     *     {@link UnlimitedUInt }
     *
     */
    public void setQueueMembersMax(UnlimitedUInt value) {
        this.queueMembersMax = value;
    }

    /**
     * Gets the value of the callCardCodesMax property.
     *
     * @return
     *     possible object is
     *     {@link UnlimitedUInt }
     *
     */
    public UnlimitedUInt getCallCardCodesMax() {
        return callCardCodesMax;
    }

    /**
     * Sets the value of the callCardCodesMax property.
     *
     * @param value
     *     allowed object is
     *     {@link UnlimitedUInt }
     *
     */
    public void setCallCardCodesMax(UnlimitedUInt value) {
        this.callCardCodesMax = value;
    }

    /**
     * Gets the value of the callbackCallerIDMax property.
     *
     * @return
     *     possible object is
     *     {@link UnlimitedUInt }
     *
     */
    public UnlimitedUInt getCallbackCallerIDMax() {
        return callbackCallerIDMax;
    }

    /**
     * Sets the value of the callbackCallerIDMax property.
     *
     * @param value
     *     allowed object is
     *     {@link UnlimitedUInt }
     *
     */
    public void setCallbackCallerIDMax(UnlimitedUInt value) {
        this.callbackCallerIDMax = value;
    }

    /**
     * Gets the value of the accountExpire property.
     *
     * @return
     *     possible object is
     *     {@link UnlimitedDate }
     *
     */
    public UnlimitedDate getAccountExpire() {
        return accountExpire;
    }

    /**
     * Sets the value of the accountExpire property.
     *
     * @param value
     *     allowed object is
     *     {@link UnlimitedDate }
     *
     */
    public void setAccountExpire(UnlimitedDate value) {
        this.accountExpire = value;
    }

    /**
     * Gets the value of the accountExpireDays property.
     *
     * @return
     *     possible object is
     *     {@link UnlimitedUInt }
     *
     */
    public UnlimitedUInt getAccountExpireDays() {
        return accountExpireDays;
    }

    /**
     * Sets the value of the accountExpireDays property.
     *
     * @param value
     *     allowed object is
     *     {@link UnlimitedUInt }
     *
     */
    public void setAccountExpireDays(UnlimitedUInt value) {
        this.accountExpireDays = value;
    }

    /**
     * Gets the value of the level property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getLevel() {
        return level;
    }

    /**
     * Sets the value of the level property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setLevel(String value) {
        this.level = value;
    }

    /**
     * Gets the value of the advertising property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isAdvertising() {
        return advertising;
    }

    /**
     * Sets the value of the advertising property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setAdvertising(Boolean value) {
        this.advertising = value;
    }

    /**
     * Gets the value of the browserChat property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isBrowserChat() {
        return browserChat;
    }

    /**
     * Sets the value of the browserChat property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setBrowserChat(Boolean value) {
        this.browserChat = value;
    }

    /**
     * Gets the value of the activeCalls property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getActiveCalls() {
        return activeCalls;
    }

    /**
     * Sets the value of the activeCalls property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setActiveCalls(String value) {
        this.activeCalls = value;
    }

    /**
     * Gets the value of the callOperations property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCallOperations() {
        return callOperations;
    }

    /**
     * Sets the value of the callOperations property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCallOperations(String value) {
        this.callOperations = value;
    }

    /**
     * Gets the value of the recordCalls property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getRecordCalls() {
        return recordCalls;
    }

    /**
     * Sets the value of the recordCalls property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setRecordCalls(String value) {
        this.recordCalls = value;
    }

    /**
     * Gets the value of the whisperOnCall property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getWhisperOnCall() {
        return whisperOnCall;
    }

    /**
     * Sets the value of the whisperOnCall property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setWhisperOnCall(String value) {
        this.whisperOnCall = value;
    }

    /**
     * Gets the value of the unparkCalls property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUnparkCalls() {
        return unparkCalls;
    }

    /**
     * Sets the value of the unparkCalls property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUnparkCalls(String value) {
        this.unparkCalls = value;
    }

    /**
     * Gets the value of the pickupCalls property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPickupCalls() {
        return pickupCalls;
    }

    /**
     * Sets the value of the pickupCalls property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPickupCalls(String value) {
        this.pickupCalls = value;
    }

}

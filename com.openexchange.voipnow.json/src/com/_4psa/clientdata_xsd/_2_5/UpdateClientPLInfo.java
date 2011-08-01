
package com._4psa.clientdata_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.clientmessages_xsd._2_5.UpdateClientPLRequest;
import com._4psa.common_xsd._2_5.Limit;
import com._4psa.common_xsd._2_5.UnlimitedDate;
import com._4psa.common_xsd._2_5.UnlimitedUInt;
import com._4psa.resellerdata_xsd._2_5.UpdateResellerPLInfo;


/**
 * Client permissions and limits data used when updateding the client limits and permissions
 *
 * <p>Java class for UpdateClientPLInfo complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="UpdateClientPLInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="permsManag" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="extensionManag" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="extFeatureManag" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="sipManag" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="IMManag" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="chargingPlanManag" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="soundManag" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="numberManag" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="callAPIManag" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="callerIDManag" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="MyVoipNowManag" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="premium"/>
 *               &lt;enumeration value="standard"/>
 *               &lt;enumeration value="none"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="ProvisionManag" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *               &lt;enumeration value="0"/>
 *               &lt;enumeration value="2"/>
 *               &lt;enumeration value="4"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="phoneExtMax" type="{http://4psa.com/Common.xsd/2.5.1}limit" minOccurs="0"/>
 *         &lt;element name="queueExtMax" type="{http://4psa.com/Common.xsd/2.5.1}limit" minOccurs="0"/>
 *         &lt;element name="ivrExtMax" type="{http://4psa.com/Common.xsd/2.5.1}limit" minOccurs="0"/>
 *         &lt;element name="voicemailExtMax" type="{http://4psa.com/Common.xsd/2.5.1}limit" minOccurs="0"/>
 *         &lt;element name="queuecenterExtMax" type="{http://4psa.com/Common.xsd/2.5.1}limit" minOccurs="0"/>
 *         &lt;element name="confExtMax" type="{http://4psa.com/Common.xsd/2.5.1}limit" minOccurs="0"/>
 *         &lt;element name="callbackExtMax" type="{http://4psa.com/Common.xsd/2.5.1}limit" minOccurs="0"/>
 *         &lt;element name="callbackCallerIDMax" type="{http://4psa.com/Common.xsd/2.5.1}limit" minOccurs="0"/>
 *         &lt;element name="callCardExtMax" type="{http://4psa.com/Common.xsd/2.5.1}limit" minOccurs="0"/>
 *         &lt;element name="callCardCodesMax" type="{http://4psa.com/Common.xsd/2.5.1}limit" minOccurs="0"/>
 *         &lt;element name="intercomExtMax" type="{http://4psa.com/Common.xsd/2.5.1}limit" minOccurs="0"/>
 *         &lt;element name="concurentCalls" type="{http://4psa.com/Common.xsd/2.5.1}limit" minOccurs="0"/>
 *         &lt;element name="concurentInternalCalls" type="{http://4psa.com/Common.xsd/2.5.1}limit" minOccurs="0"/>
 *         &lt;element name="queueMembersMax" type="{http://4psa.com/Common.xsd/2.5.1}limit" minOccurs="0"/>
 *         &lt;element name="concurentText2Speech" type="{http://4psa.com/Common.xsd/2.5.1}limit" minOccurs="0"/>
 *         &lt;element name="mailboxMax" type="{http://4psa.com/Common.xsd/2.5.1}limit" minOccurs="0"/>
 *         &lt;element name="mailboxSize" type="{http://4psa.com/Common.xsd/2.5.1}limit" minOccurs="0"/>
 *         &lt;element name="recordStorage" type="{http://4psa.com/Common.xsd/2.5.1}limit" minOccurs="0"/>
 *         &lt;element name="soundStorage" type="{http://4psa.com/Common.xsd/2.5.1}limit" minOccurs="0"/>
 *         &lt;element name="mohStorage" type="{http://4psa.com/Common.xsd/2.5.1}limit" minOccurs="0"/>
 *         &lt;element name="faxStorage" type="{http://4psa.com/Common.xsd/2.5.1}limit" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="accountExpire" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedDate" minOccurs="0"/>
 *           &lt;element name="accountExpireDays" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedUInt" minOccurs="0"/>
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
@XmlType(name = "UpdateClientPLInfo", propOrder = {
    "permsManag",
    "extensionManag",
    "extFeatureManag",
    "sipManag",
    "imManag",
    "chargingPlanManag",
    "soundManag",
    "numberManag",
    "callAPIManag",
    "callerIDManag",
    "myVoipNowManag",
    "provisionManag",
    "phoneExtMax",
    "queueExtMax",
    "ivrExtMax",
    "voicemailExtMax",
    "queuecenterExtMax",
    "confExtMax",
    "callbackExtMax",
    "callbackCallerIDMax",
    "callCardExtMax",
    "callCardCodesMax",
    "intercomExtMax",
    "concurentCalls",
    "concurentInternalCalls",
    "queueMembersMax",
    "concurentText2Speech",
    "mailboxMax",
    "mailboxSize",
    "recordStorage",
    "soundStorage",
    "mohStorage",
    "faxStorage",
    "accountExpire",
    "accountExpireDays"
})
@XmlSeeAlso({
    UpdateResellerPLInfo.class,
    UpdateClientPLRequest.class
})
public class UpdateClientPLInfo {

    protected Boolean permsManag;
    protected Boolean extensionManag;
    protected Boolean extFeatureManag;
    protected Boolean sipManag;
    @XmlElement(name = "IMManag")
    protected Boolean imManag;
    protected Boolean chargingPlanManag;
    protected Boolean soundManag;
    protected Boolean numberManag;
    protected Boolean callAPIManag;
    protected Boolean callerIDManag;
    @XmlElement(name = "MyVoipNowManag", defaultValue = "none")
    protected String myVoipNowManag;
    @XmlElement(name = "ProvisionManag", defaultValue = "0")
    protected BigInteger provisionManag;
    protected Limit phoneExtMax;
    protected Limit queueExtMax;
    protected Limit ivrExtMax;
    protected Limit voicemailExtMax;
    protected Limit queuecenterExtMax;
    protected Limit confExtMax;
    protected Limit callbackExtMax;
    protected Limit callbackCallerIDMax;
    protected Limit callCardExtMax;
    protected Limit callCardCodesMax;
    protected Limit intercomExtMax;
    protected Limit concurentCalls;
    protected Limit concurentInternalCalls;
    protected Limit queueMembersMax;
    protected Limit concurentText2Speech;
    protected Limit mailboxMax;
    protected Limit mailboxSize;
    protected Limit recordStorage;
    protected Limit soundStorage;
    protected Limit mohStorage;
    protected Limit faxStorage;
    protected UnlimitedDate accountExpire;
    protected UnlimitedUInt accountExpireDays;

    /**
     * Gets the value of the permsManag property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isPermsManag() {
        return permsManag;
    }

    /**
     * Sets the value of the permsManag property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setPermsManag(Boolean value) {
        this.permsManag = value;
    }

    /**
     * Gets the value of the extensionManag property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isExtensionManag() {
        return extensionManag;
    }

    /**
     * Sets the value of the extensionManag property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setExtensionManag(Boolean value) {
        this.extensionManag = value;
    }

    /**
     * Gets the value of the extFeatureManag property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isExtFeatureManag() {
        return extFeatureManag;
    }

    /**
     * Sets the value of the extFeatureManag property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setExtFeatureManag(Boolean value) {
        this.extFeatureManag = value;
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
     * Gets the value of the chargingPlanManag property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isChargingPlanManag() {
        return chargingPlanManag;
    }

    /**
     * Sets the value of the chargingPlanManag property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setChargingPlanManag(Boolean value) {
        this.chargingPlanManag = value;
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
     * Gets the value of the numberManag property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isNumberManag() {
        return numberManag;
    }

    /**
     * Sets the value of the numberManag property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setNumberManag(Boolean value) {
        this.numberManag = value;
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
     * Gets the value of the myVoipNowManag property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMyVoipNowManag() {
        return myVoipNowManag;
    }

    /**
     * Sets the value of the myVoipNowManag property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMyVoipNowManag(String value) {
        this.myVoipNowManag = value;
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
     * Gets the value of the phoneExtMax property.
     *
     * @return
     *     possible object is
     *     {@link Limit }
     *
     */
    public Limit getPhoneExtMax() {
        return phoneExtMax;
    }

    /**
     * Sets the value of the phoneExtMax property.
     *
     * @param value
     *     allowed object is
     *     {@link Limit }
     *
     */
    public void setPhoneExtMax(Limit value) {
        this.phoneExtMax = value;
    }

    /**
     * Gets the value of the queueExtMax property.
     *
     * @return
     *     possible object is
     *     {@link Limit }
     *
     */
    public Limit getQueueExtMax() {
        return queueExtMax;
    }

    /**
     * Sets the value of the queueExtMax property.
     *
     * @param value
     *     allowed object is
     *     {@link Limit }
     *
     */
    public void setQueueExtMax(Limit value) {
        this.queueExtMax = value;
    }

    /**
     * Gets the value of the ivrExtMax property.
     *
     * @return
     *     possible object is
     *     {@link Limit }
     *
     */
    public Limit getIvrExtMax() {
        return ivrExtMax;
    }

    /**
     * Sets the value of the ivrExtMax property.
     *
     * @param value
     *     allowed object is
     *     {@link Limit }
     *
     */
    public void setIvrExtMax(Limit value) {
        this.ivrExtMax = value;
    }

    /**
     * Gets the value of the voicemailExtMax property.
     *
     * @return
     *     possible object is
     *     {@link Limit }
     *
     */
    public Limit getVoicemailExtMax() {
        return voicemailExtMax;
    }

    /**
     * Sets the value of the voicemailExtMax property.
     *
     * @param value
     *     allowed object is
     *     {@link Limit }
     *
     */
    public void setVoicemailExtMax(Limit value) {
        this.voicemailExtMax = value;
    }

    /**
     * Gets the value of the queuecenterExtMax property.
     *
     * @return
     *     possible object is
     *     {@link Limit }
     *
     */
    public Limit getQueuecenterExtMax() {
        return queuecenterExtMax;
    }

    /**
     * Sets the value of the queuecenterExtMax property.
     *
     * @param value
     *     allowed object is
     *     {@link Limit }
     *
     */
    public void setQueuecenterExtMax(Limit value) {
        this.queuecenterExtMax = value;
    }

    /**
     * Gets the value of the confExtMax property.
     *
     * @return
     *     possible object is
     *     {@link Limit }
     *
     */
    public Limit getConfExtMax() {
        return confExtMax;
    }

    /**
     * Sets the value of the confExtMax property.
     *
     * @param value
     *     allowed object is
     *     {@link Limit }
     *
     */
    public void setConfExtMax(Limit value) {
        this.confExtMax = value;
    }

    /**
     * Gets the value of the callbackExtMax property.
     *
     * @return
     *     possible object is
     *     {@link Limit }
     *
     */
    public Limit getCallbackExtMax() {
        return callbackExtMax;
    }

    /**
     * Sets the value of the callbackExtMax property.
     *
     * @param value
     *     allowed object is
     *     {@link Limit }
     *
     */
    public void setCallbackExtMax(Limit value) {
        this.callbackExtMax = value;
    }

    /**
     * Gets the value of the callbackCallerIDMax property.
     *
     * @return
     *     possible object is
     *     {@link Limit }
     *
     */
    public Limit getCallbackCallerIDMax() {
        return callbackCallerIDMax;
    }

    /**
     * Sets the value of the callbackCallerIDMax property.
     *
     * @param value
     *     allowed object is
     *     {@link Limit }
     *
     */
    public void setCallbackCallerIDMax(Limit value) {
        this.callbackCallerIDMax = value;
    }

    /**
     * Gets the value of the callCardExtMax property.
     *
     * @return
     *     possible object is
     *     {@link Limit }
     *
     */
    public Limit getCallCardExtMax() {
        return callCardExtMax;
    }

    /**
     * Sets the value of the callCardExtMax property.
     *
     * @param value
     *     allowed object is
     *     {@link Limit }
     *
     */
    public void setCallCardExtMax(Limit value) {
        this.callCardExtMax = value;
    }

    /**
     * Gets the value of the callCardCodesMax property.
     *
     * @return
     *     possible object is
     *     {@link Limit }
     *
     */
    public Limit getCallCardCodesMax() {
        return callCardCodesMax;
    }

    /**
     * Sets the value of the callCardCodesMax property.
     *
     * @param value
     *     allowed object is
     *     {@link Limit }
     *
     */
    public void setCallCardCodesMax(Limit value) {
        this.callCardCodesMax = value;
    }

    /**
     * Gets the value of the intercomExtMax property.
     *
     * @return
     *     possible object is
     *     {@link Limit }
     *
     */
    public Limit getIntercomExtMax() {
        return intercomExtMax;
    }

    /**
     * Sets the value of the intercomExtMax property.
     *
     * @param value
     *     allowed object is
     *     {@link Limit }
     *
     */
    public void setIntercomExtMax(Limit value) {
        this.intercomExtMax = value;
    }

    /**
     * Gets the value of the concurentCalls property.
     *
     * @return
     *     possible object is
     *     {@link Limit }
     *
     */
    public Limit getConcurentCalls() {
        return concurentCalls;
    }

    /**
     * Sets the value of the concurentCalls property.
     *
     * @param value
     *     allowed object is
     *     {@link Limit }
     *
     */
    public void setConcurentCalls(Limit value) {
        this.concurentCalls = value;
    }

    /**
     * Gets the value of the concurentInternalCalls property.
     *
     * @return
     *     possible object is
     *     {@link Limit }
     *
     */
    public Limit getConcurentInternalCalls() {
        return concurentInternalCalls;
    }

    /**
     * Sets the value of the concurentInternalCalls property.
     *
     * @param value
     *     allowed object is
     *     {@link Limit }
     *
     */
    public void setConcurentInternalCalls(Limit value) {
        this.concurentInternalCalls = value;
    }

    /**
     * Gets the value of the queueMembersMax property.
     *
     * @return
     *     possible object is
     *     {@link Limit }
     *
     */
    public Limit getQueueMembersMax() {
        return queueMembersMax;
    }

    /**
     * Sets the value of the queueMembersMax property.
     *
     * @param value
     *     allowed object is
     *     {@link Limit }
     *
     */
    public void setQueueMembersMax(Limit value) {
        this.queueMembersMax = value;
    }

    /**
     * Gets the value of the concurentText2Speech property.
     *
     * @return
     *     possible object is
     *     {@link Limit }
     *
     */
    public Limit getConcurentText2Speech() {
        return concurentText2Speech;
    }

    /**
     * Sets the value of the concurentText2Speech property.
     *
     * @param value
     *     allowed object is
     *     {@link Limit }
     *
     */
    public void setConcurentText2Speech(Limit value) {
        this.concurentText2Speech = value;
    }

    /**
     * Gets the value of the mailboxMax property.
     *
     * @return
     *     possible object is
     *     {@link Limit }
     *
     */
    public Limit getMailboxMax() {
        return mailboxMax;
    }

    /**
     * Sets the value of the mailboxMax property.
     *
     * @param value
     *     allowed object is
     *     {@link Limit }
     *
     */
    public void setMailboxMax(Limit value) {
        this.mailboxMax = value;
    }

    /**
     * Gets the value of the mailboxSize property.
     *
     * @return
     *     possible object is
     *     {@link Limit }
     *
     */
    public Limit getMailboxSize() {
        return mailboxSize;
    }

    /**
     * Sets the value of the mailboxSize property.
     *
     * @param value
     *     allowed object is
     *     {@link Limit }
     *
     */
    public void setMailboxSize(Limit value) {
        this.mailboxSize = value;
    }

    /**
     * Gets the value of the recordStorage property.
     *
     * @return
     *     possible object is
     *     {@link Limit }
     *
     */
    public Limit getRecordStorage() {
        return recordStorage;
    }

    /**
     * Sets the value of the recordStorage property.
     *
     * @param value
     *     allowed object is
     *     {@link Limit }
     *
     */
    public void setRecordStorage(Limit value) {
        this.recordStorage = value;
    }

    /**
     * Gets the value of the soundStorage property.
     *
     * @return
     *     possible object is
     *     {@link Limit }
     *
     */
    public Limit getSoundStorage() {
        return soundStorage;
    }

    /**
     * Sets the value of the soundStorage property.
     *
     * @param value
     *     allowed object is
     *     {@link Limit }
     *
     */
    public void setSoundStorage(Limit value) {
        this.soundStorage = value;
    }

    /**
     * Gets the value of the mohStorage property.
     *
     * @return
     *     possible object is
     *     {@link Limit }
     *
     */
    public Limit getMohStorage() {
        return mohStorage;
    }

    /**
     * Sets the value of the mohStorage property.
     *
     * @param value
     *     allowed object is
     *     {@link Limit }
     *
     */
    public void setMohStorage(Limit value) {
        this.mohStorage = value;
    }

    /**
     * Gets the value of the faxStorage property.
     *
     * @return
     *     possible object is
     *     {@link Limit }
     *
     */
    public Limit getFaxStorage() {
        return faxStorage;
    }

    /**
     * Sets the value of the faxStorage property.
     *
     * @param value
     *     allowed object is
     *     {@link Limit }
     *
     */
    public void setFaxStorage(Limit value) {
        this.faxStorage = value;
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

}


package com._4psa.billingdata_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.billingmessages_xsd._2_5.AddChargingPlanRequest;
import com._4psa.billingmessages_xsd._2_5.EditChargingPlanRequest;
import com._4psa.common_xsd._2_5.UnlimitedUFloat;


/**
 * Charging plan data
 *
 * <p>Java class for ChargingPlanInfo complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ChargingPlanInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *         &lt;element name="default" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="channelRuleID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *           &lt;element name="channelRule" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;/choice>
 *         &lt;sequence>
 *           &lt;element name="allowIn" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *           &lt;element name="allowOut" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *           &lt;element name="allowLocal" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *           &lt;element name="allowExtended" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;/sequence>
 *         &lt;element name="planType" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;sequence>
 *           &lt;element name="includedCreditOut" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedUFloat" minOccurs="0"/>
 *           &lt;element name="includedCreditIn" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedUFloat" minOccurs="0"/>
 *           &lt;element name="externalMin" maxOccurs="unbounded" minOccurs="0">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;sequence>
 *                     &lt;element name="minutes" type="{http://4psa.com/Common.xsd/2.5.1}integer"/>
 *                     &lt;element name="intervalID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *                   &lt;/sequence>
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *         &lt;/sequence>
 *         &lt;sequence>
 *           &lt;element name="initialCreditOut" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedUFloat" minOccurs="0"/>
 *           &lt;element name="initialCreditIn" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedUFloat" minOccurs="0"/>
 *         &lt;/sequence>
 *         &lt;sequence>
 *           &lt;element name="chargeOut" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *           &lt;element name="thenChargeOut" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *           &lt;element name="chargeIn" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *           &lt;element name="thenChargeIn" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;/sequence>
 *         &lt;element name="chargeMethod" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="fixed"/>
 *               &lt;enumeration value="inherit"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;choice>
 *           &lt;element name="fixedCharge" minOccurs="0">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;sequence>
 *                     &lt;element name="externalIncoming" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *                     &lt;element name="external" maxOccurs="unbounded" minOccurs="0">
 *                       &lt;complexType>
 *                         &lt;complexContent>
 *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                             &lt;sequence>
 *                               &lt;element name="charge" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *                               &lt;element name="intervalID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *                             &lt;/sequence>
 *                           &lt;/restriction>
 *                         &lt;/complexContent>
 *                       &lt;/complexType>
 *                     &lt;/element>
 *                     &lt;element name="local" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *                     &lt;element name="extended" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *                   &lt;/sequence>
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *           &lt;element name="inheritedCharge" minOccurs="0">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;sequence>
 *                     &lt;element name="externalIncoming" minOccurs="0">
 *                       &lt;complexType>
 *                         &lt;complexContent>
 *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                             &lt;sequence>
 *                               &lt;element name="mulFactor" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *                               &lt;element name="adjustment" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *                             &lt;/sequence>
 *                           &lt;/restriction>
 *                         &lt;/complexContent>
 *                       &lt;/complexType>
 *                     &lt;/element>
 *                     &lt;element name="external" minOccurs="0">
 *                       &lt;complexType>
 *                         &lt;complexContent>
 *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                             &lt;sequence>
 *                               &lt;element name="mulFactor" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *                               &lt;element name="adjustment" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *                             &lt;/sequence>
 *                           &lt;/restriction>
 *                         &lt;/complexContent>
 *                       &lt;/complexType>
 *                     &lt;/element>
 *                     &lt;element name="local" minOccurs="0">
 *                       &lt;complexType>
 *                         &lt;complexContent>
 *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                             &lt;sequence>
 *                               &lt;element name="mulFactor" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *                               &lt;element name="adjustment" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *                             &lt;/sequence>
 *                           &lt;/restriction>
 *                         &lt;/complexContent>
 *                       &lt;/complexType>
 *                     &lt;/element>
 *                     &lt;element name="extended" minOccurs="0">
 *                       &lt;complexType>
 *                         &lt;complexContent>
 *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                             &lt;sequence>
 *                               &lt;element name="mulFactor" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *                               &lt;element name="adjustment" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *                             &lt;/sequence>
 *                           &lt;/restriction>
 *                         &lt;/complexContent>
 *                       &lt;/complexType>
 *                     &lt;/element>
 *                   &lt;/sequence>
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *         &lt;/choice>
 *         &lt;element name="soundID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChargingPlanInfo", propOrder = {
    "name",
    "_default",
    "channelRuleID",
    "channelRule",
    "allowIn",
    "allowOut",
    "allowLocal",
    "allowExtended",
    "planType",
    "includedCreditOut",
    "includedCreditIn",
    "externalMin",
    "initialCreditOut",
    "initialCreditIn",
    "chargeOut",
    "thenChargeOut",
    "chargeIn",
    "thenChargeIn",
    "chargeMethod",
    "fixedCharge",
    "inheritedCharge",
    "soundID"
})
@XmlSeeAlso({
    com._4psa.billingmessagesinfo_xsd._2_5.GetChargingPlanDetailsResponseType.ChargingPlan.class,
    EditChargingPlanRequest.class,
    AddChargingPlanRequest.class
})
public class ChargingPlanInfo {

    @XmlElement(required = true)
    protected String name;
    @XmlElement(name = "default")
    protected Boolean _default;
    protected BigInteger channelRuleID;
    protected String channelRule;
    @XmlElement(defaultValue = "0")
    protected Boolean allowIn;
    @XmlElement(defaultValue = "0")
    protected Boolean allowOut;
    @XmlElement(defaultValue = "0")
    protected Boolean allowLocal;
    @XmlElement(defaultValue = "0")
    protected Boolean allowExtended;
    @XmlElement(defaultValue = "prepaid")
    protected String planType;
    protected UnlimitedUFloat includedCreditOut;
    protected UnlimitedUFloat includedCreditIn;
    protected List<ChargingPlanInfo.ExternalMin> externalMin;
    protected UnlimitedUFloat initialCreditOut;
    protected UnlimitedUFloat initialCreditIn;
    @XmlElement(defaultValue = "60")
    protected BigInteger chargeOut;
    @XmlElement(defaultValue = "10")
    protected BigInteger thenChargeOut;
    @XmlElement(defaultValue = "60")
    protected BigInteger chargeIn;
    @XmlElement(defaultValue = "10")
    protected BigInteger thenChargeIn;
    @XmlElement(defaultValue = "fixed")
    protected String chargeMethod;
    protected ChargingPlanInfo.FixedCharge fixedCharge;
    protected ChargingPlanInfo.InheritedCharge inheritedCharge;
    protected BigInteger soundID;

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
     * Gets the value of the default property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isDefault() {
        return _default;
    }

    /**
     * Sets the value of the default property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setDefault(Boolean value) {
        this._default = value;
    }

    /**
     * Gets the value of the channelRuleID property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getChannelRuleID() {
        return channelRuleID;
    }

    /**
     * Sets the value of the channelRuleID property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setChannelRuleID(BigInteger value) {
        this.channelRuleID = value;
    }

    /**
     * Gets the value of the channelRule property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getChannelRule() {
        return channelRule;
    }

    /**
     * Sets the value of the channelRule property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setChannelRule(String value) {
        this.channelRule = value;
    }

    /**
     * Gets the value of the allowIn property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isAllowIn() {
        return allowIn;
    }

    /**
     * Sets the value of the allowIn property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setAllowIn(Boolean value) {
        this.allowIn = value;
    }

    /**
     * Gets the value of the allowOut property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isAllowOut() {
        return allowOut;
    }

    /**
     * Sets the value of the allowOut property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setAllowOut(Boolean value) {
        this.allowOut = value;
    }

    /**
     * Gets the value of the allowLocal property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isAllowLocal() {
        return allowLocal;
    }

    /**
     * Sets the value of the allowLocal property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setAllowLocal(Boolean value) {
        this.allowLocal = value;
    }

    /**
     * Gets the value of the allowExtended property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isAllowExtended() {
        return allowExtended;
    }

    /**
     * Sets the value of the allowExtended property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setAllowExtended(Boolean value) {
        this.allowExtended = value;
    }

    /**
     * Gets the value of the planType property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPlanType() {
        return planType;
    }

    /**
     * Sets the value of the planType property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPlanType(String value) {
        this.planType = value;
    }

    /**
     * Gets the value of the includedCreditOut property.
     *
     * @return
     *     possible object is
     *     {@link UnlimitedUFloat }
     *
     */
    public UnlimitedUFloat getIncludedCreditOut() {
        return includedCreditOut;
    }

    /**
     * Sets the value of the includedCreditOut property.
     *
     * @param value
     *     allowed object is
     *     {@link UnlimitedUFloat }
     *
     */
    public void setIncludedCreditOut(UnlimitedUFloat value) {
        this.includedCreditOut = value;
    }

    /**
     * Gets the value of the includedCreditIn property.
     *
     * @return
     *     possible object is
     *     {@link UnlimitedUFloat }
     *
     */
    public UnlimitedUFloat getIncludedCreditIn() {
        return includedCreditIn;
    }

    /**
     * Sets the value of the includedCreditIn property.
     *
     * @param value
     *     allowed object is
     *     {@link UnlimitedUFloat }
     *
     */
    public void setIncludedCreditIn(UnlimitedUFloat value) {
        this.includedCreditIn = value;
    }

    /**
     * Gets the value of the externalMin property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the externalMin property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExternalMin().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ChargingPlanInfo.ExternalMin }
     *
     *
     */
    public List<ChargingPlanInfo.ExternalMin> getExternalMin() {
        if (externalMin == null) {
            externalMin = new ArrayList<ChargingPlanInfo.ExternalMin>();
        }
        return this.externalMin;
    }

    /**
     * Gets the value of the initialCreditOut property.
     *
     * @return
     *     possible object is
     *     {@link UnlimitedUFloat }
     *
     */
    public UnlimitedUFloat getInitialCreditOut() {
        return initialCreditOut;
    }

    /**
     * Sets the value of the initialCreditOut property.
     *
     * @param value
     *     allowed object is
     *     {@link UnlimitedUFloat }
     *
     */
    public void setInitialCreditOut(UnlimitedUFloat value) {
        this.initialCreditOut = value;
    }

    /**
     * Gets the value of the initialCreditIn property.
     *
     * @return
     *     possible object is
     *     {@link UnlimitedUFloat }
     *
     */
    public UnlimitedUFloat getInitialCreditIn() {
        return initialCreditIn;
    }

    /**
     * Sets the value of the initialCreditIn property.
     *
     * @param value
     *     allowed object is
     *     {@link UnlimitedUFloat }
     *
     */
    public void setInitialCreditIn(UnlimitedUFloat value) {
        this.initialCreditIn = value;
    }

    /**
     * Gets the value of the chargeOut property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getChargeOut() {
        return chargeOut;
    }

    /**
     * Sets the value of the chargeOut property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setChargeOut(BigInteger value) {
        this.chargeOut = value;
    }

    /**
     * Gets the value of the thenChargeOut property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getThenChargeOut() {
        return thenChargeOut;
    }

    /**
     * Sets the value of the thenChargeOut property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setThenChargeOut(BigInteger value) {
        this.thenChargeOut = value;
    }

    /**
     * Gets the value of the chargeIn property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getChargeIn() {
        return chargeIn;
    }

    /**
     * Sets the value of the chargeIn property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setChargeIn(BigInteger value) {
        this.chargeIn = value;
    }

    /**
     * Gets the value of the thenChargeIn property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getThenChargeIn() {
        return thenChargeIn;
    }

    /**
     * Sets the value of the thenChargeIn property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setThenChargeIn(BigInteger value) {
        this.thenChargeIn = value;
    }

    /**
     * Gets the value of the chargeMethod property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getChargeMethod() {
        return chargeMethod;
    }

    /**
     * Sets the value of the chargeMethod property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setChargeMethod(String value) {
        this.chargeMethod = value;
    }

    /**
     * Gets the value of the fixedCharge property.
     *
     * @return
     *     possible object is
     *     {@link ChargingPlanInfo.FixedCharge }
     *
     */
    public ChargingPlanInfo.FixedCharge getFixedCharge() {
        return fixedCharge;
    }

    /**
     * Sets the value of the fixedCharge property.
     *
     * @param value
     *     allowed object is
     *     {@link ChargingPlanInfo.FixedCharge }
     *
     */
    public void setFixedCharge(ChargingPlanInfo.FixedCharge value) {
        this.fixedCharge = value;
    }

    /**
     * Gets the value of the inheritedCharge property.
     *
     * @return
     *     possible object is
     *     {@link ChargingPlanInfo.InheritedCharge }
     *
     */
    public ChargingPlanInfo.InheritedCharge getInheritedCharge() {
        return inheritedCharge;
    }

    /**
     * Sets the value of the inheritedCharge property.
     *
     * @param value
     *     allowed object is
     *     {@link ChargingPlanInfo.InheritedCharge }
     *
     */
    public void setInheritedCharge(ChargingPlanInfo.InheritedCharge value) {
        this.inheritedCharge = value;
    }

    /**
     * Gets the value of the soundID property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getSoundID() {
        return soundID;
    }

    /**
     * Sets the value of the soundID property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setSoundID(BigInteger value) {
        this.soundID = value;
    }


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
     *         &lt;element name="minutes" type="{http://4psa.com/Common.xsd/2.5.1}integer"/>
     *         &lt;element name="intervalID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
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
        "minutes",
        "intervalID"
    })
    public static class ExternalMin {

        @XmlElement(required = true)
        protected BigInteger minutes;
        protected BigInteger intervalID;

        /**
         * Gets the value of the minutes property.
         *
         * @return
         *     possible object is
         *     {@link BigInteger }
         *
         */
        public BigInteger getMinutes() {
            return minutes;
        }

        /**
         * Sets the value of the minutes property.
         *
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *
         */
        public void setMinutes(BigInteger value) {
            this.minutes = value;
        }

        /**
         * Gets the value of the intervalID property.
         *
         * @return
         *     possible object is
         *     {@link BigInteger }
         *
         */
        public BigInteger getIntervalID() {
            return intervalID;
        }

        /**
         * Sets the value of the intervalID property.
         *
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *
         */
        public void setIntervalID(BigInteger value) {
            this.intervalID = value;
        }

    }


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
     *         &lt;element name="externalIncoming" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
     *         &lt;element name="external" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="charge" type="{http://www.w3.org/2001/XMLSchema}float"/>
     *                   &lt;element name="intervalID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="local" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
     *         &lt;element name="extended" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
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
        "externalIncoming",
        "external",
        "local",
        "extended"
    })
    public static class FixedCharge {

        protected Float externalIncoming;
        protected List<ChargingPlanInfo.FixedCharge.External> external;
        protected Float local;
        protected Float extended;

        /**
         * Gets the value of the externalIncoming property.
         *
         * @return
         *     possible object is
         *     {@link Float }
         *
         */
        public Float getExternalIncoming() {
            return externalIncoming;
        }

        /**
         * Sets the value of the externalIncoming property.
         *
         * @param value
         *     allowed object is
         *     {@link Float }
         *
         */
        public void setExternalIncoming(Float value) {
            this.externalIncoming = value;
        }

        /**
         * Gets the value of the external property.
         *
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the external property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getExternal().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ChargingPlanInfo.FixedCharge.External }
         *
         *
         */
        public List<ChargingPlanInfo.FixedCharge.External> getExternal() {
            if (external == null) {
                external = new ArrayList<ChargingPlanInfo.FixedCharge.External>();
            }
            return this.external;
        }

        /**
         * Gets the value of the local property.
         *
         * @return
         *     possible object is
         *     {@link Float }
         *
         */
        public Float getLocal() {
            return local;
        }

        /**
         * Sets the value of the local property.
         *
         * @param value
         *     allowed object is
         *     {@link Float }
         *
         */
        public void setLocal(Float value) {
            this.local = value;
        }

        /**
         * Gets the value of the extended property.
         *
         * @return
         *     possible object is
         *     {@link Float }
         *
         */
        public Float getExtended() {
            return extended;
        }

        /**
         * Sets the value of the extended property.
         *
         * @param value
         *     allowed object is
         *     {@link Float }
         *
         */
        public void setExtended(Float value) {
            this.extended = value;
        }


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
         *         &lt;element name="charge" type="{http://www.w3.org/2001/XMLSchema}float"/>
         *         &lt;element name="intervalID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
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
            "charge",
            "intervalID"
        })
        public static class External {

            protected float charge;
            protected BigInteger intervalID;

            /**
             * Gets the value of the charge property.
             *
             */
            public float getCharge() {
                return charge;
            }

            /**
             * Sets the value of the charge property.
             *
             */
            public void setCharge(float value) {
                this.charge = value;
            }

            /**
             * Gets the value of the intervalID property.
             *
             * @return
             *     possible object is
             *     {@link BigInteger }
             *
             */
            public BigInteger getIntervalID() {
                return intervalID;
            }

            /**
             * Sets the value of the intervalID property.
             *
             * @param value
             *     allowed object is
             *     {@link BigInteger }
             *
             */
            public void setIntervalID(BigInteger value) {
                this.intervalID = value;
            }

        }

    }


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
     *         &lt;element name="externalIncoming" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="mulFactor" type="{http://www.w3.org/2001/XMLSchema}float"/>
     *                   &lt;element name="adjustment" type="{http://www.w3.org/2001/XMLSchema}float"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="external" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="mulFactor" type="{http://www.w3.org/2001/XMLSchema}float"/>
     *                   &lt;element name="adjustment" type="{http://www.w3.org/2001/XMLSchema}float"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="local" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="mulFactor" type="{http://www.w3.org/2001/XMLSchema}float"/>
     *                   &lt;element name="adjustment" type="{http://www.w3.org/2001/XMLSchema}float"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="extended" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="mulFactor" type="{http://www.w3.org/2001/XMLSchema}float"/>
     *                   &lt;element name="adjustment" type="{http://www.w3.org/2001/XMLSchema}float"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
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
        "externalIncoming",
        "external",
        "local",
        "extended"
    })
    public static class InheritedCharge {

        protected ChargingPlanInfo.InheritedCharge.ExternalIncoming externalIncoming;
        protected ChargingPlanInfo.InheritedCharge.External external;
        protected ChargingPlanInfo.InheritedCharge.Local local;
        protected ChargingPlanInfo.InheritedCharge.Extended extended;

        /**
         * Gets the value of the externalIncoming property.
         *
         * @return
         *     possible object is
         *     {@link ChargingPlanInfo.InheritedCharge.ExternalIncoming }
         *
         */
        public ChargingPlanInfo.InheritedCharge.ExternalIncoming getExternalIncoming() {
            return externalIncoming;
        }

        /**
         * Sets the value of the externalIncoming property.
         *
         * @param value
         *     allowed object is
         *     {@link ChargingPlanInfo.InheritedCharge.ExternalIncoming }
         *
         */
        public void setExternalIncoming(ChargingPlanInfo.InheritedCharge.ExternalIncoming value) {
            this.externalIncoming = value;
        }

        /**
         * Gets the value of the external property.
         *
         * @return
         *     possible object is
         *     {@link ChargingPlanInfo.InheritedCharge.External }
         *
         */
        public ChargingPlanInfo.InheritedCharge.External getExternal() {
            return external;
        }

        /**
         * Sets the value of the external property.
         *
         * @param value
         *     allowed object is
         *     {@link ChargingPlanInfo.InheritedCharge.External }
         *
         */
        public void setExternal(ChargingPlanInfo.InheritedCharge.External value) {
            this.external = value;
        }

        /**
         * Gets the value of the local property.
         *
         * @return
         *     possible object is
         *     {@link ChargingPlanInfo.InheritedCharge.Local }
         *
         */
        public ChargingPlanInfo.InheritedCharge.Local getLocal() {
            return local;
        }

        /**
         * Sets the value of the local property.
         *
         * @param value
         *     allowed object is
         *     {@link ChargingPlanInfo.InheritedCharge.Local }
         *
         */
        public void setLocal(ChargingPlanInfo.InheritedCharge.Local value) {
            this.local = value;
        }

        /**
         * Gets the value of the extended property.
         *
         * @return
         *     possible object is
         *     {@link ChargingPlanInfo.InheritedCharge.Extended }
         *
         */
        public ChargingPlanInfo.InheritedCharge.Extended getExtended() {
            return extended;
        }

        /**
         * Sets the value of the extended property.
         *
         * @param value
         *     allowed object is
         *     {@link ChargingPlanInfo.InheritedCharge.Extended }
         *
         */
        public void setExtended(ChargingPlanInfo.InheritedCharge.Extended value) {
            this.extended = value;
        }


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
         *         &lt;element name="mulFactor" type="{http://www.w3.org/2001/XMLSchema}float"/>
         *         &lt;element name="adjustment" type="{http://www.w3.org/2001/XMLSchema}float"/>
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
            "mulFactor",
            "adjustment"
        })
        public static class Extended {

            protected float mulFactor;
            protected float adjustment;

            /**
             * Gets the value of the mulFactor property.
             *
             */
            public float getMulFactor() {
                return mulFactor;
            }

            /**
             * Sets the value of the mulFactor property.
             *
             */
            public void setMulFactor(float value) {
                this.mulFactor = value;
            }

            /**
             * Gets the value of the adjustment property.
             *
             */
            public float getAdjustment() {
                return adjustment;
            }

            /**
             * Sets the value of the adjustment property.
             *
             */
            public void setAdjustment(float value) {
                this.adjustment = value;
            }

        }


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
         *         &lt;element name="mulFactor" type="{http://www.w3.org/2001/XMLSchema}float"/>
         *         &lt;element name="adjustment" type="{http://www.w3.org/2001/XMLSchema}float"/>
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
            "mulFactor",
            "adjustment"
        })
        public static class External {

            protected float mulFactor;
            protected float adjustment;

            /**
             * Gets the value of the mulFactor property.
             *
             */
            public float getMulFactor() {
                return mulFactor;
            }

            /**
             * Sets the value of the mulFactor property.
             *
             */
            public void setMulFactor(float value) {
                this.mulFactor = value;
            }

            /**
             * Gets the value of the adjustment property.
             *
             */
            public float getAdjustment() {
                return adjustment;
            }

            /**
             * Sets the value of the adjustment property.
             *
             */
            public void setAdjustment(float value) {
                this.adjustment = value;
            }

        }


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
         *         &lt;element name="mulFactor" type="{http://www.w3.org/2001/XMLSchema}float"/>
         *         &lt;element name="adjustment" type="{http://www.w3.org/2001/XMLSchema}float"/>
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
            "mulFactor",
            "adjustment"
        })
        public static class ExternalIncoming {

            protected float mulFactor;
            protected float adjustment;

            /**
             * Gets the value of the mulFactor property.
             *
             */
            public float getMulFactor() {
                return mulFactor;
            }

            /**
             * Sets the value of the mulFactor property.
             *
             */
            public void setMulFactor(float value) {
                this.mulFactor = value;
            }

            /**
             * Gets the value of the adjustment property.
             *
             */
            public float getAdjustment() {
                return adjustment;
            }

            /**
             * Sets the value of the adjustment property.
             *
             */
            public void setAdjustment(float value) {
                this.adjustment = value;
            }

        }


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
         *         &lt;element name="mulFactor" type="{http://www.w3.org/2001/XMLSchema}float"/>
         *         &lt;element name="adjustment" type="{http://www.w3.org/2001/XMLSchema}float"/>
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
            "mulFactor",
            "adjustment"
        })
        public static class Local {

            protected float mulFactor;
            protected float adjustment;

            /**
             * Gets the value of the mulFactor property.
             *
             */
            public float getMulFactor() {
                return mulFactor;
            }

            /**
             * Sets the value of the mulFactor property.
             *
             */
            public void setMulFactor(float value) {
                this.mulFactor = value;
            }

            /**
             * Gets the value of the adjustment property.
             *
             */
            public float getAdjustment() {
                return adjustment;
            }

            /**
             * Sets the value of the adjustment property.
             *
             */
            public void setAdjustment(float value) {
                this.adjustment = value;
            }

        }

    }

}

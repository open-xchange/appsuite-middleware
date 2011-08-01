
package com._4psa.channeldata_xsd._2_5;

import java.math.BigDecimal;
import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * Rule definition: {action} number {number} if time interval {intervalId} and coming from {comingFrom} transfer through {channelId} and {prefixOperation} with /prefixOperation: prefix {prefix}/ prefixOperation: replace {prefix}/prefixOperation: delete  {digits} digits starting from {digitsAfter}/prefixOperation: add {digitsAfter} after {digitsAfterAdd}/ add in position {position}
 *
 * <p>Java class for RoutingRuleInfo complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="RoutingRuleInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="action" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="block"/>
 *               &lt;enumeration value="transfer"/>
 *               &lt;enumeration value="process"/>
 *               &lt;enumeration value="portability"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="engine" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="number">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;whiteSpace value="collapse"/>
 *               &lt;pattern value="[*XZN\-\.\[\]\d]+"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="intervalID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;element name="comingFrom" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;whiteSpace value="collapse"/>
 *               &lt;pattern value="[0-9\*\.]+"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="channelID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;sequence minOccurs="0">
 *           &lt;element name="callerIDPrefixOperation" minOccurs="0">
 *             &lt;simpleType>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                 &lt;enumeration value="prefix"/>
 *                 &lt;enumeration value="replace"/>
 *                 &lt;enumeration value="add"/>
 *                 &lt;enumeration value="substract"/>
 *               &lt;/restriction>
 *             &lt;/simpleType>
 *           &lt;/element>
 *           &lt;element name="callerIDPrefix" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *           &lt;element name="callerIDDigits" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *           &lt;element name="callerIDDigitsAfter" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *           &lt;element name="callerIDNumberAdd" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *           &lt;element name="callerIDDigitsAfterAdd" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *           &lt;element name="callerIDMatch" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;/sequence>
 *         &lt;sequence minOccurs="0">
 *           &lt;element name="prefixOperation" minOccurs="0">
 *             &lt;simpleType>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                 &lt;enumeration value="prefix"/>
 *                 &lt;enumeration value="replace"/>
 *                 &lt;enumeration value="add"/>
 *                 &lt;enumeration value="substract"/>
 *               &lt;/restriction>
 *             &lt;/simpleType>
 *           &lt;/element>
 *           &lt;element name="prefix" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *           &lt;element name="digits" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *           &lt;element name="numberAdd" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *           &lt;element name="digitsAfterAdd" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *           &lt;element name="digitsAfter" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;/sequence>
 *         &lt;element name="position" type="{http://4psa.com/Common.xsd/2.5.1}unsignedInt" minOccurs="0"/>
 *         &lt;element name="final" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RoutingRuleInfo", propOrder = {
    "action",
    "engine",
    "number",
    "intervalID",
    "comingFrom",
    "channelID",
    "callerIDPrefixOperation",
    "callerIDPrefix",
    "callerIDDigits",
    "callerIDDigitsAfter",
    "callerIDNumberAdd",
    "callerIDDigitsAfterAdd",
    "callerIDMatch",
    "prefixOperation",
    "prefix",
    "digits",
    "numberAdd",
    "digitsAfterAdd",
    "digitsAfter",
    "position",
    "_final"
})
@XmlSeeAlso({
    com._4psa.channelmessagesinfo_xsd._2_5.GetCallRulesOutResponseType.Rules.class
})
public class RoutingRuleInfo {

    @XmlElement(defaultValue = "block")
    protected String action;
    protected String engine;
    @XmlElement(required = true)
    protected String number;
    protected BigInteger intervalID;
    protected String comingFrom;
    protected BigInteger channelID;
    @XmlElement(defaultValue = "prefix")
    protected String callerIDPrefixOperation;
    protected BigDecimal callerIDPrefix;
    protected BigInteger callerIDDigits;
    protected BigInteger callerIDDigitsAfter;
    protected BigInteger callerIDNumberAdd;
    protected BigDecimal callerIDDigitsAfterAdd;
    protected String callerIDMatch;
    @XmlElement(defaultValue = "prefix")
    protected String prefixOperation;
    protected BigDecimal prefix;
    protected BigInteger digits;
    protected BigInteger numberAdd;
    protected BigDecimal digitsAfterAdd;
    protected BigInteger digitsAfter;
    protected Long position;
    @XmlElement(name = "final")
    protected Boolean _final;

    /**
     * Gets the value of the action property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the value of the action property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAction(String value) {
        this.action = value;
    }

    /**
     * Gets the value of the engine property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getEngine() {
        return engine;
    }

    /**
     * Sets the value of the engine property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setEngine(String value) {
        this.engine = value;
    }

    /**
     * Gets the value of the number property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getNumber() {
        return number;
    }

    /**
     * Sets the value of the number property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setNumber(String value) {
        this.number = value;
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

    /**
     * Gets the value of the comingFrom property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getComingFrom() {
        return comingFrom;
    }

    /**
     * Sets the value of the comingFrom property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setComingFrom(String value) {
        this.comingFrom = value;
    }

    /**
     * Gets the value of the channelID property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getChannelID() {
        return channelID;
    }

    /**
     * Sets the value of the channelID property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setChannelID(BigInteger value) {
        this.channelID = value;
    }

    /**
     * Gets the value of the callerIDPrefixOperation property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCallerIDPrefixOperation() {
        return callerIDPrefixOperation;
    }

    /**
     * Sets the value of the callerIDPrefixOperation property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCallerIDPrefixOperation(String value) {
        this.callerIDPrefixOperation = value;
    }

    /**
     * Gets the value of the callerIDPrefix property.
     *
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *
     */
    public BigDecimal getCallerIDPrefix() {
        return callerIDPrefix;
    }

    /**
     * Sets the value of the callerIDPrefix property.
     *
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *
     */
    public void setCallerIDPrefix(BigDecimal value) {
        this.callerIDPrefix = value;
    }

    /**
     * Gets the value of the callerIDDigits property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getCallerIDDigits() {
        return callerIDDigits;
    }

    /**
     * Sets the value of the callerIDDigits property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setCallerIDDigits(BigInteger value) {
        this.callerIDDigits = value;
    }

    /**
     * Gets the value of the callerIDDigitsAfter property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getCallerIDDigitsAfter() {
        return callerIDDigitsAfter;
    }

    /**
     * Sets the value of the callerIDDigitsAfter property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setCallerIDDigitsAfter(BigInteger value) {
        this.callerIDDigitsAfter = value;
    }

    /**
     * Gets the value of the callerIDNumberAdd property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getCallerIDNumberAdd() {
        return callerIDNumberAdd;
    }

    /**
     * Sets the value of the callerIDNumberAdd property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setCallerIDNumberAdd(BigInteger value) {
        this.callerIDNumberAdd = value;
    }

    /**
     * Gets the value of the callerIDDigitsAfterAdd property.
     *
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *
     */
    public BigDecimal getCallerIDDigitsAfterAdd() {
        return callerIDDigitsAfterAdd;
    }

    /**
     * Sets the value of the callerIDDigitsAfterAdd property.
     *
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *
     */
    public void setCallerIDDigitsAfterAdd(BigDecimal value) {
        this.callerIDDigitsAfterAdd = value;
    }

    /**
     * Gets the value of the callerIDMatch property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCallerIDMatch() {
        return callerIDMatch;
    }

    /**
     * Sets the value of the callerIDMatch property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCallerIDMatch(String value) {
        this.callerIDMatch = value;
    }

    /**
     * Gets the value of the prefixOperation property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPrefixOperation() {
        return prefixOperation;
    }

    /**
     * Sets the value of the prefixOperation property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPrefixOperation(String value) {
        this.prefixOperation = value;
    }

    /**
     * Gets the value of the prefix property.
     *
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *
     */
    public BigDecimal getPrefix() {
        return prefix;
    }

    /**
     * Sets the value of the prefix property.
     *
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *
     */
    public void setPrefix(BigDecimal value) {
        this.prefix = value;
    }

    /**
     * Gets the value of the digits property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getDigits() {
        return digits;
    }

    /**
     * Sets the value of the digits property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setDigits(BigInteger value) {
        this.digits = value;
    }

    /**
     * Gets the value of the numberAdd property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getNumberAdd() {
        return numberAdd;
    }

    /**
     * Sets the value of the numberAdd property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setNumberAdd(BigInteger value) {
        this.numberAdd = value;
    }

    /**
     * Gets the value of the digitsAfterAdd property.
     *
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *
     */
    public BigDecimal getDigitsAfterAdd() {
        return digitsAfterAdd;
    }

    /**
     * Sets the value of the digitsAfterAdd property.
     *
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *
     */
    public void setDigitsAfterAdd(BigDecimal value) {
        this.digitsAfterAdd = value;
    }

    /**
     * Gets the value of the digitsAfter property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getDigitsAfter() {
        return digitsAfter;
    }

    /**
     * Sets the value of the digitsAfter property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setDigitsAfter(BigInteger value) {
        this.digitsAfter = value;
    }

    /**
     * Gets the value of the position property.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public Long getPosition() {
        return position;
    }

    /**
     * Sets the value of the position property.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setPosition(Long value) {
        this.position = value;
    }

    /**
     * Gets the value of the final property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isFinal() {
        return _final;
    }

    /**
     * Sets the value of the final property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setFinal(Boolean value) {
        this._final = value;
    }

}

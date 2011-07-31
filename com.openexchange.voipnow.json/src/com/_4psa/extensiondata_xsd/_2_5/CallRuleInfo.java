
package com._4psa.extensiondata_xsd._2_5;

import java.math.BigDecimal;
import java.math.BigInteger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * Incoming call rule data
 * 
 * <p>Java class for CallRuleInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CallRuleInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="match" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
 *               &lt;enumeration value="1"/>
 *               &lt;enumeration value="0"/>
 *               &lt;enumeration value="2"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="number" type="{http://4psa.com/Common.xsd/2.5.1}rule" minOccurs="0"/>
 *         &lt;element name="intervalID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;element name="position" type="{http://4psa.com/Common.xsd/2.5.1}unsignedInt" minOccurs="0"/>
 *         &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CallRuleInfo", propOrder = {
    "match",
    "number",
    "intervalID",
    "position",
    "key"
})
@XmlSeeAlso({
    com._4psa.extensionmessages_xsd._2_5.AddCallRulesInRequest.Rule.Transfer.class,
    com._4psa.extensionmessages_xsd._2_5.AddCallRulesInRequest.Rule.Cascade.class,
    com._4psa.extensionmessages_xsd._2_5.AddCallRulesInRequest.Rule.Authenticate.class,
    com._4psa.extensionmessages_xsd._2_5.AddCallRulesInRequest.Rule.SetCallPriority.class,
    com._4psa.extensionmessages_xsd._2_5.EditCallRulesInRequest.Rule.Transfer.class,
    com._4psa.extensionmessages_xsd._2_5.EditCallRulesInRequest.Rule.Cascade.class,
    com._4psa.extensionmessages_xsd._2_5.EditCallRulesInRequest.Rule.Authenticate.class,
    com._4psa.extensionmessages_xsd._2_5.EditCallRulesInRequest.Rule.SetCallPriority.class,
    com._4psa.extensionmessagesinfo_xsd._2_5.GetCallRulesInResponseType.Rules.Transfer.class,
    com._4psa.extensionmessagesinfo_xsd._2_5.GetCallRulesInResponseType.Rules.Cascade.class,
    com._4psa.extensionmessagesinfo_xsd._2_5.GetCallRulesInResponseType.Rules.Authenticate.class,
    com._4psa.extensionmessagesinfo_xsd._2_5.GetCallRulesInResponseType.Rules.SetCallPriority.class
})
public class CallRuleInfo {

    @XmlElement(defaultValue = "2")
    protected Integer match;
    protected String number;
    @XmlElementRef(name = "intervalID", namespace = "http://4psa.com/ExtensionData.xsd/2.5.1", type = JAXBElement.class)
    protected JAXBElement<BigInteger> intervalID;
    @XmlElement(defaultValue = "1")
    protected Long position;
    protected BigDecimal key;

    /**
     * Gets the value of the match property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMatch() {
        return match;
    }

    /**
     * Sets the value of the match property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMatch(Integer value) {
        this.match = value;
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
     *     {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     *     
     */
    public JAXBElement<BigInteger> getIntervalID() {
        return intervalID;
    }

    /**
     * Sets the value of the intervalID property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     *     
     */
    public void setIntervalID(JAXBElement<BigInteger> value) {
        this.intervalID = value;
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
     * Gets the value of the key property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getKey() {
        return key;
    }

    /**
     * Sets the value of the key property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setKey(BigDecimal value) {
        this.key = value;
    }

}

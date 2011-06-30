
package com._4psa.extensiondata_xsd._2_5;

import java.math.BigDecimal;
import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.extensionmessages_xsd._2_5.AssignQueueRemoteAgentRequest;
import com._4psa.extensionmessages_xsd._2_5.SetQueueRemoteAgentRequest;


/**
 * Remote agent data
 * 
 * <p>Java class for RemoteAgent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RemoteAgent">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *         &lt;element name="agentID" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *         &lt;element name="penalty" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="registration" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://4psa.com/Common.xsd/2.5.1}string">
 *               &lt;enumeration value="phone"/>
 *               &lt;enumeration value="register"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="phoneNumber" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="requireConfirmation" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RemoteAgent", propOrder = {
    "name",
    "agentID",
    "penalty",
    "registration",
    "phoneNumber",
    "requireConfirmation"
})
@XmlSeeAlso({
    AssignQueueRemoteAgentRequest.class,
    SetQueueRemoteAgentRequest.class
})
public class RemoteAgent {

    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected BigDecimal agentID;
    @XmlElement(defaultValue = "0")
    protected BigInteger penalty;
    @XmlElement(defaultValue = "phone")
    protected String registration;
    protected String phoneNumber;
    protected Boolean requireConfirmation;

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
     * Gets the value of the agentID property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAgentID() {
        return agentID;
    }

    /**
     * Sets the value of the agentID property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAgentID(BigDecimal value) {
        this.agentID = value;
    }

    /**
     * Gets the value of the penalty property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getPenalty() {
        return penalty;
    }

    /**
     * Sets the value of the penalty property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setPenalty(BigInteger value) {
        this.penalty = value;
    }

    /**
     * Gets the value of the registration property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRegistration() {
        return registration;
    }

    /**
     * Sets the value of the registration property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRegistration(String value) {
        this.registration = value;
    }

    /**
     * Gets the value of the phoneNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the value of the phoneNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPhoneNumber(String value) {
        this.phoneNumber = value;
    }

    /**
     * Gets the value of the requireConfirmation property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isRequireConfirmation() {
        return requireConfirmation;
    }

    /**
     * Sets the value of the requireConfirmation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRequireConfirmation(Boolean value) {
        this.requireConfirmation = value;
    }

}

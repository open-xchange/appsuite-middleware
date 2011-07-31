
package com._4psa.extensionmessages_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="supervisor" type="{http://4psa.com/Common.xsd/2.5.1}boolean"/>
 *         &lt;element name="penalty" type="{http://4psa.com/Common.xsd/2.5.1}integer"/>
 *         &lt;choice>
 *           &lt;element name="queueID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *           &lt;element name="queueIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;/choice>
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
@XmlType(name = "", propOrder = {
    "supervisor",
    "penalty",
    "queueID",
    "queueIdentifier",
    "id",
    "identifier"
})
@XmlRootElement(name = "SetQueueAgentRequest")
public class SetQueueAgentRequest {

    protected boolean supervisor;
    @XmlElement(required = true)
    protected BigInteger penalty;
    protected BigInteger queueID;
    protected String queueIdentifier;
    @XmlElement(name = "ID")
    protected BigInteger id;
    protected String identifier;

    /**
     * Gets the value of the supervisor property.
     *
     */
    public boolean isSupervisor() {
        return supervisor;
    }

    /**
     * Sets the value of the supervisor property.
     *
     */
    public void setSupervisor(boolean value) {
        this.supervisor = value;
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
     * Gets the value of the queueID property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getQueueID() {
        return queueID;
    }

    /**
     * Sets the value of the queueID property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setQueueID(BigInteger value) {
        this.queueID = value;
    }

    /**
     * Gets the value of the queueIdentifier property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getQueueIdentifier() {
        return queueIdentifier;
    }

    /**
     * Sets the value of the queueIdentifier property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setQueueIdentifier(String value) {
        this.queueIdentifier = value;
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

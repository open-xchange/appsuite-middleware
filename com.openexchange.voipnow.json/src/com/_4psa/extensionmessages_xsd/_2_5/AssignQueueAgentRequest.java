
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
 *         &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *         &lt;choice>
 *           &lt;element name="queueID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *           &lt;element name="identifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;/choice>
 *         &lt;element name="penalty" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="supervisor" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="auth" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="PIN" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
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
    "id",
    "queueID",
    "identifier",
    "penalty",
    "supervisor",
    "auth",
    "pin"
})
@XmlRootElement(name = "AssignQueueAgentRequest")
public class AssignQueueAgentRequest {

    @XmlElement(name = "ID", required = true)
    protected BigInteger id;
    protected BigInteger queueID;
    protected String identifier;
    @XmlElement(defaultValue = "0")
    protected BigInteger penalty;
    @XmlElement(defaultValue = "false")
    protected Boolean supervisor;
    @XmlElement(defaultValue = "false")
    protected Boolean auth;
    @XmlElement(name = "PIN")
    protected String pin;

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
     * Gets the value of the supervisor property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isSupervisor() {
        return supervisor;
    }

    /**
     * Sets the value of the supervisor property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setSupervisor(Boolean value) {
        this.supervisor = value;
    }

    /**
     * Gets the value of the auth property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isAuth() {
        return auth;
    }

    /**
     * Sets the value of the auth property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setAuth(Boolean value) {
        this.auth = value;
    }

    /**
     * Gets the value of the pin property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPIN() {
        return pin;
    }

    /**
     * Sets the value of the pin property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPIN(String value) {
        this.pin = value;
    }

}

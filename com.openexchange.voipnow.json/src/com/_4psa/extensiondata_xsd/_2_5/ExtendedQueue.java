
package com._4psa.extensiondata_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * Extended queue extension data
 * 
 * <p>Java class for ExtendedQueue complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ExtendedQueue">
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}Queue">
 *       &lt;sequence>
 *         &lt;element name="agents" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="registeredAgents" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="supervisors" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExtendedQueue", propOrder = {
    "agents",
    "registeredAgents",
    "supervisors"
})
public class ExtendedQueue
    extends Queue
{

    protected BigInteger agents;
    protected BigInteger registeredAgents;
    protected BigInteger supervisors;

    /**
     * Gets the value of the agents property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getAgents() {
        return agents;
    }

    /**
     * Sets the value of the agents property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setAgents(BigInteger value) {
        this.agents = value;
    }

    /**
     * Gets the value of the registeredAgents property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getRegisteredAgents() {
        return registeredAgents;
    }

    /**
     * Sets the value of the registeredAgents property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setRegisteredAgents(BigInteger value) {
        this.registeredAgents = value;
    }

    /**
     * Gets the value of the supervisors property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSupervisors() {
        return supervisors;
    }

    /**
     * Sets the value of the supervisors property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSupervisors(BigInteger value) {
        this.supervisors = value;
    }

}


package com._4psa.clientmessages_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
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
 *         &lt;choice>
 *           &lt;element name="resellerID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *           &lt;element name="resellerIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *         &lt;/choice>
 *         &lt;choice>
 *           &lt;element name="chargingPlanID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *           &lt;element name="chargingPlanIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;/choice>
 *         &lt;choice>
 *           &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" maxOccurs="unbounded"/>
 *           &lt;element name="identifier" type="{http://4psa.com/Common.xsd/2.5.1}string" maxOccurs="unbounded"/>
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
    "resellerID",
    "resellerIdentifier",
    "chargingPlanID",
    "chargingPlanIdentifier",
    "id",
    "identifier"
})
@XmlRootElement(name = "MoveClientsRequest")
public class MoveClientsRequest {

    protected BigInteger resellerID;
    protected String resellerIdentifier;
    protected BigInteger chargingPlanID;
    protected String chargingPlanIdentifier;
    @XmlElement(name = "ID")
    protected List<BigInteger> id;
    protected List<String> identifier;

    /**
     * Gets the value of the resellerID property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getResellerID() {
        return resellerID;
    }

    /**
     * Sets the value of the resellerID property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setResellerID(BigInteger value) {
        this.resellerID = value;
    }

    /**
     * Gets the value of the resellerIdentifier property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getResellerIdentifier() {
        return resellerIdentifier;
    }

    /**
     * Sets the value of the resellerIdentifier property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setResellerIdentifier(String value) {
        this.resellerIdentifier = value;
    }

    /**
     * Gets the value of the chargingPlanID property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getChargingPlanID() {
        return chargingPlanID;
    }

    /**
     * Sets the value of the chargingPlanID property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setChargingPlanID(BigInteger value) {
        this.chargingPlanID = value;
    }

    /**
     * Gets the value of the chargingPlanIdentifier property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getChargingPlanIdentifier() {
        return chargingPlanIdentifier;
    }

    /**
     * Sets the value of the chargingPlanIdentifier property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setChargingPlanIdentifier(String value) {
        this.chargingPlanIdentifier = value;
    }

    /**
     * Gets the value of the id property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the id property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getID().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BigInteger }
     *
     *
     */
    public List<BigInteger> getID() {
        if (id == null) {
            id = new ArrayList<BigInteger>();
        }
        return this.id;
    }

    /**
     * Gets the value of the identifier property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the identifier property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIdentifier().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     *
     *
     */
    public List<String> getIdentifier() {
        if (identifier == null) {
            identifier = new ArrayList<String>();
        }
        return this.identifier;
    }

}

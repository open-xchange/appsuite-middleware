
package com._4psa.channeldata_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.channelmessages_xsd._2_5.EditPublicNoRequest;


/**
 * Public phone number data
 * 
 * <p>Java class for PublicNoInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PublicNoInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="phoneNo" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *         &lt;element name="did" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *         &lt;element name="location" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="cost" type="{http://4psa.com/Common.xsd/2.5.1}float" minOccurs="0"/>
 *         &lt;element name="incomingCost" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="cost" type="{http://4psa.com/Common.xsd/2.5.1}float" minOccurs="0"/>
 *                   &lt;element name="interval" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="type" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="exclusive"/>
 *               &lt;enumeration value="stacked"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
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
@XmlType(name = "PublicNoInfo", propOrder = {
    "phoneNo",
    "did",
    "location",
    "cost",
    "incomingCost",
    "type"
})
@XmlSeeAlso({
    com._4psa.channelmessages_xsd._2_5.AddPublicNoRequest.PublicNo.class,
    EditPublicNoRequest.class
})
public class PublicNoInfo {

    @XmlElement(required = true)
    protected String phoneNo;
    @XmlElement(required = true)
    protected String did;
    protected String location;
    protected Float cost;
    protected PublicNoInfo.IncomingCost incomingCost;
    @XmlElement(defaultValue = "exclusive")
    protected String type;

    /**
     * Gets the value of the phoneNo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPhoneNo() {
        return phoneNo;
    }

    /**
     * Sets the value of the phoneNo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPhoneNo(String value) {
        this.phoneNo = value;
    }

    /**
     * Gets the value of the did property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDid() {
        return did;
    }

    /**
     * Sets the value of the did property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDid(String value) {
        this.did = value;
    }

    /**
     * Gets the value of the location property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocation(String value) {
        this.location = value;
    }

    /**
     * Gets the value of the cost property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getCost() {
        return cost;
    }

    /**
     * Sets the value of the cost property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setCost(Float value) {
        this.cost = value;
    }

    /**
     * Gets the value of the incomingCost property.
     * 
     * @return
     *     possible object is
     *     {@link PublicNoInfo.IncomingCost }
     *     
     */
    public PublicNoInfo.IncomingCost getIncomingCost() {
        return incomingCost;
    }

    /**
     * Sets the value of the incomingCost property.
     * 
     * @param value
     *     allowed object is
     *     {@link PublicNoInfo.IncomingCost }
     *     
     */
    public void setIncomingCost(PublicNoInfo.IncomingCost value) {
        this.incomingCost = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
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
     *         &lt;element name="cost" type="{http://4psa.com/Common.xsd/2.5.1}float" minOccurs="0"/>
     *         &lt;element name="interval" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
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
        "cost",
        "interval"
    })
    public static class IncomingCost {

        protected Float cost;
        protected BigInteger interval;

        /**
         * Gets the value of the cost property.
         * 
         * @return
         *     possible object is
         *     {@link Float }
         *     
         */
        public Float getCost() {
            return cost;
        }

        /**
         * Sets the value of the cost property.
         * 
         * @param value
         *     allowed object is
         *     {@link Float }
         *     
         */
        public void setCost(Float value) {
            this.cost = value;
        }

        /**
         * Gets the value of the interval property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getInterval() {
            return interval;
        }

        /**
         * Sets the value of the interval property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setInterval(BigInteger value) {
            this.interval = value;
        }

    }

}

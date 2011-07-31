
package com._4psa.channelmessages_xsd._2_5;

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
 *         &lt;element name="channelID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *         &lt;element name="inUse" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="type" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="exclusive"/>
 *               &lt;enumeration value="stacked"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="filter" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="userID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *           &lt;element name="userIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
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
    "channelID",
    "inUse",
    "type",
    "filter",
    "userID",
    "userIdentifier"
})
@XmlRootElement(name = "GetPublicNoRequest")
public class GetPublicNoRequest {

    @XmlElement(required = true)
    protected BigInteger channelID;
    @XmlElement(defaultValue = "0")
    protected Boolean inUse;
    protected String type;
    protected String filter;
    protected BigInteger userID;
    protected String userIdentifier;

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
     * Gets the value of the inUse property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isInUse() {
        return inUse;
    }

    /**
     * Sets the value of the inUse property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setInUse(Boolean value) {
        this.inUse = value;
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
     * Gets the value of the filter property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Sets the value of the filter property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFilter(String value) {
        this.filter = value;
    }

    /**
     * Gets the value of the userID property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getUserID() {
        return userID;
    }

    /**
     * Sets the value of the userID property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setUserID(BigInteger value) {
        this.userID = value;
    }

    /**
     * Gets the value of the userIdentifier property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserIdentifier() {
        return userIdentifier;
    }

    /**
     * Sets the value of the userIdentifier property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserIdentifier(String value) {
        this.userIdentifier = value;
    }

}

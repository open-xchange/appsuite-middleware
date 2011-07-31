
package com._4psa.pbxmessages_xsd._2_5;

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
 *         &lt;choice>
 *           &lt;element name="userID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *           &lt;element name="userIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;/choice>
 *         &lt;element name="displayLevel" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="admin"/>
 *               &lt;enumeration value="reseller"/>
 *               &lt;enumeration value="client"/>
 *               &lt;enumeration value="extension"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="filter" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
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
    "userID",
    "userIdentifier",
    "displayLevel",
    "filter"
})
@XmlRootElement(name = "GetCustomAlertsRequest")
public class GetCustomAlertsRequest {

    protected BigInteger userID;
    protected String userIdentifier;
    @XmlElement(defaultValue = "admin")
    protected String displayLevel;
    protected String filter;

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

    /**
     * Gets the value of the displayLevel property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDisplayLevel() {
        return displayLevel;
    }

    /**
     * Sets the value of the displayLevel property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDisplayLevel(String value) {
        this.displayLevel = value;
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

}

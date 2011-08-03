
package com._4psa.pbxdata_xsd._2_5;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.pbxmessages_xsd._2_5.AddCustomButtonRequest;
import com._4psa.pbxmessages_xsd._2_5.EditCustomButtonRequest;


/**
 * Custom button data
 *
 * <p>Java class for CustomButton complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="CustomButton">
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/PBXData.xsd/2.5.1}CustomAlert">
 *       &lt;sequence>
 *         &lt;element name="URL" type="{http://4psa.com/Common.xsd/2.5.1}domain" minOccurs="0"/>
 *         &lt;element name="location" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="navigation"/>
 *               &lt;enumeration value="content"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="action" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="newWindow"/>
 *               &lt;enumeration value="currentWindow"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="includeResellerID" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="includeClientID" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="includeExtensionID" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="includeExtensionNo" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="includeExtensionPublicNo" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="includeChargingPlanID" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CustomButton", propOrder = {
    "url",
    "location",
    "action",
    "includeResellerID",
    "includeClientID",
    "includeExtensionID",
    "includeExtensionNo",
    "includeExtensionPublicNo",
    "includeChargingPlanID"
})
@XmlSeeAlso({
    com._4psa.pbxmessagesinfo_xsd._2_5.GetCustomButtonsResponseType.Button.class,
    AddCustomButtonRequest.class,
    EditCustomButtonRequest.class
})
public class CustomButton
    extends CustomAlert
{

    @XmlElement(name = "URL")
    protected String url;
    @XmlElement(defaultValue = "navigation")
    protected String location;
    protected String action;
    protected Boolean includeResellerID;
    protected Boolean includeClientID;
    protected Boolean includeExtensionID;
    protected Boolean includeExtensionNo;
    protected Boolean includeExtensionPublicNo;
    protected Boolean includeChargingPlanID;

    /**
     * Gets the value of the url property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getURL() {
        return url;
    }

    /**
     * Sets the value of the url property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setURL(String value) {
        this.url = value;
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
     * Gets the value of the includeResellerID property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isIncludeResellerID() {
        return includeResellerID;
    }

    /**
     * Sets the value of the includeResellerID property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setIncludeResellerID(Boolean value) {
        this.includeResellerID = value;
    }

    /**
     * Gets the value of the includeClientID property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isIncludeClientID() {
        return includeClientID;
    }

    /**
     * Sets the value of the includeClientID property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setIncludeClientID(Boolean value) {
        this.includeClientID = value;
    }

    /**
     * Gets the value of the includeExtensionID property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isIncludeExtensionID() {
        return includeExtensionID;
    }

    /**
     * Sets the value of the includeExtensionID property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setIncludeExtensionID(Boolean value) {
        this.includeExtensionID = value;
    }

    /**
     * Gets the value of the includeExtensionNo property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isIncludeExtensionNo() {
        return includeExtensionNo;
    }

    /**
     * Sets the value of the includeExtensionNo property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setIncludeExtensionNo(Boolean value) {
        this.includeExtensionNo = value;
    }

    /**
     * Gets the value of the includeExtensionPublicNo property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isIncludeExtensionPublicNo() {
        return includeExtensionPublicNo;
    }

    /**
     * Sets the value of the includeExtensionPublicNo property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setIncludeExtensionPublicNo(Boolean value) {
        this.includeExtensionPublicNo = value;
    }

    /**
     * Gets the value of the includeChargingPlanID property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isIncludeChargingPlanID() {
        return includeChargingPlanID;
    }

    /**
     * Sets the value of the includeChargingPlanID property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setIncludeChargingPlanID(Boolean value) {
        this.includeChargingPlanID = value;
    }

}

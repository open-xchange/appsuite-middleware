
package com._4psa.extensionmessagesinfo_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.extensiondata_xsd._2_5.ExtendedExtensionInfo;
import com._4psa.extensionmessages_xsd._2_5.AddExtensionResponse;


/**
 * Get detailed extension data: response type
 *
 * <p>Java class for GetExtensionDetailsResponseType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="GetExtensionDetailsResponseType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}ExtendedExtensionInfo">
 *       &lt;sequence>
 *         &lt;element name="extensionType" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="term"/>
 *               &lt;enumeration value="queue"/>
 *               &lt;enumeration value="ivr"/>
 *               &lt;enumeration value="voicecenter"/>
 *               &lt;enumeration value="conference"/>
 *               &lt;enumeration value="callback"/>
 *               &lt;enumeration value="callcard"/>
 *               &lt;enumeration value="intercom"/>
 *               &lt;enumeration value="queuecenter"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="extensionNo" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="chargingPlanID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;element name="chargingPlanIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetExtensionDetailsResponseType", propOrder = {
    "extensionType",
    "extensionNo",
    "chargingPlanID",
    "chargingPlanIdentifier"
})
@XmlSeeAlso({
    AddExtensionResponse.class
})
public class GetExtensionDetailsResponseType
    extends ExtendedExtensionInfo
{

    @XmlElement(defaultValue = "term")
    protected String extensionType;
    protected String extensionNo;
    protected BigInteger chargingPlanID;
    protected String chargingPlanIdentifier;

    /**
     * Gets the value of the extensionType property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getExtensionType() {
        return extensionType;
    }

    /**
     * Sets the value of the extensionType property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setExtensionType(String value) {
        this.extensionType = value;
    }

    /**
     * Gets the value of the extensionNo property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getExtensionNo() {
        return extensionNo;
    }

    /**
     * Sets the value of the extensionNo property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setExtensionNo(String value) {
        this.extensionNo = value;
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

}

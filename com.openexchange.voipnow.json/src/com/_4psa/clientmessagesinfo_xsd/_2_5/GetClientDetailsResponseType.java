
package com._4psa.clientmessagesinfo_xsd._2_5;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.clientdata_xsd._2_5.ExtendedClientInfo;
import com._4psa.clientmessages_xsd._2_5.AddClientResponse;
import com._4psa.clientmessages_xsd._2_5.GetClientDetailsResponse;
import com._4psa.common_xsd._2_5.AdvertisingTemplate;


/**
 * Get client account details: response type
 * 
 * <p>Java class for GetClientDetailsResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetClientDetailsResponseType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/ClientData.xsd/2.5.1}ExtendedClientInfo">
 *       &lt;sequence>
 *         &lt;element name="advertisingTemplate" type="{http://4psa.com/Common.xsd/2.5.1}advertisingTemplate" minOccurs="0"/>
 *         &lt;element name="industry" type="{http://4psa.com/Common.xsd/2.5.1}rule" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetClientDetailsResponseType", propOrder = {
    "advertisingTemplate",
    "industry"
})
@XmlSeeAlso({
    AddClientResponse.class,
    GetClientDetailsResponse.class
})
public class GetClientDetailsResponseType
    extends ExtendedClientInfo
{

    protected AdvertisingTemplate advertisingTemplate;
    @XmlElement(defaultValue = "0")
    protected String industry;

    /**
     * Gets the value of the advertisingTemplate property.
     * 
     * @return
     *     possible object is
     *     {@link AdvertisingTemplate }
     *     
     */
    public AdvertisingTemplate getAdvertisingTemplate() {
        return advertisingTemplate;
    }

    /**
     * Sets the value of the advertisingTemplate property.
     * 
     * @param value
     *     allowed object is
     *     {@link AdvertisingTemplate }
     *     
     */
    public void setAdvertisingTemplate(AdvertisingTemplate value) {
        this.advertisingTemplate = value;
    }

    /**
     * Gets the value of the industry property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIndustry() {
        return industry;
    }

    /**
     * Sets the value of the industry property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIndustry(String value) {
        this.industry = value;
    }

}

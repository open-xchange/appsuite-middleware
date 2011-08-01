
package com._4psa.pbxmessages_xsd._2_5;

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
 *         &lt;element name="activationCode" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
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
    "activationCode"
})
@XmlRootElement(name = "UpdateLicenseRequest")
public class UpdateLicenseRequest {

    @XmlElement(required = true)
    protected String activationCode;

    /**
     * Gets the value of the activationCode property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getActivationCode() {
        return activationCode;
    }

    /**
     * Sets the value of the activationCode property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setActivationCode(String value) {
        this.activationCode = value;
    }

}

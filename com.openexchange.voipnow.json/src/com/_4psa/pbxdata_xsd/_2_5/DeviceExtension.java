
package com._4psa.pbxdata_xsd._2_5;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * Phone provisioning data
 * 
 * <p>Java class for DeviceExtension complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DeviceExtension">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="extensionID" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
 *         &lt;element name="line" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeviceExtension", propOrder = {
    "extensionID",
    "line"
})
@XmlSeeAlso({
    com._4psa.pbxmessagesinfo_xsd._2_5.GetDevicesResponseType.Devices.AssignedExtensions.class
})
public class DeviceExtension {

    protected Object extensionID;
    protected Object line;

    /**
     * Gets the value of the extensionID property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getExtensionID() {
        return extensionID;
    }

    /**
     * Sets the value of the extensionID property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setExtensionID(Object value) {
        this.extensionID = value;
    }

    /**
     * Gets the value of the line property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getLine() {
        return line;
    }

    /**
     * Sets the value of the line property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setLine(Object value) {
        this.line = value;
    }

}


package com._4psa.extensiondata_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * IVR extension data
 * 
 * <p>Java class for IVR complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="IVR">
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}PhoneCallerIDInfo">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="timeout" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="mohFolder" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="lifetime" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="lifetimeExpire" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="hangup"/>
 *               &lt;enumeration value="transfer"/>
 *               &lt;enumeration value="play"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="transferExtension" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;element name="sound" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IVR", propOrder = {
    "name",
    "description",
    "timeout",
    "mohFolder",
    "lifetime",
    "lifetimeExpire",
    "transferExtension",
    "sound"
})
@XmlSeeAlso({
    com._4psa.extensionmessages_xsd._2_5.GetExtensionSettingsResponse.IVR.class,
    com._4psa.extensionmessages_xsd._2_5.SetupExtensionRequest.IVR.class
})
public class IVR
    extends PhoneCallerIDInfo
{

    protected String name;
    protected String description;
    protected BigInteger timeout;
    @XmlElementRef(name = "mohFolder", namespace = "http://4psa.com/ExtensionData.xsd/2.5.1", type = JAXBElement.class)
    protected JAXBElement<String> mohFolder;
    protected BigInteger lifetime;
    @XmlElement(defaultValue = "hangup")
    protected String lifetimeExpire;
    protected BigInteger transferExtension;
    protected String sound;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the timeout property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTimeout() {
        return timeout;
    }

    /**
     * Sets the value of the timeout property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTimeout(BigInteger value) {
        this.timeout = value;
    }

    /**
     * Gets the value of the mohFolder property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getMohFolder() {
        return mohFolder;
    }

    /**
     * Sets the value of the mohFolder property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setMohFolder(JAXBElement<String> value) {
        this.mohFolder = value;
    }

    /**
     * Gets the value of the lifetime property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getLifetime() {
        return lifetime;
    }

    /**
     * Sets the value of the lifetime property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setLifetime(BigInteger value) {
        this.lifetime = value;
    }

    /**
     * Gets the value of the lifetimeExpire property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLifetimeExpire() {
        return lifetimeExpire;
    }

    /**
     * Sets the value of the lifetimeExpire property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLifetimeExpire(String value) {
        this.lifetimeExpire = value;
    }

    /**
     * Gets the value of the transferExtension property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTransferExtension() {
        return transferExtension;
    }

    /**
     * Sets the value of the transferExtension property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTransferExtension(BigInteger value) {
        this.transferExtension = value;
    }

    /**
     * Gets the value of the sound property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSound() {
        return sound;
    }

    /**
     * Sets the value of the sound property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSound(String value) {
        this.sound = value;
    }

}

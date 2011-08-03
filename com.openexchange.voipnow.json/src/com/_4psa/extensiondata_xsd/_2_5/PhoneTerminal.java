
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
 * Phone terminal extension data
 *
 * <p>Java class for PhoneTerminal complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="PhoneTerminal">
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}PhoneCallerIDInfo">
 *       &lt;sequence>
 *         &lt;element name="mohFolder" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="noAnswer" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="parkTimeout" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="IM" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="callWaiting" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;sequence>
 *           &lt;element name="dnd" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *           &lt;element name="dndSndActive" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *           &lt;element name="dndSnd" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;/sequence>
 *         &lt;sequence>
 *           &lt;element name="phoneAccess" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *           &lt;element name="block" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *           &lt;element name="phoneAccessPassword" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;/sequence>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PhoneTerminal", propOrder = {
    "mohFolder",
    "noAnswer",
    "parkTimeout",
    "im",
    "callWaiting",
    "dnd",
    "dndSndActive",
    "dndSnd",
    "phoneAccess",
    "block",
    "phoneAccessPassword"
})
@XmlSeeAlso({
    ExtendedPhoneTerminal.class
})
public class PhoneTerminal
    extends PhoneCallerIDInfo
{

    @XmlElementRef(name = "mohFolder", namespace = "http://4psa.com/ExtensionData.xsd/2.5.1", type = JAXBElement.class)
    protected JAXBElement<String> mohFolder;
    protected BigInteger noAnswer;
    @XmlElement(defaultValue = "180")
    protected BigInteger parkTimeout;
    @XmlElement(name = "IM")
    protected Boolean im;
    protected Boolean callWaiting;
    protected Boolean dnd;
    protected Boolean dndSndActive;
    protected String dndSnd;
    protected Boolean phoneAccess;
    protected Boolean block;
    @XmlElement(defaultValue = "")
    protected String phoneAccessPassword;

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
     * Gets the value of the noAnswer property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getNoAnswer() {
        return noAnswer;
    }

    /**
     * Sets the value of the noAnswer property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setNoAnswer(BigInteger value) {
        this.noAnswer = value;
    }

    /**
     * Gets the value of the parkTimeout property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getParkTimeout() {
        return parkTimeout;
    }

    /**
     * Sets the value of the parkTimeout property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setParkTimeout(BigInteger value) {
        this.parkTimeout = value;
    }

    /**
     * Gets the value of the im property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isIM() {
        return im;
    }

    /**
     * Sets the value of the im property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setIM(Boolean value) {
        this.im = value;
    }

    /**
     * Gets the value of the callWaiting property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isCallWaiting() {
        return callWaiting;
    }

    /**
     * Sets the value of the callWaiting property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setCallWaiting(Boolean value) {
        this.callWaiting = value;
    }

    /**
     * Gets the value of the dnd property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isDnd() {
        return dnd;
    }

    /**
     * Sets the value of the dnd property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setDnd(Boolean value) {
        this.dnd = value;
    }

    /**
     * Gets the value of the dndSndActive property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isDndSndActive() {
        return dndSndActive;
    }

    /**
     * Sets the value of the dndSndActive property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setDndSndActive(Boolean value) {
        this.dndSndActive = value;
    }

    /**
     * Gets the value of the dndSnd property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDndSnd() {
        return dndSnd;
    }

    /**
     * Sets the value of the dndSnd property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDndSnd(String value) {
        this.dndSnd = value;
    }

    /**
     * Gets the value of the phoneAccess property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isPhoneAccess() {
        return phoneAccess;
    }

    /**
     * Sets the value of the phoneAccess property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setPhoneAccess(Boolean value) {
        this.phoneAccess = value;
    }

    /**
     * Gets the value of the block property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isBlock() {
        return block;
    }

    /**
     * Sets the value of the block property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setBlock(Boolean value) {
        this.block = value;
    }

    /**
     * Gets the value of the phoneAccessPassword property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPhoneAccessPassword() {
        return phoneAccessPassword;
    }

    /**
     * Sets the value of the phoneAccessPassword property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPhoneAccessPassword(String value) {
        this.phoneAccessPassword = value;
    }

}

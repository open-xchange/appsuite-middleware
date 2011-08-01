
package com._4psa.extensiondata_xsd._2_5;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Extended phone terminal extension data
 *
 * <p>Java class for ExtendedPhoneTerminal complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ExtendedPhoneTerminal">
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}PhoneTerminal">
 *       &lt;sequence>
 *         &lt;element name="terminal" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="state" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="registration" type="{http://4psa.com/Common.xsd/2.5.1}dateTime" minOccurs="0"/>
 *         &lt;element name="IP" type="{http://4psa.com/Common.xsd/2.5.1}ip" minOccurs="0"/>
 *         &lt;element name="IMLogin" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="voicemailMsgQuota" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="faxMsgQuota" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="musicFilesQuota" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="soundFilesQuota" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="recordedMsgQuota" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExtendedPhoneTerminal", propOrder = {
    "terminal",
    "state",
    "registration",
    "ip",
    "imLogin",
    "voicemailMsgQuota",
    "faxMsgQuota",
    "musicFilesQuota",
    "soundFilesQuota",
    "recordedMsgQuota"
})
public class ExtendedPhoneTerminal
    extends PhoneTerminal
{

    protected String terminal;
    protected String state;
    protected XMLGregorianCalendar registration;
    @XmlElement(name = "IP")
    protected String ip;
    @XmlElement(name = "IMLogin")
    protected String imLogin;
    protected String voicemailMsgQuota;
    protected String faxMsgQuota;
    protected String musicFilesQuota;
    protected String soundFilesQuota;
    protected String recordedMsgQuota;

    /**
     * Gets the value of the terminal property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTerminal() {
        return terminal;
    }

    /**
     * Sets the value of the terminal property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTerminal(String value) {
        this.terminal = value;
    }

    /**
     * Gets the value of the state property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the value of the state property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setState(String value) {
        this.state = value;
    }

    /**
     * Gets the value of the registration property.
     *
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public XMLGregorianCalendar getRegistration() {
        return registration;
    }

    /**
     * Sets the value of the registration property.
     *
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public void setRegistration(XMLGregorianCalendar value) {
        this.registration = value;
    }

    /**
     * Gets the value of the ip property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getIP() {
        return ip;
    }

    /**
     * Sets the value of the ip property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setIP(String value) {
        this.ip = value;
    }

    /**
     * Gets the value of the imLogin property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getIMLogin() {
        return imLogin;
    }

    /**
     * Sets the value of the imLogin property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setIMLogin(String value) {
        this.imLogin = value;
    }

    /**
     * Gets the value of the voicemailMsgQuota property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getVoicemailMsgQuota() {
        return voicemailMsgQuota;
    }

    /**
     * Sets the value of the voicemailMsgQuota property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setVoicemailMsgQuota(String value) {
        this.voicemailMsgQuota = value;
    }

    /**
     * Gets the value of the faxMsgQuota property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFaxMsgQuota() {
        return faxMsgQuota;
    }

    /**
     * Sets the value of the faxMsgQuota property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFaxMsgQuota(String value) {
        this.faxMsgQuota = value;
    }

    /**
     * Gets the value of the musicFilesQuota property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMusicFilesQuota() {
        return musicFilesQuota;
    }

    /**
     * Sets the value of the musicFilesQuota property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMusicFilesQuota(String value) {
        this.musicFilesQuota = value;
    }

    /**
     * Gets the value of the soundFilesQuota property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSoundFilesQuota() {
        return soundFilesQuota;
    }

    /**
     * Sets the value of the soundFilesQuota property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSoundFilesQuota(String value) {
        this.soundFilesQuota = value;
    }

    /**
     * Gets the value of the recordedMsgQuota property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getRecordedMsgQuota() {
        return recordedMsgQuota;
    }

    /**
     * Sets the value of the recordedMsgQuota property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setRecordedMsgQuota(String value) {
        this.recordedMsgQuota = value;
    }

}


package com._4psa.extensionmessages_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com._4psa.extensiondata_xsd._2_5.ConferenceInfo;
import com._4psa.extensiondata_xsd._2_5.ExtendedPhoneTerminal;
import com._4psa.extensiondata_xsd._2_5.ExtendedQueue;
import com._4psa.extensiondata_xsd._2_5.QueueCenterInfo;
import com._4psa.extensiondata_xsd._2_5.VoicemailInfo;


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
 *         &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
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
 *         &lt;choice>
 *           &lt;element name="phoneTerminal" type="{http://4psa.com/ExtensionData.xsd/2.5.1}ExtendedPhoneTerminal" minOccurs="0"/>
 *           &lt;element name="queue" type="{http://4psa.com/ExtensionData.xsd/2.5.1}ExtendedQueue" minOccurs="0"/>
 *           &lt;element name="IVR" minOccurs="0">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}IVR">
 *                   &lt;sequence>
 *                     &lt;element name="cloneID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *                   &lt;/sequence>
 *                 &lt;/extension>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *           &lt;element name="voicemailCenter" type="{http://4psa.com/ExtensionData.xsd/2.5.1}VoicemailInfo" minOccurs="0"/>
 *           &lt;element name="queueCenter" type="{http://4psa.com/ExtensionData.xsd/2.5.1}QueueCenterInfo" minOccurs="0"/>
 *           &lt;element name="conference" type="{http://4psa.com/ExtensionData.xsd/2.5.1}ConferenceInfo" minOccurs="0"/>
 *           &lt;element name="callback" type="{http://www.w3.org/2001/XMLSchema}anyType" maxOccurs="0" minOccurs="0"/>
 *           &lt;element name="callingCard" type="{http://www.w3.org/2001/XMLSchema}anyType" maxOccurs="0" minOccurs="0"/>
 *           &lt;element name="intercomPaging" type="{http://www.w3.org/2001/XMLSchema}anyType" maxOccurs="0" minOccurs="0"/>
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
    "id",
    "extensionType",
    "phoneTerminal",
    "queue",
    "ivr",
    "voicemailCenter",
    "queueCenter",
    "conference"
})
@XmlRootElement(name = "GetExtensionSettingsResponse")
public class GetExtensionSettingsResponse {

    @XmlElement(name = "ID", required = true)
    protected BigInteger id;
    @XmlElement(defaultValue = "term")
    protected String extensionType;
    protected ExtendedPhoneTerminal phoneTerminal;
    protected ExtendedQueue queue;
    @XmlElement(name = "IVR")
    protected GetExtensionSettingsResponse.IVR ivr;
    protected VoicemailInfo voicemailCenter;
    protected QueueCenterInfo queueCenter;
    protected ConferenceInfo conference;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getID() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setID(BigInteger value) {
        this.id = value;
    }

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
     * Gets the value of the phoneTerminal property.
     * 
     * @return
     *     possible object is
     *     {@link ExtendedPhoneTerminal }
     *     
     */
    public ExtendedPhoneTerminal getPhoneTerminal() {
        return phoneTerminal;
    }

    /**
     * Sets the value of the phoneTerminal property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExtendedPhoneTerminal }
     *     
     */
    public void setPhoneTerminal(ExtendedPhoneTerminal value) {
        this.phoneTerminal = value;
    }

    /**
     * Gets the value of the queue property.
     * 
     * @return
     *     possible object is
     *     {@link ExtendedQueue }
     *     
     */
    public ExtendedQueue getQueue() {
        return queue;
    }

    /**
     * Sets the value of the queue property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExtendedQueue }
     *     
     */
    public void setQueue(ExtendedQueue value) {
        this.queue = value;
    }

    /**
     * Gets the value of the ivr property.
     * 
     * @return
     *     possible object is
     *     {@link GetExtensionSettingsResponse.IVR }
     *     
     */
    public GetExtensionSettingsResponse.IVR getIVR() {
        return ivr;
    }

    /**
     * Sets the value of the ivr property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetExtensionSettingsResponse.IVR }
     *     
     */
    public void setIVR(GetExtensionSettingsResponse.IVR value) {
        this.ivr = value;
    }

    /**
     * Gets the value of the voicemailCenter property.
     * 
     * @return
     *     possible object is
     *     {@link VoicemailInfo }
     *     
     */
    public VoicemailInfo getVoicemailCenter() {
        return voicemailCenter;
    }

    /**
     * Sets the value of the voicemailCenter property.
     * 
     * @param value
     *     allowed object is
     *     {@link VoicemailInfo }
     *     
     */
    public void setVoicemailCenter(VoicemailInfo value) {
        this.voicemailCenter = value;
    }

    /**
     * Gets the value of the queueCenter property.
     * 
     * @return
     *     possible object is
     *     {@link QueueCenterInfo }
     *     
     */
    public QueueCenterInfo getQueueCenter() {
        return queueCenter;
    }

    /**
     * Sets the value of the queueCenter property.
     * 
     * @param value
     *     allowed object is
     *     {@link QueueCenterInfo }
     *     
     */
    public void setQueueCenter(QueueCenterInfo value) {
        this.queueCenter = value;
    }

    /**
     * Gets the value of the conference property.
     * 
     * @return
     *     possible object is
     *     {@link ConferenceInfo }
     *     
     */
    public ConferenceInfo getConference() {
        return conference;
    }

    /**
     * Sets the value of the conference property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConferenceInfo }
     *     
     */
    public void setConference(ConferenceInfo value) {
        this.conference = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}IVR">
     *       &lt;sequence>
     *         &lt;element name="cloneID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "cloneID"
    })
    public static class IVR
        extends com._4psa.extensiondata_xsd._2_5.IVR
    {

        protected BigInteger cloneID;

        /**
         * Gets the value of the cloneID property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getCloneID() {
            return cloneID;
        }

        /**
         * Sets the value of the cloneID property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setCloneID(BigInteger value) {
            this.cloneID = value;
        }

    }

}

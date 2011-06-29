
package com._4psa.reportdata_xsd._2_5;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import com._4psa.common_xsd._2_5.DiskSpace;
import com._4psa.reportmessagesinfo_xsd._2_5.QuickStatsResponseType;


/**
 * System statistics data
 * 
 * <p>Java class for QuickStats complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QuickStats">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="resellers" type="{http://4psa.com/ReportData.xsd/2.5.1}UserStatistics" minOccurs="0"/>
 *         &lt;element name="clients" type="{http://4psa.com/ReportData.xsd/2.5.1}UserStatistics" minOccurs="0"/>
 *         &lt;element name="extensions" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="phoneTerminal" type="{http://4psa.com/ReportData.xsd/2.5.1}UserStatistics" minOccurs="0"/>
 *                   &lt;element name="queue" type="{http://4psa.com/ReportData.xsd/2.5.1}UserStatistics" minOccurs="0"/>
 *                   &lt;element name="queueCenter" type="{http://4psa.com/ReportData.xsd/2.5.1}UserStatistics" minOccurs="0"/>
 *                   &lt;element name="IVR" type="{http://4psa.com/ReportData.xsd/2.5.1}UserStatistics" minOccurs="0"/>
 *                   &lt;element name="voicemailCenter" type="{http://4psa.com/ReportData.xsd/2.5.1}UserStatistics" minOccurs="0"/>
 *                   &lt;element name="conference" type="{http://4psa.com/ReportData.xsd/2.5.1}UserStatistics" minOccurs="0"/>
 *                   &lt;element name="callback" type="{http://4psa.com/ReportData.xsd/2.5.1}UserStatistics" minOccurs="0"/>
 *                   &lt;element name="callingCard" type="{http://4psa.com/ReportData.xsd/2.5.1}UserStatistics" minOccurs="0"/>
 *                   &lt;element name="intercomPaging" type="{http://4psa.com/ReportData.xsd/2.5.1}UserStatistics" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="incomingCalls" type="{http://4psa.com/ReportData.xsd/2.5.1}CallStatistics" minOccurs="0"/>
 *         &lt;element name="outgoingCalls" type="{http://4psa.com/ReportData.xsd/2.5.1}CallStatistics" minOccurs="0"/>
 *         &lt;element name="currency" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="lastLogin" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="soundFilesQuota" type="{http://4psa.com/Common.xsd/2.5.1}diskSpace" minOccurs="0"/>
 *         &lt;element name="musicFilesQuota" type="{http://4psa.com/Common.xsd/2.5.1}diskSpace" minOccurs="0"/>
 *         &lt;element name="faxMsgQuota" type="{http://4psa.com/Common.xsd/2.5.1}diskSpace" minOccurs="0"/>
 *         &lt;element name="voicemailMsgQuota" type="{http://4psa.com/Common.xsd/2.5.1}diskSpace" minOccurs="0"/>
 *         &lt;element name="recordedMsgQuota" type="{http://4psa.com/Common.xsd/2.5.1}diskSpace" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuickStats", propOrder = {
    "resellers",
    "clients",
    "extensions",
    "incomingCalls",
    "outgoingCalls",
    "currency",
    "lastLogin",
    "soundFilesQuota",
    "musicFilesQuota",
    "faxMsgQuota",
    "voicemailMsgQuota",
    "recordedMsgQuota"
})
@XmlSeeAlso({
    QuickStatsResponseType.class
})
public class QuickStats {

    protected UserStatistics resellers;
    protected UserStatistics clients;
    protected QuickStats.Extensions extensions;
    protected CallStatistics incomingCalls;
    protected CallStatistics outgoingCalls;
    protected String currency;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastLogin;
    protected DiskSpace soundFilesQuota;
    protected DiskSpace musicFilesQuota;
    protected DiskSpace faxMsgQuota;
    protected DiskSpace voicemailMsgQuota;
    protected DiskSpace recordedMsgQuota;

    /**
     * Gets the value of the resellers property.
     * 
     * @return
     *     possible object is
     *     {@link UserStatistics }
     *     
     */
    public UserStatistics getResellers() {
        return resellers;
    }

    /**
     * Sets the value of the resellers property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserStatistics }
     *     
     */
    public void setResellers(UserStatistics value) {
        this.resellers = value;
    }

    /**
     * Gets the value of the clients property.
     * 
     * @return
     *     possible object is
     *     {@link UserStatistics }
     *     
     */
    public UserStatistics getClients() {
        return clients;
    }

    /**
     * Sets the value of the clients property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserStatistics }
     *     
     */
    public void setClients(UserStatistics value) {
        this.clients = value;
    }

    /**
     * Gets the value of the extensions property.
     * 
     * @return
     *     possible object is
     *     {@link QuickStats.Extensions }
     *     
     */
    public QuickStats.Extensions getExtensions() {
        return extensions;
    }

    /**
     * Sets the value of the extensions property.
     * 
     * @param value
     *     allowed object is
     *     {@link QuickStats.Extensions }
     *     
     */
    public void setExtensions(QuickStats.Extensions value) {
        this.extensions = value;
    }

    /**
     * Gets the value of the incomingCalls property.
     * 
     * @return
     *     possible object is
     *     {@link CallStatistics }
     *     
     */
    public CallStatistics getIncomingCalls() {
        return incomingCalls;
    }

    /**
     * Sets the value of the incomingCalls property.
     * 
     * @param value
     *     allowed object is
     *     {@link CallStatistics }
     *     
     */
    public void setIncomingCalls(CallStatistics value) {
        this.incomingCalls = value;
    }

    /**
     * Gets the value of the outgoingCalls property.
     * 
     * @return
     *     possible object is
     *     {@link CallStatistics }
     *     
     */
    public CallStatistics getOutgoingCalls() {
        return outgoingCalls;
    }

    /**
     * Sets the value of the outgoingCalls property.
     * 
     * @param value
     *     allowed object is
     *     {@link CallStatistics }
     *     
     */
    public void setOutgoingCalls(CallStatistics value) {
        this.outgoingCalls = value;
    }

    /**
     * Gets the value of the currency property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Sets the value of the currency property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCurrency(String value) {
        this.currency = value;
    }

    /**
     * Gets the value of the lastLogin property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLastLogin() {
        return lastLogin;
    }

    /**
     * Sets the value of the lastLogin property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLastLogin(XMLGregorianCalendar value) {
        this.lastLogin = value;
    }

    /**
     * Gets the value of the soundFilesQuota property.
     * 
     * @return
     *     possible object is
     *     {@link DiskSpace }
     *     
     */
    public DiskSpace getSoundFilesQuota() {
        return soundFilesQuota;
    }

    /**
     * Sets the value of the soundFilesQuota property.
     * 
     * @param value
     *     allowed object is
     *     {@link DiskSpace }
     *     
     */
    public void setSoundFilesQuota(DiskSpace value) {
        this.soundFilesQuota = value;
    }

    /**
     * Gets the value of the musicFilesQuota property.
     * 
     * @return
     *     possible object is
     *     {@link DiskSpace }
     *     
     */
    public DiskSpace getMusicFilesQuota() {
        return musicFilesQuota;
    }

    /**
     * Sets the value of the musicFilesQuota property.
     * 
     * @param value
     *     allowed object is
     *     {@link DiskSpace }
     *     
     */
    public void setMusicFilesQuota(DiskSpace value) {
        this.musicFilesQuota = value;
    }

    /**
     * Gets the value of the faxMsgQuota property.
     * 
     * @return
     *     possible object is
     *     {@link DiskSpace }
     *     
     */
    public DiskSpace getFaxMsgQuota() {
        return faxMsgQuota;
    }

    /**
     * Sets the value of the faxMsgQuota property.
     * 
     * @param value
     *     allowed object is
     *     {@link DiskSpace }
     *     
     */
    public void setFaxMsgQuota(DiskSpace value) {
        this.faxMsgQuota = value;
    }

    /**
     * Gets the value of the voicemailMsgQuota property.
     * 
     * @return
     *     possible object is
     *     {@link DiskSpace }
     *     
     */
    public DiskSpace getVoicemailMsgQuota() {
        return voicemailMsgQuota;
    }

    /**
     * Sets the value of the voicemailMsgQuota property.
     * 
     * @param value
     *     allowed object is
     *     {@link DiskSpace }
     *     
     */
    public void setVoicemailMsgQuota(DiskSpace value) {
        this.voicemailMsgQuota = value;
    }

    /**
     * Gets the value of the recordedMsgQuota property.
     * 
     * @return
     *     possible object is
     *     {@link DiskSpace }
     *     
     */
    public DiskSpace getRecordedMsgQuota() {
        return recordedMsgQuota;
    }

    /**
     * Sets the value of the recordedMsgQuota property.
     * 
     * @param value
     *     allowed object is
     *     {@link DiskSpace }
     *     
     */
    public void setRecordedMsgQuota(DiskSpace value) {
        this.recordedMsgQuota = value;
    }


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
     *         &lt;element name="phoneTerminal" type="{http://4psa.com/ReportData.xsd/2.5.1}UserStatistics" minOccurs="0"/>
     *         &lt;element name="queue" type="{http://4psa.com/ReportData.xsd/2.5.1}UserStatistics" minOccurs="0"/>
     *         &lt;element name="queueCenter" type="{http://4psa.com/ReportData.xsd/2.5.1}UserStatistics" minOccurs="0"/>
     *         &lt;element name="IVR" type="{http://4psa.com/ReportData.xsd/2.5.1}UserStatistics" minOccurs="0"/>
     *         &lt;element name="voicemailCenter" type="{http://4psa.com/ReportData.xsd/2.5.1}UserStatistics" minOccurs="0"/>
     *         &lt;element name="conference" type="{http://4psa.com/ReportData.xsd/2.5.1}UserStatistics" minOccurs="0"/>
     *         &lt;element name="callback" type="{http://4psa.com/ReportData.xsd/2.5.1}UserStatistics" minOccurs="0"/>
     *         &lt;element name="callingCard" type="{http://4psa.com/ReportData.xsd/2.5.1}UserStatistics" minOccurs="0"/>
     *         &lt;element name="intercomPaging" type="{http://4psa.com/ReportData.xsd/2.5.1}UserStatistics" minOccurs="0"/>
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
        "phoneTerminal",
        "queue",
        "queueCenter",
        "ivr",
        "voicemailCenter",
        "conference",
        "callback",
        "callingCard",
        "intercomPaging"
    })
    public static class Extensions {

        protected UserStatistics phoneTerminal;
        protected UserStatistics queue;
        protected UserStatistics queueCenter;
        @XmlElement(name = "IVR")
        protected UserStatistics ivr;
        protected UserStatistics voicemailCenter;
        protected UserStatistics conference;
        protected UserStatistics callback;
        protected UserStatistics callingCard;
        protected UserStatistics intercomPaging;

        /**
         * Gets the value of the phoneTerminal property.
         * 
         * @return
         *     possible object is
         *     {@link UserStatistics }
         *     
         */
        public UserStatistics getPhoneTerminal() {
            return phoneTerminal;
        }

        /**
         * Sets the value of the phoneTerminal property.
         * 
         * @param value
         *     allowed object is
         *     {@link UserStatistics }
         *     
         */
        public void setPhoneTerminal(UserStatistics value) {
            this.phoneTerminal = value;
        }

        /**
         * Gets the value of the queue property.
         * 
         * @return
         *     possible object is
         *     {@link UserStatistics }
         *     
         */
        public UserStatistics getQueue() {
            return queue;
        }

        /**
         * Sets the value of the queue property.
         * 
         * @param value
         *     allowed object is
         *     {@link UserStatistics }
         *     
         */
        public void setQueue(UserStatistics value) {
            this.queue = value;
        }

        /**
         * Gets the value of the queueCenter property.
         * 
         * @return
         *     possible object is
         *     {@link UserStatistics }
         *     
         */
        public UserStatistics getQueueCenter() {
            return queueCenter;
        }

        /**
         * Sets the value of the queueCenter property.
         * 
         * @param value
         *     allowed object is
         *     {@link UserStatistics }
         *     
         */
        public void setQueueCenter(UserStatistics value) {
            this.queueCenter = value;
        }

        /**
         * Gets the value of the ivr property.
         * 
         * @return
         *     possible object is
         *     {@link UserStatistics }
         *     
         */
        public UserStatistics getIVR() {
            return ivr;
        }

        /**
         * Sets the value of the ivr property.
         * 
         * @param value
         *     allowed object is
         *     {@link UserStatistics }
         *     
         */
        public void setIVR(UserStatistics value) {
            this.ivr = value;
        }

        /**
         * Gets the value of the voicemailCenter property.
         * 
         * @return
         *     possible object is
         *     {@link UserStatistics }
         *     
         */
        public UserStatistics getVoicemailCenter() {
            return voicemailCenter;
        }

        /**
         * Sets the value of the voicemailCenter property.
         * 
         * @param value
         *     allowed object is
         *     {@link UserStatistics }
         *     
         */
        public void setVoicemailCenter(UserStatistics value) {
            this.voicemailCenter = value;
        }

        /**
         * Gets the value of the conference property.
         * 
         * @return
         *     possible object is
         *     {@link UserStatistics }
         *     
         */
        public UserStatistics getConference() {
            return conference;
        }

        /**
         * Sets the value of the conference property.
         * 
         * @param value
         *     allowed object is
         *     {@link UserStatistics }
         *     
         */
        public void setConference(UserStatistics value) {
            this.conference = value;
        }

        /**
         * Gets the value of the callback property.
         * 
         * @return
         *     possible object is
         *     {@link UserStatistics }
         *     
         */
        public UserStatistics getCallback() {
            return callback;
        }

        /**
         * Sets the value of the callback property.
         * 
         * @param value
         *     allowed object is
         *     {@link UserStatistics }
         *     
         */
        public void setCallback(UserStatistics value) {
            this.callback = value;
        }

        /**
         * Gets the value of the callingCard property.
         * 
         * @return
         *     possible object is
         *     {@link UserStatistics }
         *     
         */
        public UserStatistics getCallingCard() {
            return callingCard;
        }

        /**
         * Sets the value of the callingCard property.
         * 
         * @param value
         *     allowed object is
         *     {@link UserStatistics }
         *     
         */
        public void setCallingCard(UserStatistics value) {
            this.callingCard = value;
        }

        /**
         * Gets the value of the intercomPaging property.
         * 
         * @return
         *     possible object is
         *     {@link UserStatistics }
         *     
         */
        public UserStatistics getIntercomPaging() {
            return intercomPaging;
        }

        /**
         * Sets the value of the intercomPaging property.
         * 
         * @param value
         *     allowed object is
         *     {@link UserStatistics }
         *     
         */
        public void setIntercomPaging(UserStatistics value) {
            this.intercomPaging = value;
        }

    }

}

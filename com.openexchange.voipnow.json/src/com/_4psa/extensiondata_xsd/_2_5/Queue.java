
package com._4psa.extensiondata_xsd._2_5;

import java.math.BigDecimal;
import java.math.BigInteger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.common_xsd._2_5.UnlimitedUInt;


/**
 * Queue extension data
 *
 * <p>Java class for Queue complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="Queue">
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}PhoneCallerIDInfo">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="size" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedUInt" minOccurs="0"/>
 *         &lt;element name="distribution" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="ringall"/>
 *               &lt;enumeration value="leastrecent"/>
 *               &lt;enumeration value="fewestcalls"/>
 *               &lt;enumeration value="random"/>
 *               &lt;enumeration value="rrmemory"/>
 *               &lt;enumeration value="linear"/>
 *               &lt;enumeration value="wrandom"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="waitFor" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;element name="service" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="record" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="recordStorage" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedUInt" minOccurs="0"/>
 *         &lt;sequence>
 *           &lt;element name="agentMaxRings" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *           &lt;element name="callBetween" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *           &lt;element name="delayFor" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *           &lt;element name="pickupAnnounceSnd" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *           &lt;element name="reportWaitTime" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *           &lt;element name="restartTimer" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;/sequence>
 *         &lt;sequence>
 *           &lt;element name="welcomeSndActive" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *           &lt;element name="welcomeSnd" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *           &lt;element name="announcePosition" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *           &lt;element name="announcePosFreq" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *           &lt;element name="announceHoldTime" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *           &lt;element name="periodicAnnounceFreq" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *           &lt;element name="exitToExtension" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;/sequence>
 *         &lt;sequence>
 *           &lt;element name="musicOnHoldFolder" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *           &lt;element name="thereAreSnd" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *           &lt;element name="holdTimeSnd" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *           &lt;element name="youAreNextSnd" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *           &lt;element name="thankYouSnd" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *           &lt;element name="callsWaitingSnd" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *           &lt;element name="reportHoldSnd" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *           &lt;element name="periodicAnnounceSnd" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *           &lt;element name="secondSnd" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *           &lt;element name="minuteSnd" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;/sequence>
 *         &lt;sequence>
 *           &lt;element name="timeoutStatus" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *           &lt;element name="timeout" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *           &lt;element name="timeoutParam" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *         &lt;/sequence>
 *         &lt;sequence>
 *           &lt;element name="allowCalls" minOccurs="0">
 *             &lt;simpleType>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                 &lt;enumeration value="no"/>
 *                 &lt;enumeration value="strict"/>
 *                 &lt;enumeration value="loose"/>
 *               &lt;/restriction>
 *             &lt;/simpleType>
 *           &lt;/element>
 *           &lt;element name="emptyStatus" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *           &lt;element name="emptyTransfer" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;/sequence>
 *         &lt;sequence>
 *           &lt;element name="dropCalls" minOccurs="0">
 *             &lt;simpleType>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                 &lt;enumeration value="no"/>
 *                 &lt;enumeration value="strict"/>
 *                 &lt;enumeration value="loose"/>
 *               &lt;/restriction>
 *             &lt;/simpleType>
 *           &lt;/element>
 *           &lt;element name="unavailableStatus" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *           &lt;element name="unavailableTransfer" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;/sequence>
 *         &lt;element name="disconnectSndActive" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="disconnectSnd" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Queue", propOrder = {
    "name",
    "size",
    "distribution",
    "waitFor",
    "service",
    "record",
    "recordStorage",
    "agentMaxRings",
    "callBetween",
    "delayFor",
    "pickupAnnounceSnd",
    "reportWaitTime",
    "restartTimer",
    "welcomeSndActive",
    "welcomeSnd",
    "announcePosition",
    "announcePosFreq",
    "announceHoldTime",
    "periodicAnnounceFreq",
    "exitToExtension",
    "musicOnHoldFolder",
    "thereAreSnd",
    "holdTimeSnd",
    "youAreNextSnd",
    "thankYouSnd",
    "callsWaitingSnd",
    "reportHoldSnd",
    "periodicAnnounceSnd",
    "secondSnd",
    "minuteSnd",
    "timeoutStatus",
    "timeout",
    "timeoutParam",
    "allowCalls",
    "emptyStatus",
    "emptyTransfer",
    "dropCalls",
    "unavailableStatus",
    "unavailableTransfer",
    "disconnectSndActive",
    "disconnectSnd"
})
@XmlSeeAlso({
    ExtendedQueue.class
})
public class Queue
    extends PhoneCallerIDInfo
{

    protected String name;
    protected UnlimitedUInt size;
    @XmlElement(defaultValue = "ringall")
    protected String distribution;
    @XmlElementRef(name = "waitFor", namespace = "http://4psa.com/ExtensionData.xsd/2.5.1", type = JAXBElement.class)
    protected JAXBElement<BigInteger> waitFor;
    @XmlElementRef(name = "service", namespace = "http://4psa.com/ExtensionData.xsd/2.5.1", type = JAXBElement.class)
    protected JAXBElement<BigDecimal> service;
    protected Boolean record;
    protected UnlimitedUInt recordStorage;
    @XmlElement(defaultValue = "20")
    protected BigInteger agentMaxRings;
    @XmlElementRef(name = "callBetween", namespace = "http://4psa.com/ExtensionData.xsd/2.5.1", type = JAXBElement.class)
    protected JAXBElement<BigInteger> callBetween;
    @XmlElement(defaultValue = "0")
    protected BigInteger delayFor;
    protected String pickupAnnounceSnd;
    protected Boolean reportWaitTime;
    protected Boolean restartTimer;
    protected Boolean welcomeSndActive;
    protected String welcomeSnd;
    protected Boolean announcePosition;
    protected BigInteger announcePosFreq;
    protected Boolean announceHoldTime;
    protected BigInteger periodicAnnounceFreq;
    protected BigInteger exitToExtension;
    @XmlElement(defaultValue = "-1")
    protected String musicOnHoldFolder;
    protected String thereAreSnd;
    protected String holdTimeSnd;
    protected String youAreNextSnd;
    protected String thankYouSnd;
    protected String callsWaitingSnd;
    protected String reportHoldSnd;
    protected String periodicAnnounceSnd;
    protected String secondSnd;
    protected String minuteSnd;
    protected Boolean timeoutStatus;
    protected BigInteger timeout;
    protected BigInteger timeoutParam;
    protected String allowCalls;
    protected Boolean emptyStatus;
    protected BigInteger emptyTransfer;
    protected String dropCalls;
    protected Boolean unavailableStatus;
    protected BigInteger unavailableTransfer;
    protected Boolean disconnectSndActive;
    protected String disconnectSnd;

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
     * Gets the value of the size property.
     *
     * @return
     *     possible object is
     *     {@link UnlimitedUInt }
     *
     */
    public UnlimitedUInt getSize() {
        return size;
    }

    /**
     * Sets the value of the size property.
     *
     * @param value
     *     allowed object is
     *     {@link UnlimitedUInt }
     *
     */
    public void setSize(UnlimitedUInt value) {
        this.size = value;
    }

    /**
     * Gets the value of the distribution property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDistribution() {
        return distribution;
    }

    /**
     * Sets the value of the distribution property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDistribution(String value) {
        this.distribution = value;
    }

    /**
     * Gets the value of the waitFor property.
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     *
     */
    public JAXBElement<BigInteger> getWaitFor() {
        return waitFor;
    }

    /**
     * Sets the value of the waitFor property.
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     *
     */
    public void setWaitFor(JAXBElement<BigInteger> value) {
        this.waitFor = value;
    }

    /**
     * Gets the value of the service property.
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}
     *
     */
    public JAXBElement<BigDecimal> getService() {
        return service;
    }

    /**
     * Sets the value of the service property.
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}
     *
     */
    public void setService(JAXBElement<BigDecimal> value) {
        this.service = value;
    }

    /**
     * Gets the value of the record property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isRecord() {
        return record;
    }

    /**
     * Sets the value of the record property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setRecord(Boolean value) {
        this.record = value;
    }

    /**
     * Gets the value of the recordStorage property.
     *
     * @return
     *     possible object is
     *     {@link UnlimitedUInt }
     *
     */
    public UnlimitedUInt getRecordStorage() {
        return recordStorage;
    }

    /**
     * Sets the value of the recordStorage property.
     *
     * @param value
     *     allowed object is
     *     {@link UnlimitedUInt }
     *
     */
    public void setRecordStorage(UnlimitedUInt value) {
        this.recordStorage = value;
    }

    /**
     * Gets the value of the agentMaxRings property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getAgentMaxRings() {
        return agentMaxRings;
    }

    /**
     * Sets the value of the agentMaxRings property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setAgentMaxRings(BigInteger value) {
        this.agentMaxRings = value;
    }

    /**
     * Gets the value of the callBetween property.
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     *
     */
    public JAXBElement<BigInteger> getCallBetween() {
        return callBetween;
    }

    /**
     * Sets the value of the callBetween property.
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     *
     */
    public void setCallBetween(JAXBElement<BigInteger> value) {
        this.callBetween = value;
    }

    /**
     * Gets the value of the delayFor property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getDelayFor() {
        return delayFor;
    }

    /**
     * Sets the value of the delayFor property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setDelayFor(BigInteger value) {
        this.delayFor = value;
    }

    /**
     * Gets the value of the pickupAnnounceSnd property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPickupAnnounceSnd() {
        return pickupAnnounceSnd;
    }

    /**
     * Sets the value of the pickupAnnounceSnd property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPickupAnnounceSnd(String value) {
        this.pickupAnnounceSnd = value;
    }

    /**
     * Gets the value of the reportWaitTime property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isReportWaitTime() {
        return reportWaitTime;
    }

    /**
     * Sets the value of the reportWaitTime property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setReportWaitTime(Boolean value) {
        this.reportWaitTime = value;
    }

    /**
     * Gets the value of the restartTimer property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isRestartTimer() {
        return restartTimer;
    }

    /**
     * Sets the value of the restartTimer property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setRestartTimer(Boolean value) {
        this.restartTimer = value;
    }

    /**
     * Gets the value of the welcomeSndActive property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isWelcomeSndActive() {
        return welcomeSndActive;
    }

    /**
     * Sets the value of the welcomeSndActive property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setWelcomeSndActive(Boolean value) {
        this.welcomeSndActive = value;
    }

    /**
     * Gets the value of the welcomeSnd property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getWelcomeSnd() {
        return welcomeSnd;
    }

    /**
     * Sets the value of the welcomeSnd property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setWelcomeSnd(String value) {
        this.welcomeSnd = value;
    }

    /**
     * Gets the value of the announcePosition property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isAnnouncePosition() {
        return announcePosition;
    }

    /**
     * Sets the value of the announcePosition property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setAnnouncePosition(Boolean value) {
        this.announcePosition = value;
    }

    /**
     * Gets the value of the announcePosFreq property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getAnnouncePosFreq() {
        return announcePosFreq;
    }

    /**
     * Sets the value of the announcePosFreq property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setAnnouncePosFreq(BigInteger value) {
        this.announcePosFreq = value;
    }

    /**
     * Gets the value of the announceHoldTime property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isAnnounceHoldTime() {
        return announceHoldTime;
    }

    /**
     * Sets the value of the announceHoldTime property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setAnnounceHoldTime(Boolean value) {
        this.announceHoldTime = value;
    }

    /**
     * Gets the value of the periodicAnnounceFreq property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getPeriodicAnnounceFreq() {
        return periodicAnnounceFreq;
    }

    /**
     * Sets the value of the periodicAnnounceFreq property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setPeriodicAnnounceFreq(BigInteger value) {
        this.periodicAnnounceFreq = value;
    }

    /**
     * Gets the value of the exitToExtension property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getExitToExtension() {
        return exitToExtension;
    }

    /**
     * Sets the value of the exitToExtension property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setExitToExtension(BigInteger value) {
        this.exitToExtension = value;
    }

    /**
     * Gets the value of the musicOnHoldFolder property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMusicOnHoldFolder() {
        return musicOnHoldFolder;
    }

    /**
     * Sets the value of the musicOnHoldFolder property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMusicOnHoldFolder(String value) {
        this.musicOnHoldFolder = value;
    }

    /**
     * Gets the value of the thereAreSnd property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getThereAreSnd() {
        return thereAreSnd;
    }

    /**
     * Sets the value of the thereAreSnd property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setThereAreSnd(String value) {
        this.thereAreSnd = value;
    }

    /**
     * Gets the value of the holdTimeSnd property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getHoldTimeSnd() {
        return holdTimeSnd;
    }

    /**
     * Sets the value of the holdTimeSnd property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setHoldTimeSnd(String value) {
        this.holdTimeSnd = value;
    }

    /**
     * Gets the value of the youAreNextSnd property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getYouAreNextSnd() {
        return youAreNextSnd;
    }

    /**
     * Sets the value of the youAreNextSnd property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setYouAreNextSnd(String value) {
        this.youAreNextSnd = value;
    }

    /**
     * Gets the value of the thankYouSnd property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getThankYouSnd() {
        return thankYouSnd;
    }

    /**
     * Sets the value of the thankYouSnd property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setThankYouSnd(String value) {
        this.thankYouSnd = value;
    }

    /**
     * Gets the value of the callsWaitingSnd property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCallsWaitingSnd() {
        return callsWaitingSnd;
    }

    /**
     * Sets the value of the callsWaitingSnd property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCallsWaitingSnd(String value) {
        this.callsWaitingSnd = value;
    }

    /**
     * Gets the value of the reportHoldSnd property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getReportHoldSnd() {
        return reportHoldSnd;
    }

    /**
     * Sets the value of the reportHoldSnd property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setReportHoldSnd(String value) {
        this.reportHoldSnd = value;
    }

    /**
     * Gets the value of the periodicAnnounceSnd property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPeriodicAnnounceSnd() {
        return periodicAnnounceSnd;
    }

    /**
     * Sets the value of the periodicAnnounceSnd property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPeriodicAnnounceSnd(String value) {
        this.periodicAnnounceSnd = value;
    }

    /**
     * Gets the value of the secondSnd property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSecondSnd() {
        return secondSnd;
    }

    /**
     * Sets the value of the secondSnd property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSecondSnd(String value) {
        this.secondSnd = value;
    }

    /**
     * Gets the value of the minuteSnd property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMinuteSnd() {
        return minuteSnd;
    }

    /**
     * Sets the value of the minuteSnd property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMinuteSnd(String value) {
        this.minuteSnd = value;
    }

    /**
     * Gets the value of the timeoutStatus property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isTimeoutStatus() {
        return timeoutStatus;
    }

    /**
     * Sets the value of the timeoutStatus property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setTimeoutStatus(Boolean value) {
        this.timeoutStatus = value;
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
     * Gets the value of the timeoutParam property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getTimeoutParam() {
        return timeoutParam;
    }

    /**
     * Sets the value of the timeoutParam property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setTimeoutParam(BigInteger value) {
        this.timeoutParam = value;
    }

    /**
     * Gets the value of the allowCalls property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAllowCalls() {
        return allowCalls;
    }

    /**
     * Sets the value of the allowCalls property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAllowCalls(String value) {
        this.allowCalls = value;
    }

    /**
     * Gets the value of the emptyStatus property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isEmptyStatus() {
        return emptyStatus;
    }

    /**
     * Sets the value of the emptyStatus property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setEmptyStatus(Boolean value) {
        this.emptyStatus = value;
    }

    /**
     * Gets the value of the emptyTransfer property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getEmptyTransfer() {
        return emptyTransfer;
    }

    /**
     * Sets the value of the emptyTransfer property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setEmptyTransfer(BigInteger value) {
        this.emptyTransfer = value;
    }

    /**
     * Gets the value of the dropCalls property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDropCalls() {
        return dropCalls;
    }

    /**
     * Sets the value of the dropCalls property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDropCalls(String value) {
        this.dropCalls = value;
    }

    /**
     * Gets the value of the unavailableStatus property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isUnavailableStatus() {
        return unavailableStatus;
    }

    /**
     * Sets the value of the unavailableStatus property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setUnavailableStatus(Boolean value) {
        this.unavailableStatus = value;
    }

    /**
     * Gets the value of the unavailableTransfer property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getUnavailableTransfer() {
        return unavailableTransfer;
    }

    /**
     * Sets the value of the unavailableTransfer property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setUnavailableTransfer(BigInteger value) {
        this.unavailableTransfer = value;
    }

    /**
     * Gets the value of the disconnectSndActive property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isDisconnectSndActive() {
        return disconnectSndActive;
    }

    /**
     * Sets the value of the disconnectSndActive property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setDisconnectSndActive(Boolean value) {
        this.disconnectSndActive = value;
    }

    /**
     * Gets the value of the disconnectSnd property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDisconnectSnd() {
        return disconnectSnd;
    }

    /**
     * Sets the value of the disconnectSnd property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDisconnectSnd(String value) {
        this.disconnectSnd = value;
    }

}

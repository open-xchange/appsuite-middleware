
package com._4psa.reportdata_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import com._4psa.reportmessagesinfo_xsd._2_5.CallReportResponseType;


/**
 * Call report data
 * 
 * <p>Java class for CallReport complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CallReport">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="incomingCalls" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="total" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *                   &lt;element name="answered" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *                   &lt;element name="unanswered" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *                   &lt;element name="busy" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *                   &lt;element name="failed" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *                   &lt;element name="unknown" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *                   &lt;element name="unallowed" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="outgoingCalls" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="total" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *                   &lt;element name="answered" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *                   &lt;element name="unanswered" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *                   &lt;element name="busy" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *                   &lt;element name="failed" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *                   &lt;element name="unknown" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *                   &lt;element name="unallowed" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="call" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="source" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                   &lt;element name="destination" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                   &lt;element name="initiated" type="{http://4psa.com/Common.xsd/2.5.1}dateTime" minOccurs="0"/>
 *                   &lt;element name="startDate" type="{http://4psa.com/Common.xsd/2.5.1}dateTime" minOccurs="0"/>
 *                   &lt;element name="duration" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                   &lt;element name="answerDate" type="{http://4psa.com/Common.xsd/2.5.1}dateTime" minOccurs="0"/>
 *                   &lt;element name="flow" minOccurs="0">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;enumeration value="in"/>
 *                         &lt;enumeration value="out"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                   &lt;element name="type" minOccurs="0">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;enumeration value="local"/>
 *                         &lt;enumeration value="elocal"/>
 *                         &lt;enumeration value="out"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                   &lt;element name="disposition" minOccurs="0">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;enumeration value="ANSWERED"/>
 *                         &lt;enumeration value="BUSY"/>
 *                         &lt;enumeration value="FAILED"/>
 *                         &lt;enumeration value="NO ANSWER"/>
 *                         &lt;enumeration value="UNKNOWN"/>
 *                         &lt;enumeration value="NOT ALLOWED"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                   &lt;element name="hangupCause" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *                   &lt;element name="networkCode" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CallReport", propOrder = {
    "incomingCalls",
    "outgoingCalls",
    "call"
})
@XmlSeeAlso({
    CallReportResponseType.class
})
public class CallReport {

    protected CallReport.IncomingCalls incomingCalls;
    protected CallReport.OutgoingCalls outgoingCalls;
    protected List<CallReport.Call> call;

    /**
     * Gets the value of the incomingCalls property.
     * 
     * @return
     *     possible object is
     *     {@link CallReport.IncomingCalls }
     *     
     */
    public CallReport.IncomingCalls getIncomingCalls() {
        return incomingCalls;
    }

    /**
     * Sets the value of the incomingCalls property.
     * 
     * @param value
     *     allowed object is
     *     {@link CallReport.IncomingCalls }
     *     
     */
    public void setIncomingCalls(CallReport.IncomingCalls value) {
        this.incomingCalls = value;
    }

    /**
     * Gets the value of the outgoingCalls property.
     * 
     * @return
     *     possible object is
     *     {@link CallReport.OutgoingCalls }
     *     
     */
    public CallReport.OutgoingCalls getOutgoingCalls() {
        return outgoingCalls;
    }

    /**
     * Sets the value of the outgoingCalls property.
     * 
     * @param value
     *     allowed object is
     *     {@link CallReport.OutgoingCalls }
     *     
     */
    public void setOutgoingCalls(CallReport.OutgoingCalls value) {
        this.outgoingCalls = value;
    }

    /**
     * Gets the value of the call property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the call property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCall().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CallReport.Call }
     * 
     * 
     */
    public List<CallReport.Call> getCall() {
        if (call == null) {
            call = new ArrayList<CallReport.Call>();
        }
        return this.call;
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
     *         &lt;element name="source" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
     *         &lt;element name="destination" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
     *         &lt;element name="initiated" type="{http://4psa.com/Common.xsd/2.5.1}dateTime" minOccurs="0"/>
     *         &lt;element name="startDate" type="{http://4psa.com/Common.xsd/2.5.1}dateTime" minOccurs="0"/>
     *         &lt;element name="duration" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
     *         &lt;element name="answerDate" type="{http://4psa.com/Common.xsd/2.5.1}dateTime" minOccurs="0"/>
     *         &lt;element name="flow" minOccurs="0">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;enumeration value="in"/>
     *               &lt;enumeration value="out"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *         &lt;element name="type" minOccurs="0">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;enumeration value="local"/>
     *               &lt;enumeration value="elocal"/>
     *               &lt;enumeration value="out"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *         &lt;element name="disposition" minOccurs="0">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;enumeration value="ANSWERED"/>
     *               &lt;enumeration value="BUSY"/>
     *               &lt;enumeration value="FAILED"/>
     *               &lt;enumeration value="NO ANSWER"/>
     *               &lt;enumeration value="UNKNOWN"/>
     *               &lt;enumeration value="NOT ALLOWED"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *         &lt;element name="hangupCause" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
     *         &lt;element name="networkCode" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
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
        "source",
        "destination",
        "initiated",
        "startDate",
        "duration",
        "answerDate",
        "flow",
        "type",
        "disposition",
        "hangupCause",
        "networkCode"
    })
    public static class Call {

        protected String source;
        protected String destination;
        protected XMLGregorianCalendar initiated;
        protected XMLGregorianCalendar startDate;
        protected String duration;
        protected XMLGregorianCalendar answerDate;
        protected String flow;
        protected String type;
        protected String disposition;
        protected BigInteger hangupCause;
        protected String networkCode;

        /**
         * Gets the value of the source property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSource() {
            return source;
        }

        /**
         * Sets the value of the source property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSource(String value) {
            this.source = value;
        }

        /**
         * Gets the value of the destination property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDestination() {
            return destination;
        }

        /**
         * Sets the value of the destination property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDestination(String value) {
            this.destination = value;
        }

        /**
         * Gets the value of the initiated property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getInitiated() {
            return initiated;
        }

        /**
         * Sets the value of the initiated property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setInitiated(XMLGregorianCalendar value) {
            this.initiated = value;
        }

        /**
         * Gets the value of the startDate property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getStartDate() {
            return startDate;
        }

        /**
         * Sets the value of the startDate property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setStartDate(XMLGregorianCalendar value) {
            this.startDate = value;
        }

        /**
         * Gets the value of the duration property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDuration() {
            return duration;
        }

        /**
         * Sets the value of the duration property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDuration(String value) {
            this.duration = value;
        }

        /**
         * Gets the value of the answerDate property.
         * 
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public XMLGregorianCalendar getAnswerDate() {
            return answerDate;
        }

        /**
         * Sets the value of the answerDate property.
         * 
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *     
         */
        public void setAnswerDate(XMLGregorianCalendar value) {
            this.answerDate = value;
        }

        /**
         * Gets the value of the flow property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getFlow() {
            return flow;
        }

        /**
         * Sets the value of the flow property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setFlow(String value) {
            this.flow = value;
        }

        /**
         * Gets the value of the type property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the value of the type property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setType(String value) {
            this.type = value;
        }

        /**
         * Gets the value of the disposition property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDisposition() {
            return disposition;
        }

        /**
         * Sets the value of the disposition property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDisposition(String value) {
            this.disposition = value;
        }

        /**
         * Gets the value of the hangupCause property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getHangupCause() {
            return hangupCause;
        }

        /**
         * Sets the value of the hangupCause property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setHangupCause(BigInteger value) {
            this.hangupCause = value;
        }

        /**
         * Gets the value of the networkCode property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getNetworkCode() {
            return networkCode;
        }

        /**
         * Sets the value of the networkCode property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setNetworkCode(String value) {
            this.networkCode = value;
        }

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
     *         &lt;element name="total" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
     *         &lt;element name="answered" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
     *         &lt;element name="unanswered" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
     *         &lt;element name="busy" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
     *         &lt;element name="failed" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
     *         &lt;element name="unknown" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
     *         &lt;element name="unallowed" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
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
        "total",
        "answered",
        "unanswered",
        "busy",
        "failed",
        "unknown",
        "unallowed"
    })
    public static class IncomingCalls {

        protected BigInteger total;
        protected BigInteger answered;
        protected BigInteger unanswered;
        protected BigInteger busy;
        protected BigInteger failed;
        protected BigInteger unknown;
        protected BigInteger unallowed;

        /**
         * Gets the value of the total property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getTotal() {
            return total;
        }

        /**
         * Sets the value of the total property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setTotal(BigInteger value) {
            this.total = value;
        }

        /**
         * Gets the value of the answered property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getAnswered() {
            return answered;
        }

        /**
         * Sets the value of the answered property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setAnswered(BigInteger value) {
            this.answered = value;
        }

        /**
         * Gets the value of the unanswered property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getUnanswered() {
            return unanswered;
        }

        /**
         * Sets the value of the unanswered property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setUnanswered(BigInteger value) {
            this.unanswered = value;
        }

        /**
         * Gets the value of the busy property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getBusy() {
            return busy;
        }

        /**
         * Sets the value of the busy property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setBusy(BigInteger value) {
            this.busy = value;
        }

        /**
         * Gets the value of the failed property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getFailed() {
            return failed;
        }

        /**
         * Sets the value of the failed property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setFailed(BigInteger value) {
            this.failed = value;
        }

        /**
         * Gets the value of the unknown property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getUnknown() {
            return unknown;
        }

        /**
         * Sets the value of the unknown property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setUnknown(BigInteger value) {
            this.unknown = value;
        }

        /**
         * Gets the value of the unallowed property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getUnallowed() {
            return unallowed;
        }

        /**
         * Sets the value of the unallowed property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setUnallowed(BigInteger value) {
            this.unallowed = value;
        }

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
     *         &lt;element name="total" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
     *         &lt;element name="answered" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
     *         &lt;element name="unanswered" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
     *         &lt;element name="busy" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
     *         &lt;element name="failed" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
     *         &lt;element name="unknown" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
     *         &lt;element name="unallowed" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
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
        "total",
        "answered",
        "unanswered",
        "busy",
        "failed",
        "unknown",
        "unallowed"
    })
    public static class OutgoingCalls {

        protected BigInteger total;
        protected BigInteger answered;
        protected BigInteger unanswered;
        protected BigInteger busy;
        protected BigInteger failed;
        protected BigInteger unknown;
        protected BigInteger unallowed;

        /**
         * Gets the value of the total property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getTotal() {
            return total;
        }

        /**
         * Sets the value of the total property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setTotal(BigInteger value) {
            this.total = value;
        }

        /**
         * Gets the value of the answered property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getAnswered() {
            return answered;
        }

        /**
         * Sets the value of the answered property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setAnswered(BigInteger value) {
            this.answered = value;
        }

        /**
         * Gets the value of the unanswered property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getUnanswered() {
            return unanswered;
        }

        /**
         * Sets the value of the unanswered property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setUnanswered(BigInteger value) {
            this.unanswered = value;
        }

        /**
         * Gets the value of the busy property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getBusy() {
            return busy;
        }

        /**
         * Sets the value of the busy property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setBusy(BigInteger value) {
            this.busy = value;
        }

        /**
         * Gets the value of the failed property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getFailed() {
            return failed;
        }

        /**
         * Sets the value of the failed property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setFailed(BigInteger value) {
            this.failed = value;
        }

        /**
         * Gets the value of the unknown property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getUnknown() {
            return unknown;
        }

        /**
         * Sets the value of the unknown property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setUnknown(BigInteger value) {
            this.unknown = value;
        }

        /**
         * Gets the value of the unallowed property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getUnallowed() {
            return unallowed;
        }

        /**
         * Sets the value of the unallowed property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setUnallowed(BigInteger value) {
            this.unallowed = value;
        }

    }

}

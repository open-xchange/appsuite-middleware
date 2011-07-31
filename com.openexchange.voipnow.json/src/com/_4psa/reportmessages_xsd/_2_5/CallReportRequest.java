
package com._4psa.reportmessages_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


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
 *         &lt;choice>
 *           &lt;element name="userID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *           &lt;element name="userIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *           &lt;element name="login" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;/choice>
 *         &lt;element name="interval" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="startDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *                   &lt;element name="endDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
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
 *         &lt;element name="disposion" minOccurs="0">
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
 *         &lt;element name="records" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
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
    "userID",
    "userIdentifier",
    "login",
    "interval",
    "flow",
    "type",
    "disposion",
    "records",
    "hangupCause",
    "networkCode"
})
@XmlRootElement(name = "CallReportRequest")
public class CallReportRequest {

    protected BigInteger userID;
    protected String userIdentifier;
    protected String login;
    protected CallReportRequest.Interval interval;
    protected String flow;
    protected String type;
    protected String disposion;
    @XmlElement(defaultValue = "1000")
    protected BigInteger records;
    protected BigInteger hangupCause;
    protected String networkCode;

    /**
     * Gets the value of the userID property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getUserID() {
        return userID;
    }

    /**
     * Sets the value of the userID property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setUserID(BigInteger value) {
        this.userID = value;
    }

    /**
     * Gets the value of the userIdentifier property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserIdentifier() {
        return userIdentifier;
    }

    /**
     * Sets the value of the userIdentifier property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserIdentifier(String value) {
        this.userIdentifier = value;
    }

    /**
     * Gets the value of the login property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getLogin() {
        return login;
    }

    /**
     * Sets the value of the login property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setLogin(String value) {
        this.login = value;
    }

    /**
     * Gets the value of the interval property.
     *
     * @return
     *     possible object is
     *     {@link CallReportRequest.Interval }
     *
     */
    public CallReportRequest.Interval getInterval() {
        return interval;
    }

    /**
     * Sets the value of the interval property.
     *
     * @param value
     *     allowed object is
     *     {@link CallReportRequest.Interval }
     *
     */
    public void setInterval(CallReportRequest.Interval value) {
        this.interval = value;
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
     * Gets the value of the disposion property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDisposion() {
        return disposion;
    }

    /**
     * Sets the value of the disposion property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDisposion(String value) {
        this.disposion = value;
    }

    /**
     * Gets the value of the records property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getRecords() {
        return records;
    }

    /**
     * Sets the value of the records property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setRecords(BigInteger value) {
        this.records = value;
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
     *         &lt;element name="startDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
     *         &lt;element name="endDate" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
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
        "startDate",
        "endDate"
    })
    public static class Interval {

        @XmlSchemaType(name = "date")
        protected XMLGregorianCalendar startDate;
        @XmlSchemaType(name = "date")
        protected XMLGregorianCalendar endDate;

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
         * Gets the value of the endDate property.
         *
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *
         */
        public XMLGregorianCalendar getEndDate() {
            return endDate;
        }

        /**
         * Sets the value of the endDate property.
         *
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *
         */
        public void setEndDate(XMLGregorianCalendar value) {
            this.endDate = value;
        }

    }

}

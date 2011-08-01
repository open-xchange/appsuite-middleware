
package com._4psa.billingdata_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com._4psa.common_xsd._2_5.Notice;


/**
 * Destination exception charges list
 *
 * <p>Java class for DestinationExceptionList complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="DestinationExceptionList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="chargingPlanID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *         &lt;element name="exception" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *                   &lt;element name="areaCode" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *                   &lt;element name="minutes" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *                   &lt;element name="description" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                   &lt;element name="initialAmount" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *                   &lt;element name="initialInterval" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *                   &lt;element name="amount" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *                   &lt;element name="interval" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *                   &lt;element name="chargingPackageID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *                   &lt;element name="chargingPackage" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="notice" type="{http://4psa.com/Common.xsd/2.5.1}notice" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DestinationExceptionList", propOrder = {
    "chargingPlanID",
    "exception",
    "notice"
})
public class DestinationExceptionList {

    @XmlElement(required = true)
    protected BigInteger chargingPlanID;
    protected List<DestinationExceptionList.Exception> exception;
    protected List<Notice> notice;

    /**
     * Gets the value of the chargingPlanID property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getChargingPlanID() {
        return chargingPlanID;
    }

    /**
     * Sets the value of the chargingPlanID property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setChargingPlanID(BigInteger value) {
        this.chargingPlanID = value;
    }

    /**
     * Gets the value of the exception property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the exception property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getException().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DestinationExceptionList.Exception }
     *
     *
     */
    public List<DestinationExceptionList.Exception> getException() {
        if (exception == null) {
            exception = new ArrayList<DestinationExceptionList.Exception>();
        }
        return this.exception;
    }

    /**
     * Gets the value of the notice property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the notice property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNotice().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Notice }
     *
     *
     */
    public List<Notice> getNotice() {
        if (notice == null) {
            notice = new ArrayList<Notice>();
        }
        return this.notice;
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
     *         &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
     *         &lt;element name="areaCode" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
     *         &lt;element name="minutes" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
     *         &lt;element name="description" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
     *         &lt;element name="initialAmount" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
     *         &lt;element name="initialInterval" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
     *         &lt;element name="amount" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
     *         &lt;element name="interval" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
     *         &lt;element name="chargingPackageID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
     *         &lt;element name="chargingPackage" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
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
        "areaCode",
        "minutes",
        "description",
        "initialAmount",
        "initialInterval",
        "amount",
        "interval",
        "chargingPackageID",
        "chargingPackage"
    })
    public static class Exception {

        @XmlElement(name = "ID", required = true)
        protected BigInteger id;
        @XmlElement(required = true)
        protected String areaCode;
        protected BigInteger minutes;
        protected String description;
        protected Float initialAmount;
        protected BigInteger initialInterval;
        protected Float amount;
        protected BigInteger interval;
        protected BigInteger chargingPackageID;
        protected String chargingPackage;

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
         * Gets the value of the areaCode property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getAreaCode() {
            return areaCode;
        }

        /**
         * Sets the value of the areaCode property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setAreaCode(String value) {
            this.areaCode = value;
        }

        /**
         * Gets the value of the minutes property.
         *
         * @return
         *     possible object is
         *     {@link BigInteger }
         *
         */
        public BigInteger getMinutes() {
            return minutes;
        }

        /**
         * Sets the value of the minutes property.
         *
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *
         */
        public void setMinutes(BigInteger value) {
            this.minutes = value;
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
         * Gets the value of the initialAmount property.
         *
         * @return
         *     possible object is
         *     {@link Float }
         *
         */
        public Float getInitialAmount() {
            return initialAmount;
        }

        /**
         * Sets the value of the initialAmount property.
         *
         * @param value
         *     allowed object is
         *     {@link Float }
         *
         */
        public void setInitialAmount(Float value) {
            this.initialAmount = value;
        }

        /**
         * Gets the value of the initialInterval property.
         *
         * @return
         *     possible object is
         *     {@link BigInteger }
         *
         */
        public BigInteger getInitialInterval() {
            return initialInterval;
        }

        /**
         * Sets the value of the initialInterval property.
         *
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *
         */
        public void setInitialInterval(BigInteger value) {
            this.initialInterval = value;
        }

        /**
         * Gets the value of the amount property.
         *
         * @return
         *     possible object is
         *     {@link Float }
         *
         */
        public Float getAmount() {
            return amount;
        }

        /**
         * Sets the value of the amount property.
         *
         * @param value
         *     allowed object is
         *     {@link Float }
         *
         */
        public void setAmount(Float value) {
            this.amount = value;
        }

        /**
         * Gets the value of the interval property.
         *
         * @return
         *     possible object is
         *     {@link BigInteger }
         *
         */
        public BigInteger getInterval() {
            return interval;
        }

        /**
         * Sets the value of the interval property.
         *
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *
         */
        public void setInterval(BigInteger value) {
            this.interval = value;
        }

        /**
         * Gets the value of the chargingPackageID property.
         *
         * @return
         *     possible object is
         *     {@link BigInteger }
         *
         */
        public BigInteger getChargingPackageID() {
            return chargingPackageID;
        }

        /**
         * Sets the value of the chargingPackageID property.
         *
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *
         */
        public void setChargingPackageID(BigInteger value) {
            this.chargingPackageID = value;
        }

        /**
         * Gets the value of the chargingPackage property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getChargingPackage() {
            return chargingPackage;
        }

        /**
         * Sets the value of the chargingPackage property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setChargingPackage(String value) {
            this.chargingPackage = value;
        }

    }

}

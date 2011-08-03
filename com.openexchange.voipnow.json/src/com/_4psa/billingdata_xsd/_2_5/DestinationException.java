
package com._4psa.billingdata_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.billingmessages_xsd._2_5.EditDestinationExceptionRequest;


/**
 * Destination exception charge
 *
 * <p>Java class for DestinationException complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="DestinationException">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="areaCode" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="charge" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="initialAmount" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *                   &lt;element name="initialInterval" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *                   &lt;element name="amount" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *                   &lt;element name="interval" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *                   &lt;element name="setupAmount" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;choice>
 *           &lt;element name="chargingPackageID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *           &lt;element name="package" minOccurs="0">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;sequence>
 *                     &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *                     &lt;element name="minutes" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *                   &lt;/sequence>
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
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
@XmlType(name = "DestinationException", propOrder = {
    "areaCode",
    "description",
    "charge",
    "chargingPackageID",
    "_package"
})
@XmlSeeAlso({
    EditDestinationExceptionRequest.class
})
public class DestinationException {

    protected String areaCode;
    protected String description;
    protected DestinationException.Charge charge;
    protected BigInteger chargingPackageID;
    @XmlElement(name = "package")
    protected DestinationException.Package _package;

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
     * Gets the value of the charge property.
     *
     * @return
     *     possible object is
     *     {@link DestinationException.Charge }
     *
     */
    public DestinationException.Charge getCharge() {
        return charge;
    }

    /**
     * Sets the value of the charge property.
     *
     * @param value
     *     allowed object is
     *     {@link DestinationException.Charge }
     *
     */
    public void setCharge(DestinationException.Charge value) {
        this.charge = value;
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
     * Gets the value of the package property.
     *
     * @return
     *     possible object is
     *     {@link DestinationException.Package }
     *
     */
    public DestinationException.Package getPackage() {
        return _package;
    }

    /**
     * Sets the value of the package property.
     *
     * @param value
     *     allowed object is
     *     {@link DestinationException.Package }
     *
     */
    public void setPackage(DestinationException.Package value) {
        this._package = value;
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
     *         &lt;element name="initialAmount" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
     *         &lt;element name="initialInterval" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
     *         &lt;element name="amount" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
     *         &lt;element name="interval" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
     *         &lt;element name="setupAmount" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
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
        "initialAmount",
        "initialInterval",
        "amount",
        "interval",
        "setupAmount"
    })
    public static class Charge {

        protected Float initialAmount;
        protected BigInteger initialInterval;
        protected Float amount;
        protected BigInteger interval;
        protected Float setupAmount;

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
         * Gets the value of the setupAmount property.
         *
         * @return
         *     possible object is
         *     {@link Float }
         *
         */
        public Float getSetupAmount() {
            return setupAmount;
        }

        /**
         * Sets the value of the setupAmount property.
         *
         * @param value
         *     allowed object is
         *     {@link Float }
         *
         */
        public void setSetupAmount(Float value) {
            this.setupAmount = value;
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
     *         &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
     *         &lt;element name="minutes" type="{http://www.w3.org/2001/XMLSchema}float"/>
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
        "name",
        "minutes"
    })
    public static class Package {

        @XmlElement(required = true)
        protected String name;
        protected float minutes;

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
         * Gets the value of the minutes property.
         *
         */
        public float getMinutes() {
            return minutes;
        }

        /**
         * Sets the value of the minutes property.
         *
         */
        public void setMinutes(float value) {
            this.minutes = value;
        }

    }

}

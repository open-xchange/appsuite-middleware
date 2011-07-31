
package com._4psa.billingmessages_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com._4psa.billingdata_xsd._2_5.ChargingPlanInfo;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/BillingData.xsd/2.5.1}ChargingPlanInfo">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="userID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *           &lt;element name="userIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *           &lt;element name="userLogin" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;/choice>
 *         &lt;element name="forceChannelRule" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="exception" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="areaCode" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                   &lt;element name="description" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                   &lt;element name="charge" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="initialAmount" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *                             &lt;element name="initialInterval" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *                             &lt;element name="amount" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *                             &lt;element name="interval" type="{http://4psa.com/Common.xsd/2.5.1}integer"/>
 *                             &lt;element name="setupAmount" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="package" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *                             &lt;element name="minutes" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="scope" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="signature" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
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
    "userID",
    "userIdentifier",
    "userLogin",
    "forceChannelRule",
    "exception",
    "scope",
    "signature"
})
@XmlRootElement(name = "AddChargingPlanRequest")
public class AddChargingPlanRequest
    extends ChargingPlanInfo
{

    protected BigInteger userID;
    protected String userIdentifier;
    protected String userLogin;
    @XmlElement(defaultValue = "1")
    protected Boolean forceChannelRule;
    protected List<AddChargingPlanRequest.Exception> exception;
    protected String scope;
    protected String signature;

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
     * Gets the value of the userLogin property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserLogin() {
        return userLogin;
    }

    /**
     * Sets the value of the userLogin property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserLogin(String value) {
        this.userLogin = value;
    }

    /**
     * Gets the value of the forceChannelRule property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isForceChannelRule() {
        return forceChannelRule;
    }

    /**
     * Sets the value of the forceChannelRule property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setForceChannelRule(Boolean value) {
        this.forceChannelRule = value;
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
     * {@link AddChargingPlanRequest.Exception }
     *
     *
     */
    public List<AddChargingPlanRequest.Exception> getException() {
        if (exception == null) {
            exception = new ArrayList<AddChargingPlanRequest.Exception>();
        }
        return this.exception;
    }

    /**
     * Gets the value of the scope property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getScope() {
        return scope;
    }

    /**
     * Sets the value of the scope property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setScope(String value) {
        this.scope = value;
    }

    /**
     * Gets the value of the signature property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Sets the value of the signature property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSignature(String value) {
        this.signature = value;
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
     *         &lt;element name="areaCode" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
     *         &lt;element name="description" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
     *         &lt;element name="charge" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="initialAmount" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
     *                   &lt;element name="initialInterval" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
     *                   &lt;element name="amount" type="{http://www.w3.org/2001/XMLSchema}float"/>
     *                   &lt;element name="interval" type="{http://4psa.com/Common.xsd/2.5.1}integer"/>
     *                   &lt;element name="setupAmount" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="package" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
     *                   &lt;element name="minutes" type="{http://www.w3.org/2001/XMLSchema}float"/>
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
    @XmlType(name = "", propOrder = {
        "areaCode",
        "description",
        "charge",
        "_package"
    })
    public static class Exception {

        protected String areaCode;
        protected String description;
        protected AddChargingPlanRequest.Exception.Charge charge;
        @XmlElement(name = "package")
        protected AddChargingPlanRequest.Exception.Package _package;

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
         *     {@link AddChargingPlanRequest.Exception.Charge }
         *
         */
        public AddChargingPlanRequest.Exception.Charge getCharge() {
            return charge;
        }

        /**
         * Sets the value of the charge property.
         *
         * @param value
         *     allowed object is
         *     {@link AddChargingPlanRequest.Exception.Charge }
         *
         */
        public void setCharge(AddChargingPlanRequest.Exception.Charge value) {
            this.charge = value;
        }

        /**
         * Gets the value of the package property.
         *
         * @return
         *     possible object is
         *     {@link AddChargingPlanRequest.Exception.Package }
         *
         */
        public AddChargingPlanRequest.Exception.Package getPackage() {
            return _package;
        }

        /**
         * Sets the value of the package property.
         *
         * @param value
         *     allowed object is
         *     {@link AddChargingPlanRequest.Exception.Package }
         *
         */
        public void setPackage(AddChargingPlanRequest.Exception.Package value) {
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
         *         &lt;element name="amount" type="{http://www.w3.org/2001/XMLSchema}float"/>
         *         &lt;element name="interval" type="{http://4psa.com/Common.xsd/2.5.1}integer"/>
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
            protected float amount;
            @XmlElement(required = true)
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
             */
            public float getAmount() {
                return amount;
            }

            /**
             * Sets the value of the amount property.
             *
             */
            public void setAmount(float value) {
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

}

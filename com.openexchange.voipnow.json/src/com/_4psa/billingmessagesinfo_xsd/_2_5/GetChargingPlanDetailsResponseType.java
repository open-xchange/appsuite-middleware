
package com._4psa.billingmessagesinfo_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com._4psa.billingdata_xsd._2_5.ChargingPlanInfo;
import com._4psa.common_xsd._2_5.Notice;


/**
 * Get charging plan details: response type
 *
 * <p>Java class for GetChargingPlanDetailsResponseType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="GetChargingPlanDetailsResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="chargingPlan" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://4psa.com/BillingData.xsd/2.5.1}ChargingPlanInfo">
 *                 &lt;sequence>
 *                   &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *                   &lt;element name="identifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                   &lt;element name="userID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *                   &lt;element name="userLogin" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                   &lt;element name="status" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *                   &lt;element name="originalIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                   &lt;element name="signature" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/extension>
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
@XmlType(name = "GetChargingPlanDetailsResponseType", propOrder = {
    "chargingPlan",
    "notice"
})
public class GetChargingPlanDetailsResponseType {

    protected GetChargingPlanDetailsResponseType.ChargingPlan chargingPlan;
    protected List<Notice> notice;

    /**
     * Gets the value of the chargingPlan property.
     *
     * @return
     *     possible object is
     *     {@link GetChargingPlanDetailsResponseType.ChargingPlan }
     *
     */
    public GetChargingPlanDetailsResponseType.ChargingPlan getChargingPlan() {
        return chargingPlan;
    }

    /**
     * Sets the value of the chargingPlan property.
     *
     * @param value
     *     allowed object is
     *     {@link GetChargingPlanDetailsResponseType.ChargingPlan }
     *
     */
    public void setChargingPlan(GetChargingPlanDetailsResponseType.ChargingPlan value) {
        this.chargingPlan = value;
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
     *     &lt;extension base="{http://4psa.com/BillingData.xsd/2.5.1}ChargingPlanInfo">
     *       &lt;sequence>
     *         &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
     *         &lt;element name="identifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
     *         &lt;element name="userID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
     *         &lt;element name="userLogin" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
     *         &lt;element name="status" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
     *         &lt;element name="originalIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
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
        "id",
        "identifier",
        "userID",
        "userLogin",
        "status",
        "originalIdentifier",
        "signature"
    })
    public static class ChargingPlan
        extends ChargingPlanInfo
    {

        @XmlElement(name = "ID")
        protected BigInteger id;
        protected String identifier;
        protected BigInteger userID;
        protected String userLogin;
        protected Boolean status;
        protected String originalIdentifier;
        protected String signature;

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
         * Gets the value of the identifier property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getIdentifier() {
            return identifier;
        }

        /**
         * Sets the value of the identifier property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setIdentifier(String value) {
            this.identifier = value;
        }

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
         * Gets the value of the status property.
         *
         * @return
         *     possible object is
         *     {@link Boolean }
         *
         */
        public Boolean isStatus() {
            return status;
        }

        /**
         * Sets the value of the status property.
         *
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *
         */
        public void setStatus(Boolean value) {
            this.status = value;
        }

        /**
         * Gets the value of the originalIdentifier property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getOriginalIdentifier() {
            return originalIdentifier;
        }

        /**
         * Sets the value of the originalIdentifier property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setOriginalIdentifier(String value) {
            this.originalIdentifier = value;
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

    }

}

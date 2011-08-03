
package com._4psa.billingmessages_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com._4psa.billingmessagesinfo_xsd._2_5.GetRechargesResponseType;


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
 *         &lt;element name="recharge" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://4psa.com/BillingMessagesInfo.xsd/2.5.1}GetRechargesResponseType">
 *                 &lt;sequence>
 *                   &lt;element name="userID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *                   &lt;element name="userIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/extension>
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
    "recharge"
})
@XmlRootElement(name = "GetRechargesResponse")
public class GetRechargesResponse {

    protected List<GetRechargesResponse.Recharge> recharge;

    /**
     * Gets the value of the recharge property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the recharge property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRecharge().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GetRechargesResponse.Recharge }
     *
     *
     */
    public List<GetRechargesResponse.Recharge> getRecharge() {
        if (recharge == null) {
            recharge = new ArrayList<GetRechargesResponse.Recharge>();
        }
        return this.recharge;
    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://4psa.com/BillingMessagesInfo.xsd/2.5.1}GetRechargesResponseType">
     *       &lt;sequence>
     *         &lt;element name="userID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
     *         &lt;element name="userIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
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
        "userIdentifier"
    })
    public static class Recharge
        extends GetRechargesResponseType
    {

        protected BigInteger userID;
        protected String userIdentifier;

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

    }

}

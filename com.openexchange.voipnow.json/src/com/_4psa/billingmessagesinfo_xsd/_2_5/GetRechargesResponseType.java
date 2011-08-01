
package com._4psa.billingmessagesinfo_xsd._2_5;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.billingdata_xsd._2_5.CreditsList;
import com._4psa.billingdata_xsd._2_5.LimitsList;
import com._4psa.common_xsd._2_5.Notice;
import com._4psa.common_xsd._2_5.UnlimitedUFloat;


/**
 * Recharge : response type
 *
 * <p>Java class for GetRechargesResponseType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="GetRechargesResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="credit" maxOccurs="unbounded" minOccurs="0">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;extension base="{http://4psa.com/BillingData.xsd/2.5.1}CreditsList">
 *                 &lt;/extension>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *           &lt;element name="limit" type="{http://4psa.com/BillingData.xsd/2.5.1}LimitsList" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;/choice>
 *         &lt;element name="planType" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="prepaid"/>
 *               &lt;enumeration value="postpaid"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="currentIn" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedUFloat" minOccurs="0"/>
 *         &lt;element name="currentOut" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedUFloat" minOccurs="0"/>
 *         &lt;element name="currentOverusage" type="{http://4psa.com/Common.xsd/2.5.1}unlimitedUFloat" minOccurs="0"/>
 *         &lt;element name="notice" type="{http://4psa.com/Common.xsd/2.5.1}notice" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="currency" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetRechargesResponseType", propOrder = {
    "credit",
    "limit",
    "planType",
    "currentIn",
    "currentOut",
    "currentOverusage",
    "notice",
    "currency"
})
@XmlSeeAlso({
    com._4psa.billingmessages_xsd._2_5.GetRechargesResponse.Recharge.class
})
public class GetRechargesResponseType {

    protected List<GetRechargesResponseType.Credit> credit;
    protected List<LimitsList> limit;
    @XmlElement(defaultValue = "postpaid")
    protected String planType;
    protected UnlimitedUFloat currentIn;
    protected UnlimitedUFloat currentOut;
    protected UnlimitedUFloat currentOverusage;
    protected List<Notice> notice;
    protected String currency;

    /**
     * Gets the value of the credit property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the credit property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCredit().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GetRechargesResponseType.Credit }
     *
     *
     */
    public List<GetRechargesResponseType.Credit> getCredit() {
        if (credit == null) {
            credit = new ArrayList<GetRechargesResponseType.Credit>();
        }
        return this.credit;
    }

    /**
     * Gets the value of the limit property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the limit property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLimit().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LimitsList }
     *
     *
     */
    public List<LimitsList> getLimit() {
        if (limit == null) {
            limit = new ArrayList<LimitsList>();
        }
        return this.limit;
    }

    /**
     * Gets the value of the planType property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPlanType() {
        return planType;
    }

    /**
     * Sets the value of the planType property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPlanType(String value) {
        this.planType = value;
    }

    /**
     * Gets the value of the currentIn property.
     *
     * @return
     *     possible object is
     *     {@link UnlimitedUFloat }
     *
     */
    public UnlimitedUFloat getCurrentIn() {
        return currentIn;
    }

    /**
     * Sets the value of the currentIn property.
     *
     * @param value
     *     allowed object is
     *     {@link UnlimitedUFloat }
     *
     */
    public void setCurrentIn(UnlimitedUFloat value) {
        this.currentIn = value;
    }

    /**
     * Gets the value of the currentOut property.
     *
     * @return
     *     possible object is
     *     {@link UnlimitedUFloat }
     *
     */
    public UnlimitedUFloat getCurrentOut() {
        return currentOut;
    }

    /**
     * Sets the value of the currentOut property.
     *
     * @param value
     *     allowed object is
     *     {@link UnlimitedUFloat }
     *
     */
    public void setCurrentOut(UnlimitedUFloat value) {
        this.currentOut = value;
    }

    /**
     * Gets the value of the currentOverusage property.
     *
     * @return
     *     possible object is
     *     {@link UnlimitedUFloat }
     *
     */
    public UnlimitedUFloat getCurrentOverusage() {
        return currentOverusage;
    }

    /**
     * Sets the value of the currentOverusage property.
     *
     * @param value
     *     allowed object is
     *     {@link UnlimitedUFloat }
     *
     */
    public void setCurrentOverusage(UnlimitedUFloat value) {
        this.currentOverusage = value;
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
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://4psa.com/BillingData.xsd/2.5.1}CreditsList">
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Credit
        extends CreditsList
    {


    }

}

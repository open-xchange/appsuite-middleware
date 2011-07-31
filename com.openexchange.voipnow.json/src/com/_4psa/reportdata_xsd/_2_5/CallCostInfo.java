
package com._4psa.reportdata_xsd._2_5;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.reportmessagesinfo_xsd._2_5.CallCostsResponseType;


/**
 * Call cost data
 *
 * <p>Java class for CallCostInfo complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="CallCostInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cost" type="{http://4psa.com/Common.xsd/2.5.1}float" minOccurs="0"/>
 *         &lt;element name="profit" type="{http://4psa.com/Common.xsd/2.5.1}float" minOccurs="0"/>
 *         &lt;element name="currency" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="totalCalls" type="{http://4psa.com/Common.xsd/2.5.1}unsignedInt" minOccurs="0"/>
 *         &lt;element name="localCall" type="{http://4psa.com/Common.xsd/2.5.1}unsignedInt" minOccurs="0"/>
 *         &lt;element name="elocalCall" type="{http://4psa.com/Common.xsd/2.5.1}unsignedInt" minOccurs="0"/>
 *         &lt;element name="externalCall" type="{http://4psa.com/Common.xsd/2.5.1}unsignedInt" minOccurs="0"/>
 *         &lt;element name="incomingCall" type="{http://4psa.com/Common.xsd/2.5.1}unsignedInt" minOccurs="0"/>
 *         &lt;element name="outgoingCall" type="{http://4psa.com/Common.xsd/2.5.1}unsignedInt" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CallCostInfo", propOrder = {
    "cost",
    "profit",
    "currency",
    "totalCalls",
    "localCall",
    "elocalCall",
    "externalCall",
    "incomingCall",
    "outgoingCall"
})
@XmlSeeAlso({
    CallCostsResponseType.class
})
public class CallCostInfo {

    @XmlElementRef(name = "cost", namespace = "http://4psa.com/ReportData.xsd/2.5.1", type = JAXBElement.class)
    protected JAXBElement<Float> cost;
    @XmlElementRef(name = "profit", namespace = "http://4psa.com/ReportData.xsd/2.5.1", type = JAXBElement.class)
    protected JAXBElement<Float> profit;
    protected String currency;
    protected Long totalCalls;
    protected Long localCall;
    protected Long elocalCall;
    protected Long externalCall;
    protected Long incomingCall;
    protected Long outgoingCall;

    /**
     * Gets the value of the cost property.
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Float }{@code >}
     *
     */
    public JAXBElement<Float> getCost() {
        return cost;
    }

    /**
     * Sets the value of the cost property.
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Float }{@code >}
     *
     */
    public void setCost(JAXBElement<Float> value) {
        this.cost = value;
    }

    /**
     * Gets the value of the profit property.
     *
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Float }{@code >}
     *
     */
    public JAXBElement<Float> getProfit() {
        return profit;
    }

    /**
     * Sets the value of the profit property.
     *
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Float }{@code >}
     *
     */
    public void setProfit(JAXBElement<Float> value) {
        this.profit = value;
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
     * Gets the value of the totalCalls property.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public Long getTotalCalls() {
        return totalCalls;
    }

    /**
     * Sets the value of the totalCalls property.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setTotalCalls(Long value) {
        this.totalCalls = value;
    }

    /**
     * Gets the value of the localCall property.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public Long getLocalCall() {
        return localCall;
    }

    /**
     * Sets the value of the localCall property.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setLocalCall(Long value) {
        this.localCall = value;
    }

    /**
     * Gets the value of the elocalCall property.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public Long getElocalCall() {
        return elocalCall;
    }

    /**
     * Sets the value of the elocalCall property.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setElocalCall(Long value) {
        this.elocalCall = value;
    }

    /**
     * Gets the value of the externalCall property.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public Long getExternalCall() {
        return externalCall;
    }

    /**
     * Sets the value of the externalCall property.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setExternalCall(Long value) {
        this.externalCall = value;
    }

    /**
     * Gets the value of the incomingCall property.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public Long getIncomingCall() {
        return incomingCall;
    }

    /**
     * Sets the value of the incomingCall property.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setIncomingCall(Long value) {
        this.incomingCall = value;
    }

    /**
     * Gets the value of the outgoingCall property.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public Long getOutgoingCall() {
        return outgoingCall;
    }

    /**
     * Sets the value of the outgoingCall property.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setOutgoingCall(Long value) {
        this.outgoingCall = value;
    }

}

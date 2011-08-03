
package com._4psa.extensiondata_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * Incoming call rule data
 *
 * <p>Java class for CallRuleTransferInfo complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="CallRuleTransferInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="transferNumber" type="{http://4psa.com/Common.xsd/2.5.1}rule" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ring" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="call" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="askForCaller" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="transferFromCallee" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CallRuleTransferInfo", propOrder = {
    "transferNumber",
    "ring",
    "call",
    "askForCaller",
    "transferFromCallee"
})
public class CallRuleTransferInfo {

    protected List<String> transferNumber;
    protected BigInteger ring;
    protected Boolean call;
    protected Boolean askForCaller;
    protected Boolean transferFromCallee;

    /**
     * Gets the value of the transferNumber property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the transferNumber property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTransferNumber().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     *
     *
     */
    public List<String> getTransferNumber() {
        if (transferNumber == null) {
            transferNumber = new ArrayList<String>();
        }
        return this.transferNumber;
    }

    /**
     * Gets the value of the ring property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getRing() {
        return ring;
    }

    /**
     * Sets the value of the ring property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setRing(BigInteger value) {
        this.ring = value;
    }

    /**
     * Gets the value of the call property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isCall() {
        return call;
    }

    /**
     * Sets the value of the call property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setCall(Boolean value) {
        this.call = value;
    }

    /**
     * Gets the value of the askForCaller property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isAskForCaller() {
        return askForCaller;
    }

    /**
     * Sets the value of the askForCaller property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setAskForCaller(Boolean value) {
        this.askForCaller = value;
    }

    /**
     * Gets the value of the transferFromCallee property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isTransferFromCallee() {
        return transferFromCallee;
    }

    /**
     * Sets the value of the transferFromCallee property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setTransferFromCallee(Boolean value) {
        this.transferFromCallee = value;
    }

}

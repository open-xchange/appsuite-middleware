
package com._4psa.extensionmessagesinfo_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.extensiondata_xsd._2_5.RechargeInfo;
import com._4psa.extensionmessages_xsd._2_5.GetCardCodeRechargesResponse;


/**
 * Get calling card recharges history: response type
 *
 * <p>Java class for GetCardCodeCreditResponseType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="GetCardCodeCreditResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="codeID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *         &lt;element name="recharge" type="{http://4psa.com/ExtensionData.xsd/2.5.1}RechargeInfo" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetCardCodeCreditResponseType", propOrder = {
    "codeID",
    "recharge"
})
@XmlSeeAlso({
    GetCardCodeRechargesResponse.class
})
public class GetCardCodeCreditResponseType {

    @XmlElement(required = true)
    protected BigInteger codeID;
    protected List<RechargeInfo> recharge;

    /**
     * Gets the value of the codeID property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getCodeID() {
        return codeID;
    }

    /**
     * Sets the value of the codeID property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setCodeID(BigInteger value) {
        this.codeID = value;
    }

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
     * {@link RechargeInfo }
     *
     *
     */
    public List<RechargeInfo> getRecharge() {
        if (recharge == null) {
            recharge = new ArrayList<RechargeInfo>();
        }
        return this.recharge;
    }

}

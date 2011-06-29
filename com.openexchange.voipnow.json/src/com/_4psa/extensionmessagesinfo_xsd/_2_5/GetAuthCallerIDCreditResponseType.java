
package com._4psa.extensionmessagesinfo_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com._4psa.extensiondata_xsd._2_5.RechargeInfo;


/**
 * Get authorized Caller ID recharges history: response type
 * 
 * <p>Java class for GetAuthCallerIDCreditResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetAuthCallerIDCreditResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
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
@XmlType(name = "GetAuthCallerIDCreditResponseType", propOrder = {
    "id",
    "recharge"
})
public class GetAuthCallerIDCreditResponseType {

    @XmlElement(name = "ID", required = true)
    protected BigInteger id;
    protected List<RechargeInfo> recharge;

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

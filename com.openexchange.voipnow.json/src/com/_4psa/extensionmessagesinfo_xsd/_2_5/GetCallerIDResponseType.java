
package com._4psa.extensionmessagesinfo_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com._4psa.extensiondata_xsd._2_5.CallerIDList;


/**
 * Get authorized Caller ID: response type
 * 
 * <p>Java class for GetCallerIDResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetCallerIDResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CallerID" type="{http://4psa.com/ExtensionData.xsd/2.5.1}CallerIDList" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="userID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetCallerIDResponseType", propOrder = {
    "callerID",
    "userID"
})
public class GetCallerIDResponseType {

    @XmlElement(name = "CallerID")
    protected List<CallerIDList> callerID;
    protected BigInteger userID;

    /**
     * Gets the value of the callerID property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the callerID property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCallerID().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CallerIDList }
     * 
     * 
     */
    public List<CallerIDList> getCallerID() {
        if (callerID == null) {
            callerID = new ArrayList<CallerIDList>();
        }
        return this.callerID;
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

}

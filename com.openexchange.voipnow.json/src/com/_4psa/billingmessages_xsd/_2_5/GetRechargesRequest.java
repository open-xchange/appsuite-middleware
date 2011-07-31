
package com._4psa.billingmessages_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="userID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="userIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/choice>
 *     &lt;/restriction>
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
@XmlRootElement(name = "GetRechargesRequest")
public class GetRechargesRequest {

    protected List<BigInteger> userID;
    protected List<String> userIdentifier;

    /**
     * Gets the value of the userID property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the userID property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUserID().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BigInteger }
     *
     *
     */
    public List<BigInteger> getUserID() {
        if (userID == null) {
            userID = new ArrayList<BigInteger>();
        }
        return this.userID;
    }

    /**
     * Gets the value of the userIdentifier property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the userIdentifier property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUserIdentifier().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     *
     *
     */
    public List<String> getUserIdentifier() {
        if (userIdentifier == null) {
            userIdentifier = new ArrayList<String>();
        }
        return this.userIdentifier;
    }

}

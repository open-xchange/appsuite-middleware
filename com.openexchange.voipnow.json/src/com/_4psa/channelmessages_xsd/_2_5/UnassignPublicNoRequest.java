
package com._4psa.channelmessages_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 *       &lt;sequence>
 *         &lt;element name="didID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" maxOccurs="unbounded"/>
 *         &lt;choice>
 *           &lt;element name="userID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *           &lt;element name="userIdentifier" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *         &lt;/choice>
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
    "didID",
    "userID",
    "userIdentifier"
})
@XmlRootElement(name = "UnassignPublicNoRequest")
public class UnassignPublicNoRequest {

    @XmlElement(required = true)
    protected List<BigInteger> didID;
    protected BigInteger userID;
    protected String userIdentifier;

    /**
     * Gets the value of the didID property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the didID property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDidID().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BigInteger }
     *
     *
     */
    public List<BigInteger> getDidID() {
        if (didID == null) {
            didID = new ArrayList<BigInteger>();
        }
        return this.didID;
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

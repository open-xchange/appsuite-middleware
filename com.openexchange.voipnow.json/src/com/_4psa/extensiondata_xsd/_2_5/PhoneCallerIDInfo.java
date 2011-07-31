
package com._4psa.extensiondata_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * CallerID received in call information
 *
 * <p>Java class for PhoneCallerIDInfo complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="PhoneCallerIDInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sendCallerID" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="1"/>
 *               &lt;enumeration value="0"/>
 *               &lt;enumeration value="3"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="callerName" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *         &lt;element name="sendCallerNo" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="1"/>
 *               &lt;enumeration value="0"/>
 *               &lt;enumeration value="3"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="callerNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="defaultCallerIDRef" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;element name="callerIDRefs" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="anonymous" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="callerIDInternal" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="callerIDOnTransfer" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="SIPIdentity" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PhoneCallerIDInfo", propOrder = {
    "sendCallerID",
    "callerName",
    "sendCallerNo",
    "callerNumber",
    "defaultCallerIDRef",
    "callerIDRefs",
    "anonymous",
    "callerIDInternal",
    "callerIDOnTransfer",
    "sipIdentity"
})
@XmlSeeAlso({
    QueueCenterInfo.class,
    ConferenceInfo.class,
    PhoneTerminal.class,
    Queue.class,
    IVR.class
})
public class PhoneCallerIDInfo {

    @XmlElement(defaultValue = "1")
    protected String sendCallerID;
    protected String callerName;
    @XmlElement(defaultValue = "1")
    protected String sendCallerNo;
    protected String callerNumber;
    protected BigInteger defaultCallerIDRef;
    protected List<BigInteger> callerIDRefs;
    @XmlElement(defaultValue = "false")
    protected Boolean anonymous;
    protected Boolean callerIDInternal;
    @XmlElement(defaultValue = "false")
    protected Boolean callerIDOnTransfer;
    @XmlElement(name = "SIPIdentity")
    protected Boolean sipIdentity;

    /**
     * Gets the value of the sendCallerID property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSendCallerID() {
        return sendCallerID;
    }

    /**
     * Sets the value of the sendCallerID property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSendCallerID(String value) {
        this.sendCallerID = value;
    }

    /**
     * Gets the value of the callerName property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCallerName() {
        return callerName;
    }

    /**
     * Sets the value of the callerName property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCallerName(String value) {
        this.callerName = value;
    }

    /**
     * Gets the value of the sendCallerNo property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSendCallerNo() {
        return sendCallerNo;
    }

    /**
     * Sets the value of the sendCallerNo property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSendCallerNo(String value) {
        this.sendCallerNo = value;
    }

    /**
     * Gets the value of the callerNumber property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCallerNumber() {
        return callerNumber;
    }

    /**
     * Sets the value of the callerNumber property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCallerNumber(String value) {
        this.callerNumber = value;
    }

    /**
     * Gets the value of the defaultCallerIDRef property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getDefaultCallerIDRef() {
        return defaultCallerIDRef;
    }

    /**
     * Sets the value of the defaultCallerIDRef property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setDefaultCallerIDRef(BigInteger value) {
        this.defaultCallerIDRef = value;
    }

    /**
     * Gets the value of the callerIDRefs property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the callerIDRefs property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCallerIDRefs().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BigInteger }
     *
     *
     */
    public List<BigInteger> getCallerIDRefs() {
        if (callerIDRefs == null) {
            callerIDRefs = new ArrayList<BigInteger>();
        }
        return this.callerIDRefs;
    }

    /**
     * Gets the value of the anonymous property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isAnonymous() {
        return anonymous;
    }

    /**
     * Sets the value of the anonymous property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setAnonymous(Boolean value) {
        this.anonymous = value;
    }

    /**
     * Gets the value of the callerIDInternal property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isCallerIDInternal() {
        return callerIDInternal;
    }

    /**
     * Sets the value of the callerIDInternal property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setCallerIDInternal(Boolean value) {
        this.callerIDInternal = value;
    }

    /**
     * Gets the value of the callerIDOnTransfer property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isCallerIDOnTransfer() {
        return callerIDOnTransfer;
    }

    /**
     * Sets the value of the callerIDOnTransfer property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setCallerIDOnTransfer(Boolean value) {
        this.callerIDOnTransfer = value;
    }

    /**
     * Gets the value of the sipIdentity property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isSIPIdentity() {
        return sipIdentity;
    }

    /**
     * Sets the value of the sipIdentity property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setSIPIdentity(Boolean value) {
        this.sipIdentity = value;
    }

}

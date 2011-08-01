
package com._4psa.extensionmessagesinfo_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com._4psa.common_xsd._2_5.Notice;


/**
 * Get available Caller ID list for phone terminal extension: response type
 *
 * <p>Java class for GetAvailableCallerIDResponseType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="GetAvailableCallerIDResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CallerID" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="extensionNo" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *                   &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *                   &lt;element name="phoneNo" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *                   &lt;element name="CallerIDRef" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="notice" type="{http://4psa.com/Common.xsd/2.5.1}notice" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetAvailableCallerIDResponseType", propOrder = {
    "callerID",
    "notice"
})
public class GetAvailableCallerIDResponseType {

    @XmlElement(name = "CallerID")
    protected List<GetAvailableCallerIDResponseType.CallerID> callerID;
    protected List<Notice> notice;

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
     * {@link GetAvailableCallerIDResponseType.CallerID }
     *
     *
     */
    public List<GetAvailableCallerIDResponseType.CallerID> getCallerID() {
        if (callerID == null) {
            callerID = new ArrayList<GetAvailableCallerIDResponseType.CallerID>();
        }
        return this.callerID;
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
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="extensionNo" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
     *         &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
     *         &lt;element name="phoneNo" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
     *         &lt;element name="CallerIDRef" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
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
        "extensionNo",
        "name",
        "phoneNo",
        "callerIDRef"
    })
    public static class CallerID {

        @XmlElement(required = true)
        protected String extensionNo;
        @XmlElement(required = true)
        protected String name;
        @XmlElement(required = true)
        protected String phoneNo;
        @XmlElement(name = "CallerIDRef", required = true)
        protected BigInteger callerIDRef;

        /**
         * Gets the value of the extensionNo property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getExtensionNo() {
            return extensionNo;
        }

        /**
         * Sets the value of the extensionNo property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setExtensionNo(String value) {
            this.extensionNo = value;
        }

        /**
         * Gets the value of the name property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the value of the name property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setName(String value) {
            this.name = value;
        }

        /**
         * Gets the value of the phoneNo property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getPhoneNo() {
            return phoneNo;
        }

        /**
         * Sets the value of the phoneNo property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setPhoneNo(String value) {
            this.phoneNo = value;
        }

        /**
         * Gets the value of the callerIDRef property.
         *
         * @return
         *     possible object is
         *     {@link BigInteger }
         *
         */
        public BigInteger getCallerIDRef() {
            return callerIDRef;
        }

        /**
         * Sets the value of the callerIDRef property.
         *
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *
         */
        public void setCallerIDRef(BigInteger value) {
            this.callerIDRef = value;
        }

    }

}

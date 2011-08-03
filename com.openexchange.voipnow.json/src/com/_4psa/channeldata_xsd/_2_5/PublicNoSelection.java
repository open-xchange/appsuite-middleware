
package com._4psa.channeldata_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Public phone numbers selection list: available and assigned phone numbers for a certain user
 *
 * <p>Java class for PublicNoSelection complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="PublicNoSelection">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="available" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *                   &lt;element name="channel" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                   &lt;element name="externalNo" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="assigned" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *                   &lt;element name="channelID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *                   &lt;element name="channel" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                   &lt;element name="externalNo" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                   &lt;element name="did" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                   &lt;element name="cost" type="{http://4psa.com/Common.xsd/2.5.1}float" minOccurs="0"/>
 *                   &lt;element name="callbackExt" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                   &lt;element name="callbackExtID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *                   &lt;element name="flow" minOccurs="0">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;enumeration value="in"/>
 *                         &lt;enumeration value="out"/>
 *                         &lt;enumeration value="both"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                   &lt;element name="crDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PublicNoSelection", propOrder = {
    "available",
    "assigned"
})
public class PublicNoSelection {

    protected List<PublicNoSelection.Available> available;
    protected List<PublicNoSelection.Assigned> assigned;

    /**
     * Gets the value of the available property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the available property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAvailable().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PublicNoSelection.Available }
     *
     *
     */
    public List<PublicNoSelection.Available> getAvailable() {
        if (available == null) {
            available = new ArrayList<PublicNoSelection.Available>();
        }
        return this.available;
    }

    /**
     * Gets the value of the assigned property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the assigned property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAssigned().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PublicNoSelection.Assigned }
     *
     *
     */
    public List<PublicNoSelection.Assigned> getAssigned() {
        if (assigned == null) {
            assigned = new ArrayList<PublicNoSelection.Assigned>();
        }
        return this.assigned;
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
     *         &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
     *         &lt;element name="channelID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
     *         &lt;element name="channel" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
     *         &lt;element name="externalNo" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
     *         &lt;element name="did" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
     *         &lt;element name="cost" type="{http://4psa.com/Common.xsd/2.5.1}float" minOccurs="0"/>
     *         &lt;element name="callbackExt" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
     *         &lt;element name="callbackExtID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
     *         &lt;element name="flow" minOccurs="0">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;enumeration value="in"/>
     *               &lt;enumeration value="out"/>
     *               &lt;enumeration value="both"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
     *         &lt;/element>
     *         &lt;element name="crDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
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
        "id",
        "channelID",
        "channel",
        "externalNo",
        "did",
        "cost",
        "callbackExt",
        "callbackExtID",
        "flow",
        "crDate"
    })
    public static class Assigned {

        @XmlElement(name = "ID", required = true)
        protected BigInteger id;
        @XmlElement(required = true)
        protected BigInteger channelID;
        protected String channel;
        protected String externalNo;
        protected String did;
        protected Float cost;
        protected String callbackExt;
        protected BigInteger callbackExtID;
        @XmlElement(defaultValue = "both")
        protected String flow;
        @XmlSchemaType(name = "dateTime")
        protected XMLGregorianCalendar crDate;

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
         * Gets the value of the channelID property.
         *
         * @return
         *     possible object is
         *     {@link BigInteger }
         *
         */
        public BigInteger getChannelID() {
            return channelID;
        }

        /**
         * Sets the value of the channelID property.
         *
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *
         */
        public void setChannelID(BigInteger value) {
            this.channelID = value;
        }

        /**
         * Gets the value of the channel property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getChannel() {
            return channel;
        }

        /**
         * Sets the value of the channel property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setChannel(String value) {
            this.channel = value;
        }

        /**
         * Gets the value of the externalNo property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getExternalNo() {
            return externalNo;
        }

        /**
         * Sets the value of the externalNo property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setExternalNo(String value) {
            this.externalNo = value;
        }

        /**
         * Gets the value of the did property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getDid() {
            return did;
        }

        /**
         * Sets the value of the did property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setDid(String value) {
            this.did = value;
        }

        /**
         * Gets the value of the cost property.
         *
         * @return
         *     possible object is
         *     {@link Float }
         *
         */
        public Float getCost() {
            return cost;
        }

        /**
         * Sets the value of the cost property.
         *
         * @param value
         *     allowed object is
         *     {@link Float }
         *
         */
        public void setCost(Float value) {
            this.cost = value;
        }

        /**
         * Gets the value of the callbackExt property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getCallbackExt() {
            return callbackExt;
        }

        /**
         * Sets the value of the callbackExt property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setCallbackExt(String value) {
            this.callbackExt = value;
        }

        /**
         * Gets the value of the callbackExtID property.
         *
         * @return
         *     possible object is
         *     {@link BigInteger }
         *
         */
        public BigInteger getCallbackExtID() {
            return callbackExtID;
        }

        /**
         * Sets the value of the callbackExtID property.
         *
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *
         */
        public void setCallbackExtID(BigInteger value) {
            this.callbackExtID = value;
        }

        /**
         * Gets the value of the flow property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getFlow() {
            return flow;
        }

        /**
         * Sets the value of the flow property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setFlow(String value) {
            this.flow = value;
        }

        /**
         * Gets the value of the crDate property.
         *
         * @return
         *     possible object is
         *     {@link XMLGregorianCalendar }
         *
         */
        public XMLGregorianCalendar getCrDate() {
            return crDate;
        }

        /**
         * Sets the value of the crDate property.
         *
         * @param value
         *     allowed object is
         *     {@link XMLGregorianCalendar }
         *
         */
        public void setCrDate(XMLGregorianCalendar value) {
            this.crDate = value;
        }

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
     *         &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
     *         &lt;element name="channel" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
     *         &lt;element name="externalNo" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
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
        "id",
        "channel",
        "externalNo"
    })
    public static class Available {

        @XmlElement(name = "ID")
        protected BigInteger id;
        protected String channel;
        protected String externalNo;

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
         * Gets the value of the channel property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getChannel() {
            return channel;
        }

        /**
         * Sets the value of the channel property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setChannel(String value) {
            this.channel = value;
        }

        /**
         * Gets the value of the externalNo property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getExternalNo() {
            return externalNo;
        }

        /**
         * Sets the value of the externalNo property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setExternalNo(String value) {
            this.externalNo = value;
        }

    }

}

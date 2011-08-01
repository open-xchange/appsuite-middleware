
package com._4psa.channelmessagesinfo_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * Get channel codecs: response type
 *
 * <p>Java class for GetCodecsResponseType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="GetCodecsResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="codecs" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="assigned" type="{http://4psa.com/ChannelData.xsd/2.5.1}Codecs" maxOccurs="unbounded" minOccurs="0"/>
 *                   &lt;element name="available" type="{http://4psa.com/ChannelData.xsd/2.5.1}Codecs" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="channelID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetCodecsResponseType", propOrder = {
    "codecs",
    "channelID"
})
public class GetCodecsResponseType {

    protected GetCodecsResponseType.Codecs codecs;
    protected BigInteger channelID;

    /**
     * Gets the value of the codecs property.
     *
     * @return
     *     possible object is
     *     {@link GetCodecsResponseType.Codecs }
     *
     */
    public GetCodecsResponseType.Codecs getCodecs() {
        return codecs;
    }

    /**
     * Sets the value of the codecs property.
     *
     * @param value
     *     allowed object is
     *     {@link GetCodecsResponseType.Codecs }
     *
     */
    public void setCodecs(GetCodecsResponseType.Codecs value) {
        this.codecs = value;
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
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="assigned" type="{http://4psa.com/ChannelData.xsd/2.5.1}Codecs" maxOccurs="unbounded" minOccurs="0"/>
     *         &lt;element name="available" type="{http://4psa.com/ChannelData.xsd/2.5.1}Codecs" maxOccurs="unbounded" minOccurs="0"/>
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
        "assigned",
        "available"
    })
    public static class Codecs {

        protected List<com._4psa.channeldata_xsd._2_5.Codecs> assigned;
        protected List<com._4psa.channeldata_xsd._2_5.Codecs> available;

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
         * {@link com._4psa.channeldata_xsd._2_5.Codecs }
         *
         *
         */
        public List<com._4psa.channeldata_xsd._2_5.Codecs> getAssigned() {
            if (assigned == null) {
                assigned = new ArrayList<com._4psa.channeldata_xsd._2_5.Codecs>();
            }
            return this.assigned;
        }

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
         * {@link com._4psa.channeldata_xsd._2_5.Codecs }
         *
         *
         */
        public List<com._4psa.channeldata_xsd._2_5.Codecs> getAvailable() {
            if (available == null) {
                available = new ArrayList<com._4psa.channeldata_xsd._2_5.Codecs>();
            }
            return this.available;
        }

    }

}

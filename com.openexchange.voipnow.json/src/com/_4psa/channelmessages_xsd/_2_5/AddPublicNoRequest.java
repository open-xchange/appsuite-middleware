
package com._4psa.channelmessages_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com._4psa.channeldata_xsd._2_5.PublicNoInfo;


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
 *         &lt;element name="publicNo" maxOccurs="10">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://4psa.com/ChannelData.xsd/2.5.1}PublicNoInfo">
 *                 &lt;sequence>
 *                   &lt;element name="channelID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *                 &lt;/sequence>
 *               &lt;/extension>
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
@XmlType(name = "", propOrder = {
    "publicNo"
})
@XmlRootElement(name = "AddPublicNoRequest")
public class AddPublicNoRequest {

    @XmlElement(required = true)
    protected List<AddPublicNoRequest.PublicNo> publicNo;

    /**
     * Gets the value of the publicNo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the publicNo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPublicNo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AddPublicNoRequest.PublicNo }
     * 
     * 
     */
    public List<AddPublicNoRequest.PublicNo> getPublicNo() {
        if (publicNo == null) {
            publicNo = new ArrayList<AddPublicNoRequest.PublicNo>();
        }
        return this.publicNo;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://4psa.com/ChannelData.xsd/2.5.1}PublicNoInfo">
     *       &lt;sequence>
     *         &lt;element name="channelID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
     *       &lt;/sequence>
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "channelID"
    })
    public static class PublicNo
        extends PublicNoInfo
    {

        @XmlElement(required = true)
        protected BigInteger channelID;

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

    }

}

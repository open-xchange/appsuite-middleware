
package com._4psa.extensionmessagesinfo_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com._4psa.common_xsd._2_5.Notice;
import com._4psa.extensiondata_xsd._2_5.ExtensionPLInfo;


/**
 * Get extensions permissions and limits: response data
 *
 * <p>Java class for GetExtensionPLResponseType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="GetExtensionPLResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="extensionPL" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}ExtensionPLInfo">
 *                 &lt;sequence minOccurs="0">
 *                   &lt;element name="shareVoicemail" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="groupID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *                             &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="shareFaxes" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="groupID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *                             &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="shareRecordings" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="groupID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *                             &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="shareCallHistory" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="groupID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *                             &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/extension>
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
@XmlType(name = "GetExtensionPLResponseType", propOrder = {
    "extensionPL",
    "notice"
})
public class GetExtensionPLResponseType {

    protected GetExtensionPLResponseType.ExtensionPL extensionPL;
    protected List<Notice> notice;

    /**
     * Gets the value of the extensionPL property.
     *
     * @return
     *     possible object is
     *     {@link GetExtensionPLResponseType.ExtensionPL }
     *
     */
    public GetExtensionPLResponseType.ExtensionPL getExtensionPL() {
        return extensionPL;
    }

    /**
     * Sets the value of the extensionPL property.
     *
     * @param value
     *     allowed object is
     *     {@link GetExtensionPLResponseType.ExtensionPL }
     *
     */
    public void setExtensionPL(GetExtensionPLResponseType.ExtensionPL value) {
        this.extensionPL = value;
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
     *     &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}ExtensionPLInfo">
     *       &lt;sequence minOccurs="0">
     *         &lt;element name="shareVoicemail" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="groupID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
     *                   &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="shareFaxes" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="groupID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
     *                   &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="shareRecordings" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="groupID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
     *                   &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="shareCallHistory" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="groupID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
     *                   &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
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
        "shareVoicemail",
        "shareFaxes",
        "shareRecordings",
        "shareCallHistory"
    })
    public static class ExtensionPL
        extends ExtensionPLInfo
    {

        protected List<GetExtensionPLResponseType.ExtensionPL.ShareVoicemail> shareVoicemail;
        protected List<GetExtensionPLResponseType.ExtensionPL.ShareFaxes> shareFaxes;
        protected List<GetExtensionPLResponseType.ExtensionPL.ShareRecordings> shareRecordings;
        protected List<GetExtensionPLResponseType.ExtensionPL.ShareCallHistory> shareCallHistory;

        /**
         * Gets the value of the shareVoicemail property.
         *
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the shareVoicemail property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getShareVoicemail().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link GetExtensionPLResponseType.ExtensionPL.ShareVoicemail }
         *
         *
         */
        public List<GetExtensionPLResponseType.ExtensionPL.ShareVoicemail> getShareVoicemail() {
            if (shareVoicemail == null) {
                shareVoicemail = new ArrayList<GetExtensionPLResponseType.ExtensionPL.ShareVoicemail>();
            }
            return this.shareVoicemail;
        }

        /**
         * Gets the value of the shareFaxes property.
         *
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the shareFaxes property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getShareFaxes().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link GetExtensionPLResponseType.ExtensionPL.ShareFaxes }
         *
         *
         */
        public List<GetExtensionPLResponseType.ExtensionPL.ShareFaxes> getShareFaxes() {
            if (shareFaxes == null) {
                shareFaxes = new ArrayList<GetExtensionPLResponseType.ExtensionPL.ShareFaxes>();
            }
            return this.shareFaxes;
        }

        /**
         * Gets the value of the shareRecordings property.
         *
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the shareRecordings property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getShareRecordings().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link GetExtensionPLResponseType.ExtensionPL.ShareRecordings }
         *
         *
         */
        public List<GetExtensionPLResponseType.ExtensionPL.ShareRecordings> getShareRecordings() {
            if (shareRecordings == null) {
                shareRecordings = new ArrayList<GetExtensionPLResponseType.ExtensionPL.ShareRecordings>();
            }
            return this.shareRecordings;
        }

        /**
         * Gets the value of the shareCallHistory property.
         *
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the shareCallHistory property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getShareCallHistory().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link GetExtensionPLResponseType.ExtensionPL.ShareCallHistory }
         *
         *
         */
        public List<GetExtensionPLResponseType.ExtensionPL.ShareCallHistory> getShareCallHistory() {
            if (shareCallHistory == null) {
                shareCallHistory = new ArrayList<GetExtensionPLResponseType.ExtensionPL.ShareCallHistory>();
            }
            return this.shareCallHistory;
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
         *         &lt;element name="groupID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
         *         &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
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
            "groupID",
            "name"
        })
        public static class ShareCallHistory {

            @XmlElement(required = true)
            protected BigInteger groupID;
            @XmlElement(required = true)
            protected String name;

            /**
             * Gets the value of the groupID property.
             *
             * @return
             *     possible object is
             *     {@link BigInteger }
             *
             */
            public BigInteger getGroupID() {
                return groupID;
            }

            /**
             * Sets the value of the groupID property.
             *
             * @param value
             *     allowed object is
             *     {@link BigInteger }
             *
             */
            public void setGroupID(BigInteger value) {
                this.groupID = value;
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
         *         &lt;element name="groupID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
         *         &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
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
            "groupID",
            "name"
        })
        public static class ShareFaxes {

            @XmlElement(required = true)
            protected BigInteger groupID;
            @XmlElement(required = true)
            protected String name;

            /**
             * Gets the value of the groupID property.
             *
             * @return
             *     possible object is
             *     {@link BigInteger }
             *
             */
            public BigInteger getGroupID() {
                return groupID;
            }

            /**
             * Sets the value of the groupID property.
             *
             * @param value
             *     allowed object is
             *     {@link BigInteger }
             *
             */
            public void setGroupID(BigInteger value) {
                this.groupID = value;
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
         *         &lt;element name="groupID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
         *         &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
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
            "groupID",
            "name"
        })
        public static class ShareRecordings {

            @XmlElement(required = true)
            protected BigInteger groupID;
            @XmlElement(required = true)
            protected String name;

            /**
             * Gets the value of the groupID property.
             *
             * @return
             *     possible object is
             *     {@link BigInteger }
             *
             */
            public BigInteger getGroupID() {
                return groupID;
            }

            /**
             * Sets the value of the groupID property.
             *
             * @param value
             *     allowed object is
             *     {@link BigInteger }
             *
             */
            public void setGroupID(BigInteger value) {
                this.groupID = value;
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
         *         &lt;element name="groupID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
         *         &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
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
            "groupID",
            "name"
        })
        public static class ShareVoicemail {

            @XmlElement(required = true)
            protected BigInteger groupID;
            @XmlElement(required = true)
            protected String name;

            /**
             * Gets the value of the groupID property.
             *
             * @return
             *     possible object is
             *     {@link BigInteger }
             *
             */
            public BigInteger getGroupID() {
                return groupID;
            }

            /**
             * Sets the value of the groupID property.
             *
             * @param value
             *     allowed object is
             *     {@link BigInteger }
             *
             */
            public void setGroupID(BigInteger value) {
                this.groupID = value;
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

        }

    }

}

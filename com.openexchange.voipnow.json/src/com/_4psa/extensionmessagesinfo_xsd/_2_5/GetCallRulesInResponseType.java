
package com._4psa.extensionmessagesinfo_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com._4psa.common_xsd._2_5.Notice;
import com._4psa.extensiondata_xsd._2_5.CallRuleInfo;
import com._4psa.extensiondata_xsd._2_5.CallRuleTransferInfo;


/**
 * Get extension incoming call rules: response data
 * 
 * <p>Java class for GetCallRulesInResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetCallRulesInResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;sequence>
 *           &lt;element name="userID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *           &lt;element name="rules" maxOccurs="unbounded" minOccurs="0">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;sequence>
 *                     &lt;element name="ruleID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *                     &lt;element name="status" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *                     &lt;choice>
 *                       &lt;element name="busy" type="{http://4psa.com/ExtensionData.xsd/2.5.1}CallRuleInfo"/>
 *                       &lt;element name="congestion" type="{http://4psa.com/ExtensionData.xsd/2.5.1}CallRuleInfo"/>
 *                       &lt;element name="hangup" type="{http://4psa.com/ExtensionData.xsd/2.5.1}CallRuleInfo"/>
 *                       &lt;element name="transfer">
 *                         &lt;complexType>
 *                           &lt;complexContent>
 *                             &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}CallRuleInfo">
 *                               &lt;sequence>
 *                                 &lt;choice>
 *                                   &lt;element name="toNumbers" type="{http://4psa.com/ExtensionData.xsd/2.5.1}CallRuleTransferInfo"/>
 *                                   &lt;element name="toVoicemail" type="{http://4psa.com/Common.xsd/2.5.1}boolean"/>
 *                                 &lt;/choice>
 *                                 &lt;element name="callStatus" minOccurs="0">
 *                                   &lt;simpleType>
 *                                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *                                       &lt;enumeration value="0"/>
 *                                       &lt;enumeration value="1"/>
 *                                       &lt;enumeration value="2"/>
 *                                       &lt;enumeration value="3"/>
 *                                     &lt;/restriction>
 *                                   &lt;/simpleType>
 *                                 &lt;/element>
 *                                 &lt;element name="extensionStatus" minOccurs="0">
 *                                   &lt;simpleType>
 *                                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *                                       &lt;enumeration value="0"/>
 *                                       &lt;enumeration value="-1"/>
 *                                       &lt;enumeration value="1"/>
 *                                     &lt;/restriction>
 *                                   &lt;/simpleType>
 *                                 &lt;/element>
 *                                 &lt;element name="final" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *                               &lt;/sequence>
 *                             &lt;/extension>
 *                           &lt;/complexContent>
 *                         &lt;/complexType>
 *                       &lt;/element>
 *                       &lt;element name="cascade">
 *                         &lt;complexType>
 *                           &lt;complexContent>
 *                             &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}CallRuleInfo">
 *                               &lt;sequence>
 *                                 &lt;element name="ring" type="{http://4psa.com/Common.xsd/2.5.1}integer"/>
 *                                 &lt;element name="final" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *                                 &lt;element name="toNumbers" maxOccurs="10">
 *                                   &lt;complexType>
 *                                     &lt;complexContent>
 *                                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                         &lt;sequence>
 *                                           &lt;element name="number" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *                                           &lt;element name="ringAfter" type="{http://4psa.com/Common.xsd/2.5.1}integer"/>
 *                                         &lt;/sequence>
 *                                       &lt;/restriction>
 *                                     &lt;/complexContent>
 *                                   &lt;/complexType>
 *                                 &lt;/element>
 *                               &lt;/sequence>
 *                             &lt;/extension>
 *                           &lt;/complexContent>
 *                         &lt;/complexType>
 *                       &lt;/element>
 *                       &lt;element name="authenticate">
 *                         &lt;complexType>
 *                           &lt;complexContent>
 *                             &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}CallRuleInfo">
 *                               &lt;sequence>
 *                                 &lt;element name="password" type="{http://4psa.com/Common.xsd/2.5.1}password"/>
 *                                 &lt;element name="soundID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *                                 &lt;element name="final" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *                               &lt;/sequence>
 *                             &lt;/extension>
 *                           &lt;/complexContent>
 *                         &lt;/complexType>
 *                       &lt;/element>
 *                       &lt;element name="setCallPriority">
 *                         &lt;complexType>
 *                           &lt;complexContent>
 *                             &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}CallRuleInfo">
 *                               &lt;sequence>
 *                                 &lt;element name="priority" type="{http://4psa.com/Common.xsd/2.5.1}integer"/>
 *                               &lt;/sequence>
 *                             &lt;/extension>
 *                           &lt;/complexContent>
 *                         &lt;/complexType>
 *                       &lt;/element>
 *                     &lt;/choice>
 *                   &lt;/sequence>
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *         &lt;/sequence>
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
@XmlType(name = "GetCallRulesInResponseType", propOrder = {
    "userID",
    "rules",
    "notice"
})
public class GetCallRulesInResponseType {

    @XmlElement(required = true)
    protected BigInteger userID;
    protected List<GetCallRulesInResponseType.Rules> rules;
    protected List<Notice> notice;

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
     * Gets the value of the rules property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rules property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRules().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GetCallRulesInResponseType.Rules }
     * 
     * 
     */
    public List<GetCallRulesInResponseType.Rules> getRules() {
        if (rules == null) {
            rules = new ArrayList<GetCallRulesInResponseType.Rules>();
        }
        return this.rules;
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
     *         &lt;element name="ruleID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
     *         &lt;element name="status" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
     *         &lt;choice>
     *           &lt;element name="busy" type="{http://4psa.com/ExtensionData.xsd/2.5.1}CallRuleInfo"/>
     *           &lt;element name="congestion" type="{http://4psa.com/ExtensionData.xsd/2.5.1}CallRuleInfo"/>
     *           &lt;element name="hangup" type="{http://4psa.com/ExtensionData.xsd/2.5.1}CallRuleInfo"/>
     *           &lt;element name="transfer">
     *             &lt;complexType>
     *               &lt;complexContent>
     *                 &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}CallRuleInfo">
     *                   &lt;sequence>
     *                     &lt;choice>
     *                       &lt;element name="toNumbers" type="{http://4psa.com/ExtensionData.xsd/2.5.1}CallRuleTransferInfo"/>
     *                       &lt;element name="toVoicemail" type="{http://4psa.com/Common.xsd/2.5.1}boolean"/>
     *                     &lt;/choice>
     *                     &lt;element name="callStatus" minOccurs="0">
     *                       &lt;simpleType>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
     *                           &lt;enumeration value="0"/>
     *                           &lt;enumeration value="1"/>
     *                           &lt;enumeration value="2"/>
     *                           &lt;enumeration value="3"/>
     *                         &lt;/restriction>
     *                       &lt;/simpleType>
     *                     &lt;/element>
     *                     &lt;element name="extensionStatus" minOccurs="0">
     *                       &lt;simpleType>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
     *                           &lt;enumeration value="0"/>
     *                           &lt;enumeration value="-1"/>
     *                           &lt;enumeration value="1"/>
     *                         &lt;/restriction>
     *                       &lt;/simpleType>
     *                     &lt;/element>
     *                     &lt;element name="final" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
     *                   &lt;/sequence>
     *                 &lt;/extension>
     *               &lt;/complexContent>
     *             &lt;/complexType>
     *           &lt;/element>
     *           &lt;element name="cascade">
     *             &lt;complexType>
     *               &lt;complexContent>
     *                 &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}CallRuleInfo">
     *                   &lt;sequence>
     *                     &lt;element name="ring" type="{http://4psa.com/Common.xsd/2.5.1}integer"/>
     *                     &lt;element name="final" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
     *                     &lt;element name="toNumbers" maxOccurs="10">
     *                       &lt;complexType>
     *                         &lt;complexContent>
     *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                             &lt;sequence>
     *                               &lt;element name="number" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
     *                               &lt;element name="ringAfter" type="{http://4psa.com/Common.xsd/2.5.1}integer"/>
     *                             &lt;/sequence>
     *                           &lt;/restriction>
     *                         &lt;/complexContent>
     *                       &lt;/complexType>
     *                     &lt;/element>
     *                   &lt;/sequence>
     *                 &lt;/extension>
     *               &lt;/complexContent>
     *             &lt;/complexType>
     *           &lt;/element>
     *           &lt;element name="authenticate">
     *             &lt;complexType>
     *               &lt;complexContent>
     *                 &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}CallRuleInfo">
     *                   &lt;sequence>
     *                     &lt;element name="password" type="{http://4psa.com/Common.xsd/2.5.1}password"/>
     *                     &lt;element name="soundID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
     *                     &lt;element name="final" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
     *                   &lt;/sequence>
     *                 &lt;/extension>
     *               &lt;/complexContent>
     *             &lt;/complexType>
     *           &lt;/element>
     *           &lt;element name="setCallPriority">
     *             &lt;complexType>
     *               &lt;complexContent>
     *                 &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}CallRuleInfo">
     *                   &lt;sequence>
     *                     &lt;element name="priority" type="{http://4psa.com/Common.xsd/2.5.1}integer"/>
     *                   &lt;/sequence>
     *                 &lt;/extension>
     *               &lt;/complexContent>
     *             &lt;/complexType>
     *           &lt;/element>
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
        "ruleID",
        "status",
        "busy",
        "congestion",
        "hangup",
        "transfer",
        "cascade",
        "authenticate",
        "setCallPriority"
    })
    public static class Rules {

        @XmlElement(required = true)
        protected BigInteger ruleID;
        protected Boolean status;
        protected CallRuleInfo busy;
        protected CallRuleInfo congestion;
        protected CallRuleInfo hangup;
        protected GetCallRulesInResponseType.Rules.Transfer transfer;
        protected GetCallRulesInResponseType.Rules.Cascade cascade;
        protected GetCallRulesInResponseType.Rules.Authenticate authenticate;
        protected GetCallRulesInResponseType.Rules.SetCallPriority setCallPriority;

        /**
         * Gets the value of the ruleID property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getRuleID() {
            return ruleID;
        }

        /**
         * Sets the value of the ruleID property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setRuleID(BigInteger value) {
            this.ruleID = value;
        }

        /**
         * Gets the value of the status property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean isStatus() {
            return status;
        }

        /**
         * Sets the value of the status property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setStatus(Boolean value) {
            this.status = value;
        }

        /**
         * Gets the value of the busy property.
         * 
         * @return
         *     possible object is
         *     {@link CallRuleInfo }
         *     
         */
        public CallRuleInfo getBusy() {
            return busy;
        }

        /**
         * Sets the value of the busy property.
         * 
         * @param value
         *     allowed object is
         *     {@link CallRuleInfo }
         *     
         */
        public void setBusy(CallRuleInfo value) {
            this.busy = value;
        }

        /**
         * Gets the value of the congestion property.
         * 
         * @return
         *     possible object is
         *     {@link CallRuleInfo }
         *     
         */
        public CallRuleInfo getCongestion() {
            return congestion;
        }

        /**
         * Sets the value of the congestion property.
         * 
         * @param value
         *     allowed object is
         *     {@link CallRuleInfo }
         *     
         */
        public void setCongestion(CallRuleInfo value) {
            this.congestion = value;
        }

        /**
         * Gets the value of the hangup property.
         * 
         * @return
         *     possible object is
         *     {@link CallRuleInfo }
         *     
         */
        public CallRuleInfo getHangup() {
            return hangup;
        }

        /**
         * Sets the value of the hangup property.
         * 
         * @param value
         *     allowed object is
         *     {@link CallRuleInfo }
         *     
         */
        public void setHangup(CallRuleInfo value) {
            this.hangup = value;
        }

        /**
         * Gets the value of the transfer property.
         * 
         * @return
         *     possible object is
         *     {@link GetCallRulesInResponseType.Rules.Transfer }
         *     
         */
        public GetCallRulesInResponseType.Rules.Transfer getTransfer() {
            return transfer;
        }

        /**
         * Sets the value of the transfer property.
         * 
         * @param value
         *     allowed object is
         *     {@link GetCallRulesInResponseType.Rules.Transfer }
         *     
         */
        public void setTransfer(GetCallRulesInResponseType.Rules.Transfer value) {
            this.transfer = value;
        }

        /**
         * Gets the value of the cascade property.
         * 
         * @return
         *     possible object is
         *     {@link GetCallRulesInResponseType.Rules.Cascade }
         *     
         */
        public GetCallRulesInResponseType.Rules.Cascade getCascade() {
            return cascade;
        }

        /**
         * Sets the value of the cascade property.
         * 
         * @param value
         *     allowed object is
         *     {@link GetCallRulesInResponseType.Rules.Cascade }
         *     
         */
        public void setCascade(GetCallRulesInResponseType.Rules.Cascade value) {
            this.cascade = value;
        }

        /**
         * Gets the value of the authenticate property.
         * 
         * @return
         *     possible object is
         *     {@link GetCallRulesInResponseType.Rules.Authenticate }
         *     
         */
        public GetCallRulesInResponseType.Rules.Authenticate getAuthenticate() {
            return authenticate;
        }

        /**
         * Sets the value of the authenticate property.
         * 
         * @param value
         *     allowed object is
         *     {@link GetCallRulesInResponseType.Rules.Authenticate }
         *     
         */
        public void setAuthenticate(GetCallRulesInResponseType.Rules.Authenticate value) {
            this.authenticate = value;
        }

        /**
         * Gets the value of the setCallPriority property.
         * 
         * @return
         *     possible object is
         *     {@link GetCallRulesInResponseType.Rules.SetCallPriority }
         *     
         */
        public GetCallRulesInResponseType.Rules.SetCallPriority getSetCallPriority() {
            return setCallPriority;
        }

        /**
         * Sets the value of the setCallPriority property.
         * 
         * @param value
         *     allowed object is
         *     {@link GetCallRulesInResponseType.Rules.SetCallPriority }
         *     
         */
        public void setSetCallPriority(GetCallRulesInResponseType.Rules.SetCallPriority value) {
            this.setCallPriority = value;
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}CallRuleInfo">
         *       &lt;sequence>
         *         &lt;element name="password" type="{http://4psa.com/Common.xsd/2.5.1}password"/>
         *         &lt;element name="soundID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
         *         &lt;element name="final" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
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
            "password",
            "soundID",
            "_final"
        })
        public static class Authenticate
            extends CallRuleInfo
        {

            @XmlElement(required = true)
            protected String password;
            protected BigInteger soundID;
            @XmlElement(name = "final")
            protected Boolean _final;

            /**
             * Gets the value of the password property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getPassword() {
                return password;
            }

            /**
             * Sets the value of the password property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setPassword(String value) {
                this.password = value;
            }

            /**
             * Gets the value of the soundID property.
             * 
             * @return
             *     possible object is
             *     {@link BigInteger }
             *     
             */
            public BigInteger getSoundID() {
                return soundID;
            }

            /**
             * Sets the value of the soundID property.
             * 
             * @param value
             *     allowed object is
             *     {@link BigInteger }
             *     
             */
            public void setSoundID(BigInteger value) {
                this.soundID = value;
            }

            /**
             * Gets the value of the final property.
             * 
             * @return
             *     possible object is
             *     {@link Boolean }
             *     
             */
            public Boolean isFinal() {
                return _final;
            }

            /**
             * Sets the value of the final property.
             * 
             * @param value
             *     allowed object is
             *     {@link Boolean }
             *     
             */
            public void setFinal(Boolean value) {
                this._final = value;
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
         *     &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}CallRuleInfo">
         *       &lt;sequence>
         *         &lt;element name="ring" type="{http://4psa.com/Common.xsd/2.5.1}integer"/>
         *         &lt;element name="final" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
         *         &lt;element name="toNumbers" maxOccurs="10">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;sequence>
         *                   &lt;element name="number" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
         *                   &lt;element name="ringAfter" type="{http://4psa.com/Common.xsd/2.5.1}integer"/>
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
            "ring",
            "_final",
            "toNumbers"
        })
        public static class Cascade
            extends CallRuleInfo
        {

            @XmlElement(required = true)
            protected BigInteger ring;
            @XmlElement(name = "final")
            protected Boolean _final;
            @XmlElement(required = true)
            protected List<GetCallRulesInResponseType.Rules.Cascade.ToNumbers> toNumbers;

            /**
             * Gets the value of the ring property.
             * 
             * @return
             *     possible object is
             *     {@link BigInteger }
             *     
             */
            public BigInteger getRing() {
                return ring;
            }

            /**
             * Sets the value of the ring property.
             * 
             * @param value
             *     allowed object is
             *     {@link BigInteger }
             *     
             */
            public void setRing(BigInteger value) {
                this.ring = value;
            }

            /**
             * Gets the value of the final property.
             * 
             * @return
             *     possible object is
             *     {@link Boolean }
             *     
             */
            public Boolean isFinal() {
                return _final;
            }

            /**
             * Sets the value of the final property.
             * 
             * @param value
             *     allowed object is
             *     {@link Boolean }
             *     
             */
            public void setFinal(Boolean value) {
                this._final = value;
            }

            /**
             * Gets the value of the toNumbers property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the toNumbers property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getToNumbers().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link GetCallRulesInResponseType.Rules.Cascade.ToNumbers }
             * 
             * 
             */
            public List<GetCallRulesInResponseType.Rules.Cascade.ToNumbers> getToNumbers() {
                if (toNumbers == null) {
                    toNumbers = new ArrayList<GetCallRulesInResponseType.Rules.Cascade.ToNumbers>();
                }
                return this.toNumbers;
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
             *         &lt;element name="number" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
             *         &lt;element name="ringAfter" type="{http://4psa.com/Common.xsd/2.5.1}integer"/>
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
                "number",
                "ringAfter"
            })
            public static class ToNumbers {

                @XmlElement(required = true)
                protected String number;
                @XmlElement(required = true)
                protected BigInteger ringAfter;

                /**
                 * Gets the value of the number property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getNumber() {
                    return number;
                }

                /**
                 * Sets the value of the number property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setNumber(String value) {
                    this.number = value;
                }

                /**
                 * Gets the value of the ringAfter property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigInteger }
                 *     
                 */
                public BigInteger getRingAfter() {
                    return ringAfter;
                }

                /**
                 * Sets the value of the ringAfter property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigInteger }
                 *     
                 */
                public void setRingAfter(BigInteger value) {
                    this.ringAfter = value;
                }

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
         *     &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}CallRuleInfo">
         *       &lt;sequence>
         *         &lt;element name="priority" type="{http://4psa.com/Common.xsd/2.5.1}integer"/>
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
            "priority"
        })
        public static class SetCallPriority
            extends CallRuleInfo
        {

            @XmlElement(required = true)
            protected BigInteger priority;

            /**
             * Gets the value of the priority property.
             * 
             * @return
             *     possible object is
             *     {@link BigInteger }
             *     
             */
            public BigInteger getPriority() {
                return priority;
            }

            /**
             * Sets the value of the priority property.
             * 
             * @param value
             *     allowed object is
             *     {@link BigInteger }
             *     
             */
            public void setPriority(BigInteger value) {
                this.priority = value;
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
         *     &lt;extension base="{http://4psa.com/ExtensionData.xsd/2.5.1}CallRuleInfo">
         *       &lt;sequence>
         *         &lt;choice>
         *           &lt;element name="toNumbers" type="{http://4psa.com/ExtensionData.xsd/2.5.1}CallRuleTransferInfo"/>
         *           &lt;element name="toVoicemail" type="{http://4psa.com/Common.xsd/2.5.1}boolean"/>
         *         &lt;/choice>
         *         &lt;element name="callStatus" minOccurs="0">
         *           &lt;simpleType>
         *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
         *               &lt;enumeration value="0"/>
         *               &lt;enumeration value="1"/>
         *               &lt;enumeration value="2"/>
         *               &lt;enumeration value="3"/>
         *             &lt;/restriction>
         *           &lt;/simpleType>
         *         &lt;/element>
         *         &lt;element name="extensionStatus" minOccurs="0">
         *           &lt;simpleType>
         *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
         *               &lt;enumeration value="0"/>
         *               &lt;enumeration value="-1"/>
         *               &lt;enumeration value="1"/>
         *             &lt;/restriction>
         *           &lt;/simpleType>
         *         &lt;/element>
         *         &lt;element name="final" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
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
            "toNumbers",
            "toVoicemail",
            "callStatus",
            "extensionStatus",
            "_final"
        })
        public static class Transfer
            extends CallRuleInfo
        {

            protected CallRuleTransferInfo toNumbers;
            protected Boolean toVoicemail;
            protected BigInteger callStatus;
            protected BigInteger extensionStatus;
            @XmlElement(name = "final")
            protected Boolean _final;

            /**
             * Gets the value of the toNumbers property.
             * 
             * @return
             *     possible object is
             *     {@link CallRuleTransferInfo }
             *     
             */
            public CallRuleTransferInfo getToNumbers() {
                return toNumbers;
            }

            /**
             * Sets the value of the toNumbers property.
             * 
             * @param value
             *     allowed object is
             *     {@link CallRuleTransferInfo }
             *     
             */
            public void setToNumbers(CallRuleTransferInfo value) {
                this.toNumbers = value;
            }

            /**
             * Gets the value of the toVoicemail property.
             * 
             * @return
             *     possible object is
             *     {@link Boolean }
             *     
             */
            public Boolean isToVoicemail() {
                return toVoicemail;
            }

            /**
             * Sets the value of the toVoicemail property.
             * 
             * @param value
             *     allowed object is
             *     {@link Boolean }
             *     
             */
            public void setToVoicemail(Boolean value) {
                this.toVoicemail = value;
            }

            /**
             * Gets the value of the callStatus property.
             * 
             * @return
             *     possible object is
             *     {@link BigInteger }
             *     
             */
            public BigInteger getCallStatus() {
                return callStatus;
            }

            /**
             * Sets the value of the callStatus property.
             * 
             * @param value
             *     allowed object is
             *     {@link BigInteger }
             *     
             */
            public void setCallStatus(BigInteger value) {
                this.callStatus = value;
            }

            /**
             * Gets the value of the extensionStatus property.
             * 
             * @return
             *     possible object is
             *     {@link BigInteger }
             *     
             */
            public BigInteger getExtensionStatus() {
                return extensionStatus;
            }

            /**
             * Sets the value of the extensionStatus property.
             * 
             * @param value
             *     allowed object is
             *     {@link BigInteger }
             *     
             */
            public void setExtensionStatus(BigInteger value) {
                this.extensionStatus = value;
            }

            /**
             * Gets the value of the final property.
             * 
             * @return
             *     possible object is
             *     {@link Boolean }
             *     
             */
            public Boolean isFinal() {
                return _final;
            }

            /**
             * Sets the value of the final property.
             * 
             * @param value
             *     allowed object is
             *     {@link Boolean }
             *     
             */
            public void setFinal(Boolean value) {
                this._final = value;
            }

        }

    }

}

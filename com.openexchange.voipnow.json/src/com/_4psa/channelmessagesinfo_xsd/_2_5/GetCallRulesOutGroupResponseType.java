
package com._4psa.channelmessagesinfo_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.channeldata_xsd._2_5.CallRulesOutGroupList;
import com._4psa.channelmessages_xsd._2_5.GetCallRulesOutGroupResponse;
import com._4psa.common_xsd._2_5.Notice;


/**
 * Get outgoing routing rules group: response type
 *
 * <p>Java class for GetCallRulesOutGroupResponseType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="GetCallRulesOutGroupResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;sequence>
 *           &lt;element name="userID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *           &lt;element name="rulesGroup" type="{http://4psa.com/ChannelData.xsd/2.5.1}CallRulesOutGroupList" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "GetCallRulesOutGroupResponseType", propOrder = {
    "userID",
    "rulesGroup",
    "notice"
})
@XmlSeeAlso({
    GetCallRulesOutGroupResponse.class
})
public class GetCallRulesOutGroupResponseType {

    @XmlElement(required = true)
    protected BigInteger userID;
    protected List<CallRulesOutGroupList> rulesGroup;
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
     * Gets the value of the rulesGroup property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rulesGroup property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRulesGroup().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CallRulesOutGroupList }
     *
     *
     */
    public List<CallRulesOutGroupList> getRulesGroup() {
        if (rulesGroup == null) {
            rulesGroup = new ArrayList<CallRulesOutGroupList>();
        }
        return this.rulesGroup;
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

}

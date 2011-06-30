
package com._4psa.common_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.accountmessages_xsd._2_5.LinkAccountResponse;
import com._4psa.billingmessages_xsd._2_5.AddChargingPlanResponse;
import com._4psa.billingmessages_xsd._2_5.EditChargingPlanResponse;
import com._4psa.channelmessagesinfo_xsd._2_5.AddCallRulesOutResponseType;
import com._4psa.channelmessagesinfo_xsd._2_5.AddPublicNoResponseType;
import com._4psa.channelmessagesinfo_xsd._2_5.AssignPublicNoResponseType;
import com._4psa.channelmessagesinfo_xsd._2_5.EditPublicNoResponseType;
import com._4psa.channelmessagesinfo_xsd._2_5.UpdateCallRulesOutGroupResponseType;
import com._4psa.channelmessagesinfo_xsd._2_5.UpdateChannelGroupResponseType;
import com._4psa.channelmessagesinfo_xsd._2_5.UpdateChannelResponseType;
import com._4psa.clientmessages_xsd._2_5.EditClientResponse;
import com._4psa.extensionmessages_xsd._2_5.EditExtensionResponse;
import com._4psa.pbxmessages_xsd._2_5.AddCustomAlertResponse;
import com._4psa.pbxmessages_xsd._2_5.AddCustomButtonResponse;
import com._4psa.pbxmessages_xsd._2_5.EditCustomAlertResponse;
import com._4psa.pbxmessages_xsd._2_5.EditCustomButtonResponse;
import com._4psa.pbxmessages_xsd._2_5.SetCustomAlertStatusResponse;
import com._4psa.pbxmessages_xsd._2_5.SetCustomButtonStatusResponse;
import com._4psa.pbxmessagesinfo_xsd._2_5.UpdateObjectResponseType;
import com._4psa.resellermessages_xsd._2_5.EditResellerResponse;


/**
 * Update operation response object type
 * 
 * <p>Java class for updateObject complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="updateObject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" maxOccurs="10" minOccurs="0"/>
 *         &lt;element name="result" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="success"/>
 *               &lt;enumeration value="partial"/>
 *               &lt;enumeration value="failure"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
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
@XmlType(name = "updateObject", propOrder = {
    "id",
    "result",
    "notice"
})
@XmlSeeAlso({
    UpdateObjectResponseType.class,
    UpdateChannelResponseType.class,
    AssignPublicNoResponseType.class,
    AddPublicNoResponseType.class,
    UpdateCallRulesOutGroupResponseType.class,
    UpdateChannelGroupResponseType.class,
    AddCallRulesOutResponseType.class,
    EditPublicNoResponseType.class,
    EditClientResponse.class,
    LinkAccountResponse.class,
    EditResellerResponse.class,
    EditExtensionResponse.class,
    AddChargingPlanResponse.class,
    EditChargingPlanResponse.class,
    EditCustomButtonResponse.class,
    SetCustomAlertStatusResponse.class,
    SetCustomButtonStatusResponse.class,
    EditCustomAlertResponse.class,
    AddCustomButtonResponse.class,
    AddCustomAlertResponse.class
})
public class UpdateObject {

    @XmlElement(name = "ID")
    protected List<BigInteger> id;
    protected String result;
    protected List<Notice> notice;

    /**
     * Gets the value of the id property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the id property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getID().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BigInteger }
     * 
     * 
     */
    public List<BigInteger> getID() {
        if (id == null) {
            id = new ArrayList<BigInteger>();
        }
        return this.id;
    }

    /**
     * Gets the value of the result property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResult() {
        return result;
    }

    /**
     * Sets the value of the result property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResult(String value) {
        this.result = value;
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


package com._4psa.resellermessagesinfo_xsd._2_5;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import com._4psa.common_xsd._2_5.Notice;
import com._4psa.resellerdata_xsd._2_5.ResellerPLInfo;


/**
 * Get reseller permissions and limits response type
 * 
 * <p>Java class for GetResellerPLResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetResellerPLResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="userPL" type="{http://4psa.com/ResellerData.xsd/2.5.1}ResellerPLInfo" minOccurs="0"/>
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
@XmlType(name = "GetResellerPLResponseType", propOrder = {
    "userPL",
    "notice"
})
public class GetResellerPLResponseType {

    protected ResellerPLInfo userPL;
    protected List<Notice> notice;

    /**
     * Gets the value of the userPL property.
     * 
     * @return
     *     possible object is
     *     {@link ResellerPLInfo }
     *     
     */
    public ResellerPLInfo getUserPL() {
        return userPL;
    }

    /**
     * Sets the value of the userPL property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResellerPLInfo }
     *     
     */
    public void setUserPL(ResellerPLInfo value) {
        this.userPL = value;
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


package com.microsoft.schemas.exchange.services._2006.messages;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ResponseClassType;
import org.w3c.dom.Element;


/**
 * <p>Java class for ResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ResponseMessageType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence minOccurs="0">
 *         &lt;element name="MessageText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ResponseCode" type="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseCodeType" minOccurs="0"/>
 *         &lt;element name="DescriptiveLinkKey" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="MessageXml" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;any processContents='lax' maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="ResponseClass" use="required" type="{http://schemas.microsoft.com/exchange/services/2006/types}ResponseClassType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResponseMessageType", propOrder = {
    "messageText",
    "responseCode",
    "descriptiveLinkKey",
    "messageXml"
})
@XmlSeeAlso({
    GetInboxRulesResponseType.class,
    GetPhoneCallInformationResponseMessageType.class,
    GetMailTipsResponseMessageType.class,
    PlayOnPhoneResponseMessageType.class,
    GetPasswordExpirationDateResponseMessageType.class,
    FindMessageTrackingReportResponseMessageType.class,
    DisconnectPhoneCallResponseMessageType.class,
    GetMessageTrackingReportResponseMessageType.class,
    GetServiceConfigurationResponseMessageType.class,
    GetSharingFolderResponseMessageType.class,
    UpdateInboxRulesResponseType.class,
    GetRoomListsResponseMessageType.class,
    FindConversationResponseMessageType.class,
    RefreshSharingFolderResponseMessageType.class,
    GetRoomsResponseMessageType.class,
    GetSharingMetadataResponseMessageType.class,
    ConvertIdResponseMessageType.class,
    FindItemResponseMessageType.class,
    UploadItemsResponseMessageType.class,
    SyncFolderHierarchyResponseMessageType.class,
    ResolveNamesResponseMessageType.class,
    DelegateUserResponseMessageType.class,
    ServiceConfigurationResponseMessageType.class,
    BaseDelegateResponseMessageType.class,
    GetStreamingEventsResponseMessageType.class,
    ExportItemsResponseMessageType.class,
    GetUserConfigurationResponseMessageType.class,
    GetEventsResponseMessageType.class,
    SendNotificationResponseMessageType.class,
    SyncFolderItemsResponseMessageType.class,
    DeleteAttachmentResponseMessageType.class,
    MailTipsResponseMessageType.class,
    AttachmentInfoResponseMessageType.class,
    FolderInfoResponseMessageType.class,
    GetServerTimeZonesResponseMessageType.class,
    FindFolderResponseMessageType.class,
    SubscribeResponseMessageType.class,
    FindMailboxStatisticsByKeywordsResponseMessageType.class,
    ItemInfoResponseMessageType.class,
    ExpandDLResponseMessageType.class
})
public class ResponseMessageType {

    @XmlElement(name = "MessageText")
    protected String messageText;
    @XmlElement(name = "ResponseCode")
    protected String responseCode;
    @XmlElement(name = "DescriptiveLinkKey")
    protected Integer descriptiveLinkKey;
    @XmlElement(name = "MessageXml")
    protected ResponseMessageType.MessageXml messageXml;
    @XmlAttribute(name = "ResponseClass", required = true)
    protected ResponseClassType responseClass;

    /**
     * Gets the value of the messageText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageText() {
        return messageText;
    }

    /**
     * Sets the value of the messageText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageText(String value) {
        this.messageText = value;
    }

    /**
     * Gets the value of the responseCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResponseCode() {
        return responseCode;
    }

    /**
     * Sets the value of the responseCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResponseCode(String value) {
        this.responseCode = value;
    }

    /**
     * Gets the value of the descriptiveLinkKey property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getDescriptiveLinkKey() {
        return descriptiveLinkKey;
    }

    /**
     * Sets the value of the descriptiveLinkKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setDescriptiveLinkKey(Integer value) {
        this.descriptiveLinkKey = value;
    }

    /**
     * Gets the value of the messageXml property.
     * 
     * @return
     *     possible object is
     *     {@link ResponseMessageType.MessageXml }
     *     
     */
    public ResponseMessageType.MessageXml getMessageXml() {
        return messageXml;
    }

    /**
     * Sets the value of the messageXml property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResponseMessageType.MessageXml }
     *     
     */
    public void setMessageXml(ResponseMessageType.MessageXml value) {
        this.messageXml = value;
    }

    /**
     * Gets the value of the responseClass property.
     * 
     * @return
     *     possible object is
     *     {@link ResponseClassType }
     *     
     */
    public ResponseClassType getResponseClass() {
        return responseClass;
    }

    /**
     * Sets the value of the responseClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link ResponseClassType }
     *     
     */
    public void setResponseClass(ResponseClassType value) {
        this.responseClass = value;
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
     *         &lt;any processContents='lax' maxOccurs="unbounded" minOccurs="0"/>
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
        "any"
    })
    public static class MessageXml {

        @XmlAnyElement(lax = true)
        protected List<Object> any;

        /**
         * Gets the value of the any property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the any property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAny().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Object }
         * {@link Element }
         * 
         * 
         */
        public List<Object> getAny() {
            if (any == null) {
                any = new ArrayList<Object>();
            }
            return this.any;
        }

    }

}

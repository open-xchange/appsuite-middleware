
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for NonEmptyArrayOfResponseObjectsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NonEmptyArrayOfResponseObjectsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="AcceptItem" type="{http://schemas.microsoft.com/exchange/services/2006/types}AcceptItemType"/>
 *         &lt;element name="TentativelyAcceptItem" type="{http://schemas.microsoft.com/exchange/services/2006/types}TentativelyAcceptItemType"/>
 *         &lt;element name="DeclineItem" type="{http://schemas.microsoft.com/exchange/services/2006/types}DeclineItemType"/>
 *         &lt;element name="ReplyToItem" type="{http://schemas.microsoft.com/exchange/services/2006/types}ReplyToItemType"/>
 *         &lt;element name="ForwardItem" type="{http://schemas.microsoft.com/exchange/services/2006/types}ForwardItemType"/>
 *         &lt;element name="ReplyAllToItem" type="{http://schemas.microsoft.com/exchange/services/2006/types}ReplyAllToItemType"/>
 *         &lt;element name="CancelCalendarItem" type="{http://schemas.microsoft.com/exchange/services/2006/types}CancelCalendarItemType"/>
 *         &lt;element name="RemoveItem" type="{http://schemas.microsoft.com/exchange/services/2006/types}RemoveItemType"/>
 *         &lt;element name="SuppressReadReceipt" type="{http://schemas.microsoft.com/exchange/services/2006/types}SuppressReadReceiptType"/>
 *         &lt;element name="PostReplyItem" type="{http://schemas.microsoft.com/exchange/services/2006/types}PostReplyItemType"/>
 *         &lt;element name="AcceptSharingInvitation" type="{http://schemas.microsoft.com/exchange/services/2006/types}AcceptSharingInvitationType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NonEmptyArrayOfResponseObjectsType", propOrder = {
    "acceptItemOrTentativelyAcceptItemOrDeclineItem"
})
public class NonEmptyArrayOfResponseObjectsType {

    @XmlElements({
        @XmlElement(name = "PostReplyItem", type = PostReplyItemType.class),
        @XmlElement(name = "CancelCalendarItem", type = CancelCalendarItemType.class),
        @XmlElement(name = "ReplyToItem", type = ReplyToItemType.class),
        @XmlElement(name = "DeclineItem", type = DeclineItemType.class),
        @XmlElement(name = "AcceptSharingInvitation", type = AcceptSharingInvitationType.class),
        @XmlElement(name = "RemoveItem", type = RemoveItemType.class),
        @XmlElement(name = "ReplyAllToItem", type = ReplyAllToItemType.class),
        @XmlElement(name = "SuppressReadReceipt", type = SuppressReadReceiptType.class),
        @XmlElement(name = "TentativelyAcceptItem", type = TentativelyAcceptItemType.class),
        @XmlElement(name = "AcceptItem", type = AcceptItemType.class),
        @XmlElement(name = "ForwardItem", type = ForwardItemType.class)
    })
    protected List<ResponseObjectType> acceptItemOrTentativelyAcceptItemOrDeclineItem;

    /**
     * Gets the value of the acceptItemOrTentativelyAcceptItemOrDeclineItem property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the acceptItemOrTentativelyAcceptItemOrDeclineItem property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAcceptItemOrTentativelyAcceptItemOrDeclineItem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PostReplyItemType }
     * {@link CancelCalendarItemType }
     * {@link ReplyToItemType }
     * {@link DeclineItemType }
     * {@link AcceptSharingInvitationType }
     * {@link RemoveItemType }
     * {@link ReplyAllToItemType }
     * {@link SuppressReadReceiptType }
     * {@link TentativelyAcceptItemType }
     * {@link AcceptItemType }
     * {@link ForwardItemType }
     * 
     * 
     */
    public List<ResponseObjectType> getAcceptItemOrTentativelyAcceptItemOrDeclineItem() {
        if (acceptItemOrTentativelyAcceptItemOrDeclineItem == null) {
            acceptItemOrTentativelyAcceptItemOrDeclineItem = new ArrayList<ResponseObjectType>();
        }
        return this.acceptItemOrTentativelyAcceptItemOrDeclineItem;
    }

}

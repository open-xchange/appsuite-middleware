
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfMailboxData;
import com.microsoft.schemas.exchange.services._2006.types.FreeBusyViewOptionsType;
import com.microsoft.schemas.exchange.services._2006.types.SerializableTimeZone;
import com.microsoft.schemas.exchange.services._2006.types.SuggestionsViewOptionsType;


/**
 * <p>Java class for GetUserAvailabilityRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetUserAvailabilityRequestType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element ref="{http://schemas.microsoft.com/exchange/services/2006/types}TimeZone" minOccurs="0"/>
 *         &lt;element name="MailboxDataArray" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfMailboxData"/>
 *         &lt;element ref="{http://schemas.microsoft.com/exchange/services/2006/types}FreeBusyViewOptions" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.microsoft.com/exchange/services/2006/types}SuggestionsViewOptions" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetUserAvailabilityRequestType", propOrder = {
    "timeZone",
    "mailboxDataArray",
    "freeBusyViewOptions",
    "suggestionsViewOptions"
})
public class GetUserAvailabilityRequestType
    extends BaseRequestType
{

    @XmlElement(name = "TimeZone", namespace = "http://schemas.microsoft.com/exchange/services/2006/types")
    protected SerializableTimeZone timeZone;
    @XmlElement(name = "MailboxDataArray", required = true)
    protected ArrayOfMailboxData mailboxDataArray;
    @XmlElement(name = "FreeBusyViewOptions", namespace = "http://schemas.microsoft.com/exchange/services/2006/types")
    protected FreeBusyViewOptionsType freeBusyViewOptions;
    @XmlElement(name = "SuggestionsViewOptions", namespace = "http://schemas.microsoft.com/exchange/services/2006/types")
    protected SuggestionsViewOptionsType suggestionsViewOptions;

    /**
     * Gets the value of the timeZone property.
     * 
     * @return
     *     possible object is
     *     {@link SerializableTimeZone }
     *     
     */
    public SerializableTimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * Sets the value of the timeZone property.
     * 
     * @param value
     *     allowed object is
     *     {@link SerializableTimeZone }
     *     
     */
    public void setTimeZone(SerializableTimeZone value) {
        this.timeZone = value;
    }

    /**
     * Gets the value of the mailboxDataArray property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfMailboxData }
     *     
     */
    public ArrayOfMailboxData getMailboxDataArray() {
        return mailboxDataArray;
    }

    /**
     * Sets the value of the mailboxDataArray property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfMailboxData }
     *     
     */
    public void setMailboxDataArray(ArrayOfMailboxData value) {
        this.mailboxDataArray = value;
    }

    /**
     * Gets the value of the freeBusyViewOptions property.
     * 
     * @return
     *     possible object is
     *     {@link FreeBusyViewOptionsType }
     *     
     */
    public FreeBusyViewOptionsType getFreeBusyViewOptions() {
        return freeBusyViewOptions;
    }

    /**
     * Sets the value of the freeBusyViewOptions property.
     * 
     * @param value
     *     allowed object is
     *     {@link FreeBusyViewOptionsType }
     *     
     */
    public void setFreeBusyViewOptions(FreeBusyViewOptionsType value) {
        this.freeBusyViewOptions = value;
    }

    /**
     * Gets the value of the suggestionsViewOptions property.
     * 
     * @return
     *     possible object is
     *     {@link SuggestionsViewOptionsType }
     *     
     */
    public SuggestionsViewOptionsType getSuggestionsViewOptions() {
        return suggestionsViewOptions;
    }

    /**
     * Sets the value of the suggestionsViewOptions property.
     * 
     * @param value
     *     allowed object is
     *     {@link SuggestionsViewOptionsType }
     *     
     */
    public void setSuggestionsViewOptions(SuggestionsViewOptionsType value) {
        this.suggestionsViewOptions = value;
    }

}

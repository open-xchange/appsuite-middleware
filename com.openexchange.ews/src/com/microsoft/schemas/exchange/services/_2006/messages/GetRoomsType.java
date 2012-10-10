
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.EmailAddressType;


/**
 * <p>Java class for GetRoomsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetRoomsType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="RoomList" type="{http://schemas.microsoft.com/exchange/services/2006/types}EmailAddressType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetRoomsType", propOrder = {
    "roomList"
})
public class GetRoomsType
    extends BaseRequestType
{

    @XmlElement(name = "RoomList", required = true)
    protected EmailAddressType roomList;

    /**
     * Gets the value of the roomList property.
     * 
     * @return
     *     possible object is
     *     {@link EmailAddressType }
     *     
     */
    public EmailAddressType getRoomList() {
        return roomList;
    }

    /**
     * Sets the value of the roomList property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmailAddressType }
     *     
     */
    public void setRoomList(EmailAddressType value) {
        this.roomList = value;
    }

}


package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfEmailAddressesType;


/**
 * <p>Java class for GetRoomListsResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetRoomListsResponseMessageType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType">
 *       &lt;sequence>
 *         &lt;element name="RoomLists" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfEmailAddressesType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetRoomListsResponseMessageType", propOrder = {
    "roomLists"
})
public class GetRoomListsResponseMessageType
    extends ResponseMessageType
{

    @XmlElement(name = "RoomLists")
    protected ArrayOfEmailAddressesType roomLists;

    /**
     * Gets the value of the roomLists property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfEmailAddressesType }
     *     
     */
    public ArrayOfEmailAddressesType getRoomLists() {
        return roomLists;
    }

    /**
     * Sets the value of the roomLists property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfEmailAddressesType }
     *     
     */
    public void setRoomLists(ArrayOfEmailAddressesType value) {
        this.roomLists = value;
    }

}

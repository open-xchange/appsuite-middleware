
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for WellKnownResponseObjectType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="WellKnownResponseObjectType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://schemas.microsoft.com/exchange/services/2006/types}ResponseObjectType">
 *       &lt;sequence>
 *         &lt;element name="ItemClass" type="{http://schemas.microsoft.com/exchange/services/2006/types}ItemClassType" minOccurs="0"/>
 *         &lt;element name="Sensitivity" type="{http://schemas.microsoft.com/exchange/services/2006/types}SensitivityChoicesType" minOccurs="0"/>
 *         &lt;element name="Body" type="{http://schemas.microsoft.com/exchange/services/2006/types}BodyType" minOccurs="0"/>
 *         &lt;element name="Attachments" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfAttachmentsType" minOccurs="0"/>
 *         &lt;element name="InternetMessageHeaders" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfInternetHeadersType" minOccurs="0"/>
 *         &lt;element name="Sender" type="{http://schemas.microsoft.com/exchange/services/2006/types}SingleRecipientType" minOccurs="0"/>
 *         &lt;element name="ToRecipients" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfRecipientsType" minOccurs="0"/>
 *         &lt;element name="CcRecipients" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfRecipientsType" minOccurs="0"/>
 *         &lt;element name="BccRecipients" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfRecipientsType" minOccurs="0"/>
 *         &lt;element name="IsReadReceiptRequested" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="IsDeliveryReceiptRequested" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="From" type="{http://schemas.microsoft.com/exchange/services/2006/types}SingleRecipientType" minOccurs="0"/>
 *         &lt;element name="ReferenceItemId" type="{http://schemas.microsoft.com/exchange/services/2006/types}ItemIdType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WellKnownResponseObjectType")
@XmlSeeAlso({
    TentativelyAcceptItemType.class,
    DeclineItemType.class,
    AcceptItemType.class
})
public class WellKnownResponseObjectType
    extends ResponseObjectType
{


}

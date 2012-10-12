
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ReferenceItemResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ReferenceItemResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://schemas.microsoft.com/exchange/services/2006/types}ResponseObjectType">
 *       &lt;sequence>
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
@XmlType(name = "ReferenceItemResponseType")
@XmlSeeAlso({
    AcceptSharingInvitationType.class,
    SuppressReadReceiptType.class
})
public class ReferenceItemResponseType
    extends ResponseObjectType
{


}

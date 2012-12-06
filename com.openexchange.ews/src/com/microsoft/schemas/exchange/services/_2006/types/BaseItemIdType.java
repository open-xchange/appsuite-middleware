
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * Abstract base type for item identifiers.  Should never be used in web service calls
 * 
 * <p>Java class for BaseItemIdType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BaseItemIdType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BaseItemIdType")
@XmlSeeAlso({
    RootItemIdType.class,
    ItemIdType.class,
    RecurringMasterItemIdType.class,
    RequestAttachmentIdType.class,
    OccurrenceItemIdType.class
})
public abstract class BaseItemIdType {


}

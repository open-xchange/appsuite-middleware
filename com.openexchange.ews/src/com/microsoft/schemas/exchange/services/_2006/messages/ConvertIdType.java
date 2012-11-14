
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.IdFormatType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfAlternateIdsType;


/**
 * 
 *                 Converts the passed source ids into the destination format.  Change keys are not
 *                 returned.
 *             
 * 
 * <p>Java class for ConvertIdType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ConvertIdType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="SourceIds" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfAlternateIdsType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="DestinationFormat" use="required" type="{http://schemas.microsoft.com/exchange/services/2006/types}IdFormatType" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConvertIdType", propOrder = {
    "sourceIds"
})
public class ConvertIdType
    extends BaseRequestType
{

    @XmlElement(name = "SourceIds", required = true)
    protected NonEmptyArrayOfAlternateIdsType sourceIds;
    @XmlAttribute(name = "DestinationFormat", required = true)
    protected IdFormatType destinationFormat;

    /**
     * Gets the value of the sourceIds property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfAlternateIdsType }
     *     
     */
    public NonEmptyArrayOfAlternateIdsType getSourceIds() {
        return sourceIds;
    }

    /**
     * Sets the value of the sourceIds property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfAlternateIdsType }
     *     
     */
    public void setSourceIds(NonEmptyArrayOfAlternateIdsType value) {
        this.sourceIds = value;
    }

    /**
     * Gets the value of the destinationFormat property.
     * 
     * @return
     *     possible object is
     *     {@link IdFormatType }
     *     
     */
    public IdFormatType getDestinationFormat() {
        return destinationFormat;
    }

    /**
     * Sets the value of the destinationFormat property.
     * 
     * @param value
     *     allowed object is
     *     {@link IdFormatType }
     *     
     */
    public void setDestinationFormat(IdFormatType value) {
        this.destinationFormat = value;
    }

}

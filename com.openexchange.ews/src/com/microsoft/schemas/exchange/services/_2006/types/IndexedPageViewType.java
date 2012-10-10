
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for IndexedPageViewType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="IndexedPageViewType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}BasePagingType">
 *       &lt;attribute name="Offset" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="BasePoint" use="required" type="{http://schemas.microsoft.com/exchange/services/2006/types}IndexBasePointType" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IndexedPageViewType")
public class IndexedPageViewType
    extends BasePagingType
{

    @XmlAttribute(name = "Offset", required = true)
    protected int offset;
    @XmlAttribute(name = "BasePoint", required = true)
    protected IndexBasePointType basePoint;

    /**
     * Gets the value of the offset property.
     * 
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Sets the value of the offset property.
     * 
     */
    public void setOffset(int value) {
        this.offset = value;
    }

    /**
     * Gets the value of the basePoint property.
     * 
     * @return
     *     possible object is
     *     {@link IndexBasePointType }
     *     
     */
    public IndexBasePointType getBasePoint() {
        return basePoint;
    }

    /**
     * Sets the value of the basePoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link IndexBasePointType }
     *     
     */
    public void setBasePoint(IndexBasePointType value) {
        this.basePoint = value;
    }

}

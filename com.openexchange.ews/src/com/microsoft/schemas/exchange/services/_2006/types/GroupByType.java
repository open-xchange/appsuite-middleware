
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         Allows consumers to specify arbitrary groupings for FindItem queries.
 *       
 * 
 * <p>Java class for GroupByType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GroupByType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}BaseGroupByType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="FieldURI" type="{http://schemas.microsoft.com/exchange/services/2006/types}PathToUnindexedFieldType"/>
 *           &lt;element name="IndexedFieldURI" type="{http://schemas.microsoft.com/exchange/services/2006/types}PathToIndexedFieldType"/>
 *           &lt;element name="ExtendedFieldURI" type="{http://schemas.microsoft.com/exchange/services/2006/types}PathToExtendedFieldType"/>
 *         &lt;/choice>
 *         &lt;element name="AggregateOn" type="{http://schemas.microsoft.com/exchange/services/2006/types}AggregateOnType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GroupByType", propOrder = {
    "fieldURI",
    "indexedFieldURI",
    "extendedFieldURI",
    "aggregateOn"
})
public class GroupByType
    extends BaseGroupByType
{

    @XmlElement(name = "FieldURI")
    protected PathToUnindexedFieldType fieldURI;
    @XmlElement(name = "IndexedFieldURI")
    protected PathToIndexedFieldType indexedFieldURI;
    @XmlElement(name = "ExtendedFieldURI")
    protected PathToExtendedFieldType extendedFieldURI;
    @XmlElement(name = "AggregateOn", required = true)
    protected AggregateOnType aggregateOn;

    /**
     * Gets the value of the fieldURI property.
     * 
     * @return
     *     possible object is
     *     {@link PathToUnindexedFieldType }
     *     
     */
    public PathToUnindexedFieldType getFieldURI() {
        return fieldURI;
    }

    /**
     * Sets the value of the fieldURI property.
     * 
     * @param value
     *     allowed object is
     *     {@link PathToUnindexedFieldType }
     *     
     */
    public void setFieldURI(PathToUnindexedFieldType value) {
        this.fieldURI = value;
    }

    /**
     * Gets the value of the indexedFieldURI property.
     * 
     * @return
     *     possible object is
     *     {@link PathToIndexedFieldType }
     *     
     */
    public PathToIndexedFieldType getIndexedFieldURI() {
        return indexedFieldURI;
    }

    /**
     * Sets the value of the indexedFieldURI property.
     * 
     * @param value
     *     allowed object is
     *     {@link PathToIndexedFieldType }
     *     
     */
    public void setIndexedFieldURI(PathToIndexedFieldType value) {
        this.indexedFieldURI = value;
    }

    /**
     * Gets the value of the extendedFieldURI property.
     * 
     * @return
     *     possible object is
     *     {@link PathToExtendedFieldType }
     *     
     */
    public PathToExtendedFieldType getExtendedFieldURI() {
        return extendedFieldURI;
    }

    /**
     * Sets the value of the extendedFieldURI property.
     * 
     * @param value
     *     allowed object is
     *     {@link PathToExtendedFieldType }
     *     
     */
    public void setExtendedFieldURI(PathToExtendedFieldType value) {
        this.extendedFieldURI = value;
    }

    /**
     * Gets the value of the aggregateOn property.
     * 
     * @return
     *     possible object is
     *     {@link AggregateOnType }
     *     
     */
    public AggregateOnType getAggregateOn() {
        return aggregateOn;
    }

    /**
     * Sets the value of the aggregateOn property.
     * 
     * @param value
     *     allowed object is
     *     {@link AggregateOnType }
     *     
     */
    public void setAggregateOn(AggregateOnType value) {
        this.aggregateOn = value;
    }

}

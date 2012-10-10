
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         Represents the field of each item to aggregate on and the qualifier to apply to that
 *         field in determining which item will represent the group.
 *       
 * 
 * <p>Java class for AggregateOnType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AggregateOnType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="FieldURI" type="{http://schemas.microsoft.com/exchange/services/2006/types}PathToUnindexedFieldType"/>
 *         &lt;element name="IndexedFieldURI" type="{http://schemas.microsoft.com/exchange/services/2006/types}PathToIndexedFieldType"/>
 *         &lt;element name="ExtendedFieldURI" type="{http://schemas.microsoft.com/exchange/services/2006/types}PathToExtendedFieldType"/>
 *       &lt;/choice>
 *       &lt;attribute name="Aggregate" use="required" type="{http://schemas.microsoft.com/exchange/services/2006/types}AggregateType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AggregateOnType", propOrder = {
    "fieldURI",
    "indexedFieldURI",
    "extendedFieldURI"
})
public class AggregateOnType {

    @XmlElement(name = "FieldURI")
    protected PathToUnindexedFieldType fieldURI;
    @XmlElement(name = "IndexedFieldURI")
    protected PathToIndexedFieldType indexedFieldURI;
    @XmlElement(name = "ExtendedFieldURI")
    protected PathToExtendedFieldType extendedFieldURI;
    @XmlAttribute(name = "Aggregate", required = true)
    protected AggregateType aggregate;

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
     * Gets the value of the aggregate property.
     * 
     * @return
     *     possible object is
     *     {@link AggregateType }
     *     
     */
    public AggregateType getAggregate() {
        return aggregate;
    }

    /**
     * Sets the value of the aggregate property.
     * 
     * @param value
     *     allowed object is
     *     {@link AggregateType }
     *     
     */
    public void setAggregate(AggregateType value) {
        this.aggregate = value;
    }

}


package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FindItemParentType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FindItemParentType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="Items" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfRealItemsType"/>
 *         &lt;element name="Groups" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfGroupedItemsType"/>
 *       &lt;/choice>
 *       &lt;attGroup ref="{http://schemas.microsoft.com/exchange/services/2006/types}FindResponsePagingAttributes"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FindItemParentType", propOrder = {
    "items",
    "groups"
})
public class FindItemParentType {

    @XmlElement(name = "Items")
    protected ArrayOfRealItemsType items;
    @XmlElement(name = "Groups")
    protected ArrayOfGroupedItemsType groups;
    @XmlAttribute(name = "IndexedPagingOffset")
    protected Integer indexedPagingOffset;
    @XmlAttribute(name = "NumeratorOffset")
    protected Integer numeratorOffset;
    @XmlAttribute(name = "AbsoluteDenominator")
    protected Integer absoluteDenominator;
    @XmlAttribute(name = "IncludesLastItemInRange")
    protected Boolean includesLastItemInRange;
    @XmlAttribute(name = "TotalItemsInView")
    protected Integer totalItemsInView;

    /**
     * Gets the value of the items property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfRealItemsType }
     *     
     */
    public ArrayOfRealItemsType getItems() {
        return items;
    }

    /**
     * Sets the value of the items property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfRealItemsType }
     *     
     */
    public void setItems(ArrayOfRealItemsType value) {
        this.items = value;
    }

    /**
     * Gets the value of the groups property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfGroupedItemsType }
     *     
     */
    public ArrayOfGroupedItemsType getGroups() {
        return groups;
    }

    /**
     * Sets the value of the groups property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfGroupedItemsType }
     *     
     */
    public void setGroups(ArrayOfGroupedItemsType value) {
        this.groups = value;
    }

    /**
     * Gets the value of the indexedPagingOffset property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getIndexedPagingOffset() {
        return indexedPagingOffset;
    }

    /**
     * Sets the value of the indexedPagingOffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setIndexedPagingOffset(Integer value) {
        this.indexedPagingOffset = value;
    }

    /**
     * Gets the value of the numeratorOffset property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumeratorOffset() {
        return numeratorOffset;
    }

    /**
     * Sets the value of the numeratorOffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumeratorOffset(Integer value) {
        this.numeratorOffset = value;
    }

    /**
     * Gets the value of the absoluteDenominator property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAbsoluteDenominator() {
        return absoluteDenominator;
    }

    /**
     * Sets the value of the absoluteDenominator property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAbsoluteDenominator(Integer value) {
        this.absoluteDenominator = value;
    }

    /**
     * Gets the value of the includesLastItemInRange property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIncludesLastItemInRange() {
        return includesLastItemInRange;
    }

    /**
     * Sets the value of the includesLastItemInRange property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncludesLastItemInRange(Boolean value) {
        this.includesLastItemInRange = value;
    }

    /**
     * Gets the value of the totalItemsInView property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTotalItemsInView() {
        return totalItemsInView;
    }

    /**
     * Sets the value of the totalItemsInView property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTotalItemsInView(Integer value) {
        this.totalItemsInView = value;
    }

}

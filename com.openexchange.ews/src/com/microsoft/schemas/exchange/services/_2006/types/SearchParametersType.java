
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SearchParametersType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SearchParametersType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Restriction" type="{http://schemas.microsoft.com/exchange/services/2006/types}RestrictionType"/>
 *         &lt;element name="BaseFolderIds" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfBaseFolderIdsType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Traversal" type="{http://schemas.microsoft.com/exchange/services/2006/types}SearchFolderTraversalType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SearchParametersType", propOrder = {
    "restriction",
    "baseFolderIds"
})
public class SearchParametersType {

    @XmlElement(name = "Restriction", required = true)
    protected RestrictionType restriction;
    @XmlElement(name = "BaseFolderIds", required = true)
    protected NonEmptyArrayOfBaseFolderIdsType baseFolderIds;
    @XmlAttribute(name = "Traversal")
    protected SearchFolderTraversalType traversal;

    /**
     * Gets the value of the restriction property.
     * 
     * @return
     *     possible object is
     *     {@link RestrictionType }
     *     
     */
    public RestrictionType getRestriction() {
        return restriction;
    }

    /**
     * Sets the value of the restriction property.
     * 
     * @param value
     *     allowed object is
     *     {@link RestrictionType }
     *     
     */
    public void setRestriction(RestrictionType value) {
        this.restriction = value;
    }

    /**
     * Gets the value of the baseFolderIds property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfBaseFolderIdsType }
     *     
     */
    public NonEmptyArrayOfBaseFolderIdsType getBaseFolderIds() {
        return baseFolderIds;
    }

    /**
     * Sets the value of the baseFolderIds property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfBaseFolderIdsType }
     *     
     */
    public void setBaseFolderIds(NonEmptyArrayOfBaseFolderIdsType value) {
        this.baseFolderIds = value;
    }

    /**
     * Gets the value of the traversal property.
     * 
     * @return
     *     possible object is
     *     {@link SearchFolderTraversalType }
     *     
     */
    public SearchFolderTraversalType getTraversal() {
        return traversal;
    }

    /**
     * Sets the value of the traversal property.
     * 
     * @param value
     *     allowed object is
     *     {@link SearchFolderTraversalType }
     *     
     */
    public void setTraversal(SearchFolderTraversalType value) {
        this.traversal = value;
    }

}

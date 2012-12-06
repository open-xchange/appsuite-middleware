
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.FolderQueryTraversalType;
import com.microsoft.schemas.exchange.services._2006.types.FolderResponseShapeType;
import com.microsoft.schemas.exchange.services._2006.types.FractionalPageViewType;
import com.microsoft.schemas.exchange.services._2006.types.IndexedPageViewType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfBaseFolderIdsType;
import com.microsoft.schemas.exchange.services._2006.types.RestrictionType;


/**
 * <p>Java class for FindFolderType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FindFolderType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="FolderShape" type="{http://schemas.microsoft.com/exchange/services/2006/types}FolderResponseShapeType"/>
 *         &lt;choice minOccurs="0">
 *           &lt;element name="IndexedPageFolderView" type="{http://schemas.microsoft.com/exchange/services/2006/types}IndexedPageViewType"/>
 *           &lt;element name="FractionalPageFolderView" type="{http://schemas.microsoft.com/exchange/services/2006/types}FractionalPageViewType"/>
 *         &lt;/choice>
 *         &lt;element name="Restriction" type="{http://schemas.microsoft.com/exchange/services/2006/types}RestrictionType" minOccurs="0"/>
 *         &lt;element name="ParentFolderIds" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfBaseFolderIdsType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Traversal" use="required" type="{http://schemas.microsoft.com/exchange/services/2006/types}FolderQueryTraversalType" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FindFolderType", propOrder = {
    "folderShape",
    "indexedPageFolderView",
    "fractionalPageFolderView",
    "restriction",
    "parentFolderIds"
})
public class FindFolderType
    extends BaseRequestType
{

    @XmlElement(name = "FolderShape", required = true)
    protected FolderResponseShapeType folderShape;
    @XmlElement(name = "IndexedPageFolderView")
    protected IndexedPageViewType indexedPageFolderView;
    @XmlElement(name = "FractionalPageFolderView")
    protected FractionalPageViewType fractionalPageFolderView;
    @XmlElement(name = "Restriction")
    protected RestrictionType restriction;
    @XmlElement(name = "ParentFolderIds", required = true)
    protected NonEmptyArrayOfBaseFolderIdsType parentFolderIds;
    @XmlAttribute(name = "Traversal", required = true)
    protected FolderQueryTraversalType traversal;

    /**
     * Gets the value of the folderShape property.
     * 
     * @return
     *     possible object is
     *     {@link FolderResponseShapeType }
     *     
     */
    public FolderResponseShapeType getFolderShape() {
        return folderShape;
    }

    /**
     * Sets the value of the folderShape property.
     * 
     * @param value
     *     allowed object is
     *     {@link FolderResponseShapeType }
     *     
     */
    public void setFolderShape(FolderResponseShapeType value) {
        this.folderShape = value;
    }

    /**
     * Gets the value of the indexedPageFolderView property.
     * 
     * @return
     *     possible object is
     *     {@link IndexedPageViewType }
     *     
     */
    public IndexedPageViewType getIndexedPageFolderView() {
        return indexedPageFolderView;
    }

    /**
     * Sets the value of the indexedPageFolderView property.
     * 
     * @param value
     *     allowed object is
     *     {@link IndexedPageViewType }
     *     
     */
    public void setIndexedPageFolderView(IndexedPageViewType value) {
        this.indexedPageFolderView = value;
    }

    /**
     * Gets the value of the fractionalPageFolderView property.
     * 
     * @return
     *     possible object is
     *     {@link FractionalPageViewType }
     *     
     */
    public FractionalPageViewType getFractionalPageFolderView() {
        return fractionalPageFolderView;
    }

    /**
     * Sets the value of the fractionalPageFolderView property.
     * 
     * @param value
     *     allowed object is
     *     {@link FractionalPageViewType }
     *     
     */
    public void setFractionalPageFolderView(FractionalPageViewType value) {
        this.fractionalPageFolderView = value;
    }

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
     * Gets the value of the parentFolderIds property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfBaseFolderIdsType }
     *     
     */
    public NonEmptyArrayOfBaseFolderIdsType getParentFolderIds() {
        return parentFolderIds;
    }

    /**
     * Sets the value of the parentFolderIds property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfBaseFolderIdsType }
     *     
     */
    public void setParentFolderIds(NonEmptyArrayOfBaseFolderIdsType value) {
        this.parentFolderIds = value;
    }

    /**
     * Gets the value of the traversal property.
     * 
     * @return
     *     possible object is
     *     {@link FolderQueryTraversalType }
     *     
     */
    public FolderQueryTraversalType getTraversal() {
        return traversal;
    }

    /**
     * Sets the value of the traversal property.
     * 
     * @param value
     *     allowed object is
     *     {@link FolderQueryTraversalType }
     *     
     */
    public void setTraversal(FolderQueryTraversalType value) {
        this.traversal = value;
    }

}

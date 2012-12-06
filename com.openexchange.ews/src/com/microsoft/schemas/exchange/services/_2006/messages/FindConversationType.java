
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.IndexedPageViewType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfFieldOrdersType;
import com.microsoft.schemas.exchange.services._2006.types.TargetFolderIdType;


/**
 * <p>Java class for FindConversationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FindConversationType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="IndexedPageItemView" type="{http://schemas.microsoft.com/exchange/services/2006/types}IndexedPageViewType"/>
 *         &lt;element name="SortOrder" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfFieldOrdersType" minOccurs="0"/>
 *         &lt;element name="ParentFolderId" type="{http://schemas.microsoft.com/exchange/services/2006/types}TargetFolderIdType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FindConversationType", propOrder = {
    "indexedPageItemView",
    "sortOrder",
    "parentFolderId"
})
public class FindConversationType
    extends BaseRequestType
{

    @XmlElement(name = "IndexedPageItemView", required = true)
    protected IndexedPageViewType indexedPageItemView;
    @XmlElement(name = "SortOrder")
    protected NonEmptyArrayOfFieldOrdersType sortOrder;
    @XmlElement(name = "ParentFolderId", required = true)
    protected TargetFolderIdType parentFolderId;

    /**
     * Gets the value of the indexedPageItemView property.
     * 
     * @return
     *     possible object is
     *     {@link IndexedPageViewType }
     *     
     */
    public IndexedPageViewType getIndexedPageItemView() {
        return indexedPageItemView;
    }

    /**
     * Sets the value of the indexedPageItemView property.
     * 
     * @param value
     *     allowed object is
     *     {@link IndexedPageViewType }
     *     
     */
    public void setIndexedPageItemView(IndexedPageViewType value) {
        this.indexedPageItemView = value;
    }

    /**
     * Gets the value of the sortOrder property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfFieldOrdersType }
     *     
     */
    public NonEmptyArrayOfFieldOrdersType getSortOrder() {
        return sortOrder;
    }

    /**
     * Sets the value of the sortOrder property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfFieldOrdersType }
     *     
     */
    public void setSortOrder(NonEmptyArrayOfFieldOrdersType value) {
        this.sortOrder = value;
    }

    /**
     * Gets the value of the parentFolderId property.
     * 
     * @return
     *     possible object is
     *     {@link TargetFolderIdType }
     *     
     */
    public TargetFolderIdType getParentFolderId() {
        return parentFolderId;
    }

    /**
     * Sets the value of the parentFolderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link TargetFolderIdType }
     *     
     */
    public void setParentFolderId(TargetFolderIdType value) {
        this.parentFolderId = value;
    }

}

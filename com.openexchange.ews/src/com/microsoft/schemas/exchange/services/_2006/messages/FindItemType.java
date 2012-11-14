
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.CalendarViewType;
import com.microsoft.schemas.exchange.services._2006.types.ContactsViewType;
import com.microsoft.schemas.exchange.services._2006.types.DistinguishedGroupByType;
import com.microsoft.schemas.exchange.services._2006.types.FractionalPageViewType;
import com.microsoft.schemas.exchange.services._2006.types.GroupByType;
import com.microsoft.schemas.exchange.services._2006.types.IndexedPageViewType;
import com.microsoft.schemas.exchange.services._2006.types.ItemQueryTraversalType;
import com.microsoft.schemas.exchange.services._2006.types.ItemResponseShapeType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfBaseFolderIdsType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfFieldOrdersType;
import com.microsoft.schemas.exchange.services._2006.types.RestrictionType;


/**
 * <p>Java class for FindItemType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FindItemType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="ItemShape" type="{http://schemas.microsoft.com/exchange/services/2006/types}ItemResponseShapeType"/>
 *         &lt;choice minOccurs="0">
 *           &lt;element name="IndexedPageItemView" type="{http://schemas.microsoft.com/exchange/services/2006/types}IndexedPageViewType"/>
 *           &lt;element name="FractionalPageItemView" type="{http://schemas.microsoft.com/exchange/services/2006/types}FractionalPageViewType"/>
 *           &lt;element name="CalendarView" type="{http://schemas.microsoft.com/exchange/services/2006/types}CalendarViewType"/>
 *           &lt;element name="ContactsView" type="{http://schemas.microsoft.com/exchange/services/2006/types}ContactsViewType"/>
 *         &lt;/choice>
 *         &lt;choice minOccurs="0">
 *           &lt;element name="GroupBy" type="{http://schemas.microsoft.com/exchange/services/2006/types}GroupByType"/>
 *           &lt;element name="DistinguishedGroupBy" type="{http://schemas.microsoft.com/exchange/services/2006/types}DistinguishedGroupByType"/>
 *         &lt;/choice>
 *         &lt;element name="Restriction" type="{http://schemas.microsoft.com/exchange/services/2006/types}RestrictionType" minOccurs="0"/>
 *         &lt;element name="SortOrder" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfFieldOrdersType" minOccurs="0"/>
 *         &lt;element name="ParentFolderIds" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfBaseFolderIdsType"/>
 *         &lt;element name="QueryString" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Traversal" use="required" type="{http://schemas.microsoft.com/exchange/services/2006/types}ItemQueryTraversalType" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FindItemType", propOrder = {
    "itemShape",
    "indexedPageItemView",
    "fractionalPageItemView",
    "calendarView",
    "contactsView",
    "groupBy",
    "distinguishedGroupBy",
    "restriction",
    "sortOrder",
    "parentFolderIds",
    "queryString"
})
public class FindItemType
    extends BaseRequestType
{

    @XmlElement(name = "ItemShape", required = true)
    protected ItemResponseShapeType itemShape;
    @XmlElement(name = "IndexedPageItemView")
    protected IndexedPageViewType indexedPageItemView;
    @XmlElement(name = "FractionalPageItemView")
    protected FractionalPageViewType fractionalPageItemView;
    @XmlElement(name = "CalendarView")
    protected CalendarViewType calendarView;
    @XmlElement(name = "ContactsView")
    protected ContactsViewType contactsView;
    @XmlElement(name = "GroupBy")
    protected GroupByType groupBy;
    @XmlElement(name = "DistinguishedGroupBy")
    protected DistinguishedGroupByType distinguishedGroupBy;
    @XmlElement(name = "Restriction")
    protected RestrictionType restriction;
    @XmlElement(name = "SortOrder")
    protected NonEmptyArrayOfFieldOrdersType sortOrder;
    @XmlElement(name = "ParentFolderIds", required = true)
    protected NonEmptyArrayOfBaseFolderIdsType parentFolderIds;
    @XmlElement(name = "QueryString")
    protected String queryString;
    @XmlAttribute(name = "Traversal", required = true)
    protected ItemQueryTraversalType traversal;

    /**
     * Gets the value of the itemShape property.
     * 
     * @return
     *     possible object is
     *     {@link ItemResponseShapeType }
     *     
     */
    public ItemResponseShapeType getItemShape() {
        return itemShape;
    }

    /**
     * Sets the value of the itemShape property.
     * 
     * @param value
     *     allowed object is
     *     {@link ItemResponseShapeType }
     *     
     */
    public void setItemShape(ItemResponseShapeType value) {
        this.itemShape = value;
    }

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
     * Gets the value of the fractionalPageItemView property.
     * 
     * @return
     *     possible object is
     *     {@link FractionalPageViewType }
     *     
     */
    public FractionalPageViewType getFractionalPageItemView() {
        return fractionalPageItemView;
    }

    /**
     * Sets the value of the fractionalPageItemView property.
     * 
     * @param value
     *     allowed object is
     *     {@link FractionalPageViewType }
     *     
     */
    public void setFractionalPageItemView(FractionalPageViewType value) {
        this.fractionalPageItemView = value;
    }

    /**
     * Gets the value of the calendarView property.
     * 
     * @return
     *     possible object is
     *     {@link CalendarViewType }
     *     
     */
    public CalendarViewType getCalendarView() {
        return calendarView;
    }

    /**
     * Sets the value of the calendarView property.
     * 
     * @param value
     *     allowed object is
     *     {@link CalendarViewType }
     *     
     */
    public void setCalendarView(CalendarViewType value) {
        this.calendarView = value;
    }

    /**
     * Gets the value of the contactsView property.
     * 
     * @return
     *     possible object is
     *     {@link ContactsViewType }
     *     
     */
    public ContactsViewType getContactsView() {
        return contactsView;
    }

    /**
     * Sets the value of the contactsView property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContactsViewType }
     *     
     */
    public void setContactsView(ContactsViewType value) {
        this.contactsView = value;
    }

    /**
     * Gets the value of the groupBy property.
     * 
     * @return
     *     possible object is
     *     {@link GroupByType }
     *     
     */
    public GroupByType getGroupBy() {
        return groupBy;
    }

    /**
     * Sets the value of the groupBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link GroupByType }
     *     
     */
    public void setGroupBy(GroupByType value) {
        this.groupBy = value;
    }

    /**
     * Gets the value of the distinguishedGroupBy property.
     * 
     * @return
     *     possible object is
     *     {@link DistinguishedGroupByType }
     *     
     */
    public DistinguishedGroupByType getDistinguishedGroupBy() {
        return distinguishedGroupBy;
    }

    /**
     * Sets the value of the distinguishedGroupBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link DistinguishedGroupByType }
     *     
     */
    public void setDistinguishedGroupBy(DistinguishedGroupByType value) {
        this.distinguishedGroupBy = value;
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
     * Gets the value of the queryString property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQueryString() {
        return queryString;
    }

    /**
     * Sets the value of the queryString property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQueryString(String value) {
        this.queryString = value;
    }

    /**
     * Gets the value of the traversal property.
     * 
     * @return
     *     possible object is
     *     {@link ItemQueryTraversalType }
     *     
     */
    public ItemQueryTraversalType getTraversal() {
        return traversal;
    }

    /**
     * Sets the value of the traversal property.
     * 
     * @param value
     *     allowed object is
     *     {@link ItemQueryTraversalType }
     *     
     */
    public void setTraversal(ItemQueryTraversalType value) {
        this.traversal = value;
    }

}

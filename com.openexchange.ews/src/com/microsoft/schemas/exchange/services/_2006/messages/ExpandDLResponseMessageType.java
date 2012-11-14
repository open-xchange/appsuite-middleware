
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.ArrayOfDLExpansionType;


/**
 * <p>Java class for ExpandDLResponseMessageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ExpandDLResponseMessageType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}ResponseMessageType">
 *       &lt;sequence>
 *         &lt;element name="DLExpansion" type="{http://schemas.microsoft.com/exchange/services/2006/types}ArrayOfDLExpansionType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://schemas.microsoft.com/exchange/services/2006/types}FindResponsePagingAttributes"/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExpandDLResponseMessageType", propOrder = {
    "dlExpansion"
})
public class ExpandDLResponseMessageType
    extends ResponseMessageType
{

    @XmlElement(name = "DLExpansion")
    protected ArrayOfDLExpansionType dlExpansion;
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
     * Gets the value of the dlExpansion property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfDLExpansionType }
     *     
     */
    public ArrayOfDLExpansionType getDLExpansion() {
        return dlExpansion;
    }

    /**
     * Sets the value of the dlExpansion property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfDLExpansionType }
     *     
     */
    public void setDLExpansion(ArrayOfDLExpansionType value) {
        this.dlExpansion = value;
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

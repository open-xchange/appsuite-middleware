
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ContainsExpressionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ContainsExpressionType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}SearchExpressionType">
 *       &lt;sequence>
 *         &lt;element ref="{http://schemas.microsoft.com/exchange/services/2006/types}Path"/>
 *         &lt;element name="Constant" type="{http://schemas.microsoft.com/exchange/services/2006/types}ConstantValueType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ContainmentMode" type="{http://schemas.microsoft.com/exchange/services/2006/types}ContainmentModeType" />
 *       &lt;attribute name="ContainmentComparison" type="{http://schemas.microsoft.com/exchange/services/2006/types}ContainmentComparisonType" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ContainsExpressionType", propOrder = {
    "path",
    "constant"
})
public class ContainsExpressionType
    extends SearchExpressionType
{

    @XmlElementRef(name = "Path", namespace = "http://schemas.microsoft.com/exchange/services/2006/types", type = JAXBElement.class)
    protected JAXBElement<? extends BasePathToElementType> path;
    @XmlElement(name = "Constant", required = true)
    protected ConstantValueType constant;
    @XmlAttribute(name = "ContainmentMode")
    protected ContainmentModeType containmentMode;
    @XmlAttribute(name = "ContainmentComparison")
    protected ContainmentComparisonType containmentComparison;

    /**
     * Gets the value of the path property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link PathToUnindexedFieldType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PathToExtendedFieldType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BasePathToElementType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PathToIndexedFieldType }{@code >}
     *     
     */
    public JAXBElement<? extends BasePathToElementType> getPath() {
        return path;
    }

    /**
     * Sets the value of the path property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link PathToUnindexedFieldType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PathToExtendedFieldType }{@code >}
     *     {@link JAXBElement }{@code <}{@link BasePathToElementType }{@code >}
     *     {@link JAXBElement }{@code <}{@link PathToIndexedFieldType }{@code >}
     *     
     */
    public void setPath(JAXBElement<? extends BasePathToElementType> value) {
        this.path = ((JAXBElement<? extends BasePathToElementType> ) value);
    }

    /**
     * Gets the value of the constant property.
     * 
     * @return
     *     possible object is
     *     {@link ConstantValueType }
     *     
     */
    public ConstantValueType getConstant() {
        return constant;
    }

    /**
     * Sets the value of the constant property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantValueType }
     *     
     */
    public void setConstant(ConstantValueType value) {
        this.constant = value;
    }

    /**
     * Gets the value of the containmentMode property.
     * 
     * @return
     *     possible object is
     *     {@link ContainmentModeType }
     *     
     */
    public ContainmentModeType getContainmentMode() {
        return containmentMode;
    }

    /**
     * Sets the value of the containmentMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContainmentModeType }
     *     
     */
    public void setContainmentMode(ContainmentModeType value) {
        this.containmentMode = value;
    }

    /**
     * Gets the value of the containmentComparison property.
     * 
     * @return
     *     possible object is
     *     {@link ContainmentComparisonType }
     *     
     */
    public ContainmentComparisonType getContainmentComparison() {
        return containmentComparison;
    }

    /**
     * Sets the value of the containmentComparison property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContainmentComparisonType }
     *     
     */
    public void setContainmentComparison(ContainmentComparisonType value) {
        this.containmentComparison = value;
    }

}

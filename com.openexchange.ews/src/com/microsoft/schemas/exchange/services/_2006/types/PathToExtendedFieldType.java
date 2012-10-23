
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         Represents an extended property.  Note that there are only a couple of valid attribute
 *         combinations.  Note that all occurances require the PropertyType attribute.
 * 
 *         1.  (DistinguishedPropertySetId || PropertySetId) + (PropertyName || Property Id)
 *         2.  PropertyTag
 * 
 *       
 * 
 * <p>Java class for PathToExtendedFieldType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PathToExtendedFieldType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}BasePathToElementType">
 *       &lt;attribute name="DistinguishedPropertySetId" type="{http://schemas.microsoft.com/exchange/services/2006/types}DistinguishedPropertySetType" />
 *       &lt;attribute name="PropertySetId" type="{http://schemas.microsoft.com/exchange/services/2006/types}GuidType" />
 *       &lt;attribute name="PropertyTag" type="{http://schemas.microsoft.com/exchange/services/2006/types}PropertyTagType" />
 *       &lt;attribute name="PropertyName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="PropertyId" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="PropertyType" use="required" type="{http://schemas.microsoft.com/exchange/services/2006/types}MapiPropertyTypeType" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PathToExtendedFieldType")
public class PathToExtendedFieldType
    extends BasePathToElementType
{

    @XmlAttribute(name = "DistinguishedPropertySetId")
    protected DistinguishedPropertySetType distinguishedPropertySetId;
    @XmlAttribute(name = "PropertySetId")
    protected String propertySetId;
    @XmlAttribute(name = "PropertyTag")
    protected String propertyTag;
    @XmlAttribute(name = "PropertyName")
    protected String propertyName;
    @XmlAttribute(name = "PropertyId")
    protected Integer propertyId;
    @XmlAttribute(name = "PropertyType", required = true)
    protected MapiPropertyTypeType propertyType;

    /**
     * Gets the value of the distinguishedPropertySetId property.
     * 
     * @return
     *     possible object is
     *     {@link DistinguishedPropertySetType }
     *     
     */
    public DistinguishedPropertySetType getDistinguishedPropertySetId() {
        return distinguishedPropertySetId;
    }

    /**
     * Sets the value of the distinguishedPropertySetId property.
     * 
     * @param value
     *     allowed object is
     *     {@link DistinguishedPropertySetType }
     *     
     */
    public void setDistinguishedPropertySetId(DistinguishedPropertySetType value) {
        this.distinguishedPropertySetId = value;
    }

    /**
     * Gets the value of the propertySetId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPropertySetId() {
        return propertySetId;
    }

    /**
     * Sets the value of the propertySetId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPropertySetId(String value) {
        this.propertySetId = value;
    }

    /**
     * Gets the value of the propertyTag property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPropertyTag() {
        return propertyTag;
    }

    /**
     * Sets the value of the propertyTag property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPropertyTag(String value) {
        this.propertyTag = value;
    }

    /**
     * Gets the value of the propertyName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Sets the value of the propertyName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPropertyName(String value) {
        this.propertyName = value;
    }

    /**
     * Gets the value of the propertyId property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPropertyId() {
        return propertyId;
    }

    /**
     * Sets the value of the propertyId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPropertyId(Integer value) {
        this.propertyId = value;
    }

    /**
     * Gets the value of the propertyType property.
     * 
     * @return
     *     possible object is
     *     {@link MapiPropertyTypeType }
     *     
     */
    public MapiPropertyTypeType getPropertyType() {
        return propertyType;
    }

    /**
     * Sets the value of the propertyType property.
     * 
     * @param value
     *     allowed object is
     *     {@link MapiPropertyTypeType }
     *     
     */
    public void setPropertyType(MapiPropertyTypeType value) {
        this.propertyType = value;
    }

}

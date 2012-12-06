
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PathToIndexedFieldType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PathToIndexedFieldType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}BasePathToElementType">
 *       &lt;attribute name="FieldURI" use="required" type="{http://schemas.microsoft.com/exchange/services/2006/types}DictionaryURIType" />
 *       &lt;attribute name="FieldIndex" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PathToIndexedFieldType")
public class PathToIndexedFieldType
    extends BasePathToElementType
{

    @XmlAttribute(name = "FieldURI", required = true)
    protected DictionaryURIType fieldURI;
    @XmlAttribute(name = "FieldIndex", required = true)
    protected String fieldIndex;

    /**
     * Gets the value of the fieldURI property.
     * 
     * @return
     *     possible object is
     *     {@link DictionaryURIType }
     *     
     */
    public DictionaryURIType getFieldURI() {
        return fieldURI;
    }

    /**
     * Sets the value of the fieldURI property.
     * 
     * @param value
     *     allowed object is
     *     {@link DictionaryURIType }
     *     
     */
    public void setFieldURI(DictionaryURIType value) {
        this.fieldURI = value;
    }

    /**
     * Gets the value of the fieldIndex property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFieldIndex() {
        return fieldIndex;
    }

    /**
     * Sets the value of the fieldIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFieldIndex(String value) {
        this.fieldIndex = value;
    }

}

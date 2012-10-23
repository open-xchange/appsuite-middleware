
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PathToUnindexedFieldType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PathToUnindexedFieldType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}BasePathToElementType">
 *       &lt;attribute name="FieldURI" use="required" type="{http://schemas.microsoft.com/exchange/services/2006/types}UnindexedFieldURIType" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PathToUnindexedFieldType")
public class PathToUnindexedFieldType
    extends BasePathToElementType
{

    @XmlAttribute(name = "FieldURI", required = true)
    protected UnindexedFieldURIType fieldURI;

    /**
     * Gets the value of the fieldURI property.
     * 
     * @return
     *     possible object is
     *     {@link UnindexedFieldURIType }
     *     
     */
    public UnindexedFieldURIType getFieldURI() {
        return fieldURI;
    }

    /**
     * Sets the value of the fieldURI property.
     * 
     * @param value
     *     allowed object is
     *     {@link UnindexedFieldURIType }
     *     
     */
    public void setFieldURI(UnindexedFieldURIType value) {
        this.fieldURI = value;
    }

}


package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * Identifier for a fully resolved item
 * 
 * <p>Java class for ItemIdType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ItemIdType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}BaseItemIdType">
 *       &lt;attribute name="Id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="ChangeKey" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ItemIdType")
public class ItemIdType
    extends BaseItemIdType
{

    @XmlAttribute(name = "Id", required = true)
    protected String id;
    @XmlAttribute(name = "ChangeKey")
    protected String changeKey;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the changeKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChangeKey() {
        return changeKey;
    }

    /**
     * Sets the value of the changeKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChangeKey(String value) {
        this.changeKey = value;
    }

}

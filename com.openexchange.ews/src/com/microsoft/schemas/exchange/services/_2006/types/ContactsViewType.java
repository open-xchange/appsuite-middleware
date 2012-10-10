
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ContactsViewType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ContactsViewType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}BasePagingType">
 *       &lt;attribute name="InitialName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="FinalName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ContactsViewType")
public class ContactsViewType
    extends BasePagingType
{

    @XmlAttribute(name = "InitialName")
    protected String initialName;
    @XmlAttribute(name = "FinalName")
    protected String finalName;

    /**
     * Gets the value of the initialName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInitialName() {
        return initialName;
    }

    /**
     * Sets the value of the initialName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInitialName(String value) {
        this.initialName = value;
    }

    /**
     * Gets the value of the finalName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFinalName() {
        return finalName;
    }

    /**
     * Sets the value of the finalName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFinalName(String value) {
        this.finalName = value;
    }

}

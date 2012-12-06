
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AttachmentIdType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AttachmentIdType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}RequestAttachmentIdType">
 *       &lt;attribute name="RootItemId" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="RootItemChangeKey" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AttachmentIdType")
public class AttachmentIdType
    extends RequestAttachmentIdType
{

    @XmlAttribute(name = "RootItemId")
    protected String rootItemId;
    @XmlAttribute(name = "RootItemChangeKey")
    protected String rootItemChangeKey;

    /**
     * Gets the value of the rootItemId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRootItemId() {
        return rootItemId;
    }

    /**
     * Sets the value of the rootItemId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRootItemId(String value) {
        this.rootItemId = value;
    }

    /**
     * Gets the value of the rootItemChangeKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRootItemChangeKey() {
        return rootItemChangeKey;
    }

    /**
     * Sets the value of the rootItemChangeKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRootItemChangeKey(String value) {
        this.rootItemChangeKey = value;
    }

}


package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfFolderChangesType;


/**
 * <p>Java class for UpdateFolderType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UpdateFolderType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="FolderChanges" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfFolderChangesType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpdateFolderType", propOrder = {
    "folderChanges"
})
public class UpdateFolderType
    extends BaseRequestType
{

    @XmlElement(name = "FolderChanges", required = true)
    protected NonEmptyArrayOfFolderChangesType folderChanges;

    /**
     * Gets the value of the folderChanges property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfFolderChangesType }
     *     
     */
    public NonEmptyArrayOfFolderChangesType getFolderChanges() {
        return folderChanges;
    }

    /**
     * Sets the value of the folderChanges property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfFolderChangesType }
     *     
     */
    public void setFolderChanges(NonEmptyArrayOfFolderChangesType value) {
        this.folderChanges = value;
    }

}

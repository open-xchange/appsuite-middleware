
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfFoldersType;
import com.microsoft.schemas.exchange.services._2006.types.TargetFolderIdType;


/**
 * <p>Java class for CreateFolderType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CreateFolderType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="ParentFolderId" type="{http://schemas.microsoft.com/exchange/services/2006/types}TargetFolderIdType"/>
 *         &lt;element name="Folders" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfFoldersType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreateFolderType", propOrder = {
    "parentFolderId",
    "folders"
})
public class CreateFolderType
    extends BaseRequestType
{

    @XmlElement(name = "ParentFolderId", required = true)
    protected TargetFolderIdType parentFolderId;
    @XmlElement(name = "Folders", required = true)
    protected NonEmptyArrayOfFoldersType folders;

    /**
     * Gets the value of the parentFolderId property.
     * 
     * @return
     *     possible object is
     *     {@link TargetFolderIdType }
     *     
     */
    public TargetFolderIdType getParentFolderId() {
        return parentFolderId;
    }

    /**
     * Sets the value of the parentFolderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link TargetFolderIdType }
     *     
     */
    public void setParentFolderId(TargetFolderIdType value) {
        this.parentFolderId = value;
    }

    /**
     * Gets the value of the folders property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfFoldersType }
     *     
     */
    public NonEmptyArrayOfFoldersType getFolders() {
        return folders;
    }

    /**
     * Sets the value of the folders property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfFoldersType }
     *     
     */
    public void setFolders(NonEmptyArrayOfFoldersType value) {
        this.folders = value;
    }

}

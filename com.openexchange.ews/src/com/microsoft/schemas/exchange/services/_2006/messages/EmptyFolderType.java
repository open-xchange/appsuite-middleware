
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.DisposalType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfBaseFolderIdsType;


/**
 * <p>Java class for EmptyFolderType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EmptyFolderType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="FolderIds" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfBaseFolderIdsType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="DeleteType" use="required" type="{http://schemas.microsoft.com/exchange/services/2006/types}DisposalType" />
 *       &lt;attribute name="DeleteSubFolders" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EmptyFolderType", propOrder = {
    "folderIds"
})
public class EmptyFolderType
    extends BaseRequestType
{

    @XmlElement(name = "FolderIds", required = true)
    protected NonEmptyArrayOfBaseFolderIdsType folderIds;
    @XmlAttribute(name = "DeleteType", required = true)
    protected DisposalType deleteType;
    @XmlAttribute(name = "DeleteSubFolders", required = true)
    protected boolean deleteSubFolders;

    /**
     * Gets the value of the folderIds property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfBaseFolderIdsType }
     *     
     */
    public NonEmptyArrayOfBaseFolderIdsType getFolderIds() {
        return folderIds;
    }

    /**
     * Sets the value of the folderIds property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfBaseFolderIdsType }
     *     
     */
    public void setFolderIds(NonEmptyArrayOfBaseFolderIdsType value) {
        this.folderIds = value;
    }

    /**
     * Gets the value of the deleteType property.
     * 
     * @return
     *     possible object is
     *     {@link DisposalType }
     *     
     */
    public DisposalType getDeleteType() {
        return deleteType;
    }

    /**
     * Sets the value of the deleteType property.
     * 
     * @param value
     *     allowed object is
     *     {@link DisposalType }
     *     
     */
    public void setDeleteType(DisposalType value) {
        this.deleteType = value;
    }

    /**
     * Gets the value of the deleteSubFolders property.
     * 
     */
    public boolean isDeleteSubFolders() {
        return deleteSubFolders;
    }

    /**
     * Sets the value of the deleteSubFolders property.
     * 
     */
    public void setDeleteSubFolders(boolean value) {
        this.deleteSubFolders = value;
    }

}

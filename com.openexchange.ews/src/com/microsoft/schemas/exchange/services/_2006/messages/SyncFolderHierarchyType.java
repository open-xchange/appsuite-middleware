
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.FolderResponseShapeType;
import com.microsoft.schemas.exchange.services._2006.types.TargetFolderIdType;


/**
 * <p>Java class for SyncFolderHierarchyType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SyncFolderHierarchyType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="FolderShape" type="{http://schemas.microsoft.com/exchange/services/2006/types}FolderResponseShapeType"/>
 *         &lt;element name="SyncFolderId" type="{http://schemas.microsoft.com/exchange/services/2006/types}TargetFolderIdType" minOccurs="0"/>
 *         &lt;element name="SyncState" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SyncFolderHierarchyType", propOrder = {
    "folderShape",
    "syncFolderId",
    "syncState"
})
public class SyncFolderHierarchyType
    extends BaseRequestType
{

    @XmlElement(name = "FolderShape", required = true)
    protected FolderResponseShapeType folderShape;
    @XmlElement(name = "SyncFolderId")
    protected TargetFolderIdType syncFolderId;
    @XmlElement(name = "SyncState")
    protected String syncState;

    /**
     * Gets the value of the folderShape property.
     * 
     * @return
     *     possible object is
     *     {@link FolderResponseShapeType }
     *     
     */
    public FolderResponseShapeType getFolderShape() {
        return folderShape;
    }

    /**
     * Sets the value of the folderShape property.
     * 
     * @param value
     *     allowed object is
     *     {@link FolderResponseShapeType }
     *     
     */
    public void setFolderShape(FolderResponseShapeType value) {
        this.folderShape = value;
    }

    /**
     * Gets the value of the syncFolderId property.
     * 
     * @return
     *     possible object is
     *     {@link TargetFolderIdType }
     *     
     */
    public TargetFolderIdType getSyncFolderId() {
        return syncFolderId;
    }

    /**
     * Sets the value of the syncFolderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link TargetFolderIdType }
     *     
     */
    public void setSyncFolderId(TargetFolderIdType value) {
        this.syncFolderId = value;
    }

    /**
     * Gets the value of the syncState property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSyncState() {
        return syncState;
    }

    /**
     * Sets the value of the syncState property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSyncState(String value) {
        this.syncState = value;
    }

}

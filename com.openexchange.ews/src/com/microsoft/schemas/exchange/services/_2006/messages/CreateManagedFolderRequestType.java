
package com.microsoft.schemas.exchange.services._2006.messages;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.microsoft.schemas.exchange.services._2006.types.EmailAddressType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfFolderNamesType;


/**
 * <p>Java class for CreateManagedFolderRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CreateManagedFolderRequestType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/messages}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element name="FolderNames" type="{http://schemas.microsoft.com/exchange/services/2006/types}NonEmptyArrayOfFolderNamesType"/>
 *         &lt;element name="Mailbox" type="{http://schemas.microsoft.com/exchange/services/2006/types}EmailAddressType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreateManagedFolderRequestType", propOrder = {
    "folderNames",
    "mailbox"
})
public class CreateManagedFolderRequestType
    extends BaseRequestType
{

    @XmlElement(name = "FolderNames", required = true)
    protected NonEmptyArrayOfFolderNamesType folderNames;
    @XmlElement(name = "Mailbox")
    protected EmailAddressType mailbox;

    /**
     * Gets the value of the folderNames property.
     * 
     * @return
     *     possible object is
     *     {@link NonEmptyArrayOfFolderNamesType }
     *     
     */
    public NonEmptyArrayOfFolderNamesType getFolderNames() {
        return folderNames;
    }

    /**
     * Sets the value of the folderNames property.
     * 
     * @param value
     *     allowed object is
     *     {@link NonEmptyArrayOfFolderNamesType }
     *     
     */
    public void setFolderNames(NonEmptyArrayOfFolderNamesType value) {
        this.folderNames = value;
    }

    /**
     * Gets the value of the mailbox property.
     * 
     * @return
     *     possible object is
     *     {@link EmailAddressType }
     *     
     */
    public EmailAddressType getMailbox() {
        return mailbox;
    }

    /**
     * Sets the value of the mailbox property.
     * 
     * @param value
     *     allowed object is
     *     {@link EmailAddressType }
     *     
     */
    public void setMailbox(EmailAddressType value) {
        this.mailbox = value;
    }

}

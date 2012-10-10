
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         Mailbox statistics search result.
 *       
 * 
 * <p>Java class for MailboxStatisticsSearchResultType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MailboxStatisticsSearchResultType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="UserMailbox" type="{http://schemas.microsoft.com/exchange/services/2006/types}UserMailboxType"/>
 *         &lt;element name="KeywordStatisticsSearchResult" type="{http://schemas.microsoft.com/exchange/services/2006/types}KeywordStatisticsSearchResultType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MailboxStatisticsSearchResultType", propOrder = {
    "userMailbox",
    "keywordStatisticsSearchResult"
})
public class MailboxStatisticsSearchResultType {

    @XmlElement(name = "UserMailbox", required = true)
    protected UserMailboxType userMailbox;
    @XmlElement(name = "KeywordStatisticsSearchResult")
    protected KeywordStatisticsSearchResultType keywordStatisticsSearchResult;

    /**
     * Gets the value of the userMailbox property.
     * 
     * @return
     *     possible object is
     *     {@link UserMailboxType }
     *     
     */
    public UserMailboxType getUserMailbox() {
        return userMailbox;
    }

    /**
     * Sets the value of the userMailbox property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserMailboxType }
     *     
     */
    public void setUserMailbox(UserMailboxType value) {
        this.userMailbox = value;
    }

    /**
     * Gets the value of the keywordStatisticsSearchResult property.
     * 
     * @return
     *     possible object is
     *     {@link KeywordStatisticsSearchResultType }
     *     
     */
    public KeywordStatisticsSearchResultType getKeywordStatisticsSearchResult() {
        return keywordStatisticsSearchResult;
    }

    /**
     * Sets the value of the keywordStatisticsSearchResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link KeywordStatisticsSearchResultType }
     *     
     */
    public void setKeywordStatisticsSearchResult(KeywordStatisticsSearchResultType value) {
        this.keywordStatisticsSearchResult = value;
    }

}

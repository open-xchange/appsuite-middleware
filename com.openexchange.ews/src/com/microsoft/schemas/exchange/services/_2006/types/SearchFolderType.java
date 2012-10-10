
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SearchFolderType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SearchFolderType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}FolderType">
 *       &lt;sequence>
 *         &lt;element name="SearchParameters" type="{http://schemas.microsoft.com/exchange/services/2006/types}SearchParametersType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SearchFolderType", propOrder = {
    "searchParameters"
})
public class SearchFolderType
    extends FolderType
{

    @XmlElement(name = "SearchParameters")
    protected SearchParametersType searchParameters;

    /**
     * Gets the value of the searchParameters property.
     * 
     * @return
     *     possible object is
     *     {@link SearchParametersType }
     *     
     */
    public SearchParametersType getSearchParameters() {
        return searchParameters;
    }

    /**
     * Sets the value of the searchParameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link SearchParametersType }
     *     
     */
    public void setSearchParameters(SearchParametersType value) {
        this.searchParameters = value;
    }

}

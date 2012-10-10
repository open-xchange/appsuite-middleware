
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         Array of search item kind enum.
 *       
 * 
 * <p>Java class for ArrayOfSearchItemKindsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfSearchItemKindsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SearchItemKind" type="{http://schemas.microsoft.com/exchange/services/2006/types}SearchItemKindType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfSearchItemKindsType", propOrder = {
    "searchItemKind"
})
public class ArrayOfSearchItemKindsType {

    @XmlElement(name = "SearchItemKind", required = true)
    protected List<SearchItemKindType> searchItemKind;

    /**
     * Gets the value of the searchItemKind property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the searchItemKind property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSearchItemKind().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SearchItemKindType }
     * 
     * 
     */
    public List<SearchItemKindType> getSearchItemKind() {
        if (searchItemKind == null) {
            searchItemKind = new ArrayList<SearchItemKindType>();
        }
        return this.searchItemKind;
    }

}

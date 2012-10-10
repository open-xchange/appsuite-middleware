
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for NonEmptyArrayOfRestrictedGroupIdentifiersType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NonEmptyArrayOfRestrictedGroupIdentifiersType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RestrictedGroupIdentifier" type="{http://schemas.microsoft.com/exchange/services/2006/types}SidAndAttributesType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NonEmptyArrayOfRestrictedGroupIdentifiersType", propOrder = {
    "restrictedGroupIdentifier"
})
public class NonEmptyArrayOfRestrictedGroupIdentifiersType {

    @XmlElement(name = "RestrictedGroupIdentifier", required = true)
    protected List<SidAndAttributesType> restrictedGroupIdentifier;

    /**
     * Gets the value of the restrictedGroupIdentifier property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the restrictedGroupIdentifier property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRestrictedGroupIdentifier().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SidAndAttributesType }
     * 
     * 
     */
    public List<SidAndAttributesType> getRestrictedGroupIdentifier() {
        if (restrictedGroupIdentifier == null) {
            restrictedGroupIdentifier = new ArrayList<SidAndAttributesType>();
        }
        return this.restrictedGroupIdentifier;
    }

}

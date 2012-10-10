
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfTransitionsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfTransitionsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://schemas.microsoft.com/exchange/services/2006/types}Transition" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfTransitionsType", propOrder = {
    "transition"
})
public class ArrayOfTransitionsType {

    @XmlElementRef(name = "Transition", namespace = "http://schemas.microsoft.com/exchange/services/2006/types", type = JAXBElement.class)
    protected List<JAXBElement<? extends TransitionType>> transition;
    @XmlAttribute(name = "Id")
    protected String id;

    /**
     * Gets the value of the transition property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the transition property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTransition().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link AbsoluteDateTransitionType }{@code >}
     * {@link JAXBElement }{@code <}{@link RecurringDayTransitionType }{@code >}
     * {@link JAXBElement }{@code <}{@link RecurringDateTransitionType }{@code >}
     * {@link JAXBElement }{@code <}{@link TransitionType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends TransitionType>> getTransition() {
        if (transition == null) {
            transition = new ArrayList<JAXBElement<? extends TransitionType>>();
        }
        return this.transition;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}

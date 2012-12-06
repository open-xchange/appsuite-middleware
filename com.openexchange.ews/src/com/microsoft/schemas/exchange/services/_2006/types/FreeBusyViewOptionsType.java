
package com.microsoft.schemas.exchange.services._2006.types;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FreeBusyViewOptionsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FreeBusyViewOptionsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TimeWindow" type="{http://schemas.microsoft.com/exchange/services/2006/types}Duration"/>
 *         &lt;element name="MergedFreeBusyIntervalInMinutes" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="RequestedView" type="{http://schemas.microsoft.com/exchange/services/2006/types}FreeBusyViewType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FreeBusyViewOptionsType", propOrder = {
    "timeWindow",
    "mergedFreeBusyIntervalInMinutes",
    "requestedView"
})
public class FreeBusyViewOptionsType {

    @XmlElement(name = "TimeWindow", required = true)
    protected Duration timeWindow;
    @XmlElement(name = "MergedFreeBusyIntervalInMinutes")
    protected Integer mergedFreeBusyIntervalInMinutes;
    @XmlList
    @XmlElement(name = "RequestedView")
    protected List<String> requestedView;

    /**
     * Gets the value of the timeWindow property.
     * 
     * @return
     *     possible object is
     *     {@link Duration }
     *     
     */
    public Duration getTimeWindow() {
        return timeWindow;
    }

    /**
     * Sets the value of the timeWindow property.
     * 
     * @param value
     *     allowed object is
     *     {@link Duration }
     *     
     */
    public void setTimeWindow(Duration value) {
        this.timeWindow = value;
    }

    /**
     * Gets the value of the mergedFreeBusyIntervalInMinutes property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMergedFreeBusyIntervalInMinutes() {
        return mergedFreeBusyIntervalInMinutes;
    }

    /**
     * Sets the value of the mergedFreeBusyIntervalInMinutes property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMergedFreeBusyIntervalInMinutes(Integer value) {
        this.mergedFreeBusyIntervalInMinutes = value;
    }

    /**
     * Gets the value of the requestedView property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the requestedView property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRequestedView().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getRequestedView() {
        if (requestedView == null) {
            requestedView = new ArrayList<String>();
        }
        return this.requestedView;
    }

}

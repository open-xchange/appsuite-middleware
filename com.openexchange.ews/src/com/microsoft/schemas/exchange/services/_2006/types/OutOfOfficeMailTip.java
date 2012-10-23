
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OutOfOfficeMailTip complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OutOfOfficeMailTip">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ReplyBody" type="{http://schemas.microsoft.com/exchange/services/2006/types}ReplyBody"/>
 *         &lt;element name="Duration" type="{http://schemas.microsoft.com/exchange/services/2006/types}Duration" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OutOfOfficeMailTip", propOrder = {
    "replyBody",
    "duration"
})
public class OutOfOfficeMailTip {

    @XmlElement(name = "ReplyBody", required = true)
    protected ReplyBody replyBody;
    @XmlElement(name = "Duration")
    protected Duration duration;

    /**
     * Gets the value of the replyBody property.
     * 
     * @return
     *     possible object is
     *     {@link ReplyBody }
     *     
     */
    public ReplyBody getReplyBody() {
        return replyBody;
    }

    /**
     * Sets the value of the replyBody property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReplyBody }
     *     
     */
    public void setReplyBody(ReplyBody value) {
        this.replyBody = value;
    }

    /**
     * Gets the value of the duration property.
     * 
     * @return
     *     possible object is
     *     {@link Duration }
     *     
     */
    public Duration getDuration() {
        return duration;
    }

    /**
     * Sets the value of the duration property.
     * 
     * @param value
     *     allowed object is
     *     {@link Duration }
     *     
     */
    public void setDuration(Duration value) {
        this.duration = value;
    }

}

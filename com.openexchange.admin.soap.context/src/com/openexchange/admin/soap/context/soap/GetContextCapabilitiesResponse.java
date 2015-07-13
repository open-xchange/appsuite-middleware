
package com.openexchange.admin.soap.context.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;



@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "_return"
})
@XmlRootElement(name = "getContextCapabilitiesResponse")
public class GetContextCapabilitiesResponse {

    @XmlElement(name = "return", nillable = true)
    protected String _return;

    /**
     * Ruft den Wert der return-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Context }
     *
     */
    public String getReturn() {
        return _return;
    }

    /**
     * Legt den Wert der return-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Context }
     *
     */
    public void setReturn(String value) {
        this._return = value;
    }

}

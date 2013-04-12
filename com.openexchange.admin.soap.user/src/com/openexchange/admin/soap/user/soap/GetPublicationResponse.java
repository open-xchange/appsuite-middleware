
package com.openexchange.admin.soap.user.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.user.dataobjects.Publication;


/**
 * <p>Java-Klasse f\u00fcr anonymous complex type.
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "_return"
})
@XmlRootElement(name = "getPublicationResponse")
public class GetPublicationResponse {

    @XmlElement(name = "return", nillable = true)
    protected Publication _return;

    /**
     * Ruft den Wert der return-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Publication }
     *
     */
    public Publication getReturn() {
        return _return;
    }

    /**
     * Legt den Wert der return-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Publication }
     *
     */
    public void setReturn(Publication value) {
        this._return = value;
    }

}

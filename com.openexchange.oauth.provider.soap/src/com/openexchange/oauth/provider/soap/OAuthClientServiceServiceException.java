
package com.openexchange.oauth.provider.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ServiceException" type="{http://soap.provider.oauth.openexchange.com}ServiceException" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "serviceException"
})
@XmlRootElement(name = "OAuthClientServiceServiceException")
public class OAuthClientServiceServiceException {

    @XmlElement(name = "ServiceException", nillable = true)
    protected ServiceException serviceException;

    /**
     * Ruft den Wert der serviceException-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ServiceException }
     *     
     */
    public ServiceException getServiceException() {
        return serviceException;
    }

    /**
     * Legt den Wert der serviceException-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceException }
     *     
     */
    public void setServiceException(ServiceException value) {
        this.serviceException = value;
    }

}

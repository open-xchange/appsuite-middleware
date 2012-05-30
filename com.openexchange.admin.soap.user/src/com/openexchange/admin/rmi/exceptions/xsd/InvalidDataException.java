
package com.openexchange.admin.rmi.exceptions.xsd;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.Exception;


/**
 * <p>Java-Klasse für InvalidDataException complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="InvalidDataException">
 *   &lt;complexContent>
 *     &lt;extension base="{http://soap.admin.openexchange.com}Exception">
 *       &lt;sequence>
 *         &lt;element name="objectname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InvalidDataException", propOrder = {
    "objectname"
})
public class InvalidDataException
    extends Exception
{

    @XmlElementRef(name = "objectname", namespace = "http://exceptions.rmi.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> objectname;

    /**
     * Ruft den Wert der objectname-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getObjectname() {
        return objectname;
    }

    /**
     * Legt den Wert der objectname-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setObjectname(JAXBElement<String> value) {
        this.objectname = value;
    }

}

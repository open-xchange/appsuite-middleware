
package com.openexchange.admin.soap;

import java.io.xsd.IOException;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.rmi.exceptions.xsd.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.xsd.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.xsd.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.xsd.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.xsd.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.xsd.NoSuchDatabaseException;
import com.openexchange.admin.rmi.exceptions.xsd.NoSuchFilestoreException;
import com.openexchange.admin.rmi.exceptions.xsd.NoSuchReasonException;
import com.openexchange.admin.rmi.exceptions.xsd.OXContextException;
import com.openexchange.admin.rmi.exceptions.xsd.StorageException;


/**
 * <p>Java-Klasse für Exception complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Exception">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Exception" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Exception", propOrder = {
    "exception"
})
@XmlSeeAlso({
    InvalidDataException.class,
    StorageException.class,
    ContextExistsException.class,
    NoSuchDatabaseException.class,
    InvalidCredentialsException.class,
    NoSuchReasonException.class,
    NoSuchFilestoreException.class,
    DatabaseUpdateException.class,
    NoSuchContextException.class,
    OXContextException.class,
    IOException.class
})
public class Exception {

    @XmlElementRef(name = "Exception", namespace = "http://soap.admin.openexchange.com", type = JAXBElement.class)
    protected JAXBElement<Object> exception;

    /**
     * Ruft den Wert der exception-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     
     */
    public JAXBElement<Object> getException() {
        return exception;
    }

    /**
     * Legt den Wert der exception-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     
     */
    public void setException(JAXBElement<Object> value) {
        this.exception = value;
    }

}

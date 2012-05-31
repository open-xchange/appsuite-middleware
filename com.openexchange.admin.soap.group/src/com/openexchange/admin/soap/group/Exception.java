
package com.openexchange.admin.soap.group;

import java.io.xsd.IOException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.rmi.exceptions.xsd.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.xsd.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.xsd.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.xsd.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.xsd.NoSuchGroupException;
import com.openexchange.admin.rmi.exceptions.xsd.NoSuchUserException;
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
    NoSuchContextException.class,
    StorageException.class,
    NoSuchUserException.class,
    NoSuchGroupException.class,
    InvalidCredentialsException.class,
    DatabaseUpdateException.class,
    IOException.class
})
public class Exception {

    @XmlElement(name = "Exception", nillable = true)
    protected Object exception;

    /**
     * Ruft den Wert der exception-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getException() {
        return exception;
    }

    /**
     * Legt den Wert der exception-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setException(Object value) {
        this.exception = value;
    }

}

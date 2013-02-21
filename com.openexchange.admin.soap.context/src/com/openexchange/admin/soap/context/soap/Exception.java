
package com.openexchange.admin.soap.context.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.context.exceptions.ContextExistsException;
import com.openexchange.admin.soap.context.exceptions.DatabaseUpdateException;
import com.openexchange.admin.soap.context.exceptions.InvalidCredentialsException;
import com.openexchange.admin.soap.context.exceptions.InvalidDataException;
import com.openexchange.admin.soap.context.exceptions.NoSuchContextException;
import com.openexchange.admin.soap.context.exceptions.NoSuchDatabaseException;
import com.openexchange.admin.soap.context.exceptions.NoSuchFilestoreException;
import com.openexchange.admin.soap.context.exceptions.NoSuchReasonException;
import com.openexchange.admin.soap.context.exceptions.OXContextException;
import com.openexchange.admin.soap.context.exceptions.StorageException;
import com.openexchange.admin.soap.context.io.IOException;


/**
 * <p>Java-Klasse f\u00fcr Exception complex type.
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

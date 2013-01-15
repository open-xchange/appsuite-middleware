
package com.openexchange.admin.soap.context.exceptions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.context.soap.Exception;


/**
 * <p>Java-Klasse f\u00fcr DatabaseUpdateException complex type.
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 *
 * <pre>
 * &lt;complexType name="DatabaseUpdateException">
 *   &lt;complexContent>
 *     &lt;extension base="{http://soap.admin.openexchange.com}Exception">
 *       &lt;sequence>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DatabaseUpdateException")
public class DatabaseUpdateException
    extends Exception
{


}

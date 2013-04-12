
package com.openexchange.admin.soap.user.exceptions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.user.soap.Exception;


/**
 * <p>Java-Klasse f\u00fcr MissingServiceException complex type.
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 *
 * <pre>
 * &lt;complexType name="NoSuchContextException">
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
@XmlType(name = "MissingServiceException")
public class MissingServiceException
    extends Exception
{


}

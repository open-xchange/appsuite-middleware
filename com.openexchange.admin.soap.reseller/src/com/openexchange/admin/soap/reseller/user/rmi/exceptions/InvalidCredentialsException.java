
package com.openexchange.admin.soap.reseller.user.rmi.exceptions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.reseller.user.reseller.soap.Exception;


/**
 * <p>Java-Klasse f\u00fcr InvalidCredentialsException complex type.
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 *
 * <pre>
 * &lt;complexType name="InvalidCredentialsException">
 *   &lt;complexContent>
 *     &lt;extension base="{http://soap.reseller.admin.openexchange.com}Exception">
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
@XmlType(name = "InvalidCredentialsException")
public class InvalidCredentialsException
    extends Exception
{


}

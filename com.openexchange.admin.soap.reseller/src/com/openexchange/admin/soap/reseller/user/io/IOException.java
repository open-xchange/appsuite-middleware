
package com.openexchange.admin.soap.reseller.user.io;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.reseller.user.rmi.RemoteException;
import com.openexchange.admin.soap.reseller.user.soap.Exception;


/**
 * <p>Java-Klasse für IOException complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="IOException">
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
@XmlType(name = "IOException")
@XmlSeeAlso({
    RemoteException.class
})
public class IOException
    extends Exception
{


}


package com._4psa.resellerdata_xsd._2_5;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.clientdata_xsd._2_5.ExtendedClientInfo;
import com._4psa.resellermessagesinfo_xsd._2_5.GetResellerDetailsResponseType;


/**
 * Reseller account details data
 *
 * <p>Java class for ExtendedResellerInfo complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ExtendedResellerInfo">
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/ClientData.xsd/2.5.1}ExtendedClientInfo">
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExtendedResellerInfo")
@XmlSeeAlso({
    GetResellerDetailsResponseType.class
})
public class ExtendedResellerInfo
    extends ExtendedClientInfo
{


}

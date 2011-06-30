
package com._4psa.resellermessagesinfo_xsd._2_5;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import com._4psa.resellerdata_xsd._2_5.ExtendedResellerInfo;
import com._4psa.resellermessages_xsd._2_5.AddResellerResponse;


/**
 * Get detailed reseller data response type
 * 
 * <p>Java class for GetResellerDetailsResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetResellerDetailsResponseType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/ResellerData.xsd/2.5.1}ExtendedResellerInfo">
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetResellerDetailsResponseType")
@XmlSeeAlso({
    AddResellerResponse.class
})
public class GetResellerDetailsResponseType
    extends ExtendedResellerInfo
{


}

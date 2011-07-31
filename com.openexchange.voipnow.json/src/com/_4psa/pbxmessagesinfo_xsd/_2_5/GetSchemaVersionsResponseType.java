
package com._4psa.pbxmessagesinfo_xsd._2_5;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * Get SOAP schema version: response type
 *
 * <p>Java class for GetSchemaVersionsResponseType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="GetSchemaVersionsResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="versions" type="{http://4psa.com/Common.xsd/2.5.1}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetSchemaVersionsResponseType", propOrder = {
    "versions"
})
public class GetSchemaVersionsResponseType {

    protected List<String> versions;

    /**
     * Gets the value of the versions property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the versions property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVersions().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     *
     *
     */
    public List<String> getVersions() {
        if (versions == null) {
            versions = new ArrayList<String>();
        }
        return this.versions;
    }

}

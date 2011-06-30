
package com._4psa.pbxmessages_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com._4psa.pbxdata_xsd._2_5.DeviceExtension;
import com._4psa.pbxdata_xsd._2_5.DeviceInfo;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/PBXData.xsd/2.5.1}DeviceInfo">
 *       &lt;sequence>
 *         &lt;element name="serial" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *         &lt;element name="ownerID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;element name="assignedClientID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *         &lt;element name="assignedExtensions" type="{http://4psa.com/PBXData.xsd/2.5.1}DeviceExtension" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "serial",
    "ownerID",
    "assignedClientID",
    "assignedExtensions"
})
@XmlRootElement(name = "AddDeviceRequest")
public class AddDeviceRequest
    extends DeviceInfo
{

    @XmlElement(required = true)
    protected String serial;
    protected BigInteger ownerID;
    protected BigInteger assignedClientID;
    protected List<DeviceExtension> assignedExtensions;

    /**
     * Gets the value of the serial property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSerial() {
        return serial;
    }

    /**
     * Sets the value of the serial property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSerial(String value) {
        this.serial = value;
    }

    /**
     * Gets the value of the ownerID property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getOwnerID() {
        return ownerID;
    }

    /**
     * Sets the value of the ownerID property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setOwnerID(BigInteger value) {
        this.ownerID = value;
    }

    /**
     * Gets the value of the assignedClientID property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getAssignedClientID() {
        return assignedClientID;
    }

    /**
     * Sets the value of the assignedClientID property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setAssignedClientID(BigInteger value) {
        this.assignedClientID = value;
    }

    /**
     * Gets the value of the assignedExtensions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the assignedExtensions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAssignedExtensions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DeviceExtension }
     * 
     * 
     */
    public List<DeviceExtension> getAssignedExtensions() {
        if (assignedExtensions == null) {
            assignedExtensions = new ArrayList<DeviceExtension>();
        }
        return this.assignedExtensions;
    }

}

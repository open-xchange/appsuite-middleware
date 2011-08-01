
package com._4psa.pbxmessagesinfo_xsd._2_5;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import com._4psa.common_xsd._2_5.Notice;
import com._4psa.pbxdata_xsd._2_5.DeviceExtension;


/**
 * Get time intervals: response type
 *
 * <p>Java class for GetDevicesResponseType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="GetDevicesResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="devices" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="deviceID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *                   &lt;element name="status" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
 *                   &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                   &lt;element name="serial" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                   &lt;element name="manufacturer" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                   &lt;element name="model" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                   &lt;element name="firmware" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                   &lt;element name="firmwareVersion" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                   &lt;element name="mac" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                   &lt;element name="ownerID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *                   &lt;element name="assignedClientID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
 *                   &lt;element name="assignedExtensions" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;extension base="{http://4psa.com/PBXData.xsd/2.5.1}DeviceExtension">
 *                           &lt;sequence>
 *                             &lt;element name="number" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/extension>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="tplID" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="notice" type="{http://4psa.com/Common.xsd/2.5.1}notice" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetDevicesResponseType", propOrder = {
    "devices",
    "notice"
})
public class GetDevicesResponseType {

    protected List<GetDevicesResponseType.Devices> devices;
    protected List<Notice> notice;

    /**
     * Gets the value of the devices property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the devices property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDevices().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GetDevicesResponseType.Devices }
     *
     *
     */
    public List<GetDevicesResponseType.Devices> getDevices() {
        if (devices == null) {
            devices = new ArrayList<GetDevicesResponseType.Devices>();
        }
        return this.devices;
    }

    /**
     * Gets the value of the notice property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the notice property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNotice().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Notice }
     *
     *
     */
    public List<Notice> getNotice() {
        if (notice == null) {
            notice = new ArrayList<Notice>();
        }
        return this.notice;
    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="deviceID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
     *         &lt;element name="status" type="{http://4psa.com/Common.xsd/2.5.1}integer" minOccurs="0"/>
     *         &lt;element name="name" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
     *         &lt;element name="serial" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
     *         &lt;element name="manufacturer" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
     *         &lt;element name="model" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
     *         &lt;element name="firmware" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
     *         &lt;element name="firmwareVersion" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
     *         &lt;element name="mac" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
     *         &lt;element name="ownerID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
     *         &lt;element name="assignedClientID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger" minOccurs="0"/>
     *         &lt;element name="assignedExtensions" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;extension base="{http://4psa.com/PBXData.xsd/2.5.1}DeviceExtension">
     *                 &lt;sequence>
     *                   &lt;element name="number" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
     *                 &lt;/sequence>
     *               &lt;/extension>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="tplID" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "deviceID",
        "status",
        "name",
        "serial",
        "manufacturer",
        "model",
        "firmware",
        "firmwareVersion",
        "mac",
        "ownerID",
        "assignedClientID",
        "assignedExtensions",
        "tplID"
    })
    public static class Devices {

        protected BigInteger deviceID;
        protected BigInteger status;
        protected String name;
        protected String serial;
        protected String manufacturer;
        protected String model;
        protected String firmware;
        protected String firmwareVersion;
        protected String mac;
        protected BigInteger ownerID;
        protected BigInteger assignedClientID;
        protected List<GetDevicesResponseType.Devices.AssignedExtensions> assignedExtensions;
        protected Object tplID;

        /**
         * Gets the value of the deviceID property.
         *
         * @return
         *     possible object is
         *     {@link BigInteger }
         *
         */
        public BigInteger getDeviceID() {
            return deviceID;
        }

        /**
         * Sets the value of the deviceID property.
         *
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *
         */
        public void setDeviceID(BigInteger value) {
            this.deviceID = value;
        }

        /**
         * Gets the value of the status property.
         *
         * @return
         *     possible object is
         *     {@link BigInteger }
         *
         */
        public BigInteger getStatus() {
            return status;
        }

        /**
         * Sets the value of the status property.
         *
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *
         */
        public void setStatus(BigInteger value) {
            this.status = value;
        }

        /**
         * Gets the value of the name property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the value of the name property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setName(String value) {
            this.name = value;
        }

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
         * Gets the value of the manufacturer property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getManufacturer() {
            return manufacturer;
        }

        /**
         * Sets the value of the manufacturer property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setManufacturer(String value) {
            this.manufacturer = value;
        }

        /**
         * Gets the value of the model property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getModel() {
            return model;
        }

        /**
         * Sets the value of the model property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setModel(String value) {
            this.model = value;
        }

        /**
         * Gets the value of the firmware property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getFirmware() {
            return firmware;
        }

        /**
         * Sets the value of the firmware property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setFirmware(String value) {
            this.firmware = value;
        }

        /**
         * Gets the value of the firmwareVersion property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getFirmwareVersion() {
            return firmwareVersion;
        }

        /**
         * Sets the value of the firmwareVersion property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setFirmwareVersion(String value) {
            this.firmwareVersion = value;
        }

        /**
         * Gets the value of the mac property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getMac() {
            return mac;
        }

        /**
         * Sets the value of the mac property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setMac(String value) {
            this.mac = value;
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
         * {@link GetDevicesResponseType.Devices.AssignedExtensions }
         *
         *
         */
        public List<GetDevicesResponseType.Devices.AssignedExtensions> getAssignedExtensions() {
            if (assignedExtensions == null) {
                assignedExtensions = new ArrayList<GetDevicesResponseType.Devices.AssignedExtensions>();
            }
            return this.assignedExtensions;
        }

        /**
         * Gets the value of the tplID property.
         *
         * @return
         *     possible object is
         *     {@link Object }
         *
         */
        public Object getTplID() {
            return tplID;
        }

        /**
         * Sets the value of the tplID property.
         *
         * @param value
         *     allowed object is
         *     {@link Object }
         *
         */
        public void setTplID(Object value) {
            this.tplID = value;
        }


        /**
         * <p>Java class for anonymous complex type.
         *
         * <p>The following schema fragment specifies the expected content contained within this class.
         *
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;extension base="{http://4psa.com/PBXData.xsd/2.5.1}DeviceExtension">
         *       &lt;sequence>
         *         &lt;element name="number" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
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
            "number"
        })
        public static class AssignedExtensions
            extends DeviceExtension
        {

            protected Object number;

            /**
             * Gets the value of the number property.
             *
             * @return
             *     possible object is
             *     {@link Object }
             *
             */
            public Object getNumber() {
                return number;
            }

            /**
             * Sets the value of the number property.
             *
             * @param value
             *     allowed object is
             *     {@link Object }
             *
             */
            public void setNumber(Object value) {
                this.number = value;
            }

        }

    }

}

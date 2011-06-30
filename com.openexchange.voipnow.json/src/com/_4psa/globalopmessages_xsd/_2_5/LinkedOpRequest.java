
package com._4psa.globalopmessages_xsd._2_5;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="operation" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="identifier" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *                   &lt;element name="package" type="{http://4psa.com/Common.xsd/2.5.1}base64" minOccurs="0"/>
 *                   &lt;element name="finalOnFailure" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *                   &lt;element name="appendOpResponse" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="identifier" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *                             &lt;element name="responseMapping" maxOccurs="unbounded">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="responseParam" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *                                       &lt;element name="requestParam" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                                       &lt;element name="node" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
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
    "operation"
})
@XmlRootElement(name = "LinkedOpRequest")
public class LinkedOpRequest {

    protected List<LinkedOpRequest.Operation> operation;

    /**
     * Gets the value of the operation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the operation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOperation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LinkedOpRequest.Operation }
     * 
     * 
     */
    public List<LinkedOpRequest.Operation> getOperation() {
        if (operation == null) {
            operation = new ArrayList<LinkedOpRequest.Operation>();
        }
        return this.operation;
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
     *         &lt;element name="identifier" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
     *         &lt;element name="package" type="{http://4psa.com/Common.xsd/2.5.1}base64" minOccurs="0"/>
     *         &lt;element name="finalOnFailure" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
     *         &lt;element name="appendOpResponse" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="identifier" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
     *                   &lt;element name="responseMapping" maxOccurs="unbounded">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence>
     *                             &lt;element name="responseParam" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
     *                             &lt;element name="requestParam" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
     *                             &lt;element name="node" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
     *                           &lt;/sequence>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
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
        "identifier",
        "_package",
        "finalOnFailure",
        "appendOpResponse"
    })
    public static class Operation {

        @XmlElement(required = true)
        protected String identifier;
        @XmlElement(name = "package")
        protected byte[] _package;
        protected Boolean finalOnFailure;
        protected List<LinkedOpRequest.Operation.AppendOpResponse> appendOpResponse;

        /**
         * Gets the value of the identifier property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getIdentifier() {
            return identifier;
        }

        /**
         * Sets the value of the identifier property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setIdentifier(String value) {
            this.identifier = value;
        }

        /**
         * Gets the value of the package property.
         * 
         * @return
         *     possible object is
         *     byte[]
         */
        public byte[] getPackage() {
            return _package;
        }

        /**
         * Sets the value of the package property.
         * 
         * @param value
         *     allowed object is
         *     byte[]
         */
        public void setPackage(byte[] value) {
            this._package = ((byte[]) value);
        }

        /**
         * Gets the value of the finalOnFailure property.
         * 
         * @return
         *     possible object is
         *     {@link Boolean }
         *     
         */
        public Boolean isFinalOnFailure() {
            return finalOnFailure;
        }

        /**
         * Sets the value of the finalOnFailure property.
         * 
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *     
         */
        public void setFinalOnFailure(Boolean value) {
            this.finalOnFailure = value;
        }

        /**
         * Gets the value of the appendOpResponse property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the appendOpResponse property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAppendOpResponse().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link LinkedOpRequest.Operation.AppendOpResponse }
         * 
         * 
         */
        public List<LinkedOpRequest.Operation.AppendOpResponse> getAppendOpResponse() {
            if (appendOpResponse == null) {
                appendOpResponse = new ArrayList<LinkedOpRequest.Operation.AppendOpResponse>();
            }
            return this.appendOpResponse;
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
         *         &lt;element name="identifier" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
         *         &lt;element name="responseMapping" maxOccurs="unbounded">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;sequence>
         *                   &lt;element name="responseParam" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
         *                   &lt;element name="requestParam" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
         *                   &lt;element name="node" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
         *                 &lt;/sequence>
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
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
            "identifier",
            "responseMapping"
        })
        public static class AppendOpResponse {

            @XmlElement(required = true)
            protected String identifier;
            @XmlElement(required = true)
            protected List<LinkedOpRequest.Operation.AppendOpResponse.ResponseMapping> responseMapping;

            /**
             * Gets the value of the identifier property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getIdentifier() {
                return identifier;
            }

            /**
             * Sets the value of the identifier property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setIdentifier(String value) {
                this.identifier = value;
            }

            /**
             * Gets the value of the responseMapping property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the responseMapping property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getResponseMapping().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link LinkedOpRequest.Operation.AppendOpResponse.ResponseMapping }
             * 
             * 
             */
            public List<LinkedOpRequest.Operation.AppendOpResponse.ResponseMapping> getResponseMapping() {
                if (responseMapping == null) {
                    responseMapping = new ArrayList<LinkedOpRequest.Operation.AppendOpResponse.ResponseMapping>();
                }
                return this.responseMapping;
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
             *         &lt;element name="responseParam" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
             *         &lt;element name="requestParam" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
             *         &lt;element name="node" type="{http://4psa.com/Common.xsd/2.5.1}string" minOccurs="0"/>
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
                "responseParam",
                "requestParam",
                "node"
            })
            public static class ResponseMapping {

                @XmlElement(required = true)
                protected String responseParam;
                protected String requestParam;
                protected String node;

                /**
                 * Gets the value of the responseParam property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getResponseParam() {
                    return responseParam;
                }

                /**
                 * Sets the value of the responseParam property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setResponseParam(String value) {
                    this.responseParam = value;
                }

                /**
                 * Gets the value of the requestParam property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getRequestParam() {
                    return requestParam;
                }

                /**
                 * Sets the value of the requestParam property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setRequestParam(String value) {
                    this.requestParam = value;
                }

                /**
                 * Gets the value of the node property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getNode() {
                    return node;
                }

                /**
                 * Sets the value of the node property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setNode(String value) {
                    this.node = value;
                }

            }

        }

    }

}

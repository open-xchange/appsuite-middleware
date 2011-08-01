
package com._4psa.resellerdata_xsd._2_5;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com._4psa.clientdata_xsd._2_5.UpdateClientPLInfo;
import com._4psa.common_xsd._2_5.Limit;


/**
 * Increase/decrease reseller permissions and limits data
 *
 * <p>Java class for UpdateResellerPLInfo complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="UpdateResellerPLInfo">
 *   &lt;complexContent>
 *     &lt;extension base="{http://4psa.com/ClientData.xsd/2.5.1}UpdateClientPLInfo">
 *       &lt;sequence>
 *         &lt;element name="clientManag" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="advertisingManag" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="stackedManag" type="{http://4psa.com/Common.xsd/2.5.1}boolean" minOccurs="0"/>
 *         &lt;element name="clientMax" type="{http://4psa.com/Common.xsd/2.5.1}limit" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element name="ID" type="{http://4psa.com/Common.xsd/2.5.1}positiveInteger"/>
 *           &lt;element name="identifier" type="{http://4psa.com/Common.xsd/2.5.1}string"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpdateResellerPLInfo", propOrder = {
    "clientManag",
    "advertisingManag",
    "stackedManag",
    "clientMax",
    "id",
    "identifier"
})
public class UpdateResellerPLInfo
    extends UpdateClientPLInfo
{

    protected Boolean clientManag;
    protected Boolean advertisingManag;
    protected Boolean stackedManag;
    protected Limit clientMax;
    @XmlElement(name = "ID")
    protected BigInteger id;
    protected String identifier;

    /**
     * Gets the value of the clientManag property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isClientManag() {
        return clientManag;
    }

    /**
     * Sets the value of the clientManag property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setClientManag(Boolean value) {
        this.clientManag = value;
    }

    /**
     * Gets the value of the advertisingManag property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isAdvertisingManag() {
        return advertisingManag;
    }

    /**
     * Sets the value of the advertisingManag property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setAdvertisingManag(Boolean value) {
        this.advertisingManag = value;
    }

    /**
     * Gets the value of the stackedManag property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isStackedManag() {
        return stackedManag;
    }

    /**
     * Sets the value of the stackedManag property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setStackedManag(Boolean value) {
        this.stackedManag = value;
    }

    /**
     * Gets the value of the clientMax property.
     *
     * @return
     *     possible object is
     *     {@link Limit }
     *
     */
    public Limit getClientMax() {
        return clientMax;
    }

    /**
     * Sets the value of the clientMax property.
     *
     * @param value
     *     allowed object is
     *     {@link Limit }
     *
     */
    public void setClientMax(Limit value) {
        this.clientMax = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getID() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setID(BigInteger value) {
        this.id = value;
    }

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

}

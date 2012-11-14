
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MapiPropertyTypeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="MapiPropertyTypeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ApplicationTime"/>
 *     &lt;enumeration value="ApplicationTimeArray"/>
 *     &lt;enumeration value="Binary"/>
 *     &lt;enumeration value="BinaryArray"/>
 *     &lt;enumeration value="Boolean"/>
 *     &lt;enumeration value="CLSID"/>
 *     &lt;enumeration value="CLSIDArray"/>
 *     &lt;enumeration value="Currency"/>
 *     &lt;enumeration value="CurrencyArray"/>
 *     &lt;enumeration value="Double"/>
 *     &lt;enumeration value="DoubleArray"/>
 *     &lt;enumeration value="Error"/>
 *     &lt;enumeration value="Float"/>
 *     &lt;enumeration value="FloatArray"/>
 *     &lt;enumeration value="Integer"/>
 *     &lt;enumeration value="IntegerArray"/>
 *     &lt;enumeration value="Long"/>
 *     &lt;enumeration value="LongArray"/>
 *     &lt;enumeration value="Null"/>
 *     &lt;enumeration value="Object"/>
 *     &lt;enumeration value="ObjectArray"/>
 *     &lt;enumeration value="Short"/>
 *     &lt;enumeration value="ShortArray"/>
 *     &lt;enumeration value="SystemTime"/>
 *     &lt;enumeration value="SystemTimeArray"/>
 *     &lt;enumeration value="String"/>
 *     &lt;enumeration value="StringArray"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "MapiPropertyTypeType")
@XmlEnum
public enum MapiPropertyTypeType {

    @XmlEnumValue("ApplicationTime")
    APPLICATION_TIME("ApplicationTime"),
    @XmlEnumValue("ApplicationTimeArray")
    APPLICATION_TIME_ARRAY("ApplicationTimeArray"),
    @XmlEnumValue("Binary")
    BINARY("Binary"),
    @XmlEnumValue("BinaryArray")
    BINARY_ARRAY("BinaryArray"),
    @XmlEnumValue("Boolean")
    BOOLEAN("Boolean"),
    CLSID("CLSID"),
    @XmlEnumValue("CLSIDArray")
    CLSID_ARRAY("CLSIDArray"),
    @XmlEnumValue("Currency")
    CURRENCY("Currency"),
    @XmlEnumValue("CurrencyArray")
    CURRENCY_ARRAY("CurrencyArray"),
    @XmlEnumValue("Double")
    DOUBLE("Double"),
    @XmlEnumValue("DoubleArray")
    DOUBLE_ARRAY("DoubleArray"),
    @XmlEnumValue("Error")
    ERROR("Error"),
    @XmlEnumValue("Float")
    FLOAT("Float"),
    @XmlEnumValue("FloatArray")
    FLOAT_ARRAY("FloatArray"),
    @XmlEnumValue("Integer")
    INTEGER("Integer"),
    @XmlEnumValue("IntegerArray")
    INTEGER_ARRAY("IntegerArray"),
    @XmlEnumValue("Long")
    LONG("Long"),
    @XmlEnumValue("LongArray")
    LONG_ARRAY("LongArray"),
    @XmlEnumValue("Null")
    NULL("Null"),
    @XmlEnumValue("Object")
    OBJECT("Object"),
    @XmlEnumValue("ObjectArray")
    OBJECT_ARRAY("ObjectArray"),
    @XmlEnumValue("Short")
    SHORT("Short"),
    @XmlEnumValue("ShortArray")
    SHORT_ARRAY("ShortArray"),
    @XmlEnumValue("SystemTime")
    SYSTEM_TIME("SystemTime"),
    @XmlEnumValue("SystemTimeArray")
    SYSTEM_TIME_ARRAY("SystemTimeArray"),
    @XmlEnumValue("String")
    STRING("String"),
    @XmlEnumValue("StringArray")
    STRING_ARRAY("StringArray");
    private final String value;

    MapiPropertyTypeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MapiPropertyTypeType fromValue(String v) {
        for (MapiPropertyTypeType c: MapiPropertyTypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

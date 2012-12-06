
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for UserConfigurationDictionaryObjectTypesType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="UserConfigurationDictionaryObjectTypesType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="DateTime"/>
 *     &lt;enumeration value="Boolean"/>
 *     &lt;enumeration value="Byte"/>
 *     &lt;enumeration value="String"/>
 *     &lt;enumeration value="Integer32"/>
 *     &lt;enumeration value="UnsignedInteger32"/>
 *     &lt;enumeration value="Integer64"/>
 *     &lt;enumeration value="UnsignedInteger64"/>
 *     &lt;enumeration value="StringArray"/>
 *     &lt;enumeration value="ByteArray"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "UserConfigurationDictionaryObjectTypesType")
@XmlEnum
public enum UserConfigurationDictionaryObjectTypesType {

    @XmlEnumValue("DateTime")
    DATE_TIME("DateTime"),
    @XmlEnumValue("Boolean")
    BOOLEAN("Boolean"),
    @XmlEnumValue("Byte")
    BYTE("Byte"),
    @XmlEnumValue("String")
    STRING("String"),
    @XmlEnumValue("Integer32")
    INTEGER_32("Integer32"),
    @XmlEnumValue("UnsignedInteger32")
    UNSIGNED_INTEGER_32("UnsignedInteger32"),
    @XmlEnumValue("Integer64")
    INTEGER_64("Integer64"),
    @XmlEnumValue("UnsignedInteger64")
    UNSIGNED_INTEGER_64("UnsignedInteger64"),
    @XmlEnumValue("StringArray")
    STRING_ARRAY("StringArray"),
    @XmlEnumValue("ByteArray")
    BYTE_ARRAY("ByteArray");
    private final String value;

    UserConfigurationDictionaryObjectTypesType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static UserConfigurationDictionaryObjectTypesType fromValue(String v) {
        for (UserConfigurationDictionaryObjectTypesType c: UserConfigurationDictionaryObjectTypesType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}


package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DefaultShapeNamesType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="DefaultShapeNamesType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="IdOnly"/>
 *     &lt;enumeration value="Default"/>
 *     &lt;enumeration value="AllProperties"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DefaultShapeNamesType")
@XmlEnum
public enum DefaultShapeNamesType {

    @XmlEnumValue("IdOnly")
    ID_ONLY("IdOnly"),
    @XmlEnumValue("Default")
    DEFAULT("Default"),
    @XmlEnumValue("AllProperties")
    ALL_PROPERTIES("AllProperties");
    private final String value;

    DefaultShapeNamesType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DefaultShapeNamesType fromValue(String v) {
        for (DefaultShapeNamesType c: DefaultShapeNamesType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

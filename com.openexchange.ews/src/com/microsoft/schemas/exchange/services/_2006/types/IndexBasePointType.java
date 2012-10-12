
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for IndexBasePointType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="IndexBasePointType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Beginning"/>
 *     &lt;enumeration value="End"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "IndexBasePointType")
@XmlEnum
public enum IndexBasePointType {

    @XmlEnumValue("Beginning")
    BEGINNING("Beginning"),
    @XmlEnumValue("End")
    END("End");
    private final String value;

    IndexBasePointType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static IndexBasePointType fromValue(String v) {
        for (IndexBasePointType c: IndexBasePointType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}


package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BodyTypeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="BodyTypeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="HTML"/>
 *     &lt;enumeration value="Text"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "BodyTypeType")
@XmlEnum
public enum BodyTypeType {

    HTML("HTML"),
    @XmlEnumValue("Text")
    TEXT("Text");
    private final String value;

    BodyTypeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BodyTypeType fromValue(String v) {
        for (BodyTypeType c: BodyTypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

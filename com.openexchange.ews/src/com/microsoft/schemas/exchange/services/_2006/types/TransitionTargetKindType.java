
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TransitionTargetKindType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="TransitionTargetKindType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Period"/>
 *     &lt;enumeration value="Group"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "TransitionTargetKindType")
@XmlEnum
public enum TransitionTargetKindType {

    @XmlEnumValue("Period")
    PERIOD("Period"),
    @XmlEnumValue("Group")
    GROUP("Group");
    private final String value;

    TransitionTargetKindType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TransitionTargetKindType fromValue(String v) {
        for (TransitionTargetKindType c: TransitionTargetKindType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

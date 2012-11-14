
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MemberStatusType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="MemberStatusType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Unrecognized"/>
 *     &lt;enumeration value="Normal"/>
 *     &lt;enumeration value="Demoted"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "MemberStatusType")
@XmlEnum
public enum MemberStatusType {

    @XmlEnumValue("Unrecognized")
    UNRECOGNIZED("Unrecognized"),
    @XmlEnumValue("Normal")
    NORMAL("Normal"),
    @XmlEnumValue("Demoted")
    DEMOTED("Demoted");
    private final String value;

    MemberStatusType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MemberStatusType fromValue(String v) {
        for (MemberStatusType c: MemberStatusType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

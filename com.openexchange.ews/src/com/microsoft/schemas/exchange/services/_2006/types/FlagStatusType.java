
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FlagStatusType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="FlagStatusType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="NotFlagged"/>
 *     &lt;enumeration value="Flagged"/>
 *     &lt;enumeration value="Complete"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "FlagStatusType")
@XmlEnum
public enum FlagStatusType {

    @XmlEnumValue("NotFlagged")
    NOT_FLAGGED("NotFlagged"),
    @XmlEnumValue("Flagged")
    FLAGGED("Flagged"),
    @XmlEnumValue("Complete")
    COMPLETE("Complete");
    private final String value;

    FlagStatusType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FlagStatusType fromValue(String v) {
        for (FlagStatusType c: FlagStatusType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

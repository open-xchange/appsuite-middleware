
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OofState.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="OofState">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Disabled"/>
 *     &lt;enumeration value="Enabled"/>
 *     &lt;enumeration value="Scheduled"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "OofState")
@XmlEnum
public enum OofState {

    @XmlEnumValue("Disabled")
    DISABLED("Disabled"),
    @XmlEnumValue("Enabled")
    ENABLED("Enabled"),
    @XmlEnumValue("Scheduled")
    SCHEDULED("Scheduled");
    private final String value;

    OofState(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static OofState fromValue(String v) {
        for (OofState c: OofState.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

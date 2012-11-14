
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LegacyFreeBusyType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="LegacyFreeBusyType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Free"/>
 *     &lt;enumeration value="Tentative"/>
 *     &lt;enumeration value="Busy"/>
 *     &lt;enumeration value="OOF"/>
 *     &lt;enumeration value="NoData"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "LegacyFreeBusyType")
@XmlEnum
public enum LegacyFreeBusyType {

    @XmlEnumValue("Free")
    FREE("Free"),
    @XmlEnumValue("Tentative")
    TENTATIVE("Tentative"),
    @XmlEnumValue("Busy")
    BUSY("Busy"),
    OOF("OOF"),
    @XmlEnumValue("NoData")
    NO_DATA("NoData");
    private final String value;

    LegacyFreeBusyType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LegacyFreeBusyType fromValue(String v) {
        for (LegacyFreeBusyType c: LegacyFreeBusyType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

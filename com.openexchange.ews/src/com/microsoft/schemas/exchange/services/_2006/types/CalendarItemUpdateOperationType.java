
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CalendarItemUpdateOperationType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="CalendarItemUpdateOperationType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="SendToNone"/>
 *     &lt;enumeration value="SendOnlyToAll"/>
 *     &lt;enumeration value="SendOnlyToChanged"/>
 *     &lt;enumeration value="SendToAllAndSaveCopy"/>
 *     &lt;enumeration value="SendToChangedAndSaveCopy"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "CalendarItemUpdateOperationType")
@XmlEnum
public enum CalendarItemUpdateOperationType {

    @XmlEnumValue("SendToNone")
    SEND_TO_NONE("SendToNone"),
    @XmlEnumValue("SendOnlyToAll")
    SEND_ONLY_TO_ALL("SendOnlyToAll"),
    @XmlEnumValue("SendOnlyToChanged")
    SEND_ONLY_TO_CHANGED("SendOnlyToChanged"),
    @XmlEnumValue("SendToAllAndSaveCopy")
    SEND_TO_ALL_AND_SAVE_COPY("SendToAllAndSaveCopy"),
    @XmlEnumValue("SendToChangedAndSaveCopy")
    SEND_TO_CHANGED_AND_SAVE_COPY("SendToChangedAndSaveCopy");
    private final String value;

    CalendarItemUpdateOperationType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CalendarItemUpdateOperationType fromValue(String v) {
        for (CalendarItemUpdateOperationType c: CalendarItemUpdateOperationType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

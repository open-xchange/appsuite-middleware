
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CalendarItemCreateOrDeleteOperationType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="CalendarItemCreateOrDeleteOperationType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="SendToNone"/>
 *     &lt;enumeration value="SendOnlyToAll"/>
 *     &lt;enumeration value="SendToAllAndSaveCopy"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "CalendarItemCreateOrDeleteOperationType")
@XmlEnum
public enum CalendarItemCreateOrDeleteOperationType {

    @XmlEnumValue("SendToNone")
    SEND_TO_NONE("SendToNone"),
    @XmlEnumValue("SendOnlyToAll")
    SEND_ONLY_TO_ALL("SendOnlyToAll"),
    @XmlEnumValue("SendToAllAndSaveCopy")
    SEND_TO_ALL_AND_SAVE_COPY("SendToAllAndSaveCopy");
    private final String value;

    CalendarItemCreateOrDeleteOperationType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CalendarItemCreateOrDeleteOperationType fromValue(String v) {
        for (CalendarItemCreateOrDeleteOperationType c: CalendarItemCreateOrDeleteOperationType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

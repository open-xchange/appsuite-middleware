
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for NotificationEventTypeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="NotificationEventTypeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="CopiedEvent"/>
 *     &lt;enumeration value="CreatedEvent"/>
 *     &lt;enumeration value="DeletedEvent"/>
 *     &lt;enumeration value="ModifiedEvent"/>
 *     &lt;enumeration value="MovedEvent"/>
 *     &lt;enumeration value="NewMailEvent"/>
 *     &lt;enumeration value="FreeBusyChangedEvent"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "NotificationEventTypeType")
@XmlEnum
public enum NotificationEventTypeType {

    @XmlEnumValue("CopiedEvent")
    COPIED_EVENT("CopiedEvent"),
    @XmlEnumValue("CreatedEvent")
    CREATED_EVENT("CreatedEvent"),
    @XmlEnumValue("DeletedEvent")
    DELETED_EVENT("DeletedEvent"),
    @XmlEnumValue("ModifiedEvent")
    MODIFIED_EVENT("ModifiedEvent"),
    @XmlEnumValue("MovedEvent")
    MOVED_EVENT("MovedEvent"),
    @XmlEnumValue("NewMailEvent")
    NEW_MAIL_EVENT("NewMailEvent"),
    @XmlEnumValue("FreeBusyChangedEvent")
    FREE_BUSY_CHANGED_EVENT("FreeBusyChangedEvent");
    private final String value;

    NotificationEventTypeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static NotificationEventTypeType fromValue(String v) {
        for (NotificationEventTypeType c: NotificationEventTypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

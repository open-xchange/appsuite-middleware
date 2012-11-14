
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TaskStatusType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="TaskStatusType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="NotStarted"/>
 *     &lt;enumeration value="InProgress"/>
 *     &lt;enumeration value="Completed"/>
 *     &lt;enumeration value="WaitingOnOthers"/>
 *     &lt;enumeration value="Deferred"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "TaskStatusType")
@XmlEnum
public enum TaskStatusType {

    @XmlEnumValue("NotStarted")
    NOT_STARTED("NotStarted"),
    @XmlEnumValue("InProgress")
    IN_PROGRESS("InProgress"),
    @XmlEnumValue("Completed")
    COMPLETED("Completed"),
    @XmlEnumValue("WaitingOnOthers")
    WAITING_ON_OTHERS("WaitingOnOthers"),
    @XmlEnumValue("Deferred")
    DEFERRED("Deferred");
    private final String value;

    TaskStatusType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TaskStatusType fromValue(String v) {
        for (TaskStatusType c: TaskStatusType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

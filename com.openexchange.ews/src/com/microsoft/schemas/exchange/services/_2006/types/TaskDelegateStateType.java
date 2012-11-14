
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TaskDelegateStateType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="TaskDelegateStateType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="NoMatch"/>
 *     &lt;enumeration value="OwnNew"/>
 *     &lt;enumeration value="Owned"/>
 *     &lt;enumeration value="Accepted"/>
 *     &lt;enumeration value="Declined"/>
 *     &lt;enumeration value="Max"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "TaskDelegateStateType")
@XmlEnum
public enum TaskDelegateStateType {

    @XmlEnumValue("NoMatch")
    NO_MATCH("NoMatch"),
    @XmlEnumValue("OwnNew")
    OWN_NEW("OwnNew"),
    @XmlEnumValue("Owned")
    OWNED("Owned"),
    @XmlEnumValue("Accepted")
    ACCEPTED("Accepted"),
    @XmlEnumValue("Declined")
    DECLINED("Declined"),
    @XmlEnumValue("Max")
    MAX("Max");
    private final String value;

    TaskDelegateStateType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TaskDelegateStateType fromValue(String v) {
        for (TaskDelegateStateType c: TaskDelegateStateType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

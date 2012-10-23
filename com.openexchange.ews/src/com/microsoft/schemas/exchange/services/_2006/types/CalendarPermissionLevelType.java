
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CalendarPermissionLevelType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="CalendarPermissionLevelType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="None"/>
 *     &lt;enumeration value="Owner"/>
 *     &lt;enumeration value="PublishingEditor"/>
 *     &lt;enumeration value="Editor"/>
 *     &lt;enumeration value="PublishingAuthor"/>
 *     &lt;enumeration value="Author"/>
 *     &lt;enumeration value="NoneditingAuthor"/>
 *     &lt;enumeration value="Reviewer"/>
 *     &lt;enumeration value="Contributor"/>
 *     &lt;enumeration value="FreeBusyTimeOnly"/>
 *     &lt;enumeration value="FreeBusyTimeAndSubjectAndLocation"/>
 *     &lt;enumeration value="Custom"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "CalendarPermissionLevelType")
@XmlEnum
public enum CalendarPermissionLevelType {

    @XmlEnumValue("None")
    NONE("None"),
    @XmlEnumValue("Owner")
    OWNER("Owner"),
    @XmlEnumValue("PublishingEditor")
    PUBLISHING_EDITOR("PublishingEditor"),
    @XmlEnumValue("Editor")
    EDITOR("Editor"),
    @XmlEnumValue("PublishingAuthor")
    PUBLISHING_AUTHOR("PublishingAuthor"),
    @XmlEnumValue("Author")
    AUTHOR("Author"),
    @XmlEnumValue("NoneditingAuthor")
    NONEDITING_AUTHOR("NoneditingAuthor"),
    @XmlEnumValue("Reviewer")
    REVIEWER("Reviewer"),
    @XmlEnumValue("Contributor")
    CONTRIBUTOR("Contributor"),
    @XmlEnumValue("FreeBusyTimeOnly")
    FREE_BUSY_TIME_ONLY("FreeBusyTimeOnly"),
    @XmlEnumValue("FreeBusyTimeAndSubjectAndLocation")
    FREE_BUSY_TIME_AND_SUBJECT_AND_LOCATION("FreeBusyTimeAndSubjectAndLocation"),
    @XmlEnumValue("Custom")
    CUSTOM("Custom");
    private final String value;

    CalendarPermissionLevelType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CalendarPermissionLevelType fromValue(String v) {
        for (CalendarPermissionLevelType c: CalendarPermissionLevelType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

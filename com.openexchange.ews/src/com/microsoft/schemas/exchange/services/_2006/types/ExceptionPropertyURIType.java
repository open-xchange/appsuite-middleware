
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ExceptionPropertyURIType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ExceptionPropertyURIType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="attachment:Name"/>
 *     &lt;enumeration value="attachment:ContentType"/>
 *     &lt;enumeration value="attachment:Content"/>
 *     &lt;enumeration value="recurrence:Month"/>
 *     &lt;enumeration value="recurrence:DayOfWeekIndex"/>
 *     &lt;enumeration value="recurrence:DaysOfWeek"/>
 *     &lt;enumeration value="recurrence:DayOfMonth"/>
 *     &lt;enumeration value="recurrence:Interval"/>
 *     &lt;enumeration value="recurrence:NumberOfOccurrences"/>
 *     &lt;enumeration value="timezone:Offset"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ExceptionPropertyURIType")
@XmlEnum
public enum ExceptionPropertyURIType {

    @XmlEnumValue("attachment:Name")
    ATTACHMENT_NAME("attachment:Name"),
    @XmlEnumValue("attachment:ContentType")
    ATTACHMENT_CONTENT_TYPE("attachment:ContentType"),
    @XmlEnumValue("attachment:Content")
    ATTACHMENT_CONTENT("attachment:Content"),
    @XmlEnumValue("recurrence:Month")
    RECURRENCE_MONTH("recurrence:Month"),
    @XmlEnumValue("recurrence:DayOfWeekIndex")
    RECURRENCE_DAY_OF_WEEK_INDEX("recurrence:DayOfWeekIndex"),
    @XmlEnumValue("recurrence:DaysOfWeek")
    RECURRENCE_DAYS_OF_WEEK("recurrence:DaysOfWeek"),
    @XmlEnumValue("recurrence:DayOfMonth")
    RECURRENCE_DAY_OF_MONTH("recurrence:DayOfMonth"),
    @XmlEnumValue("recurrence:Interval")
    RECURRENCE_INTERVAL("recurrence:Interval"),
    @XmlEnumValue("recurrence:NumberOfOccurrences")
    RECURRENCE_NUMBER_OF_OCCURRENCES("recurrence:NumberOfOccurrences"),
    @XmlEnumValue("timezone:Offset")
    TIMEZONE_OFFSET("timezone:Offset");
    private final String value;

    ExceptionPropertyURIType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ExceptionPropertyURIType fromValue(String v) {
        for (ExceptionPropertyURIType c: ExceptionPropertyURIType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

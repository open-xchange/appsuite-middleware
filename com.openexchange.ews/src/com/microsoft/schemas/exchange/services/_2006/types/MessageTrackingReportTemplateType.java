
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MessageTrackingReportTemplateType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="MessageTrackingReportTemplateType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Summary"/>
 *     &lt;enumeration value="RecipientPath"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "MessageTrackingReportTemplateType")
@XmlEnum
public enum MessageTrackingReportTemplateType {

    @XmlEnumValue("Summary")
    SUMMARY("Summary"),
    @XmlEnumValue("RecipientPath")
    RECIPIENT_PATH("RecipientPath");
    private final String value;

    MessageTrackingReportTemplateType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MessageTrackingReportTemplateType fromValue(String v) {
        for (MessageTrackingReportTemplateType c: MessageTrackingReportTemplateType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

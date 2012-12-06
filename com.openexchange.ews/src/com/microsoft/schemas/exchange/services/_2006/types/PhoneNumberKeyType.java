
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PhoneNumberKeyType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="PhoneNumberKeyType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="AssistantPhone"/>
 *     &lt;enumeration value="BusinessFax"/>
 *     &lt;enumeration value="BusinessPhone"/>
 *     &lt;enumeration value="BusinessPhone2"/>
 *     &lt;enumeration value="Callback"/>
 *     &lt;enumeration value="CarPhone"/>
 *     &lt;enumeration value="CompanyMainPhone"/>
 *     &lt;enumeration value="HomeFax"/>
 *     &lt;enumeration value="HomePhone"/>
 *     &lt;enumeration value="HomePhone2"/>
 *     &lt;enumeration value="Isdn"/>
 *     &lt;enumeration value="MobilePhone"/>
 *     &lt;enumeration value="OtherFax"/>
 *     &lt;enumeration value="OtherTelephone"/>
 *     &lt;enumeration value="Pager"/>
 *     &lt;enumeration value="PrimaryPhone"/>
 *     &lt;enumeration value="RadioPhone"/>
 *     &lt;enumeration value="Telex"/>
 *     &lt;enumeration value="TtyTddPhone"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "PhoneNumberKeyType")
@XmlEnum
public enum PhoneNumberKeyType {

    @XmlEnumValue("AssistantPhone")
    ASSISTANT_PHONE("AssistantPhone"),
    @XmlEnumValue("BusinessFax")
    BUSINESS_FAX("BusinessFax"),
    @XmlEnumValue("BusinessPhone")
    BUSINESS_PHONE("BusinessPhone"),
    @XmlEnumValue("BusinessPhone2")
    BUSINESS_PHONE_2("BusinessPhone2"),
    @XmlEnumValue("Callback")
    CALLBACK("Callback"),
    @XmlEnumValue("CarPhone")
    CAR_PHONE("CarPhone"),
    @XmlEnumValue("CompanyMainPhone")
    COMPANY_MAIN_PHONE("CompanyMainPhone"),
    @XmlEnumValue("HomeFax")
    HOME_FAX("HomeFax"),
    @XmlEnumValue("HomePhone")
    HOME_PHONE("HomePhone"),
    @XmlEnumValue("HomePhone2")
    HOME_PHONE_2("HomePhone2"),
    @XmlEnumValue("Isdn")
    ISDN("Isdn"),
    @XmlEnumValue("MobilePhone")
    MOBILE_PHONE("MobilePhone"),
    @XmlEnumValue("OtherFax")
    OTHER_FAX("OtherFax"),
    @XmlEnumValue("OtherTelephone")
    OTHER_TELEPHONE("OtherTelephone"),
    @XmlEnumValue("Pager")
    PAGER("Pager"),
    @XmlEnumValue("PrimaryPhone")
    PRIMARY_PHONE("PrimaryPhone"),
    @XmlEnumValue("RadioPhone")
    RADIO_PHONE("RadioPhone"),
    @XmlEnumValue("Telex")
    TELEX("Telex"),
    @XmlEnumValue("TtyTddPhone")
    TTY_TDD_PHONE("TtyTddPhone");
    private final String value;

    PhoneNumberKeyType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PhoneNumberKeyType fromValue(String v) {
        for (PhoneNumberKeyType c: PhoneNumberKeyType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

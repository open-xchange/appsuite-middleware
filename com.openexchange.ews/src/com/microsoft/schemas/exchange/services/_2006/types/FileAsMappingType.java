
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FileAsMappingType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="FileAsMappingType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="None"/>
 *     &lt;enumeration value="LastCommaFirst"/>
 *     &lt;enumeration value="FirstSpaceLast"/>
 *     &lt;enumeration value="Company"/>
 *     &lt;enumeration value="LastCommaFirstCompany"/>
 *     &lt;enumeration value="CompanyLastFirst"/>
 *     &lt;enumeration value="LastFirst"/>
 *     &lt;enumeration value="LastFirstCompany"/>
 *     &lt;enumeration value="CompanyLastCommaFirst"/>
 *     &lt;enumeration value="LastFirstSuffix"/>
 *     &lt;enumeration value="LastSpaceFirstCompany"/>
 *     &lt;enumeration value="CompanyLastSpaceFirst"/>
 *     &lt;enumeration value="LastSpaceFirst"/>
 *     &lt;enumeration value="DisplayName"/>
 *     &lt;enumeration value="FirstName"/>
 *     &lt;enumeration value="LastFirstMiddleSuffix"/>
 *     &lt;enumeration value="LastName"/>
 *     &lt;enumeration value="Empty"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "FileAsMappingType")
@XmlEnum
public enum FileAsMappingType {

    @XmlEnumValue("None")
    NONE("None"),
    @XmlEnumValue("LastCommaFirst")
    LAST_COMMA_FIRST("LastCommaFirst"),
    @XmlEnumValue("FirstSpaceLast")
    FIRST_SPACE_LAST("FirstSpaceLast"),
    @XmlEnumValue("Company")
    COMPANY("Company"),
    @XmlEnumValue("LastCommaFirstCompany")
    LAST_COMMA_FIRST_COMPANY("LastCommaFirstCompany"),
    @XmlEnumValue("CompanyLastFirst")
    COMPANY_LAST_FIRST("CompanyLastFirst"),
    @XmlEnumValue("LastFirst")
    LAST_FIRST("LastFirst"),
    @XmlEnumValue("LastFirstCompany")
    LAST_FIRST_COMPANY("LastFirstCompany"),
    @XmlEnumValue("CompanyLastCommaFirst")
    COMPANY_LAST_COMMA_FIRST("CompanyLastCommaFirst"),
    @XmlEnumValue("LastFirstSuffix")
    LAST_FIRST_SUFFIX("LastFirstSuffix"),
    @XmlEnumValue("LastSpaceFirstCompany")
    LAST_SPACE_FIRST_COMPANY("LastSpaceFirstCompany"),
    @XmlEnumValue("CompanyLastSpaceFirst")
    COMPANY_LAST_SPACE_FIRST("CompanyLastSpaceFirst"),
    @XmlEnumValue("LastSpaceFirst")
    LAST_SPACE_FIRST("LastSpaceFirst"),
    @XmlEnumValue("DisplayName")
    DISPLAY_NAME("DisplayName"),
    @XmlEnumValue("FirstName")
    FIRST_NAME("FirstName"),
    @XmlEnumValue("LastFirstMiddleSuffix")
    LAST_FIRST_MIDDLE_SUFFIX("LastFirstMiddleSuffix"),
    @XmlEnumValue("LastName")
    LAST_NAME("LastName"),
    @XmlEnumValue("Empty")
    EMPTY("Empty");
    private final String value;

    FileAsMappingType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FileAsMappingType fromValue(String v) {
        for (FileAsMappingType c: FileAsMappingType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

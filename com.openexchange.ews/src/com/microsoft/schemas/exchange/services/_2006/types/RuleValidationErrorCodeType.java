
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RuleValidationErrorCodeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="RuleValidationErrorCodeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ADOperationFailure"/>
 *     &lt;enumeration value="ConnectedAccountNotFound"/>
 *     &lt;enumeration value="CreateWithRuleId"/>
 *     &lt;enumeration value="EmptyValueFound"/>
 *     &lt;enumeration value="DuplicatedPriority"/>
 *     &lt;enumeration value="DuplicatedOperationOnTheSameRule"/>
 *     &lt;enumeration value="FolderDoesNotExist"/>
 *     &lt;enumeration value="InvalidAddress"/>
 *     &lt;enumeration value="InvalidDateRange"/>
 *     &lt;enumeration value="InvalidFolderId"/>
 *     &lt;enumeration value="InvalidSizeRange"/>
 *     &lt;enumeration value="InvalidValue"/>
 *     &lt;enumeration value="MessageClassificationNotFound"/>
 *     &lt;enumeration value="MissingAction"/>
 *     &lt;enumeration value="MissingParameter"/>
 *     &lt;enumeration value="MissingRangeValue"/>
 *     &lt;enumeration value="NotSettable"/>
 *     &lt;enumeration value="RecipientDoesNotExist"/>
 *     &lt;enumeration value="RuleNotFound"/>
 *     &lt;enumeration value="SizeLessThanZero"/>
 *     &lt;enumeration value="StringValueTooBig"/>
 *     &lt;enumeration value="UnsupportedAddress"/>
 *     &lt;enumeration value="UnexpectedError"/>
 *     &lt;enumeration value="UnsupportedRule"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "RuleValidationErrorCodeType")
@XmlEnum
public enum RuleValidationErrorCodeType {

    @XmlEnumValue("ADOperationFailure")
    AD_OPERATION_FAILURE("ADOperationFailure"),
    @XmlEnumValue("ConnectedAccountNotFound")
    CONNECTED_ACCOUNT_NOT_FOUND("ConnectedAccountNotFound"),
    @XmlEnumValue("CreateWithRuleId")
    CREATE_WITH_RULE_ID("CreateWithRuleId"),
    @XmlEnumValue("EmptyValueFound")
    EMPTY_VALUE_FOUND("EmptyValueFound"),
    @XmlEnumValue("DuplicatedPriority")
    DUPLICATED_PRIORITY("DuplicatedPriority"),
    @XmlEnumValue("DuplicatedOperationOnTheSameRule")
    DUPLICATED_OPERATION_ON_THE_SAME_RULE("DuplicatedOperationOnTheSameRule"),
    @XmlEnumValue("FolderDoesNotExist")
    FOLDER_DOES_NOT_EXIST("FolderDoesNotExist"),
    @XmlEnumValue("InvalidAddress")
    INVALID_ADDRESS("InvalidAddress"),
    @XmlEnumValue("InvalidDateRange")
    INVALID_DATE_RANGE("InvalidDateRange"),
    @XmlEnumValue("InvalidFolderId")
    INVALID_FOLDER_ID("InvalidFolderId"),
    @XmlEnumValue("InvalidSizeRange")
    INVALID_SIZE_RANGE("InvalidSizeRange"),
    @XmlEnumValue("InvalidValue")
    INVALID_VALUE("InvalidValue"),
    @XmlEnumValue("MessageClassificationNotFound")
    MESSAGE_CLASSIFICATION_NOT_FOUND("MessageClassificationNotFound"),
    @XmlEnumValue("MissingAction")
    MISSING_ACTION("MissingAction"),
    @XmlEnumValue("MissingParameter")
    MISSING_PARAMETER("MissingParameter"),
    @XmlEnumValue("MissingRangeValue")
    MISSING_RANGE_VALUE("MissingRangeValue"),
    @XmlEnumValue("NotSettable")
    NOT_SETTABLE("NotSettable"),
    @XmlEnumValue("RecipientDoesNotExist")
    RECIPIENT_DOES_NOT_EXIST("RecipientDoesNotExist"),
    @XmlEnumValue("RuleNotFound")
    RULE_NOT_FOUND("RuleNotFound"),
    @XmlEnumValue("SizeLessThanZero")
    SIZE_LESS_THAN_ZERO("SizeLessThanZero"),
    @XmlEnumValue("StringValueTooBig")
    STRING_VALUE_TOO_BIG("StringValueTooBig"),
    @XmlEnumValue("UnsupportedAddress")
    UNSUPPORTED_ADDRESS("UnsupportedAddress"),
    @XmlEnumValue("UnexpectedError")
    UNEXPECTED_ERROR("UnexpectedError"),
    @XmlEnumValue("UnsupportedRule")
    UNSUPPORTED_RULE("UnsupportedRule");
    private final String value;

    RuleValidationErrorCodeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RuleValidationErrorCodeType fromValue(String v) {
        for (RuleValidationErrorCodeType c: RuleValidationErrorCodeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

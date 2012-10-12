
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for InvalidRecipientResponseCodeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="InvalidRecipientResponseCodeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="OtherError"/>
 *     &lt;enumeration value="RecipientOrganizationNotFederated"/>
 *     &lt;enumeration value="CannotObtainTokenFromSTS"/>
 *     &lt;enumeration value="SystemPolicyBlocksSharingWithThisRecipient"/>
 *     &lt;enumeration value="RecipientOrganizationFederatedWithUnknownTokenIssuer"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "InvalidRecipientResponseCodeType")
@XmlEnum
public enum InvalidRecipientResponseCodeType {

    @XmlEnumValue("OtherError")
    OTHER_ERROR("OtherError"),
    @XmlEnumValue("RecipientOrganizationNotFederated")
    RECIPIENT_ORGANIZATION_NOT_FEDERATED("RecipientOrganizationNotFederated"),
    @XmlEnumValue("CannotObtainTokenFromSTS")
    CANNOT_OBTAIN_TOKEN_FROM_STS("CannotObtainTokenFromSTS"),
    @XmlEnumValue("SystemPolicyBlocksSharingWithThisRecipient")
    SYSTEM_POLICY_BLOCKS_SHARING_WITH_THIS_RECIPIENT("SystemPolicyBlocksSharingWithThisRecipient"),
    @XmlEnumValue("RecipientOrganizationFederatedWithUnknownTokenIssuer")
    RECIPIENT_ORGANIZATION_FEDERATED_WITH_UNKNOWN_TOKEN_ISSUER("RecipientOrganizationFederatedWithUnknownTokenIssuer");
    private final String value;

    InvalidRecipientResponseCodeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static InvalidRecipientResponseCodeType fromValue(String v) {
        for (InvalidRecipientResponseCodeType c: InvalidRecipientResponseCodeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}

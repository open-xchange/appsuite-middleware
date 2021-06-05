/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.contact.vcard.impl.mapping;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.openexchange.contact.vcard.DistributionListMode;
import com.openexchange.contact.vcard.VCardParameters;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.java.Strings;
import ezvcard.VCard;
import ezvcard.parameter.EmailType;
import ezvcard.property.Email;
import ezvcard.property.Kind;
import ezvcard.property.Member;
import ezvcard.property.RawProperty;

/**
 * {@link DistributionlistMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DistributionlistMapping extends AbstractMapping {

    private static final String KIND_PROPERTY_VALUE_GROUP = "group";
    private static final String PROPERTY_X_ADDRESSBOOKSERVER_MEMBER = "X-ADDRESSBOOKSERVER-MEMBER";
    private static final String PROPERTY_X_ADDRESSBOOKSERVER_KIND = "X-ADDRESSBOOKSERVER-KIND";
    private static final String PROPERTY_MEMBER = "MEMBER";
    private static final String PROPERTY_KIND = "KIND";
    private static final String X_OX_REF = "X-OX-REF";
    private static final String X_OX_FN = "X-OX-FN";

    private static final String URN_UUID_PREFIX = "urn:uuid:";
    private static final String MAILTO_PREFIX = "mailto:";

    /**
     * Initializes a new {@link DistributionlistMapping}.
     */
    public DistributionlistMapping() {
        super(new String[] { PROPERTY_KIND, PROPERTY_MEMBER, PROPERTY_X_ADDRESSBOOKSERVER_KIND, PROPERTY_X_ADDRESSBOOKSERVER_MEMBER }, new ContactField[] { ContactField.DISTRIBUTIONLIST, ContactField.MARK_AS_DISTRIBUTIONLIST, ContactField.NUMBER_OF_DISTRIBUTIONLIST });
    }

    @Override
    public void exportContact(Contact contact, VCard vCard, VCardParameters parameters, List<OXException> warnings) {
        /*
         * clear legacy distribution list remnants during export
         */
        if (isLegacyDistributionList(vCard)) {
            List<Email> memberEmails = getPropertiesWithTypes(vCard.getEmails(), EmailType.INTERNET.getValue());
            if (null != memberEmails && 0 < memberEmails.size()) {
                for (Email memberEmail : memberEmails) {
                    vCard.removeProperty(memberEmail);
                }
            }
        }
        vCard.removeExtendedProperty("X-OPEN-XCHANGE-CTYPE");
        Kind existingKind = vCard.getKind();
        List<Member> existingMembers = vCard.getMembers();
        RawProperty existingAddressbookserverKind = vCard.getExtendedProperty(PROPERTY_X_ADDRESSBOOKSERVER_KIND);
        List<RawProperty> existingAddressbookserverMembers = vCard.getExtendedProperties(PROPERTY_X_ADDRESSBOOKSERVER_KIND);
        if (contact.getMarkAsDistribtuionlist()) {
            /*
             * apply "group" kind and take over members
             */
            vCard.removeProperties(Member.class);
            vCard.removeProperties(Kind.class);
            vCard.removeExtendedProperty(PROPERTY_X_ADDRESSBOOKSERVER_MEMBER);
            vCard.removeExtendedProperty(PROPERTY_X_ADDRESSBOOKSERVER_KIND);
            if (null != parameters && DistributionListMode.ADDRESSBOOKSERVER.equals(parameters.getDistributionListMode())) {
                vCard.addProperty(new RawProperty(PROPERTY_X_ADDRESSBOOKSERVER_KIND, KIND_PROPERTY_VALUE_GROUP));
                List<RawProperty> members = exportSyncMembers(contact.getDistributionList());
                for (RawProperty member : members) {
                    if (member != null) {
                        vCard.addProperty(member);
                    }
                }
            } else {
                vCard.setKind(Kind.group());
                List<Member> members = exportMembers(contact.getDistributionList(), warnings);
                for (Member member : members) {
                    vCard.addMember(member);
                }
            }
        } else {
            /*
             * not/no longer a distribution list, remove previous kind and member properties
             */
            if (null != existingKind) {
                vCard.removeProperty(existingKind);
            }
            if (null != existingMembers && 0 < existingMembers.size()) {
                vCard.removeProperties(Member.class);
            }
            if (null != existingAddressbookserverKind) {
                vCard.removeProperty(existingAddressbookserverKind);
            }
            if (null != existingAddressbookserverMembers && 0 < existingAddressbookserverMembers.size()) {
                for (RawProperty property : existingAddressbookserverMembers) {
                    vCard.removeProperty(property);
                }
            }
        }
    }

    private List<RawProperty> exportSyncMembers(DistributionListEntryObject[] entries) {
        if (null == entries) {
            return Collections.emptyList();
        }
        List<RawProperty> members = new ArrayList<RawProperty>(entries.length);
        for (DistributionListEntryObject entry : entries) {
            RawProperty member = exportSyncMember(entry);
            if (null != member) {
                members.add(member);
            }
        }
        return members;
    }

    private RawProperty exportSyncMember(DistributionListEntryObject distListEntryObject) {
        RawProperty member;
        if (Strings.isNotEmpty(distListEntryObject.getContactUid())) {
            member = new RawProperty(PROPERTY_X_ADDRESSBOOKSERVER_MEMBER, URN_UUID_PREFIX + distListEntryObject.getContactUid());
        } else {
            String emailaddress = distListEntryObject.getEmailaddress();
            if (Strings.isEmpty(emailaddress)) {
                return null; // remove one-off not having mail from sync
            }
            member = new RawProperty(PROPERTY_X_ADDRESSBOOKSERVER_MEMBER, MAILTO_PREFIX + emailaddress);
        }
        String displayname = distListEntryObject.getDisplayname();
        if (Strings.isNotEmpty(displayname)) {
            member.addParameter("X-CN", displayname);
        }
        String emailaddress = distListEntryObject.getEmailaddress();
        if (Strings.isNotEmpty(emailaddress)) {
            member.addParameter("X-EMAIL", distListEntryObject.getEmailaddress());
        }
        return member;
    }

    private static List<Member> exportMembers(DistributionListEntryObject[] entries, List<OXException> warnings) {
        if (null == entries) {
            return Collections.emptyList();
        }
        List<Member> members = new ArrayList<Member>(entries.length);
        for (DistributionListEntryObject entry : entries) {
            Member member = exportMember(entry, warnings);
            if (null != member) {
                members.add(member);
            }
        }
        return members;
    }

    private static Member exportMember(DistributionListEntryObject entry, List<OXException> warnings) {
        String email = entry.getEmailaddress();
        if (null != email) {
            String uriString = null;
            try {
                uriString = new URI("mailto", email, null).toString();
            } catch (URISyntaxException e) {
                addConversionWarning(warnings, e, PROPERTY_MEMBER, e.getMessage());
                uriString = email;
            }
            Member member = new Member(uriString);
            if (Strings.isNotEmpty(entry.getDisplayname())) {
                member.addParameter(X_OX_FN, entry.getDisplayname());
            }
            if (DistributionListEntryObject.INDEPENDENT != entry.getEmailfield()) {
                // TODO: encode context id, contact id & email field in X_OX_REF (or better to avoid wrong references when importing vcard from other server?)
            }
            return member;
        }
        return null;
    }

    @Override
    public void importVCard(VCard vCard, Contact contact, VCardParameters parameters, List<OXException> warnings) {
        if (isLegacyDistributionList(vCard)) {
            /*
             * import legacy distribution list members
             */
            List<Email> memberEmails = getPropertiesWithTypes(vCard.getEmails(), EmailType.INTERNET.getValue());
            contact.setDistributionList(importLegacyMembers(memberEmails, warnings));
            contact.setMarkAsDistributionlist(true);
        } else if (isAppleGroup(vCard)) {
            contact.setDistributionList(importAppleMembers(vCard.getExtendedProperties(PROPERTY_X_ADDRESSBOOKSERVER_MEMBER), warnings));
            contact.setMarkAsDistributionlist(true);
        } else if (null != vCard.getKind() && vCard.getKind().isGroup()) {
            /*
             * apply distribution list flag and import members
             */
            contact.setDistributionList(importMembers(vCard.getMembers(), warnings));
            contact.setMarkAsDistributionlist(true);
        } else {
            /*
             * not/no longer a distribution list, remove distribution list entries and flag
             */
            contact.setDistributionList(null);
            contact.setMarkAsDistributionlist(false);
        }
    }

    private DistributionListEntryObject[] importAppleMembers(List<RawProperty> list, List<OXException> warnings) {
        if (null != list && 0 < list.size()) {
            List<DistributionListEntryObject> entries = new ArrayList<DistributionListEntryObject>(list.size());
            for (RawProperty property : list) {
                try {
                    DistributionListEntryObject entry = toDistributionListEntryObject(property);
                    if (entry != null) {
                        entries.add(entry);
                    }
                } catch (OXException e) {
                    addConversionWarning(warnings, e, PROPERTY_X_ADDRESSBOOKSERVER_MEMBER, e.getMessage());
                    continue;
                }
            }
            return 0 < entries.size() ? entries.toArray(new DistributionListEntryObject[entries.size()]) : null;
        }
        return null;
    }

    private DistributionListEntryObject[] importMembers(List<Member> members, List<OXException> warnings) {
        if (null != members && 0 < members.size()) {
            List<DistributionListEntryObject> entries = new ArrayList<DistributionListEntryObject>(members.size());
            for (Member member : members) {
                DistributionListEntryObject entry = importMember(member, warnings);
                if (null != entry) {
                    entries.add(entry);
                }
            }
            return 0 < entries.size() ? entries.toArray(new DistributionListEntryObject[entries.size()]) : null;
        }
        return null;
    }

    private DistributionListEntryObject importMember(Member member, List<OXException> warnings) {
        String email = extractEMailAddress(member.getUri(), warnings);
        if (null != email) {
            DistributionListEntryObject entry = new DistributionListEntryObject();
            entry.setDisplayname(member.getParameter(X_OX_FN));
            try {
                entry.setEmailaddress(email);
            } catch (OXException e) {
                addConversionWarning(warnings, e, PROPERTY_MEMBER, e.getMessage());
                return null;
            }
            String oxReference = member.getParameter(X_OX_REF);
            if (Strings.isNotEmpty(oxReference)) {
                // TODO: decode context id, contact id & email field
            } else {
                entry.setEmailfield(DistributionListEntryObject.INDEPENDENT);
            }
            return entry;
        }
        return null;
    }

    private DistributionListEntryObject[] importLegacyMembers(List<Email> members, List<OXException> warnings) {
        if (null != members && 0 < members.size()) {
            List<DistributionListEntryObject> entries = new ArrayList<DistributionListEntryObject>(members.size());
            for (Email member : members) {
                DistributionListEntryObject entry = importLegacyMember(member, warnings);
                if (null != entry) {
                    entries.add(entry);
                }
            }
            return 0 < entries.size() ? entries.toArray(new DistributionListEntryObject[entries.size()]) : null;
        }
        return null;
    }

    private DistributionListEntryObject importLegacyMember(Email member, List<OXException> warnings) {
        String email = member.getValue();
        if (null != email) {
            DistributionListEntryObject entry = new DistributionListEntryObject();
            try {
                entry.setEmailaddress(email);
            } catch (OXException e) {
                addConversionWarning(warnings, e, PROPERTY_MEMBER, e.getMessage());
                return null;
            }
            return entry;
        }
        return null;
    }

    private String extractEMailAddress(String uriString, List<OXException> warnings) {
        if (Strings.isEmpty(uriString)) {
            return null;
        }
        String email = null;
        try {
            URI uri = new URI(uriString);
            if (null == uri.getScheme() || "mailto".equalsIgnoreCase(uri.getScheme())) {
                email = uri.getSchemeSpecificPart();
            }
        } catch (URISyntaxException e) {
            addConversionWarning(warnings, e, PROPERTY_MEMBER, e.getMessage());
        }
        if (null != email) {
            try {
                @SuppressWarnings("unused") InternetAddress parsedAddress = new InternetAddress(email);
                return email;
            } catch (AddressException e) {
                addConversionWarning(warnings, e, PROPERTY_MEMBER, e.getMessage());
            }
        }
        return null;
    }

    /**
     * Gets a value indicating whether the vCard represents an Apple-style group.
     *
     * @param vCard The vCard to check
     * @return <code>true</code> if the vCard represents an Apple-style group, <code>false</code>, otherwise
     */
    private boolean isAppleGroup(VCard vCard) {
        RawProperty property = vCard.getExtendedProperty(PROPERTY_X_ADDRESSBOOKSERVER_KIND);
        return null != property && KIND_PROPERTY_VALUE_GROUP.equalsIgnoreCase(property.getValue());
    }

    /**
     * Helper method to construct a new {@link DistributionListEntryObject} based on the provided {@link RawProperty}.
     *
     * @param member containing source information for the new {@link DistributionListEntryObject}
     * @return A new {@link RawProperty} representing the provided {@link RawProperty} or <code>null</code> if null is provided
     * @throws OXException
     */
    private DistributionListEntryObject toDistributionListEntryObject(RawProperty member) throws OXException {
        if (member == null) {
            return null;
        }
        DistributionListEntryObject entry = new DistributionListEntryObject();
        String commonName = member.getParameter("CN");
        String extCommonName = member.getParameter("X-CN");
        if (Strings.isNotEmpty(commonName)) {
            entry.setDisplayname(commonName);
        } else if (Strings.isNotEmpty(extCommonName)) {
            entry.setDisplayname(extCommonName);
        } else if (hasMailTo(member)) {
            entry.setDisplayname(getMailTo(member));
        }
        if (hasContactUid(member)) {
            entry.setContactUid(getContactUid(member));
            String email = member.getParameter("EMAIL");
            String extEmail = member.getParameter("X-EMAIL");
            if (Strings.isNotEmpty(email)) {
                entry.setEmailaddress(email);
            } else if (Strings.isNotEmpty(extEmail)) {
                entry.setEmailaddress(extEmail);
            }
        } else if (hasMailTo(member)) {
            entry.setEmailaddress(getMailTo(member));
            entry.setEmailfield(DistributionListEntryObject.INDEPENDENT);
        }
        return entry;
    }

    /**
     * Returns if the value is a reference to a known user
     *
     * @return <code>true</code> if it is a reference; otherwise <code>false</code>
     */
    private boolean hasContactUid(RawProperty property) {
        return property.getValue() != null && property.getValue().startsWith(URN_UUID_PREFIX);
    }

    /**
     * Returns the plain user reference (without leading 'urn:uuid:' prefix)
     *
     * @return The user id
     */
    private String getContactUid(RawProperty property) {
        String lValue = property.getValue();
        return lValue.substring(URN_UUID_PREFIX.length());
    }

    /**
     * Returns if the value is an external mail address
     *
     * @return <code>true</code> if it is a mailto address; otherwise <code>false</code>
     */
    private boolean hasMailTo(RawProperty property) {
        return property.getValue() != null && property.getValue().startsWith(MAILTO_PREFIX);
    }

    private String getMailTo(RawProperty property) {
        String lValue = property.getValue();
        return lValue.substring(MAILTO_PREFIX.length());
    }
}

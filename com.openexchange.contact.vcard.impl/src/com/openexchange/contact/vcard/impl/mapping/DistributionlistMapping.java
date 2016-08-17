/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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

    private static final String X_OX_REF = "X-OX-REF";
    private static final String X_OX_FN = "X-OX-FN";

    /**
     * Initializes a new {@link DistributionlistMapping}.
     */
    public DistributionlistMapping() {
        super(new String[] { "KIND", "MEMBER" },
            new ContactField[] { ContactField.DISTRIBUTIONLIST, ContactField.MARK_AS_DISTRIBUTIONLIST, ContactField.NUMBER_OF_DISTRIBUTIONLIST });
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
        if (contact.getMarkAsDistribtuionlist()) {
            /*
             * apply "group" kind and take over members
             */
            vCard.setKind(Kind.group());
            vCard.removeProperties(Member.class);
            List<Member> members = exportMembers(contact.getDistributionList(), parameters, warnings);
            for (Member member : members) {
                vCard.addMember(member);
            }
        } else {
            /*
             * not/no longer a distribution list, remove previous kind and member properties
             */
            if (null != existingKind && existingKind.isGroup()) {
                vCard.removeProperty(existingKind);
            }
            if (null != existingMembers && 0 < existingMembers.size()) {
                vCard.removeProperties(Member.class);
            }
        }
    }

    @Override
    public void importVCard(VCard vCard, Contact contact, VCardParameters parameters, List<OXException> warnings) {
        if (isLegacyDistributionList(vCard)) {
            /*
             * import legacy distribution list members
             */
            contact.setMarkAsDistributionlist(true);
            List<Email> memberEmails = getPropertiesWithTypes(vCard.getEmails(), EmailType.INTERNET.getValue());
            contact.setDistributionList(importLegacyMembers(memberEmails, parameters, warnings));
        } else if (isAppleGroup(vCard)) {
            contact.setMarkAsDistributionlist(true);
        } else if (null != vCard.getKind() && vCard.getKind().isGroup()) {
            /*
             * apply distribution list flag and import members
             */
            contact.setMarkAsDistributionlist(true);
            contact.setDistributionList(importMembers(vCard.getMembers(), parameters, warnings));
        } else {
            /*
             * not/no longer a distribution list, remove distribution list entries and flag
             */
            contact.setDistributionList(null);
            contact.setMarkAsDistributionlist(false);
        }
    }

    private static DistributionListEntryObject[] importMembers(List<Member> members, VCardParameters parameters, List<OXException> warnings) {
        if (null != members && 0 < members.size()) {
            List<DistributionListEntryObject> entries = new ArrayList<DistributionListEntryObject>(members.size());
            for (Member member : members) {
                DistributionListEntryObject entry = importMember(member, parameters, warnings);
                if (null != entry) {
                    entries.add(entry);
                }
            }
            return 0 < entries.size() ? entries.toArray(new DistributionListEntryObject[entries.size()]) : null;
        }
        return null;
    }

    private static DistributionListEntryObject importMember(Member member, VCardParameters parameters, List<OXException> warnings) {
        String email = extractEMailAddress(member.getUri(), parameters, warnings);
        if (null != email) {
            DistributionListEntryObject entry = new DistributionListEntryObject();
            entry.setDisplayname(member.getParameter(X_OX_FN));
            try {
                entry.setEmailaddress(email);
            } catch (OXException e) {
                addConversionWarning(warnings, e, "MEMBER", e.getMessage());
                return null;
            }
            String oxReference = member.getParameter(X_OX_REF);
            if (false == Strings.isEmpty(oxReference)) {
                // TODO: decode context id, contact id & email field
            } else {
                entry.setEmailfield(DistributionListEntryObject.INDEPENDENT);
            }
            return entry;
        }
        return null;
    }

    private static DistributionListEntryObject[] importLegacyMembers(List<Email> members, VCardParameters parameters, List<OXException> warnings) {
        if (null != members && 0 < members.size()) {
            List<DistributionListEntryObject> entries = new ArrayList<DistributionListEntryObject>(members.size());
            for (Email member : members) {
                DistributionListEntryObject entry = importLegacyMember(member, parameters, warnings);
                if (null != entry) {
                    entries.add(entry);
                }
            }
            return 0 < entries.size() ? entries.toArray(new DistributionListEntryObject[entries.size()]) : null;
        }
        return null;
    }

    private static DistributionListEntryObject importLegacyMember(Email member, VCardParameters parameters, List<OXException> warnings) {
        String email = member.getValue();
        if (null != email) {
            DistributionListEntryObject entry = new DistributionListEntryObject();
            try {
                entry.setEmailaddress(email);
            } catch (OXException e) {
                addConversionWarning(warnings, e, "MEMBER", e.getMessage());
                return null;
            }
            return entry;
        }
        return null;
    }

    private static String extractEMailAddress(String uriString, VCardParameters parameters, List<OXException> warnings) {
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
            addConversionWarning(warnings, e, "MEMBER", e.getMessage());
        }
        if (null != email) {
            try {
                new InternetAddress(email);
                return email;
            } catch (AddressException e) {
                addConversionWarning(warnings, e, "MEMBER", e.getMessage());
            }
        }
        return null;
    }

    private static Member exportMember(DistributionListEntryObject entry, VCardParameters parameters, List<OXException> warnings) {
        String email = entry.getEmailaddress();
        if (null != email) {
            String uriString = null;
            try {
                uriString = new URI("mailto", email, null).toString();
            } catch (URISyntaxException e) {
                addConversionWarning(warnings, e, "MEMBER", e.getMessage());
                uriString = email;
            }
            Member member = new Member(uriString);
            if (false == Strings.isEmpty(entry.getDisplayname())) {
                member.addParameter(X_OX_FN, entry.getDisplayname());
            }
            if (DistributionListEntryObject.INDEPENDENT != entry.getEmailfield()) {
                // TODO: encode context id, contact id & email field in X_OX_REF (or better to avoid wrong references when importing vcard from other server?)
            }
            return member;
        }
        return null;
    }

    private static List<Member> exportMembers(DistributionListEntryObject[] entries, VCardParameters parameters, List<OXException> warnings) {
        if (null == entries) {
            return Collections.emptyList();
        }
        List<Member> members = new ArrayList<Member>(entries.length);
        for (DistributionListEntryObject entry : entries) {
            Member member = exportMember(entry, parameters, warnings);
            if (null != member) {
                members.add(member);
            }
        }
        return members;
    }

    /**
     * Gets a value indicating whether the vCard represents an Apple-style group.
     *
     * @param vCard The vCard to check
     * @return <code>true</code> if the vCard represents an Apple-style group, <code>false</code>, otherwise
     */
    private static boolean isAppleGroup(VCard vCard) {
        RawProperty property = vCard.getExtendedProperty("X-ADDRESSBOOKSERVER-KIND");
        return null != property && "group".equalsIgnoreCase(property.getValue());
    }

}

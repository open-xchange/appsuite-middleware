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

import java.util.List;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.openexchange.contact.vcard.VCardParameters;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Strings;
import ezvcard.VCard;
import ezvcard.parameter.EmailType;
import ezvcard.property.Email;

/**
 * {@link EMailMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class EMailMapping extends AbstractMapping {

    /**
     * Initializes a new {@link EMailMapping}.
     */
    public EMailMapping() {
        super("EMAIL", ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3);
    }

    @Override
    public void exportContact(Contact contact, VCard vCard, VCardParameters parameters, List<OXException> warnings) {
        List<Email> emails = vCard.getEmails();
        /*
         * email1 - type "WORK"
         */
        Email businessEmail = getEmail(vCard, emails, EmailType.WORK.getValue(), null, 0, false);
        boolean hasWork = hasWork(emails, businessEmail);
        if (has(contact, Contact.EMAIL1)) {
            if (null == businessEmail) {
                if (hasWork) { // Only add PREF type if ambiguous
                    vCard.addEmail(contact.getEmail1(), EmailType.WORK, EmailType.PREF);
                } else {
                    vCard.addEmail(contact.getEmail1(), EmailType.WORK);
                }
            } else {
                businessEmail.setValue(contact.getEmail1());
                if (hasWork) { // Only add PREF type if ambiguous
                    addTypesIfMissing(businessEmail, EmailType.WORK.getValue(), EmailType.PREF.getValue());
                } else {
                    addTypesIfMissing(businessEmail, EmailType.WORK.getValue());
                }
            }
        } else if (null != businessEmail) {
            vCard.removeProperty(businessEmail);
        }
        /*
         * email2 - type "HOME"
         */
        Email homeEmail = getEmail(vCard, emails, EmailType.HOME.getValue(), null, 1, false);
        if (has(contact, Contact.EMAIL2)) {
            if (null == homeEmail) {
                vCard.addEmail(contact.getEmail2(), EmailType.HOME);
            } else {
                homeEmail.setValue(contact.getEmail2());
                addTypeIfMissing(homeEmail, EmailType.HOME.getValue());
            }
        } else if (null != homeEmail) {
            vCard.removeProperty(homeEmail);
        }
        /*
         * email3 - type "X-OTHER", or no specific type
         */
        Email otherEmail = getEmail(vCard, emails, TYPE_OTHER, ABLABEL_OTHER, 2, true);
        if (has(contact, Contact.EMAIL3)) {
            if (null == otherEmail) {
                otherEmail = new Email(contact.getEmail3());
                otherEmail.addParameter(ezvcard.parameter.VCardParameters.TYPE, TYPE_OTHER);
                vCard.addEmail(otherEmail);
            } else {
                otherEmail.setValue(contact.getEmail3());
                addTypeIfMissing(otherEmail, TYPE_OTHER);
            }
        } else if (null != otherEmail) {
            vCard.removeProperty(otherEmail);
        }
        /*
         * telex - type "TLX"
         */
        Email telexEmail = getPropertyWithTypes(emails, EmailType.TLX);
        if (contact.containsTelephoneTelex()) {
            if (null == telexEmail) {
                vCard.addEmail(contact.getTelephoneTelex(), EmailType.TLX);
            } else {
                telexEmail.setValue(contact.getTelephoneTelex());
            }
        } else if (null != telexEmail) {
            vCard.removeProperty(telexEmail);
        }
    }

    /**
     * Checks, if the given List of Emails contains an email with the PREF type.
     * Ignoring the given business Email as this will be added anyway.
     *
     * @param emails
     * @param businessEmail
     * @return
     */
    private boolean hasWork(List<Email> emails, Email businessEmail) {
        for (Email email : emails) {
            if (email.equals(businessEmail)) {
                continue;
            }
            for (EmailType type : email.getTypes()) {
                if (type.equals(EmailType.WORK)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void importVCard(VCard vCard, Contact contact, VCardParameters parameters, List<OXException> warnings) {
        /*
         * skip import for legacy distribution list vCards
         */
        if (isLegacyDistributionList(vCard)) {
            return;
        }
        List<Email> emails = vCard.getEmails();
        /*
         * email1 - type "WORK"
         */
        contact.setEmail1(parseEMail(getEmail(vCard, emails, EmailType.WORK.getValue(), null, 0, false), parameters, warnings));
        /*
         * email2 - type "HOME"
         */
        contact.setEmail2(parseEMail(getEmail(vCard, emails, EmailType.HOME.getValue(), null, 1, false), parameters, warnings));
        /*
         * email3 - type "X-OTHER", or no specific type
         */
        contact.setEmail3(parseEMail(getEmail(vCard, emails, TYPE_OTHER, ABLABEL_OTHER, 2, true), parameters, warnings));
        /*
         * telex - type "TLX"
         */
        Email property = getPropertyWithTypes(emails, EmailType.TLX);
        contact.setTelephoneTelex(null != property ? property.getValue() : null);
    }

    private String parseEMail(Email property, VCardParameters parameters, List<OXException> warnings) {
        if (null != property) {
            String value = property.getValue();
            if (Strings.isNotEmpty(value)) {
                if (null == parameters || false == parameters.isValidateContactEMail()) {
                    return value;
                }
                try {
                    new InternetAddress(value).validate();
                    return value;
                } catch (AddressException e) {
                    addConversionWarning(warnings, e, "EMAIL", e.getMessage());
                }
            }
        }
        return null;
    }

    /**
     * Chooses a specific e-mail address from a list of candidates matching either a distinguishing type, or, if the candidates are not
     * using any distinguishing e-mail types at all, the n-th e-mail property as fallback, or, if there are candidates with unknown
     * or no distinguishing types, the first of those e-mail properties as fallback.
     *
     * @param vCard The vCard
     * @param emails The possible e-mail properties to choose from
     * @param distinguishingType The distinguishing type
     * @param abLabel The distinguishing <code>X-ABLabel</code> property, or <code>null</code> if not used
     * @param fallbackIndex The index in the candidate list to use when selecting the fallback property, or <code>-1</code> to use no fallback
     * @param fallbackToUnknownType <code>true</code> to use the first e-mail with unknown distinguishing type as fallback, <code>false</code>, otherwise
     * @return The matching e-mail property, or <code>null</code> if none was found
     */
    private Email getEmail(VCard vCard, List<Email> emails, String distinguishingType, String abLabel, int fallbackIndex, boolean fallbackToUnknownType) {
        if (null == emails || 0 == emails.size()) {
            return null;
        }
        /*
         * prefer the most preferred property matching the type
         */
        Email email = getPropertyWithTypes(emails, distinguishingType);
        if (null == email && null != abLabel) {
            /*
             * fallback to an item associated with a matching X-ABLabel
             */
            email = getPropertyWithABLabel(vCard, emails, abLabel);
        }
        if (null == email && 0 <= fallbackIndex) {
            /*
             * if no distinguishing e-mail types defined, use the first address as fallback
             */
            List<Email> simpleEmails = getPropertiesWithoutTypes(emails,
                EmailType.WORK.getValue(), EmailType.HOME.getValue(), TYPE_OTHER, EmailType.TLX.getValue());
            if (fallbackIndex < simpleEmails.size() && simpleEmails.size() == emails.size()) {
                sort(simpleEmails);
                email = simpleEmails.get(fallbackIndex);
            }
        }
        if (null == email && fallbackToUnknownType) {
            /*
             * if no distinguishing e-mail type found, use first non-distinguishing address as fallback, in case there are other distinguishing ones
             */
            List<Email> simpleEmails = getPropertiesWithoutTypes(emails,
                EmailType.WORK.getValue(), EmailType.HOME.getValue(), TYPE_OTHER, EmailType.TLX.getValue());
            if (0 < simpleEmails.size() && simpleEmails.size() != emails.size()) {
                sort(simpleEmails);
                email = simpleEmails.get(0);
            }
        }
        return email;
    }

}

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
        Email businessEmail = getEmail(vCard, emails, EmailType.WORK.getValue(), null, 0);
        if (has(contact, Contact.EMAIL1)) {
            if (null == businessEmail) {
                vCard.addEmail(contact.getEmail1(), EmailType.WORK, EmailType.PREF);
            } else {
                businessEmail.setValue(contact.getEmail1());
                addTypesIfMissing(businessEmail, EmailType.WORK.getValue(), EmailType.PREF.getValue());
            }
        } else if (null != businessEmail) {
            vCard.removeProperty(businessEmail);
        }
        /*
         * email2 - type "HOME"
         */
        Email homeEmail = getEmail(vCard, emails, EmailType.HOME.getValue(), null, 1);
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
        Email otherEmail = getEmail(vCard, emails, TYPE_OTHER, ABLABEL_OTHER, 2);
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
        contact.setEmail1(parseEMail(getEmail(vCard, emails, EmailType.WORK.getValue(), null, 0), parameters, warnings));
        /*
         * email2 - type "HOME"
         */
        contact.setEmail2(parseEMail(getEmail(vCard, emails, EmailType.HOME.getValue(), null, 1), parameters, warnings));
        /*
         * email3 - type "X-OTHER", or no specific type
         */
        contact.setEmail3(parseEMail(getEmail(vCard, emails, TYPE_OTHER, ABLABEL_OTHER, 2), parameters, warnings));
        /*
         * telex - type "TLX"
         */
        Email property = getPropertyWithTypes(emails, EmailType.TLX);
        contact.setTelephoneTelex(null != property ? property.getValue() : null);
    }

    private String parseEMail(Email property, VCardParameters parameters, List<OXException> warnings) {
        if (null != property) {
            String value = property.getValue();
            if (false == Strings.isEmpty(value)) {
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
     * using any distinguishing e-mail types at all, the n-th e-mail property as fallback.
     *
     * @param vCard The vCard
     * @param emails The possible e-mail properties to choose from
     * @param distinguishingType The distinguishing type
     * @param abLabel The distinguishing <code>X-ABLabel</code> property, or <code>null</code> if not used
     * @param fallbackIndex The index in the candidate list to use when selecting the fallback property, or <code>-1</code> to use no fallback
     * @return The matching e-mail property, or <code>null</code> if none was found
     */
    private Email getEmail(VCard vCard, List<Email> emails, String distinguishingType, String abLabel, int fallbackIndex) {
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
        return email;
    }

}

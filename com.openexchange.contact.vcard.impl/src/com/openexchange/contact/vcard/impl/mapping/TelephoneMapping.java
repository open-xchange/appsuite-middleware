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

import java.util.Collection;
import java.util.List;
import com.openexchange.contact.vcard.VCardParameters;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Strings;
import ezvcard.VCard;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Telephone;

/**
 * {@link TelephoneMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class TelephoneMapping extends AbstractMapping {

    static final String TYPE_CALLBACK = "x-callback";
    static final String TYPE_COMPANY = "x-company";
    static final String TYPE_ASSISTENT = "x-assistant";
    static final String TYPE_IP = "x-ip";
    static final String TYPE_RADIO = "x-radio";
    static final String TYPE_PRIMARY = "x-primary";
    static final String TYPE_2ND = "x-2nd";

    /**
     * Initializes a new {@link TelephoneMapping}.
     */
    public TelephoneMapping() {
        super("TEL", ContactField.TELEPHONE_PAGER, ContactField.TELEPHONE_TTYTDD, ContactField.TELEPHONE_ISDN, ContactField.TELEPHONE_CAR,
            ContactField.CELLULAR_TELEPHONE1, ContactField.CELLULAR_TELEPHONE2, ContactField.TELEPHONE_CALLBACK,
            ContactField.TELEPHONE_COMPANY, ContactField.TELEPHONE_ASSISTANT, ContactField.TELEPHONE_IP, ContactField.TELEPHONE_RADIO,
            ContactField.TELEPHONE_PRIMARY, ContactField.FAX_BUSINESS, ContactField.FAX_BUSINESS, ContactField.FAX_OTHER,
            ContactField.TELEPHONE_BUSINESS1, ContactField.TELEPHONE_BUSINESS2, ContactField.TELEPHONE_HOME1,
            ContactField.TELEPHONE_HOME2, ContactField.TELEPHONE_OTHER);
    }

    @Override
    public void exportContact(Contact contact, VCard vCard, VCardParameters parameters, List<OXException> warnings) {
        List<Telephone> properties = vCard.getProperties(Telephone.class);
        /*
         * special
         */
        exportTelephone(vCard, properties, 0, false, null, contact, Contact.TELEPHONE_PAGER, TelephoneType.PAGER.getValue());
        exportTelephone(vCard, properties, 0, false, null, contact, Contact.TELEPHONE_TTYTDD, TelephoneType.TEXTPHONE.getValue());
        exportTelephone(vCard, properties, 0, false, null, contact, Contact.TELEPHONE_ISDN, TelephoneType.ISDN.getValue());
        exportTelephone(vCard, properties, 0, false, null, contact, Contact.TELEPHONE_CAR, TelephoneType.CAR.getValue());
        exportTelephone(vCard, properties, 0, false, new String[] { TelephoneType.PREF.getValue() }, contact, Contact.CELLULAR_TELEPHONE1, TelephoneType.CELL.getValue());
        exportTelephone(vCard, properties, 1, false, new String[] { TYPE_2ND }, contact, Contact.CELLULAR_TELEPHONE2, TelephoneType.CELL.getValue());
        exportTelephone(vCard, properties, 0, false, null, contact, Contact.TELEPHONE_CALLBACK, TYPE_CALLBACK);
        exportTelephone(vCard, properties, 0, false, null, contact, Contact.TELEPHONE_COMPANY, TYPE_COMPANY);
        exportTelephone(vCard, properties, 0, false, null, contact, Contact.TELEPHONE_ASSISTANT, TYPE_ASSISTENT);
        exportTelephone(vCard, properties, 0, false, null, contact, Contact.TELEPHONE_IP, TYPE_IP);
        exportTelephone(vCard, properties, 0, false, null, contact, Contact.TELEPHONE_RADIO, TYPE_RADIO);
        exportTelephone(vCard, properties, 0, false, null, contact, Contact.TELEPHONE_PRIMARY, TYPE_PRIMARY);
        /*
         * fax
         */
        exportTelephone(vCard, properties, 0, false, null, contact, Contact.FAX_BUSINESS, TelephoneType.FAX.getValue(), TelephoneType.WORK.getValue());
        exportTelephone(vCard, properties, 0, false, null, contact, Contact.FAX_HOME, TelephoneType.FAX.getValue(), TelephoneType.HOME.getValue());
        exportTelephone(vCard, properties, 0, false, null, contact, Contact.FAX_OTHER, TelephoneType.FAX.getValue(), TYPE_OTHER);
        /*
         * voice
         */
        exportTelephone(vCard, properties, 0, true, new String[] { TelephoneType.PREF.getValue() }, contact, Contact.TELEPHONE_BUSINESS1, TelephoneType.WORK.getValue());
        exportTelephone(vCard, properties, 1, true, new String[] { TYPE_2ND }, contact, Contact.TELEPHONE_BUSINESS2, TelephoneType.WORK.getValue());
        exportTelephone(vCard, properties, 0, true, new String[] { TelephoneType.PREF.getValue() }, contact, Contact.TELEPHONE_HOME1, TelephoneType.HOME.getValue());
        exportTelephone(vCard, properties, 1, true, new String[] { TYPE_2ND }, contact, Contact.TELEPHONE_HOME2, TelephoneType.HOME.getValue());
        exportTelephone(vCard, properties, 0, true, null, contact, Contact.TELEPHONE_OTHER, TYPE_OTHER);
    }

    @Override
    public void importVCard(VCard vCard, Contact contact, VCardParameters parameters, List<OXException> warnings) {
        List<Telephone> properties = vCard.getProperties(Telephone.class);
        /*
         * special
         */
        importTelephone(properties, 0, false, contact, Contact.TELEPHONE_PAGER, TelephoneType.PAGER.getValue());
        importTelephone(properties, 0, false, contact, Contact.TELEPHONE_TTYTDD, TelephoneType.TEXTPHONE.getValue());
        importTelephone(properties, 0, false, contact, Contact.TELEPHONE_ISDN, TelephoneType.ISDN.getValue());
        importTelephone(properties, 0, false, contact, Contact.TELEPHONE_CAR, TelephoneType.CAR.getValue());
        importTelephone(properties, 0, false, contact, Contact.CELLULAR_TELEPHONE1, TelephoneType.CELL.getValue());
        importTelephone(properties, 1, false, contact, Contact.CELLULAR_TELEPHONE2, TelephoneType.CELL.getValue());
        importTelephone(properties, 0, false, contact, Contact.TELEPHONE_CALLBACK, TYPE_CALLBACK);
        importTelephone(properties, 0, false, contact, Contact.TELEPHONE_COMPANY, TYPE_COMPANY);
        importTelephone(properties, 0, false, contact, Contact.TELEPHONE_ASSISTANT, TYPE_ASSISTENT);
        importTelephone(properties, 0, false, contact, Contact.TELEPHONE_IP, TYPE_IP);
        importTelephone(properties, 0, false, contact, Contact.TELEPHONE_RADIO, TYPE_RADIO);
        importTelephone(properties, 0, false, contact, Contact.TELEPHONE_PRIMARY, TYPE_PRIMARY);
        /*
         * fax
         */
        importTelephone(properties, 0, false, contact, Contact.FAX_BUSINESS, TelephoneType.FAX.getValue(), TelephoneType.WORK.getValue());
        importTelephone(properties, 0, false, contact, Contact.FAX_HOME, TelephoneType.FAX.getValue(), TelephoneType.HOME.getValue());
        importTelephone(properties, 0, false, contact, Contact.FAX_OTHER, TelephoneType.FAX.getValue(), TYPE_OTHER);
        /*
         * voice
         */
        importTelephone(properties, 0, true, contact, Contact.TELEPHONE_BUSINESS1, TelephoneType.WORK.getValue());
        importTelephone(properties, 1, true, contact, Contact.TELEPHONE_BUSINESS2, TelephoneType.WORK.getValue());
        importTelephone(properties, 0, true, contact, Contact.TELEPHONE_HOME1, TelephoneType.HOME.getValue());
        importTelephone(properties, 1, true, contact, Contact.TELEPHONE_HOME2, TelephoneType.HOME.getValue());
        importTelephone(properties, 0, true, contact, Contact.TELEPHONE_OTHER, TYPE_OTHER);
    }

    /**
     * Exports a specific telephone property.
     *
     * @param vCard The target vCard
     * @param properties All existing telephone properties of the vCard
     * @param index The 0-based index in the list of all matching candidates to use
     * @param voice <code>true</code> to only match telephone properties of type <i>voice</i>, <code>false</code>, otherwise
     * @param additionalTypes Additional type parameter values to add to the exported property, or <code>null</code> to ignore
     * @param contact The contact to export from
     * @param field The source contact field
     * @param types The vCard telephone types to match
     * @return The exported telephone property, or <code>null</code> if none was exported
     */
    private static Telephone exportTelephone(VCard vCard, List<Telephone> properties, int index, boolean voice, String[] additionalTypes, Contact contact, int field, String...types) {
        Telephone telephone = getTelephone(properties, index, voice, types);
        if (has(contact, field)) {
            if (null == telephone) {
                telephone = new Telephone((String) contact.get(field));
                if (voice) {
                    telephone.getTypes().add(TelephoneType.VOICE);
                }
                for (String type : types) {
                    telephone.getParameters().addType(type);
                }
                vCard.addTelephoneNumber(telephone);
            } else {
                telephone.setText((String) contact.get(field));
            }
            if (null != additionalTypes && 0 < additionalTypes.length) {
                for (String additionalType : additionalTypes) {
                    addTypeIfMissing(telephone, additionalType);
                }
            }
            return telephone;
        } else if (null != telephone) {
            vCard.removeProperty(telephone);
        }
        return null;
    }

    /**
     * Imports a specific telephone property.
     *
     * @param properties All available telephone properties
     * @param index The 0-based index in the list of all matching candidates to use
     * @param voice <code>true</code> to only match telephone properties of type <i>voice</i>, <code>false</code>, otherwise
     * @param contact The contact to set the telephone number for
     * @param field The targeted contact field
     * @param types The vCard telephone types to match
     * @return The imported telephone property, or <code>null</code> if none was imported
     */
    private static Telephone importTelephone(List<Telephone> properties, int index, boolean voice, Contact contact, int field, String...types) {
        Telephone telephone = getTelephone(properties, index, voice, types);
        if (null != telephone) {
            String value = telephone.getText();
            if (Strings.isEmpty(value) && null != telephone.getUri()) {
                value = telephone.getUri().getNumber();
            }
            contact.set(field, value);
        } else {
            contact.set(field, null);
        }
        return telephone;
    }

    /**
     * Gets a telephone property based on its type parameters.
     *
     * @param properties All available telephone properties
     * @param index The 0-based index in the list of all matching candidates to use
     * @param voice <code>true</code> to only match telephone properties of type <i>voice</i>, <code>false</code>, otherwise
     * @param types The types to match
     * @return The matching telephone number, or <code>null</code> if not found
     */
    private static Telephone getTelephone(List<Telephone> properties, int index, boolean voice, String...types) {
        int matches = 0;
        for (Telephone telephone : getPropertiesWithTypes(properties, types)) {
            if (voice && false == isVoice(telephone)) {
                continue;
            }
            /*
             * possible match, first check for explicit index hint in type parameters
             */
            Collection<String> telephoneTypes = getParameterValues(telephone.getTypes());
            if (containsIgnoreCase(telephoneTypes, TYPE_2ND)) {
                return 1 == index ? telephone : null;
            }
            /*
             * return property if requested index matches
             */
            if (matches++ == index) {
                return telephone;
            }
        }
        return null;
    }

    private static boolean isVoice(Telephone telephone) {
        /*
         * assume "voice" if specified explicitly, or if no other distinguishing type is present
         */
        List<TelephoneType> telephoneTypes = telephone.getTypes();
        if (null != telephoneTypes && false == telephoneTypes.contains(TelephoneType.VOICE) && (
            telephoneTypes.contains(TelephoneType.TEXT) || telephoneTypes.contains(TelephoneType.FAX) ||
            telephoneTypes.contains(TelephoneType.CELL) || telephoneTypes.contains(TelephoneType.VIDEO) ||
            telephoneTypes.contains(TelephoneType.PAGER) || telephoneTypes.contains(TelephoneType.TEXTPHONE))) {
                return false;
        }
        return true;
    }

}

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

package com.openexchange.groupware.contact;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.groupware.contact.datasource.ContactImageDataSource;
import com.openexchange.groupware.contact.datasource.UserImageDataSource;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.image.ImageDataSource;
import com.openexchange.image.ImageLocation;
import com.openexchange.java.util.MsisdnCheck;
import com.openexchange.java.util.Pair;

/**
 * First start of a utility class for contacts. This class should contain methods that are useful for the complete backend and not only the
 * contacts component.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContactUtil {

    /**
     * Initializes a new {@link ContactUtil}.
     */
    private ContactUtil() {
        super();
    }

    /**
     * Generates the display name for given contact
     *
     * @param contact The contact
     */
    public static void generateDisplayName(final Contact contact) {
        if (contact.containsDisplayName()) {
            return;
        }
        final boolean hasUsefulGivenName = contact.containsGivenName() && contact.getGivenName() != null && contact.getGivenName().length() > 0;
        final boolean hasUsefulSureName = contact.containsSurName() && contact.getSurName() != null && contact.getSurName().length() > 0;
        if (hasUsefulGivenName || hasUsefulSureName) {
            final StringBuilder sb = new StringBuilder();
            if (hasUsefulSureName) {
                sb.append(contact.getSurName());
            }
            if (hasUsefulGivenName && hasUsefulSureName) {
                sb.append(", ");
            }
            if (hasUsefulGivenName) {
                sb.append(contact.getGivenName());
            }
            contact.setDisplayName(sb.toString());
            return;
        }
        if (contact.containsCompany() && contact.getCompany() != null && contact.getCompany().length() > 0) {
            contact.setDisplayName(contact.getCompany());
            return;
        }
    }

    /**
     * Gathers valid MSISDN telephone numbers for given contact
     *
     * @param contact The contact
     * @return The set providing valid MSISDN number for given contact
     */
    public static Set<String> gatherTelephoneNumbers(final Contact contact) {
        if (null == contact) {
            return Collections.emptySet();
        }
        final Set<String> set = new HashSet<String>(20);
        String tmp = contact.getCellularTelephone1();
        if (MsisdnCheck.checkMsisdn(tmp)) {
            set.add(tmp);
        }
        tmp = contact.getCellularTelephone2();
        if (MsisdnCheck.checkMsisdn(tmp)) {
            set.add(tmp);
        }
        tmp = contact.getTelephoneAssistant();
        if (MsisdnCheck.checkMsisdn(tmp)) {
            set.add(tmp);
        }
        tmp = contact.getTelephoneBusiness1();
        if (MsisdnCheck.checkMsisdn(tmp)) {
            set.add(tmp);
        }
        tmp = contact.getTelephoneBusiness2();
        if (MsisdnCheck.checkMsisdn(tmp)) {
            set.add(tmp);
        }
        tmp = contact.getTelephoneCallback();
        if (MsisdnCheck.checkMsisdn(tmp)) {
            set.add(tmp);
        }
        tmp = contact.getTelephoneCar();
        if (MsisdnCheck.checkMsisdn(tmp)) {
            set.add(tmp);
        }
        tmp = contact.getTelephoneCompany();
        if (MsisdnCheck.checkMsisdn(tmp)) {
            set.add(tmp);
        }
        tmp = contact.getTelephoneHome1();
        if (MsisdnCheck.checkMsisdn(tmp)) {
            set.add(tmp);
        }
        tmp = contact.getTelephoneHome2();
        if (MsisdnCheck.checkMsisdn(tmp)) {
            set.add(tmp);
        }
        tmp = contact.getTelephoneIP();
        if (MsisdnCheck.checkMsisdn(tmp)) {
            set.add(tmp);
        }
        tmp = contact.getTelephoneISDN();
        if (MsisdnCheck.checkMsisdn(tmp)) {
            set.add(tmp);
        }
        tmp = contact.getTelephoneOther();
        if (MsisdnCheck.checkMsisdn(tmp)) {
            set.add(tmp);
        }
        tmp = contact.getTelephonePager();
        if (MsisdnCheck.checkMsisdn(tmp)) {
            set.add(tmp);
        }
        tmp = contact.getTelephonePrimary();
        if (MsisdnCheck.checkMsisdn(tmp)) {
            set.add(tmp);
        }
        tmp = contact.getTelephoneRadio();
        if (MsisdnCheck.checkMsisdn(tmp)) {
            set.add(tmp);
        }
        tmp = contact.getTelephoneTelex();
        if (MsisdnCheck.checkMsisdn(tmp)) {
            set.add(tmp);
        }
        tmp = contact.getTelephoneTTYTTD();
        if (MsisdnCheck.checkMsisdn(tmp)) {
            set.add(tmp);
        }
        return set;
    }

    /**
     * Prepares an {@link ImageDataSource} and an {@link ImageLocation} for a given contact. Both
     * can be used to e.g. generate an URL to the contacts image.
     *
     * @param contact The contact, never <code>null</code>
     * @return A {@link Pair} containing both, the {@link ImageDataSource} and {@link ImageLocation}.
     *         If The contact misses an image, <code>null</code> is returned.
     */
    public static Pair<ImageDataSource, ImageLocation> prepareImageData(Contact contact) {
        if (0 < contact.getNumberOfImages() || contact.containsImage1() && null != contact.getImage1()) {
            String timestamp = null != contact.getLastModified() ? String.valueOf(contact.getLastModified().getTime()) : null;
            if (FolderObject.SYSTEM_LDAP_FOLDER_ID == contact.getParentFolderID() && contact.containsInternalUserId()) {
                /*
                 * prefer user contact image url
                 */
                ImageLocation imageLocation = new ImageLocation.Builder().id(
                    String.valueOf(contact.getInternalUserId())).timestamp(timestamp).build();
                return new Pair<ImageDataSource, ImageLocation>(UserImageDataSource.getInstance(), imageLocation);
            } else {
                /*
                 * use default contact image data source
                 */
                ImageLocation imageLocation = new ImageLocation.Builder().folder(String.valueOf(contact.getParentFolderID())).id(
                    String.valueOf(contact.getObjectID())).timestamp(timestamp).build();
                return new Pair<ImageDataSource, ImageLocation>(ContactImageDataSource.getInstance(), imageLocation);
            }
        }

        return null;
    }

}

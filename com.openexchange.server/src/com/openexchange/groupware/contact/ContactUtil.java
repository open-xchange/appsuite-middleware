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

package com.openexchange.groupware.contact;

import static com.openexchange.java.Autoboxing.L;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.util.MsisdnCheck;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * First start of a utility class for contacts. This class should contain methods that are useful for the complete backend and not only the
 * contacts component.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContactUtil {

    /**
     * The default contacts account prefix.
     */
    public static final String DEFAULT_ACCOUNT_PREFIX = "con://0/";

    /** A timestamp in the distant future as substitute for the client timestamp when circumventing concurrent modification checks */
    public static final long DISTANT_FUTURE = Long.MAX_VALUE;

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
        final Set<String> set = new HashSet<>(20);
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
     * Creates an URL to the contact image of the given contact.
     *
     * @param session The user session
     * @param con The contact
     * @return The URL or <code>null</code>
     * @throws OXException If services or parameter are missing
     */
    public static String generateImageUrl(Session session, Contact con) throws OXException {
        if (0 < con.getNumberOfImages() || con.containsImage1() && null != con.getImage1()) {
            Date lastModified = con.getImageLastModified();
            ContactPictureURLService service = ServerServiceRegistry.getInstance().getService(ContactPictureURLService.class, true);
            if (con.containsInternalUserId() && (FolderObject.SYSTEM_LDAP_FOLDER_ID == con.getParentFolderID() || 
                FolderObject.VIRTUAL_GUEST_CONTACT_FOLDER_ID == con.getParentFolderID())) { 
                return service.getUserPictureUrl(con.getInternalUserId(), session, lastModified == null ? null : L(lastModified.getTime()), true);
            }
            return service.getContactPictureUrl(con.getId(true), con.getFolderId(true), session, lastModified == null ? null : L(lastModified.getTime()), true);
        }
        return null;
    }

}

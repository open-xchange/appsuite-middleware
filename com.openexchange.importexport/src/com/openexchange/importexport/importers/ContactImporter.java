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

package com.openexchange.importexport.importers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.vcard.storage.VCardStorageService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.tools.mappings.MappedIncorrectString;
import com.openexchange.groupware.tools.mappings.MappedTruncation;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link ContactImporter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class ContactImporter extends AbstractImporter {

    protected ContactImporter(ServiceLookup services) {
        super(services);
    }

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactImporter.class);

    /**
     * Defines the maximum number of implicit retries in case of truncation errors.
     */
    protected static final int MAX_RETRIES = 10;

    /**
     * Creates a new contact, implicitly trying again with trimmed values in
     * case of truncation errors.
     *
     * @param session the current session
     * @param contact the contact to create
     * @param folderID the target folder ID
     * @throws OXException
     */
    protected void createContact(Session session, Contact contact, String folderID) throws OXException {
        this.createContact(session, contact, folderID, (InputStream) null);
    }

    /**
     * Creates a new contact, implicitly trying again with trimmed values in
     * case of truncation errors.
     *
     * @param session the current session
     * @param contact the contact to create
     * @param folderID the target folder ID
     * @param vCard the FileHolder containing the original vCard
     * @throws OXException
     */
    protected void createContact(Session session, Contact contact, String folderID, IFileHolder vCard) throws OXException {
        this.createContact(session, contact, folderID, vCard == null ? null : vCard.getStream());
    }

    /**
     * Creates a new contact, implicitly trying again with trimmed values in
     * case of truncation errors.
     *
     * @param session the current session
     * @param contact the contact to create
     * @param folderID the target folder ID
     * @param vCard the VCard to persist or null if not available
     * @throws OXException
     */
    protected void createContact(Session session, Contact contact, String folderID, String vCard) throws OXException {
        this.createContact(session, contact, folderID, vCard == null ? null : new ByteArrayInputStream(vCard.getBytes()));
    }

    /**
     * Creates a new contact, implicitly trying again with trimmed values in
     * case of truncation errors.
     *
     * @param session the current session
     * @param contact the contact to create
     * @param folderID the target folder ID
     * @param vCard the InputStream providing the original vCard
     * @throws OXException
     */
    protected void createContact(Session session, Contact contact, String folderID, InputStream vCard) throws OXException {
        ContactService contactService = ImportExportServices.getContactService();
        VCardStorageService vCardStorage = ImportExportServices.getVCardStorageService(session.getContextId());

        if (null == contactService) {
            throw ImportExportExceptionCodes.CONTACT_INTERFACE_MISSING.create();
        }

        if (vCard != null && vCardStorage != null && contactService.supports(session, folderID, ContactField.VCARD_ID)) {
            String vCardId = vCardStorage.saveVCard(vCard, session.getContextId());
            if (vCardId != null) {
                contact.setVCardId(vCardId);
            }
        }

        for (int retryCount = 1; retryCount <= MAX_RETRIES; retryCount++) {
            try {
                contactService.createContact(session, folderID, contact);
                return;
            } catch (OXException e) {
                if (retryCount < MAX_RETRIES && handle(e, contact)) {
                    // try again
                    LOG.debug("{} - trying again ({}/{})", e.getMessage(), retryCount, MAX_RETRIES, e);
                    continue;
                }
                // re-throw
                throw e;
            } finally {
                if (vCardStorage != null && contact.getVCardId() != null && contact.getObjectID() == 0) {
                    vCardStorage.deleteVCard(contact.getVCardId(), session.getContextId());
                    contact.removeVCardId();
                }
            }
        }
    }

    protected boolean handle(OXException e, Contact contact) {
        if (ContactExceptionCodes.DATA_TRUNCATION.equals(e)) {
            return null != e.getProblematics() && trimTruncatedAttributes(e, contact);
        }
        if (ContactExceptionCodes.INCORRECT_STRING.equals(e)) {
            return null != e.getProblematics() && removeIncorrectStrings(e, contact);
        }
        return false;
    }

    private static boolean trimTruncatedAttributes(OXException e, Contact contact) {
        try {
            return MappedTruncation.truncate(e.getProblematics(), contact);
        } catch (OXException x) {
            LOG.warn("error trying to handle truncated attributes", x);
            return false;
        }
    }

    private static boolean removeIncorrectStrings(OXException e, Contact contact) {
        try {
            return MappedIncorrectString.replace(e.getProblematics(), contact, "");
        } catch (OXException x) {
            LOG.warn("error trying to handle incorrect strings", x);
            return false;
        }
    }

}

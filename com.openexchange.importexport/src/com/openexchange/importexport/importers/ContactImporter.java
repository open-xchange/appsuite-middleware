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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.util.List;
import org.apache.commons.logging.Log;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.tools.mappings.MappedTruncation;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.log.LogFactory;
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

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ContactImporter.class));

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
        ContactService contactService = ImportExportServices.getContactService();
        if (null == contactService) {
            throw ImportExportExceptionCodes.CONTACT_INTERFACE_MISSING.create();
        }
        for (int retryCount = 1; retryCount <= MAX_RETRIES; retryCount++) {
            try {
                contactService.createContact(session, folderID, contact);
                return;
            } catch (OXException e) {
                if (retryCount < MAX_RETRIES && handle(e, contact)) {
                    // try again
                    LOG.debug(e.getMessage() + " - trying again (" + retryCount + "/" + MAX_RETRIES + ")", e);
                    continue;
                } else {
                    // re-throw
                    throw e;
                }
            }
        }
    }

    protected boolean handle(OXException e, Contact contact) {
        return ContactExceptionCodes.DATA_TRUNCATION.equals(e) && null != e.getProblematics() && trimTruncatedAttributes(e, contact);
    }

    private static boolean trimTruncatedAttributes(OXException e, Contact contact) {
        try {
            return MappedTruncation.truncate(e.getProblematics(), contact);
        } catch (OXException x) {
            LOG.warn("error trying to handle truncated attributes", x);
            return false;
        }
    }

    @Override
    protected String getNameForFieldInTruncationError(int id, OXException e) {
        if (null != e && ContactExceptionCodes.DATA_TRUNCATION.equals(e) && null != e.getProblematics()) {
            List<MappedTruncation<Object>> truncations = MappedTruncation.extract(e.getProblematics());
            if (0 < truncations.size()) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(truncations.get(0).getReadableName());
                for (int i = 1; i < truncations.size(); i++) {
                    stringBuilder.append(", ").append(truncations.get(i).getReadableName());
                }
                return stringBuilder.toString();
            }
        }
        return Integer.toString(id);
    }

}

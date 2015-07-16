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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.vcard.VCardImport;
import com.openexchange.contact.vcard.VCardParameters;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.exception.OXExceptionConstants;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;

/**
 * This importer translates VCards into contacts for the OX.
 *
 * @see ContactService - if you cannot enter the parsed content as contact into the database
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a> (minor: changes to new interface)
 */
public class VCardImporter extends ContactImporter implements OXExceptionConstants {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(VCardImporter.class);

    public VCardImporter(ServiceLookup services) {
        super(services);
    }

    @Override
    public boolean canImport(final ServerSession session, final Format format, final List<String> folders, final Map<String, String[]> optionalParams) throws OXException {
        if (!format.equals(Format.VCARD)) {
            return false;
        }
        if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), session.getContext()).hasContact()) {
            throw ImportExportExceptionCodes.CONTACTS_DISABLED.create().setGeneric(Generic.NO_PERMISSION);
        }
        final OXFolderAccess folderAccess = new OXFolderAccess(session.getContext());
        final Iterator<String> iterator = folders.iterator();
        while (iterator.hasNext()) {
            final String folder = iterator.next();

            int folderId = 0;
            try {
                folderId = Integer.parseInt(folder);
            } catch (final NumberFormatException e) {
                throw ImportExportExceptionCodes.NUMBER_FAILED.create(e, folder);
            }

            FolderObject fo;
            try {
                fo = folderAccess.getFolderObject(folderId);
            } catch (final OXException e) {
                return false;
            }

            // check format of folder
            if (fo.getModule() == FolderObject.CONTACT) {
                if (!UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(),
                        session.getContext()).hasContact()) {
                    return false;
                }
            } else {
                return false;
            }
            // check read access to folder
            EffectivePermission perm;
            try {
                perm = fo.getEffectiveUserPermission(session.getUserId(), UserConfigurationStorage.getInstance()
                        .getUserConfigurationSafe(session.getUserId(), session.getContext()));
            } catch (final OXException e) {
                throw ImportExportExceptionCodes.NO_DATABASE_CONNECTION.create(e);
            } catch (final RuntimeException e) {
                throw ImportExportExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
            }

            if (!perm.canCreateObjects()) {
                return false;
            }
        }

        return true;
    }

    Logger myLog = LoggerFactory.getLogger("MyLog");
    
    @Override
    public List<ImportResult> importData(final ServerSession session, final Format format, final InputStream is,
            final List<String> folders, final Map<String, String[]> optionalParams) throws OXException {

        int contactFolderId = -1;
        final OXFolderAccess folderAccess = new OXFolderAccess(session.getContext());
        final Iterator<String> iterator = folders.iterator();
        while (iterator.hasNext()) {
            final String folder = iterator.next();

            final int folderId = Integer.parseInt(folder);
            FolderObject fo;
            try {
                fo = folderAccess.getFolderObject(folderId);
            } catch (final OXException e) {
                throw ImportExportExceptionCodes.LOADING_FOLDER_FAILED.create(folder);
            }

            if (fo.getModule() == FolderObject.CONTACT) {
                contactFolderId = folderId;
                break;
            }
        }

        final List<ImportResult> list = new ArrayList<ImportResult>();

        try {
            int count = 0;
            int limit = getLimit(session);

            VCardService vCardService = ImportExportServices.getVCardService();
            
            VCardParameters vCardParameters = vCardService.createParameters(session);
            vCardParameters.setKeepOriginalVCard(true);
            SearchIterator<VCardImport> importVCards = vCardService.importVCards(is, vCardParameters);
            while (importVCards.hasNext()) {
                ImportResult importResult = new ImportResult();
                if (limit <= 0 || count <= limit) {
                    try (VCardImport vCardImport = importVCards.next()) {
                        if (vCardImport.getWarnings() != null && vCardImport.getWarnings().size() > 0) {
                            List<ConversionWarning> warnings = new ArrayList<ConversionWarning>(vCardImport.getWarnings().size());
                            for (OXException oxe : vCardImport.getWarnings()) {
                                warnings.add(new ConversionWarning(count, oxe));
                            }
                            importResult.addWarnings(warnings);
                        }
                        Contact contactObj = vCardImport.getContact();
                        contactObj.setParentFolderID(contactFolderId);
                        importResult.setDate(new Date());
                        try {
                            //myLog.debug(Streams.stream2string(vCardImport.getVCard().getStream(), "UTF-8"));
                            super.createContact(session, contactObj, Integer.toString(contactFolderId), vCardImport.getVCard());
                            count++;
                        } catch (final OXException oxEx) {
                            if (CATEGORY_USER_INPUT.equals(oxEx.getCategory())) {
                                LOG.debug("", oxEx);
                            } else {
                                LOG.error("", oxEx);
                            }
                            importResult.setException(oxEx);
                            LOG.debug("cannot import contact object", oxEx);
                        }
                        importResult.setObjectId(String.valueOf(contactObj.getObjectID()));
                        importResult.setDate(contactObj.getLastModified());
                    }
                    list.add(importResult);
                } else {
                    importResult.setException(ImportExportExceptionCodes.LIMIT_EXCEEDED.create(limit));
                    list.add(importResult);
                    break;
                }
            }
        } catch (final UnsupportedEncodingException e) {
            LOG.error("", e);
            throw ImportExportExceptionCodes.UTF8_ENCODE_FAILED.create(e);
        } catch (final IOException e) {
            LOG.error("", e);
            throw ImportExportExceptionCodes.VCARD_PARSING_PROBLEM.create(e, e.getMessage());
        }

        return list;
    }

}

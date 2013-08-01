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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.exception.OXExceptionConstants;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.formats.vcard.VCardFileToken;
import com.openexchange.importexport.formats.vcard.VCardTokenizer;
import com.openexchange.java.Charsets;
import com.openexchange.java.StringAllocator;
import com.openexchange.java.Strings;
import com.openexchange.log.LogFactory;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitException;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;

/**
 * This importer translates VCards into contacts for the OX.
 *
 * @see OXContainerConverter - if you have a problem with the content of the parsed ICAL file
 * @see ContactService - if you cannot enter the parsed content as contact into the database
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a> (minor: changes to new interface)
 */
public class VCardImporter extends ContactImporter implements OXExceptionConstants {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(VCardImporter.class));
    private static final boolean DEBUG = LOG.isDebugEnabled();
    
    
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

        OXContainerConverter oxContainerConverter = null;

        final List<ImportResult> list = new ArrayList<ImportResult>();

        try {
            oxContainerConverter = new OXContainerConverter(session);
            final VCardTokenizer tokenizer = new VCardTokenizer(is);
            final List<VCardFileToken> chunks = tokenizer.split();
            if (chunks.isEmpty()) {
                throw ImportExportExceptionCodes.NO_VCARD_FOUND.create();
            }
            int count = 0;
            int limit = getLimit(session);
            for (final VCardFileToken chunk : chunks) {
                final VersitDefinition def = chunk.getVersitDefinition();
                final ImportResult importResult = new ImportResult();

                if (def == null) {
                    // could not find appropriate parser for this part of the vcard file
                    LOG.error("Could not recognize format of the following VCard data: " + Arrays.toString(chunk.getContent()));
                    importResult.setDate(new Date(System.currentTimeMillis()));
                    importResult.setException(ImportExportExceptionCodes.UNKNOWN_VCARD_FORMAT.create(chunk.getContent()));
                } else {
                    final VersitDefinition.Reader versitReader = def.getReader(new UnsynchronizedByteArrayInputStream(chunk.getContent()), "UTF-8");
                    try {
                        final VersitObject versitObject = def.parse(versitReader);
                        
                        if (limit <= 0 || count <= limit) {
                            
                            importResult.setFolder(String.valueOf(contactFolderId));

                            final Contact contactObj = oxContainerConverter.convertContact(versitObject);
                            contactObj.setParentFolderID(contactFolderId);
                            importResult.setDate(new Date());
                            try {
                                super.createContact(session, contactObj, Integer.toString(contactFolderId));
                                count++;
                            } catch (final OXException oxEx) {
                                if (CATEGORY_USER_INPUT.equals(oxEx.getCategory())) {
                                    LOG.debug(oxEx.getMessage(), oxEx);
                                } else {
                                    LOG.error(oxEx.getMessage(), oxEx);
                                }
                                importResult.setException(oxEx);
                                LOG.debug("cannot import contact object", oxEx);
                            }
                            importResult.setObjectId(String.valueOf(contactObj.getObjectID()));
                            importResult.setDate(contactObj.getLastModified());
                        } else {
                            throw ImportExportExceptionCodes.LIMIT_EXCEEDED.create(limit);
                        }
                    } catch (final VersitException e) {
                        LOG.error(generateErrorMessage(e, DEBUG ? new String(chunk.getContent(), Charsets.UTF_8) : null), e);
                        importResult.setException(ImportExportExceptionCodes.VCARD_PARSING_PROBLEM.create(e, e.getMessage()));
                    } catch (final ConverterException e) {
                        LOG.error(generateErrorMessage(e, DEBUG ? new String(chunk.getContent(), Charsets.UTF_8) : null), e);
                        importResult.setException(ImportExportExceptionCodes.VCARD_CONVERSION_PROBLEM.create(e, e.getMessage()));
                    } catch (final RuntimeException e) {
                        LOG.error(generateErrorMessage(e, DEBUG ? new String(chunk.getContent(), Charsets.UTF_8) : null), e);
                        importResult.setException(ImportExportExceptionCodes.VCARD_CONVERSION_PROBLEM.create(e, e.getMessage()));
                    }
                }
                list.add(importResult);
            }
        } catch (final UnsupportedEncodingException e) {
            LOG.fatal(e.getMessage(), e);
            throw ImportExportExceptionCodes.UTF8_ENCODE_FAILED.create(e);
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
            throw ImportExportExceptionCodes.VCARD_PARSING_PROBLEM.create(e, e.getMessage());
        } catch (final ConverterException e) {
            LOG.error(e.getMessage(), e);
            throw ImportExportExceptionCodes.VCARD_CONVERSION_PROBLEM.create(e, e.getMessage());
        } finally {
            if (oxContainerConverter != null) {
                oxContainerConverter.close();
            }
        }

        return list;
    }

    private String generateErrorMessage(final Exception e, final String vcard) {
        final StringAllocator sb = new StringAllocator(null != vcard ? 8192 : 128);
        sb.append("Cannot parse contact object: ").append(e.getMessage());
        if (null != vcard) {
            final String sep = Strings.getLineSeparator();
            sb.append(sep).append("Associated VCard content:").append(sep);
            for (final String line : vcard.split("\r?\n")) {
                sb.append(line).append(sep);
            }
        }
        return sb.toString();
    }

}

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

package com.openexchange.file.storage.json.actions.accounts;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.CapabilityAware;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.MetadataAware;
import com.openexchange.file.storage.json.FileStorageAccountParser;
import com.openexchange.file.storage.json.FileStorageAccountWriter;
import com.openexchange.file.storage.json.actions.files.AbstractFileAction;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;


/**
 * The common superclass for all AJAXActionServices handling file storage account management. Provides a unified handling
 * for JSONExceptions and stores commonly used services (the registry, a writer and a parser) for subclasses.
 * Subclasses must implement the {@link #doIt(AJAXRequestData, ServerSession)} method.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractFileAction.MODULE, type = RestrictedAction.Type.READ)
public abstract class AbstractFileStorageAccountAction implements AJAXActionService {

    protected FileStorageServiceRegistry registry;
    protected FileStorageAccountWriter writer;
    protected FileStorageAccountParser parser;

    public AbstractFileStorageAccountAction(final FileStorageServiceRegistry registry) {
        this.registry = registry;
        writer = new FileStorageAccountWriter();
        parser = new FileStorageAccountParser(registry);
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        try {
            return doIt(requestData, session);
        } catch (JSONException x) {
            throw FileStorageExceptionCodes.JSON_ERROR.create(x,x.toString());
        }
    }

    /**
     * Optionally gets the account's root folder from a file storage account.
     *
     * @param access The file storage account access to get the root folder from
     * @return The root folder, or <code>null</code> if there is none
     */
    protected FileStorageFolder optRootFolder(FileStorageAccountAccess access) throws OXException {
        try {
            return access.getRootFolder();
        } catch (OXException e) {
            if (FileStorageExceptionCodes.NO_SUCH_FOLDER.equals(e)) {
                return null;
            }
            throw e;
        }
    }

    protected JSONObject optMetadata(Session session, FileStorageAccount account) throws OXException {
        if (MetadataAware.class.isInstance(account.getFileStorageService())) {
            return ((MetadataAware) account.getFileStorageService()).getMetadata(session, account);
        }
        return null;
    }

    protected Set<String> determineCapabilities(FileStorageAccountAccess access) {
        if (!(access instanceof CapabilityAware)) {
            return null;
        }

        CapabilityAware capabilityAware = (CapabilityAware) access;
        Set<String> caps = new HashSet<String>();

        Boolean supported = capabilityAware.supports(FileStorageCapability.FILE_VERSIONS);
        if (null != supported && supported.booleanValue()) {
            caps.add(FileStorageCapability.FILE_VERSIONS.name());
        }

        supported = capabilityAware.supports(FileStorageCapability.EXTENDED_METADATA);
        if (null != supported && supported.booleanValue()) {
            caps.add(FileStorageCapability.EXTENDED_METADATA.name());
        }

        supported = capabilityAware.supports(FileStorageCapability.RANDOM_FILE_ACCESS);
        if (null != supported && supported.booleanValue()) {
            caps.add(FileStorageCapability.RANDOM_FILE_ACCESS.name());
        }

        supported = capabilityAware.supports(FileStorageCapability.LOCKS);
        if (null != supported && supported.booleanValue()) {
            caps.add(FileStorageCapability.LOCKS.name());
        }

        supported = capabilityAware.supports(FileStorageCapability.READ_ONLY);
        if (null != supported && supported.booleanValue()) {
            caps.add(FileStorageCapability.READ_ONLY.name());
        }

        supported = capabilityAware.supports(FileStorageCapability.MAIL_ATTACHMENTS);
        if (null != supported && supported.booleanValue()) {
            caps.add(FileStorageCapability.MAIL_ATTACHMENTS.name());
        }

        supported = capabilityAware.supports(FileStorageCapability.AUTO_NEW_VERSION);
        if (null != supported && supported.booleanValue()) {
            caps.add(FileStorageCapability.AUTO_NEW_VERSION.name());
        }

        supported = capabilityAware.supports(FileStorageCapability.ZIPPABLE_FOLDER);
        if (null != supported && supported.booleanValue()) {
            caps.add(FileStorageCapability.ZIPPABLE_FOLDER.name());
        }

        supported = capabilityAware.supports(FileStorageCapability.COUNT_TOTAL);
        if (null != supported && supported.booleanValue()) {
            caps.add(FileStorageCapability.COUNT_TOTAL.name());
        }

        supported = capabilityAware.supports(FileStorageCapability.CASE_INSENSITIVE);
        if (null != supported && supported.booleanValue()) {
            caps.add(FileStorageCapability.CASE_INSENSITIVE.name());
        }

        supported = capabilityAware.supports(FileStorageCapability.AUTO_RENAME_FOLDERS);
        if (null != supported && supported.booleanValue()) {
            caps.add(FileStorageCapability.AUTO_RENAME_FOLDERS.name());
        }

        supported = capabilityAware.supports(FileStorageCapability.RESTORE);
        if (null != supported && supported.booleanValue()) {
            caps.add(FileStorageCapability.RESTORE.name());
        }

        return caps;
    }

    protected static Locale localeFrom(final Session session) {
        if (null == session) {
            return Locale.US;
        }
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUser().getLocale();
        }
        try {
            return UserStorage.getInstance().getUser(session.getUserId(), session.getContextId()).getLocale();
        } catch (OXException e) {
            return Locale.US;
        }
    }

    protected abstract AJAXRequestResult doIt(AJAXRequestData request, ServerSession session) throws JSONException, OXException;

}

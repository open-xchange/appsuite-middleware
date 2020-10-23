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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.share.core.subscription;

import java.util.concurrent.Callable;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.generic.DefaultFileStorageAccount;
import com.openexchange.session.Session;

/**
 * {@link AccountMetadataHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class AccountMetadataHelper {
    
    private final FileStorageAccount account;
    private final Session session;
    
    /**
     * Initializes a new {@link AccountMetadataHelper}.
     * 
     * @param account The underlying file storage account
     * @param session The account owner's session
     */
    public AccountMetadataHelper(FileStorageAccount account, Session session) {
        super();
        this.account = account;
        this.session = session;
    }

    /**
     * Gets the account's metadata.
     * 
     * @return The metadata of the account, or a new {@link JSONObject} if not yet set
     */
    public JSONObject getAccountMetadata() {
        JSONObject metadata = account.getMetadata();
        return null == metadata ? new JSONObject() : metadata;
    }
    
    /**
     * Gets certain metadata stored in the account's metadata JSON object, refreshing the cached data implicitly as needed.
     * 
     * @param <T> The type of the cached value. Since stored in the JSON-based configuration, it should be one of
     *            Boolean, Double, Integer, JSONArray, JSONObject, Long,String, or the JSONObject.NULL object
     * @param key The key under which the value is cached
     * @param maxAge The time after which a cached value is updated
     * @param clazz The class to cast the cached value to
     * @param loader The callable to retrieve the value
     * @return The cached metadata, or <code>null</code> if the callable yielded a <code>null</code> result
     */
    public <T> T getCachedValue(String key, long maxAge, Class<T> clazz, Callable<T> loader) throws OXException {
        JSONObject accountMetadata = account.getMetadata();
        if (null == accountMetadata) {
            accountMetadata = new JSONObject();
        }
        /*
         * reload cached value if needed
         */
        String lastUpdatedKey = key + "@lastUpdated";
        Object value = accountMetadata.opt(key);
        if (System.currentTimeMillis() - maxAge > accountMetadata.optLong(lastUpdatedKey, -1L)) {
            T loadedValue;
            try {
                loadedValue = loader.call();
            } catch (Exception e) {
                Throwable cause = e.getCause();
                throw OXException.class.isInstance(cause) ? (OXException) cause : FileStorageExceptionCodes.UNEXPECTED_ERROR.create(cause, e.getMessage());
            }
            accountMetadata.putSafe(key, null != loadedValue ? loadedValue : JSONObject.NULL);
            accountMetadata.putSafe(lastUpdatedKey, Long.valueOf(System.currentTimeMillis()));
            saveAccountMetadata(accountMetadata);
            return loadedValue;
        }
        return null == value || JSONObject.NULL.equals(value) ? null : clazz.cast(value);
    }

    /**
     * Updates the account's metadata configuration in the storage.
     * 
     * @param accountMetadata The metadata to store
     */
    public void saveAccountMetadata(JSONObject accountMetadata) throws OXException {
        FileStorageService fileStorageService = account.getFileStorageService();
        DefaultFileStorageAccount accountUpdate = new DefaultFileStorageAccount();
        accountUpdate.setServiceId(fileStorageService.getId());
        accountUpdate.setFileStorageService(fileStorageService);
        accountUpdate.setId(account.getId());
        accountUpdate.setDisplayName(account.getDisplayName());
        accountUpdate.setConfiguration(account.getConfiguration());
        accountUpdate.setMetaData(accountMetadata);
        fileStorageService.getAccountManager().updateAccount(accountUpdate, session);
    }

}

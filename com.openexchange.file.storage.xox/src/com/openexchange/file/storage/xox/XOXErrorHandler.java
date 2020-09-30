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

package com.openexchange.file.storage.xox;

import java.time.Instant;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.common.DataHandlers;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountMetaDataUtil;

/**
 * {@link XOXErrorHandler} - provides functionality to handle and persist errors occurred while accessing the XOX share.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class XOXErrorHandler {

    private final XOXAccountAccess accountAccess;
    private final int retryAfterError;
    private final ConversionService conversionService;

    /**
     * Initializes a new {@link XOXErrorHandler}.
     *
     * @param conversionService The required {@link ConversionService}
     * @param accountAccess The related {@link XOXAccountAccess}
     * @param retryAfterError The amount of time in seconds after which an persistent account error should be ignored.
     */
    public XOXErrorHandler(ConversionService conversionService, XOXAccountAccess accountAccess, int retryAfterError) {
        this.conversionService = conversionService;
        this.accountAccess = accountAccess;
        this.retryAfterError = retryAfterError;
    }

    /**
     * Checks whether or not the given exception should be saved to the DB
     *
     * @param exception The exception to check
     * @return <code>True</code> if the exception should be stored to the DB, <code>false</code> otherwise
     */
    private boolean shouldSaveException(OXException exception) {
        if (exception != null) {

            //TODO: check whether or not we want to store the exception
            return true;
        }
        return false;
    }

    /**
     * Gets the last known error for this account, occurred during the last n seconds.
     *
     * @param t The time in seconds
     * @return The error occurred in the last t seconds, or null if there is no current error or it occurred longer than the given t seconds.
     * @throws OXException
     */
    private FileStorageAccountError getRecentError(int t) throws OXException {
        try {
            final FileStorageAccount account = accountAccess.getService().getAccountManager().getAccount(accountAccess.getAccountId(), accountAccess.getSession());
            JSONObject lastError = FileStorageAccountMetaDataUtil.getAccountError(account);
            if (lastError != null) {
                JSONObject error = lastError.getJSONObject(FileStorageAccountMetaDataUtil.JSON_FIELD_EXCEPTION);
                Date lastErrorTimeStamp = new Date(lastError.getLong(FileStorageAccountMetaDataUtil.JSON_FIELD_TIMESTAMP));
                //Check if the error occurred in the last n seconds
                final Instant now = new Date().toInstant();
                final Instant errorOccuredOn = lastErrorTimeStamp.toInstant();
                if (errorOccuredOn.isAfter(now.minusSeconds(t))) {
                    DataHandler dataHandler = conversionService.getDataHandler(DataHandlers.JSON2OXEXCEPTION);
                    ConversionResult result = dataHandler.processData(new SimpleData<JSONObject>(error), new DataArguments(), null);
                    if (result != null && result.getData() != null && OXException.class.isInstance(result.getData())) {
                        //The error occurred in the last n seconds
                        return new FileStorageAccountError((OXException) result.getData(), lastErrorTimeStamp);
                    }
                }
            }
            return null;
        } catch (JSONException e) {
            throw XOXFileStorageExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the last known exception for this account, occurred during the last t seconds.
     *
     * @param t The time in seconds
     * @return The exception occurred in the last t seconds, or null if there is no current error or it occurred longer than the given t seconds.
     * @throws OXException
     */
    private OXException getRecentException(int t) throws OXException {
        FileStorageAccountError lastError = getRecentError(t);
        return  lastError != null ? lastError.getException() : null;
    }

    /**
     * Gets the last known exception for this account, occurred during the last t (configured) seconds.
     * getRecentException
     *
     * @return The exception occurred in the last t (configured) seconds, or null if there is no current error or it occurred longer than the given t seconds.
     * @throws OXException
     */
    public OXException getRecentException() throws OXException {
        return getRecentException(retryAfterError);
    }

    /**
     * Asserts that there was no recent exception, in the last t seconds, accessing this account
     *
     * @param t time in seconds
     * @param ignore If <code>ignore</code> is set to true, this method does actually nothing; useful for a more fluent like style for the caller
     * @throws OXException if there was a known exception in the last t seconds
     */
    private void assertNoRecentException(int t) throws OXException {
        OXException recentException = getRecentException(t);
        if (recentException != null) {
            throw recentException;
        }
    }

    /**
     * Asserts that there was no recent exception, in the last t (configured) seconds, accessing this account.
     *
     * @throws OXException if there was a known exception in the last t seconds
     */
    public void assertNoRecentException() throws OXException {
        assertNoRecentException(retryAfterError);
    }

    /**
     * Checks if there is a recent exception, in the last t (configured) seconds, accessing this account.
     *
     * @return <code>true</code> if there was a recent exception in the last t (configured) seconds, <code>false</code> otherwise.
     * @throws OXException
     */
    public boolean hasRecentException() throws OXException {
        return getRecentException(retryAfterError) != null;
    }

    /**
     * Handles an exception; i.e. if the exception is found to be long-lasting, it is saved to the DB
     *
     * @param exception exception to save
     * @throws OXException If the exception could not be saved
     */
    public OXException handleException(OXException exception) throws OXException {
        try {
            if (exception != null && shouldSaveException(exception)) {
                FileStorageAccount account = accountAccess.getAccount();
                JSONObject metadata = FileStorageAccountMetaDataUtil.getAccountMetaData(account);
                JSONObject lastError = FileStorageAccountMetaDataUtil.getAccountError(account);
                if (lastError == null) {
                    lastError = new JSONObject();
                    metadata.put(FileStorageAccountMetaDataUtil.JSON_FIELD_LAST_ERROR, lastError);
                }
                lastError.put(FileStorageAccountMetaDataUtil.JSON_FIELD_TIMESTAMP, new Date().getTime());

                DataHandler dataHandler = conversionService.getDataHandler(DataHandlers.OXEXCEPTION2JSON);
                ConversionResult result = dataHandler.processData(new SimpleData<OXException>(exception), new DataArguments(), null);
                Object data = result.getData();
                if (data != null && JSONObject.class.isInstance(data)) {
                    lastError.put(FileStorageAccountMetaDataUtil.JSON_FIELD_EXCEPTION, data);
                }

                accountAccess.getService().getAccountManager().updateAccount(account, accountAccess.getSession());
            }
            return exception;
        } catch (JSONException e) {
            throw XOXFileStorageExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Deletes the recent exception, if any
     *
     * @throws OXException
     */
    public void removeRecentException() throws OXException {
        FileStorageAccount account = accountAccess.getAccount();
        JSONObject metadata = account.getMetadata();
        Object objectRemoved = metadata.remove(FileStorageAccountMetaDataUtil.JSON_FIELD_LAST_ERROR);
        if(objectRemoved != null) {
            accountAccess.getService().getAccountManager().updateAccount(account, accountAccess.getSession());
        }
    }
}

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

package com.openexchange.mailaccount.json.actions;

import static com.openexchange.mail.utils.ProviderUtility.extractProtocol;
import static com.openexchange.mailaccount.Tools.getUnsignedInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONValue;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.jslob.JSlobExceptionCodes;
import com.openexchange.jslob.storage.JSlobStorage;
import com.openexchange.jslob.storage.registry.JSlobStorageRegistry;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.MailProvider;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountFacade;
import com.openexchange.mailaccount.Tools;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.mailaccount.json.fields.MailAccountFields;
import com.openexchange.secret.SecretService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.net.URIDefaults;
import com.openexchange.tools.net.URIParser;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractMailAccountAction} - An abstract folder action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractMailAccountAction implements AJAXActionService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractMailAccountAction.class);

    /** The service identifier for JSlob */
    public static final String JSLOB_SERVICE_ID = "com.openexchange.mailaccount";

    /** The reference to JSlobStorageRegistry */
    private static final AtomicReference<JSlobStorageRegistry> STORAGE_REGISTRY = new AtomicReference<JSlobStorageRegistry>();

    /**
     * Initializes a new {@link AbstractMailAccountAction}.
     */
    protected AbstractMailAccountAction() {
        super();
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        try {
            final Object data = requestData.getData();
            if (data instanceof JSONValue) {
                final JSONValue jBody = (JSONValue) data;
                return innerPerform(requestData, session, jBody);
            }
            return innerPerform(requestData, session, null);
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create( e, e.getMessage());
        }
    }

    /**
     * Performs given request.
     *
     * @param requestData The request to perform
     * @param session The session providing needed user data
     * @param optBody The optional JSON body
     * @return The result yielded from given request
     * @throws OXException If an error occurs
     * @throws JSONException If a JSON error occurs
     */
    protected abstract AJAXRequestResult innerPerform(final AJAXRequestData requestData, final ServerSession session, final JSONValue optBody) throws OXException, JSONException;

    /**
     * Sets the JSlobStorageRegistry.
     *
     * @param storageRegistry The JSlobStorageRegistry instance or <code>null</code>
     */
    public static void setJSlobStorageRegistry(final JSlobStorageRegistry storageRegistry) {
        STORAGE_REGISTRY.set(storageRegistry);
    }

    /**
     * Gets the JSON storage service.
     *
     * @return The storage service
     * @throws OXException If service cannot be returned
     * @see #JSLOB_SERVICE_ID
     */
    public static JSlobStorage getStorage() throws OXException {
        final JSlobStorageRegistry storageRegistry = STORAGE_REGISTRY.get();
        // TODO: Make configurable
        final String storageId = "io.ox.wd.jslob.storage.db";
        final JSlobStorage storage = storageRegistry.getJSlobStorage(storageId);
        if (null == storage) {
            throw JSlobExceptionCodes.NOT_FOUND.create(storageId);
        }
        return storage;
    }

    /**
     * Gets the default tree identifier to use if request does not provide any.
     *
     * @return The default tree identifier
     */
    protected static String getDefaultTreeIdentifier() {
        return FolderStorage.REAL_TREE_ID;
    }

    /**
     * Gets the default allowed modules.
     *
     * @return The default allowed modules
     */
    protected static List<ContentType> getDefaultAllowedModules() {
        return Collections.emptyList();
    }

    /**
     * Parses specified parameter into <code>int</code>.
     *
     * @param parameterName The parameter name
     * @param request The request
     * @return The parsed <code>int</code>
     * @throws OXException If parameter is not present in given request
     */
    protected static int parseIntParameter(final String parameterName, final AJAXRequestData request) throws OXException {
        final String tmp = request.getParameter(parameterName);
        if (null == tmp) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(parameterName);
        }
        return getUnsignedInteger(tmp);
    }

    private static final Pattern PAT = Pattern.compile(" *, *");

    /**
     * Parses specified parameter into an array of <code>int</code>.
     *
     * @param parameterName The parameter name
     * @param request The request
     * @return The parsed array of <code>int</code>
     * @throws OXException If parameter is not present in given request
     */
    protected static int[] parseIntArrayParameter(final String parameterName, final AJAXRequestData request) throws OXException {
        final String tmp = request.getParameter(parameterName);
        if (null == tmp) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(parameterName);
        }
        final String[] sa = PAT.split(tmp, 0);
        final int[] columns = new int[sa.length];
        for (int i = 0; i < sa.length; i++) {
            columns[i] = getUnsignedInteger(sa[i]);
        }
        return columns;
    }

    /**
     * Parses specified optional parameter into an array of <code>int</code>.
     *
     * @param parameterName The parameter name
     * @param request The request
     * @return The parsed array of <code>int</code>; a zero length array is returned if parameter is missing
     */
    protected static int[] parseOptionalIntArrayParameter(final String parameterName, final AJAXRequestData request) {
        final String tmp = request.getParameter(parameterName);
        if (null == tmp) {
            return new int[0];
        }
        final String[] sa = PAT.split(tmp, 0);
        final int[] columns = new int[sa.length];
        for (int i = 0; i < sa.length; i++) {
            columns[i] = getUnsignedInteger(sa[i]);
        }
        return columns;
    }

    protected static boolean isUnifiedINBOXAccount(final MailAccount mailAccount) {
        return isUnifiedINBOXAccount(mailAccount.getMailProtocol());
    }

    protected static boolean isUnifiedINBOXAccount(final String mailProtocol) {
        return UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX.equals(mailProtocol);
    }

    protected static void checkNeededFields(final MailAccountDescription accountDescription) throws OXException {
        // Check needed fields
        if (isEmpty(accountDescription.getMailServer())) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(MailAccountFields.MAIL_URL);
        }
        if (isEmpty(accountDescription.getLogin())) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(MailAccountFields.LOGIN);
        }
        if (isEmpty(accountDescription.getPassword())) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(MailAccountFields.PASSWORD);
        }
    }

    /**
     * Gets the secret string for specified session.
     *
     * @param session The session
     * @return The secret string
     * @throws OXException If secret string cannot be returned
     */
    protected static String getSecret(final ServerSession session) throws OXException {
        return ServerServiceRegistry.getInstance().getService(SecretService.class, true).getSecret(session);
    }

    /**
     * Checks if specified {@link MailAccount} is considered as default aka primary account.
     *
     * @param mailAccount The mail account to examine
     * @return <code>true</code> if specified {@link MailAccount} is considered as defaul account; otherwise <code>false</code>
     */
    protected static boolean isDefaultMailAccount(final MailAccount mailAccount) {
        return mailAccount.isDefaultAccount() || MailAccount.DEFAULT_ID == mailAccount.getId();
    }

    /**
     * Checks if specified {@link MailAccountDescription} is considered as default aka primary account.
     *
     * @param mailAccount The mail account description to examine
     * @return <code>true</code> if specified {@link MailAccountDescription} is considered as defaul account; otherwise <code>false</code>
     */
    protected static boolean isDefaultMailAccount(final MailAccountDescription mailAccount) {
        return MailAccount.DEFAULT_ID == mailAccount.getId();
    }

    /**
     * Parses the attributes from passed comma-separated list.
     *
     * @param colString The comma-separated list
     * @return The parsed attributes
     */
    protected static List<Attribute> getColumns(final String colString) {
        List<Attribute> attributes = null;
        if (colString != null && !"".equals(colString.trim())) {
            attributes = new LinkedList<Attribute>();
            for (final String col : colString.split("\\s*,\\s*")) {
                if ("".equals(col)) {
                    continue;
                }
                attributes.add(Attribute.getById(Integer.parseInt(col)));
            }
            return attributes;
        }
        // All columns
        return Arrays.asList(Attribute.values());
    }

    /**
     * Gets the appropriate {@link MailAccess} instance for specified mail account description and session.
     *
     * @param accountDescription The mail account description
     * @param session The session providing needed user information
     * @param warnings
     * @return The appropriate {@link MailAccess} instance
     * @throws OXException If appropriate {@link MailAccess} instance cannot be determined
     */
    protected static MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getMailAccess(MailAccountDescription accountDescription, ServerSession session, List<OXException> warnings) throws OXException {
        final String mailServerURL = accountDescription.generateMailServerURL();
        // Get the appropriate mail provider by mail server URL
        final MailProvider mailProvider = getMailProviderByURL(mailServerURL);
        if (null == mailProvider) {
            LOG.debug("Validating mail account failed. No mail provider found for URL: {}", mailServerURL);
            return null;
        }
        // Set marker
        session.setParameter("mail-account.request", "validate");
        MailConfig mailConfig = null;
        try {
            // Create a mail access instance
            int accountId = accountDescription.getId();
            MailAccess<?, ?> mailAccess = accountId >= 0 ? mailProvider.createNewMailAccess(session, accountId) : mailProvider.createNewMailAccess(session);
            mailConfig = mailAccess.getMailConfig();
            // Set login and password
            mailConfig.setLogin(accountDescription.getLogin());
            mailConfig.setPassword(accountDescription.getPassword());
            // Set server and port
            final URI uri;
            try {
                uri = URIParser.parse(mailServerURL, URIDefaults.IMAP);
            } catch (final URISyntaxException e) {
                throw MailExceptionCode.URI_PARSE_FAILED.create(e, mailServerURL);
            }
            if (null != uri) {
                mailConfig.setServer(uri.getHost());
                mailConfig.setPort(uri.getPort());
            }
            mailConfig.setSecure(accountDescription.isMailSecure());
            mailAccess.setCacheable(false);
            return mailAccess;
        } catch (final OXException e) {
            if (null != mailConfig) {
                Throwable cause = e.getCause();
                while ((null != cause) && (cause instanceof OXException)) {
                    cause = cause.getCause();
                }
                if (null != cause) {
                    warnings.add(MailAccountExceptionCodes.VALIDATE_FAILED_MAIL.create(cause, mailConfig.getServer(), mailConfig.getLogin()));
                }
            } else {
                e.setCategory(Category.CATEGORY_WARNING);
                warnings.add(e);
            }
        } finally {
            // Unset marker
            session.setParameter("mail-account.request", null);
        }
        return null;
    }

    private static MailProvider getMailProviderByURL(final String serverUrl) {
        /*
         * Get appropriate provider
         */
        return MailProviderRegistry.getRealMailProvider(extractProtocol(serverUrl, MailProperties.getInstance().getDefaultMailProvider()));
    }

    /**
     * Checks for presence of default folder full names and creates them if absent.
     *
     * @param account The corresponding account
     * @param storageService The storage service (needed for update)
     * @param session The session providing needed user information
     * @return The mail account with full names present
     * @throws OXException If check for full names fails
     */
    protected static MailAccount checkFullNames(final MailAccount account, final MailAccountFacade mailAccountFacade, final ServerSession session) throws OXException {
        return checkFullNames(account, mailAccountFacade, session, null, null);
    }

    /**
     * Checks for presence of default folder full names and creates them if absent.
     *
     * @param account The corresponding account
     * @param storageService The storage service (needed for update)
     * @param session The session providing needed user information
     * @param folderNames Array of predefined folder names (e.g. Special-Use Folders) or null entries. If present, these names are used for creation.
     * @return The mail account with full names present
     * @throws OXException If check for full names fails
     */
    public static MailAccount checkFullNames(final MailAccount account, final MailAccountFacade mailAccountFacade, final ServerSession session, final Connection con, final Map<String, String> folderNames) throws OXException {
        return Tools.checkFullNames(account, mailAccountFacade, session, con, folderNames);
    }

    /** Checks for an empty string */
    protected static boolean isEmpty(final String string) {
        return com.openexchange.java.Strings.isEmpty(string);
    }

    protected MailAccountFacade getAccountFacade() throws OXException {
        return ServerServiceRegistry.getInstance().getService(MailAccountFacade.class, true);
    }

}

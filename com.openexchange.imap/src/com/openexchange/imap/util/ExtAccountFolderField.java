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

package com.openexchange.imap.util;

import static com.openexchange.imap.util.ImapUtility.prepareImapCommandForLogging;
import static com.openexchange.java.Strings.isEmpty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.mail.MessagingException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.ajax.customizer.AdditionalFieldsUtils;
import com.openexchange.ajax.customizer.folder.AdditionalFolderField;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.imap.IMAPCommandsCollection;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.IMAPFolderStorage;
import com.openexchange.imap.services.Services;
import com.openexchange.java.Strings;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailFolderStorageDelegator;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.tools.session.ServerSession;
import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.ParsingException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.BASE64MailboxDecoder;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;


/**
 * {@link ExtAccountFolderField}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ExtAccountFolderField implements AdditionalFolderField {

    static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ExtAccountFolderField.class);

    /** The cache region name */
    public static final String REGION_NAME = "ExternalAccountFolders";

    /**
     * Initializes a new {@link ExtAccountFolderField}.
     */
    public ExtAccountFolderField() {
        super();
    }

    @SuppressWarnings("unchecked")
    private Map<String, FolderInfo> getExternalAccountFolders(ServerSession session) throws OXException {
        CacheService cacheService = Services.getService(CacheService.class);
        Cache cache = cacheService.getCache(REGION_NAME);

        // Get from cache
        Object object = cache.get(cacheService.newCacheKey(session.getContextId(), session.getUserId()));
        if (object instanceof Map) {
            return (Map<String, FolderInfo>) object;
        }

        // Cache MISS
        ConcurrentHashMap<String, FolderInfo> tmp;
        {
            IMAPFolderStorage folderStorage = null;
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
            boolean doClose = true;
            try {
                {
                    Object param = session.getParameter(MailAccess.PARAM_MAIL_ACCESS);
                    if ((param instanceof MailAccess)) {
                        mailAccess = (MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage>) param;
                        doClose = false;
                    } else {
                        mailAccess = MailAccess.getInstance(session, MailAccount.DEFAULT_ID);
                        mailAccess.connect();
                    }
                }


                folderStorage = getImapFolderStorage(mailAccess);
                IMAPStore imapStore = folderStorage.getImapStore();
                Map<String, FolderInfo> folders = getExternalAccountFolders((IMAPFolder) imapStore.getDefaultFolder());
                if (null == folders) {
                    tmp = new ConcurrentHashMap<String, FolderInfo>(0, 0.9f, 1);
                } else {
                    tmp = new ConcurrentHashMap<>(folders.size(), 0.9f, 1);
                    tmp.putAll(folders);
                }
            } catch (MessagingException e) {
                throw folderStorage.handleMessagingException(e);
            } finally {
                if (doClose && (null != mailAccess)) {
                    mailAccess.close(true);
                }
            }
        }
        cache.put(cacheService.newCacheKey(session.getContextId(), session.getUserId()), tmp, false);
        return tmp;
    }

    @Override
    public Object renderJSON(AJAXRequestData requestData, Object value) {
        return value == null ? JSONObject.NULL : value;
    }

    @Override
    public Object getValue(FolderObject folder, ServerSession session) {
        String fullName = folder.getFullName();
        if (Strings.isEmpty(fullName)) {
            return JSONObject.NULL;
        }

        FullnameArgument fa = MailFolderUtility.prepareMailFolderParam(fullName);
        if (fa.getAccountId() != MailAccount.DEFAULT_ID) {
            return JSONObject.NULL;
        }

        try {
            FolderInfo folderInfo = getExternalAccountFolders(session).get(fa.getFullName());
            if (null == folderInfo) {
                return JSONObject.NULL;
            }

            JSONObject jResult = new JSONObject(2);
            if (null != folderInfo.alias) {
                jResult.put("alias", folderInfo.alias);
            }
            if (null != folderInfo.externalAccount) {
                jResult.put("externalAccount", folderInfo.externalAccount);
            }
            return jResult;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getColumnName() {
        return "com.openexchange.imap.extAccount";
    }

    @Override
    public int getColumnID() {
        return 3050;
    }

    @Override
    public List<Object> getValues(List<FolderObject> folder, ServerSession session) {
        return AdditionalFieldsUtils.bulk(this, folder, session);
    }

    // -------------------------------------------------------------------------------------------------------------

    private static final class FolderInfo {

        final String externalAccount;
        final String alias;

        FolderInfo(String externalAccount, String alias) {
            super();
            this.externalAccount = externalAccount;
            this.alias = alias;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(48);
            builder.append('{');
            builder.append("externalAccount=").append(externalAccount).append(", ");
            builder.append("alias=").append(alias);
            builder.append('}');
            return builder.toString();
        }
    }

    /**
     * Get the IMP folder storage from the specified mail access
     *
     * @param mailAccess
     * @return
     * @throws OXException
     */
    private IMAPFolderStorage getImapFolderStorage(final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException {
        IMailFolderStorage fstore = mailAccess.getFolderStorage();
        if (!(fstore instanceof IMAPFolderStorage)) {
            if (!(fstore instanceof IMailFolderStorageDelegator)) {
                throw MailExceptionCode.UNEXPECTED_ERROR.create("Unknown MAL implementation");
            }
            fstore = ((IMailFolderStorageDelegator) fstore).getDelegateFolderStorage();
            if (!(fstore instanceof IMAPFolderStorage)) {
                throw MailExceptionCode.UNEXPECTED_ERROR.create("Unknown MAL implementation");
            }
        }
        return (IMAPFolderStorage) fstore;
    }

    /**
     * Get the external account folders
     *
     * @param imapFolder
     * @return
     * @throws OXException
     */
    private Map<String, FolderInfo> getExternalAccountFolders(final IMAPFolder imapFolder) throws OXException {
        try {
            final Map<String, FolderInfo> results = new HashMap<String, FolderInfo>(8);
            imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

                @Override
                public Object doCommand(IMAPProtocol protocol) throws ProtocolException {
                    if (!protocol.hasCapability("METADATA")) {
                        // No support for METADATA extension
                        LOG.debug("METADATA not supported by IMAP server {}", protocol.getHost());
                        return null;
                    }

                    // Perform command
                    String command = "GETMETADATA * (/shared/vendor/vendor.dovecot/ext-account /shared/vendor/vendor.dovecot/alias)";
                    com.sun.mail.iap.Response[] r = IMAPCommandsCollection.performCommand(protocol, command);
                    int mlen = r.length - 1;
                    com.sun.mail.iap.Response response = r[mlen];

                    // Also consider NO responses here
                    if (response.isOK() || response.isNO()) {
                        String cMetadata = "METADATA";
                        for (int i = 0; i < mlen; i++) {
                            if (!(r[i] instanceof IMAPResponse)) {
                                continue;
                            }

                            IMAPResponse ir = (IMAPResponse) r[i];
                            if (ir.keyEquals(cMetadata)) {
                                // E.g.
                                // * METADATA INBOX/alias@orange.fr (/shared/vendor/vendor.dovecot/ext-account NIL /shared/vendor/vendor.dovecot/alias {15} alias@orange.fr)
                                // * METADATA INBOX/teppo.testaaja.in@gmail.com (/shared/vendor/vendor.dovecot/ext-account {27} teppo.testaaja.in@gmail.com /shared/vendor/vendor.dovecot/alias NIL)
                                // * METADATA INBOX/QUARANTAINE (/shared/vendor/vendor.dovecot/ext-account NIL /shared/vendor/vendor.dovecot/alias NIL)
                                String fullName = ir.readAtomString();
                                String[] metadatas = ir.readAtomStringList();
                                int length = metadatas.length;
                                int index = 0;

                                String extAccount = null;
                                String alias = null;

                                while (index < length) {
                                    String name = metadatas[index++];
                                    if ("/shared/vendor/vendor.dovecot/ext-account".equals(name)) {
                                        String value = metadatas[index++];
                                        if (!"NIL".equalsIgnoreCase(value)) {
                                            if (value.startsWith("{")) {
                                                extAccount = metadatas[index++];
                                            } else {
                                                extAccount = value;
                                            }
                                        }
                                    } else if ("/shared/vendor/vendor.dovecot/alias".equals(name)) {
                                        String value = metadatas[index++];
                                        if (!"NIL".equalsIgnoreCase(value)) {
                                            if (value.startsWith("{")) {
                                                alias = metadatas[index++];
                                            } else {
                                                alias = value;
                                            }
                                        }
                                    }
                                }

                                if (null != extAccount || null != alias) {
                                    results.put(fullName, new FolderInfo(extAccount, alias));
                                }
                                r[i] = null;
                            }
                        }

                        // Dispatch remaining untagged responses
                        protocol.notifyResponseHandlers(r);

                        return null;
                    } else if (response.isBAD()) {
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                        throw new BadCommandException(IMAPException.getFormattedMessage(
                            IMAPException.Code.PROTOCOL_ERROR,
                            command,
                            ImapUtility.appendCommandInfo(response.toString(), imapFolder)));
                    } else {
                        LogProperties.putProperty(LogProperties.Name.MAIL_COMMAND, prepareImapCommandForLogging(command));
                        protocol.handleResult(response);
                    }
                    return null;
                }
            });
            return results;
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    /**
     * Parse the metadata response from the IMAP server
     *
     * @param metadataResponse
     * @param results
     * @throws ParsingException
     */
    void parseMetadataResponse(com.sun.mail.iap.Response metadataResponse, Map<String, String> results) throws ParsingException {
        if (null == metadataResponse) {
            throw new ParsingException("Parse error in METADATA response: No opening parenthesized list found.");
        }
        int cnt = 0;
        {
            final String resp = metadataResponse.toString();
            if (isEmpty(resp)) {
                throw new ParsingException("Parse error in METADATA response: No opening parenthesized list found.");
            }
            int pos = -1;
            while ((pos = resp.indexOf('(', pos + 1)) > 0) {
                cnt++;
            }
        }
        if (cnt <= 0) {
            throw new ParsingException("Parse error in STATUS response: No opening parenthesized list found.");
        }
        // Read full name; decode the name (using RFC2060's modified UTF7)
        metadataResponse.skipSpaces();
        final String fullName = BASE64MailboxDecoder.decode(metadataResponse.readAtomString());

        // Read until opening parenthesis or EOF
        byte b = 0;
        do {
            b = metadataResponse.readByte();
            if (b == '(' && --cnt > 0) {
                b = metadataResponse.readByte();
            }
        } while (b != 0 && b != '(');
        if (0 == b || cnt > 0) {
            // EOF
            throw new ParsingException("Parse error in STATUS response: No opening parenthesized list found.");
        }

        // Parse parenthesized list
        // * METADATA INBOX.jane@barfoo.org (/shared/vendor/vendor.dovecot/ext-account {15} jane@barfoo.org)
        {
            String attr = metadataResponse.readAtom();
            if ("/shared/vendor/vendor.dovecot/ext-account".equals(attr)) {
                String literal = metadataResponse.readString();
                if (null != literal) {
                    String addr = metadataResponse.readAtomString();
                    if (null != addr) {
                        results.put(fullName, addr);
                        return;
                    }
                }
            }
        }
    }

}

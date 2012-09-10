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

package com.openexchange.zmal;

import static com.openexchange.mail.dataobjects.MailFolder.DEFAULT_FOLDER_ID;
import static com.openexchange.mail.utils.MailFolderUtility.isEmpty;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.Quota;
import com.openexchange.mail.Quota.Type;
import com.openexchange.mail.api.IMailFolderStorageEnhanced2;
import com.openexchange.mail.api.MailFolderStorage;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.session.Session;
import com.openexchange.zmal.config.ZmalConfig;
import com.openexchange.zmal.converters.ZFolderConverter;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.JSONElement;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.account.soap.SoapProvisioning.QuotaUsage;
import com.zimbra.cs.zclient.ZFolder;
import com.zimbra.cs.zclient.ZFolder.View;
import com.zimbra.cs.zclient.ZGrant;
import com.zimbra.cs.zclient.ZMailbox;
import com.zimbra.cs.zclient.ZMailbox.ZActionResult;
import com.zimbra.cs.zclient.ZSearchHit;
import com.zimbra.cs.zclient.ZSearchPagerResult;
import com.zimbra.cs.zclient.ZSearchParams;
import com.zimbra.cs.zclient.ZSearchResult;

/**
 * {@link ZmalFolderStorage} - The Zimbra mail folder storage implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ZmalFolderStorage extends MailFolderStorage implements IMailFolderStorageEnhanced2 {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(ZmalFolderStorage.class));

    /**
     * The max. length for a mailbox name
     */
    private static final int MAX_MAILBOX_NAME = 60;

    private final ZmalAccess zmalAccess;
    private final ZmalSoapPerformer performer;
    private final int accountId;
    private final Session session;
    private final Context ctx;
    private final ZmalConfig zmalConfig;
    private Character separator;
    private final String authToken;
    private final String url;


    /**
     * Initializes a new {@link ZmalFolderStorage}
     *
     * @param performer The SOAP performer
     * @param zmalAccess The Zimbra mail access
     * @param session The session providing needed user data
     * @throws OXException If context loading fails
     */
    public ZmalFolderStorage(final String authToken, final ZmalSoapPerformer performer, final ZmalAccess zmalAccess, final Session session) throws OXException {
        super();
        this.authToken = authToken;
        url = performer.getUrl();
        this.performer = performer;
        this.zmalAccess = zmalAccess;
        accountId = zmalAccess.getAccountId();
        this.session = session;
        ctx = ContextStorage.getStorageContext(session.getContextId());
        zmalConfig = zmalAccess.getZmalConfig();
    }

    private ZMailbox.Options newOptions() {
        final ZMailbox.Options options = new ZMailbox.Options(authToken, url);
        options.setRequestProtocol(performer.isUseJson() ? SoapProtocol.SoapJS : SoapProtocol.Soap11);
        options.setResponseProtocol(performer.isUseJson() ? SoapProtocol.SoapJS : SoapProtocol.Soap11);
        options.setUserAgent("Open-Xchange Http Client", "v6.22");
        return options;
    }

    private boolean isTrash(final String id, final ZMailbox mailbox) throws ServiceException {
        return mailbox.getTrash().getId().equals(id);
    }

    private ZFolder checkFolder(final String fullName, final ZMailbox mailbox) throws ServiceException, OXException {
        final ZFolder folder = mailbox.getFolderById(fullName);
        if (null == folder) {
            throw MailExceptionCode.FOLDER_NOT_FOUND.create(fullName);
        }
        return folder;
    }

    private List<String> getAllIds(final String folderId, final ZMailbox mailbox) throws ServiceException {
        // Search for all
        final ZSearchParams mSearchParams = new ZSearchParams("in:"+folderId);
        mSearchParams.setTypes(ZSearchParams.TYPE_MESSAGE);
        int mSearchPage = 0;
        final List<String> ids = new LinkedList<String>();
        boolean keegoing = true;
        while (keegoing) {
            final ZSearchPagerResult pager = mailbox.search(mSearchParams, mSearchPage++, false, false);
            final ZSearchResult result = pager.getResult();
            keegoing = result.hasMore();
            for (final Iterator<ZSearchHit> iterator = result.getHits().iterator(); iterator.hasNext();) {
                final ZSearchHit hit = iterator.next();
                ids.add(hit.getId());
            }
        }
        return ids;
    }

    private static String toCSV(final List<String> ids) {
        if (null == ids) {
            return null;
        }
        final int size = ids.size();
        if (0 == size) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(size << 1);
        sb.append(ids.get(0));
        for (int i = 1; i < size; i++) {
            sb.append(',').append(ids.get(i));
        }
        return sb.toString();
    }

    /**
     * Gets the associated session.
     *
     * @return The session
     */
    public Session getSession() {
        return session;
    }

    /**
     * Gets the associated context.
     *
     * @return The context
     */
    public Context getContext() {
        return ctx;
    }

    /**
     * Gets the associated Zimbra mail configuration.
     *
     * @return The Zimbra mail configuration
     */
    public ZmalConfig getZmalConfig() {
        return zmalConfig;
    }

    /**
     * Gets the Zimbra mail access.
     *
     * @return The Zimbra mail access
     */
    public ZmalAccess getZmalAccess() {
        return zmalAccess;
    }

    /**
     * Gets the associated account identifier.
     *
     * @return The account identifier
     */
    public int getAccountId() {
        return accountId;
    }

    @Override
    public int[] getTotalAndUnreadCounter(final String fullName) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            return new int[] { 0, 0 };
        }
        try {
            final ZMailbox mailbox = new ZMailbox(newOptions());
            final ZFolder folder = checkFolder(fullName, mailbox);
            return new int[] { folder.getMessageCount(), folder.getUnreadCount() };
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public int getUnreadCounter(final String fullName) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            return 0;
        }
        try {
            final ZMailbox mailbox = new ZMailbox(newOptions());
            final ZFolder folder = checkFolder(fullName, mailbox);
            return folder.getUnreadCount();
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public int getNewCounter(final String fullName) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            return 0;
        }
        return -1;
    }

    @Override
    public int getTotalCounter(final String fullName) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            return 0;
        }
        try {
            final ZMailbox mailbox = new ZMailbox(newOptions());
            final ZFolder folder = checkFolder(fullName, mailbox);
            return folder.getMessageCount();
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean exists(final String fullName) throws OXException {
        try {
            final ZMailbox mailbox = new ZMailbox(newOptions());
            final ZFolder folder = mailbox.getFolderById(fullName);
            return null != folder;
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public MailFolder getFolder(final String fullName) throws OXException {
        try {
            final ZMailbox mailbox = new ZMailbox(newOptions());
            final ZFolder folder = checkFolder(fullName, mailbox);
            return new ZFolderConverter(url, zmalConfig).convert(folder, mailbox);
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public MailFolder[] getSubfolders(final String parentFullName, final boolean all) throws OXException {
        try {
            final ZMailbox mailbox = new ZMailbox(newOptions());
            final ZFolder folder = checkFolder(parentFullName, mailbox);
            final List<ZFolder> subFolders = folder.getSubFolders();
            if (null == subFolders || subFolders.isEmpty()) {
                return EMPTY_PATH;
            }
            final ZFolderConverter converter = new ZFolderConverter(url, zmalConfig);
            final List<MailFolder> list = new ArrayList<MailFolder>(subFolders.size());
            for (final ZFolder subfolder : subFolders) {
                list.add(converter.convert(subfolder, mailbox));
            }
            return list.toArray(new MailFolder[0]);
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public MailFolder getRootFolder() throws OXException {
        return getFolder(MailFolder.DEFAULT_FOLDER_ID);
    }

    @Override
    public void checkDefaultFolders() throws OXException {
        // Nothing
    }

    @Override
    public String createFolder(final MailFolderDescription toCreate) throws OXException {
        final String name = toCreate.getName();
        if (isEmpty(name)) {
            throw MailExceptionCode.INVALID_FOLDER_NAME_EMPTY.create();
        }
        if (name.length() > MAX_MAILBOX_NAME) {
            throw MailExceptionCode.INVALID_FOLDER_NAME_TOO_LONG.create(Integer.valueOf(MAX_MAILBOX_NAME));
        }
        try {
            final ZMailbox mailbox = new ZMailbox(newOptions());
            final ZFolder createdFolder = mailbox.createFolder(toCreate.getParentFullname(), toCreate.getName(), View.message, null, toCreate.isSubscribed() ? "*" : "", null);
            return createdFolder.getId();
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String renameFolder(final String fullName, final String newName) throws OXException {
        if (isEmpty(newName)) {
            throw MailExceptionCode.INVALID_FOLDER_NAME_EMPTY.create();
        }
        if (newName.length() > MAX_MAILBOX_NAME) {
            throw MailExceptionCode.INVALID_FOLDER_NAME_TOO_LONG.create(Integer.valueOf(MAX_MAILBOX_NAME));
        }
        try {
            final ZMailbox mailbox = new ZMailbox(newOptions());
            final ZActionResult result = mailbox.renameFolder(fullName, newName);
            final String id = result.getIds();
            return null == id ? fullName : id;
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String moveFolder(final String fullName, final String newFullname) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName) || DEFAULT_FOLDER_ID.equals(newFullname)) {
            throw ZmalException.create(ZmalException.Code.NO_ROOT_MOVE, zmalConfig, session, new Object[0]);
        }
        try {
            final ZMailbox mailbox = new ZMailbox(newOptions());
            final ZActionResult result = mailbox.moveFolder(fullName, newFullname);
            final String id = result.getIds();
            return null == id ? fullName : id;
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String updateFolder(final String fullName, final MailFolderDescription toUpdate) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw MailExceptionCode.NO_ROOT_FOLDER_MODIFY_DELETE.create();
        }
        try {
            final ZMailbox mailbox = new ZMailbox(newOptions());
            List<ZGrant> acl = null;
            if (toUpdate.containsPermissions()) {
                final MailPermission[] permissions = toUpdate.getPermissions();
                if (null != permissions && permissions.length > 0) {
                    acl = new ArrayList<ZGrant>(permissions.length);
                    final String name = MailConstants.E_GRANT;
                    for (final MailPermission p : permissions) {
                        final Element element = performer.isUseJson() ? new JSONElement(name) : new XMLElement(name);
                        // TODO: Compose GRant element
                        acl.add(new ZGrant(element));
                    }
                }
            }
            final ZActionResult result = mailbox.updateFolder(fullName, null, null, null, toUpdate.isSubscribed() ? "*" : "", acl);
            final String id = result.getIds();
            return null == id ? fullName : id;
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String deleteFolder(final String fullName, final boolean hardDelete) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw MailExceptionCode.NO_ROOT_FOLDER_MODIFY_DELETE.create();
        }
        try {
            final ZMailbox mailbox = new ZMailbox(newOptions());
            if (hardDelete || isTrash(fullName, mailbox)) {
                mailbox.deleteFolder(fullName);
                return fullName;
            }
            final ZActionResult result = mailbox.trashFolder(fullName);
            final String id = result.getIds();
            return null == id ? fullName : id;
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void clearFolder(final String fullName, final boolean hardDelete) throws OXException {
        if (DEFAULT_FOLDER_ID.equals(fullName)) {
            throw MailExceptionCode.NO_ROOT_FOLDER_MODIFY_DELETE.create();
        }
        try {
            final ZMailbox mailbox = new ZMailbox(newOptions());
            if (hardDelete || isTrash(fullName, mailbox)) {
                mailbox.emptyFolder(toCSV(getAllIds(fullName, mailbox)), true);
            }
            mailbox.moveMessage(toCSV(getAllIds(fullName, mailbox)), mailbox.getTrash().getId());
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public MailFolder[] getPath2DefaultFolder(final String fullName) throws OXException {
        if (fullName.equals(DEFAULT_FOLDER_ID)) {
            return EMPTY_PATH;
        }
        try {
            final ZMailbox mailbox = new ZMailbox(newOptions());
            ZFolder folder = checkFolder(fullName, mailbox);
            final List<MailFolder> path = new LinkedList<MailFolder>();
            final ZFolderConverter converter = new ZFolderConverter(url, zmalConfig);
            path.add(converter.convert(folder, mailbox));
            while (null != (folder = mailbox.getFolderById(folder.getParentId())).getParentId()) {
                path.add(converter.convert(folder, mailbox));
            }
            return path.toArray(new MailFolder[path.size()]);
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String getDefaultFolderPrefix() throws OXException {
        return "";
    }

    @Override
    public String getConfirmedHamFolder() throws OXException {
        return null;
    }

    @Override
    public String getConfirmedSpamFolder() throws OXException {
        return null;
    }

    @Override
    public String getDraftsFolder() throws OXException {
        try {
            return new ZMailbox(newOptions()).getDrafts().getId();
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String getSentFolder() throws OXException {
        try {
            return new ZMailbox(newOptions()).getSent().getId();
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String getSpamFolder() throws OXException {
        try {
            return new ZMailbox(newOptions()).getSpam().getId();
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String getTrashFolder() throws OXException {
        try {
            return new ZMailbox(newOptions()).getTrash().getId();
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void releaseResources() throws ZmalException {
        // Nothing to release
    }

    @Override
    public com.openexchange.mail.Quota[] getQuotas(final String fullName, final com.openexchange.mail.Quota.Type[] types) throws OXException {
        try {
            final SoapProvisioning.Options options = new SoapProvisioning.Options(authToken, performer.getAdminUrl());
            final SoapProvisioning provisioning = new SoapProvisioning(options);
            final List<QuotaUsage> quotaUsages = provisioning.getQuotaUsage(performer.getServer());
            final List<com.openexchange.mail.Quota> ret = new ArrayList<Quota>(quotaUsages.size());
            for (final QuotaUsage quotaUsage : quotaUsages) {
                ret.add(new Quota(quotaUsage.getLimit(), quotaUsage.getUsed(), Type.valueOf(quotaUsage.getName())));
            }
            return ret.toArray(new com.openexchange.mail.Quota[0]);
        } catch (final ServiceException e) {
            throw ZmalException.create(ZmalException.Code.SERVICE_ERROR, e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /*
     * ++++++++++++++++++ Helper methods ++++++++++++++++++
     */

}

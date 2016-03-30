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

package com.openexchange.freebusy.publisher.ews.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import com.microsoft.schemas.exchange.services._2006.types.BaseFolderType;
import com.microsoft.schemas.exchange.services._2006.types.DefaultShapeNamesType;
import com.microsoft.schemas.exchange.services._2006.types.DisposalType;
import com.microsoft.schemas.exchange.services._2006.types.DistinguishedFolderIdNameType;
import com.microsoft.schemas.exchange.services._2006.types.ExchangeVersionType;
import com.microsoft.schemas.exchange.services._2006.types.FolderIdType;
import com.microsoft.schemas.exchange.services._2006.types.FolderQueryTraversalType;
import com.microsoft.schemas.exchange.services._2006.types.ItemIdType;
import com.microsoft.schemas.exchange.services._2006.types.ItemQueryTraversalType;
import com.microsoft.schemas.exchange.services._2006.types.ItemType;
import com.microsoft.schemas.exchange.services._2006.types.MessageDispositionType;
import com.microsoft.schemas.exchange.services._2006.types.PostItemType;
import com.openexchange.context.ContextService;
import com.openexchange.ews.EWSFactoryService;
import com.openexchange.ews.ExchangeWebService;
import com.openexchange.exception.OXException;
import com.openexchange.freebusy.FreeBusyData;
import com.openexchange.freebusy.provider.InternalFreeBusyProvider;
import com.openexchange.freebusy.publisher.ews.Tools;
import com.openexchange.freebusy.publisher.ews.lookup.LdapLookup;
import com.openexchange.freebusy.publisher.ews.lookup.Lookup;
import com.openexchange.freebusy.publisher.ews.lookup.StaticLookup;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.session.Session;
import com.openexchange.user.UserService;

/**
 * {@link Publisher}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Publisher implements Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Publisher.class);

    private final Map<String, FolderIdType> freeBusySubfolders;
    private final Session session;
    private final Lookup lookup;
    private final ExchangeWebService ews;

    /**
     * Initializes a new {@link Publisher}.
     *
     * @param session The session to use when retrieving the free/busy data
     * @param lookup The lookup
     * @param ews The Exchange web service
     */
    public Publisher(Session session, Lookup lookup, ExchangeWebService ews) {
        super();
        this.freeBusySubfolders = new HashMap<String, FolderIdType>();
        this.session = session;
        this.lookup = lookup;
        this.ews = ews;
    }

    /**
     * Initializes a new {@link Publisher}.
     *
     * @throws OXException
     */
    public Publisher() throws OXException {
        this(createSession(), createLookup(), createWebService());
    }

    @Override
    public void run() {
        long start = new Date().getTime();
        LOG.info("Publication cycle starting.");
        try {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date from = calendar.getTime();
            calendar.add(Calendar.MONTH, Tools.getConfigPropertyInt("com.openexchange.freebusy.publisher.ews.publishMonths", 3));
            Date until = calendar.getTime();
            int chunkSize = Tools.getConfigPropertyInt("com.openexchange.freebusy.publisher.ews.chunkSize", 50);
            int[] userIDs = getUserIDs();
            for (int i = 0; i < userIDs.length; i += chunkSize) {
                int[] userIDChunk = Arrays.copyOfRange(userIDs, i, i + Math.min(userIDs.length - i, chunkSize));
                this.publish(getUsers(userIDChunk), from, until);
            }
        } catch (OXException e) {
            LOG.error("Unexpected error publising free/busy data", e);
        }
        LOG.info("Publication cycle finished, {}s elapsed.", ((System.currentTimeMillis() - start) / 1000));
    }

    /**
     * Publishes the free/busy data of the supplied users.
     *
     * @param users The users whose data should be published
     * @param from The lower (inclusive) limit of the requested time-range
     * @param until The upper (exclusive) limit of the requested time-range
     * @throws OXException
     */
    public void publish(User[] users, Date from, Date until) throws OXException {
        if (null == users || 0 == users.length) {
            return;
        }
        /*
         * lookup legacyExchangeDNs
         */
        String[] legacyExchangeDNs = lookup.getLegacyExchangeDNs(users);
        /*
         * delete existing messages
         */
        deleteFreeBusyMessages(legacyExchangeDNs);
        /*
         * generate updated messages
         */
        Map<FolderIdType, List<PostItemType>> messagesPerFolder = new HashMap<FolderIdType, List<PostItemType>>();
        for (int i = 0; i < users.length; i++) {
            String legacyExchangeDN = legacyExchangeDNs[i];
            if (null == legacyExchangeDN) {
                LOG.debug("No legacyExchangeDN for user {}, skipping.", users[i].getLoginInfo());
                continue;
            } else {
                LOG.trace("Using {} as legacyExchangeDN for user {}.", legacyExchangeDN, users[i].getLoginInfo());
            }
            FreeBusyData freeBusyData = getFreeBusyInformation(users[i], from, until);
            if (null == freeBusyData) {
                LOG.debug("No free/busy data for user {}, skipping.", users[i].getLoginInfo());
                continue;
            } else {
                LOG.trace("Got the following free/busy data for user {}:\n{}", users[i].getLoginInfo(), freeBusyData);
            }
            FolderIdType folderId = getFreeBusySubfolderId(legacyExchangeDN);
            if (null == folderId) {
                LOG.warn("No free/busy subfolder for {}, skipping.", legacyExchangeDN);
                continue;
            } else {
                LOG.trace("Using free/busy subfolder {} for user {}:\n{}", folderId, users[i].getLoginInfo(), freeBusyData);
            }
            if (null == messagesPerFolder.get(folderId)) {
                messagesPerFolder.put(folderId, new ArrayList<PostItemType>());
            }
            PostItemType postItem = new FreeBusyMessage(freeBusyData).createPostItem(legacyExchangeDN, getUSER(legacyExchangeDN));
            messagesPerFolder.get(folderId).add(postItem);
        }
        /*
         * upload messages
         */
        for (Entry<FolderIdType, List<PostItemType>> entry : messagesPerFolder.entrySet()) {
            ews.getItems().createItems(entry.getKey(), entry.getValue(), MessageDispositionType.SAVE_ONLY);
            LOG.debug("Successfully published {} free/busy messages.", entry.getValue().size());
        }
    }

    private void deleteFreeBusyMessages(String[] legacyExchangeDNs) throws OXException {
        List<ItemIdType> freeBusyMessages = getFreeBusyMessages(legacyExchangeDNs);
        if (null != freeBusyMessages && 0 < freeBusyMessages.size()) {
            ews.getItems().deleteItems(freeBusyMessages, DisposalType.HARD_DELETE);
        }
    }

    private List<ItemIdType> getFreeBusyMessages(String[] legacyExchangeDNs) throws OXException {
        Map<FolderIdType, List<String>> subjectsPerFolder = getSubjectsPerFolder(legacyExchangeDNs);
        List<ItemIdType> freeBusyMessages = new ArrayList<ItemIdType>();
        for (Entry<FolderIdType, List<String>> entry : subjectsPerFolder.entrySet()) {
            List<ItemType> items = ews.getItems().findItemsBySubject(entry.getKey(), entry.getValue(),
                ItemQueryTraversalType.SHALLOW, DefaultShapeNamesType.ID_ONLY);
            if (null != items && 0 < items.size()) {
                for (ItemType item : items) {
                    freeBusyMessages.add(item.getItemId());
                }
            }
        }
        return freeBusyMessages;
    }

    private Map<FolderIdType, List<String>> getSubjectsPerFolder(String[] legacyExchangeDNs) throws OXException {
        Map<FolderIdType, List<String>> subjectsPerFolder = new HashMap<FolderIdType, List<String>>();
        for (String legacyExchangeDN : legacyExchangeDNs) {
            if (null != legacyExchangeDN) {
                FolderIdType folder = getFreeBusySubfolderId(legacyExchangeDN);
                String subject = getUSER(legacyExchangeDN);
                if (false == subjectsPerFolder.containsKey(folder)) {
                    subjectsPerFolder.put(folder, new ArrayList<String>());
                }
                subjectsPerFolder.get(folder).add(subject);
            }
        }
        return subjectsPerFolder;
    }

    private FreeBusyData getFreeBusyInformation(User user, Date from, Date until) throws OXException {
        return EWSFreeBusyPublisherLookup.getService(InternalFreeBusyProvider.class).getUserFreeBusy(
            this.session, user.getId(), from, until);
    }

    private FolderIdType getFreeBusySubfolderId(String legacyExchangeDN) throws OXException {
        String ex = getEX(legacyExchangeDN);
        if (this.freeBusySubfolders.containsKey(ex)) {
            return freeBusySubfolders.get(ex);
        } else {
            FolderIdType subfolder = this.discoverFreeBusySubfolder(ex);
            freeBusySubfolders.put(ex, subfolder);
            return subfolder;
        }
    }

    private FolderIdType discoverFreeBusySubfolder(String folderName) throws OXException {
        BaseFolderType publicFoldersRoot = ews.getFolders().getFolder(
            DistinguishedFolderIdNameType.PUBLICFOLDERSROOT, DefaultShapeNamesType.ALL_PROPERTIES);
        BaseFolderType nonIpmSubtree = ews.getFolders().findFolderByName(
            publicFoldersRoot.getParentFolderId(), "NON_IPM_SUBTREE", FolderQueryTraversalType.SHALLOW, DefaultShapeNamesType.ID_ONLY);
        BaseFolderType schedulePlusFreeBusy = ews.getFolders().findFolderByName(
            nonIpmSubtree.getFolderId(), "SCHEDULE+ FREE BUSY", FolderQueryTraversalType.SHALLOW, DefaultShapeNamesType.ID_ONLY);
        BaseFolderType subfolder = ews.getFolders().findFolderByName(
            schedulePlusFreeBusy.getFolderId(), folderName, FolderQueryTraversalType.SHALLOW, DefaultShapeNamesType.ID_ONLY);
        return subfolder.getFolderId();
    }

    private int[] getUserIDs() throws OXException {
        return EWSFreeBusyPublisherLookup.getService(UserService.class).listAllUser(getContext());
    }

    private User[] getUsers(int[] userIDs) throws OXException {
        return EWSFreeBusyPublisherLookup.getService(UserService.class).getUser(getContext(), userIDs);
    }

    private Context getContext() throws OXException {
        return EWSFreeBusyPublisherLookup.getService(ContextService.class).getContext(session.getContextId());
    }

    private static String getEX(String legacyExchangeDN) {
        int idx = legacyExchangeDN.toLowerCase().indexOf("/cn");
        String administrativeGroup = legacyExchangeDN.substring(0, idx);
        return "EX:" + administrativeGroup;
    }

    private static String getUSER(String legacyExchangeDN) {
        int idx = legacyExchangeDN.toLowerCase().indexOf("/cn");
        String recipient = legacyExchangeDN.substring(idx);
        return "USER-" + recipient;
    }

    private static ExchangeWebService createWebService() throws OXException {
        ExchangeWebService ews = EWSFreeBusyPublisherLookup.getService(EWSFactoryService.class).create(
            Tools.getConfigProperty("com.openexchange.freebusy.publisher.ews.url"),
            Tools.getConfigProperty("com.openexchange.freebusy.publisher.ews.userName"),
            Tools.getConfigProperty("com.openexchange.freebusy.publisher.ews.password"));
        ews.getConfig().setExchangeVersion(ExchangeVersionType.valueOf(ExchangeVersionType.class,
            Tools.getConfigProperty("com.openexchange.freebusy.publisher.ews.exchangeVersion", "EXCHANGE_2010").toUpperCase()));
        ews.getConfig().setIgnoreHostnameValidation(Tools.getConfigPropertyBool(
            "com.openexchange.freebusy.publisher.ews.skipHostVerification", false));
        ews.getConfig().setTrustAllCerts(Tools.getConfigPropertyBool("com.openexchange.freebusy.publisher.ews.trustAllCerts", false));
        return ews;
    }

    private static Session createSession() throws OXException {
        return new PublishSession(Tools.getConfigPropertyInt("com.openexchange.freebusy.publisher.ews.contextID"),
            Tools.getConfigPropertyInt("com.openexchange.freebusy.publisher.ews.userID"));
    }

    private static Lookup createLookup() throws OXException {
        if ("ldap".equalsIgnoreCase(Tools.getConfigProperty("com.openexchange.freebusy.publisher.ews.lookup"))) {
            return new LdapLookup(
                Tools.getConfigProperty("com.openexchange.freebusy.publisher.ews.lookup.ldap.uri"),
                Tools.getConfigProperty("com.openexchange.freebusy.publisher.ews.lookup.ldap.filter"),
                Tools.getConfigProperty("com.openexchange.freebusy.publisher.ews.lookup.ldap.baseDN", null),
                Tools.getConfigProperty("com.openexchange.freebusy.publisher.ews.lookup.ldap.bindDN"),
                Tools.getConfigProperty("com.openexchange.freebusy.publisher.ews.lookup.ldap.bindPW"),
                Tools.getConfigPropertyBool("com.openexchange.freebusy.publisher.ews.trustAllCerts", false)
            );
        } else {
            return new StaticLookup(Tools.getConfigProperty("com.openexchange.freebusy.publisher.ews.lookup.static"));
        }
    }

}

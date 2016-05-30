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

package com.openexchange.imap.cache;

import static com.openexchange.imap.IMAPMessageStorage.allowSORTDISPLAY;
import java.util.Map;
import javax.mail.MessagingException;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPCapabilities;
import com.openexchange.imap.acl.ACLExtension;
import com.openexchange.imap.acl.ACLExtensionFactory;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.services.Services;
import com.openexchange.mail.cache.SessionMailCache;
import com.openexchange.mail.cache.SessionMailCacheEntry;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.session.Session;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link CapabilitiesCache} - A cache to check for capabilities for a certain IMAP server.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CapabilitiesCache {

    /**
     * Initializes a new {@link CapabilitiesCache}.
     */
    private CapabilitiesCache() {
        super();
    }

    public static final class CapabilitiesResponse {

        private final ACLExtension aclExtension;

        private final IMAPCapabilities imapCapabilities;

        private final Map<String, String> map;

        CapabilitiesResponse(final ACLExtension aclExtension, final IMAPCapabilities imapCapabilities, final Map<String, String> map) {
            super();
            this.aclExtension = aclExtension;
            this.imapCapabilities = imapCapabilities;
            this.map = map;
        }

        /**
         * Gets the ACL extension.
         *
         * @return The ACL extension
         */
        public ACLExtension getAclExtension() {
            return aclExtension;
        }

        /**
         * Gets the IMAP capabilities.
         *
         * @return The IMAP capabilities
         */
        public IMAPCapabilities getImapCapabilities() {
            return imapCapabilities;
        }

        /**
         * Gets the map.
         *
         * @return The map
         */
        public Map<String, String> getMap() {
            return map;
        }

    }

    private static final class CapsCacheEntry implements SessionMailCacheEntry<CapabilitiesResponse> {

        private final int user;

        private volatile CapabilitiesResponse capRes;

        private volatile CacheKey key;

        public CapsCacheEntry(final int user) {
            this(null, user);
        }

        public CapsCacheEntry(final CapabilitiesResponse capRes, final int user) {
            super();
            this.user = user;
            this.capRes = capRes;
        }

        private CacheKey getKeyInternal() {
            CacheKey tmp = key;
            if (null == tmp) {
                final CacheService service = Services.getService(CacheService.class);
                key = tmp = null == service ? null : service.newCacheKey(MailCacheCode.CAPS.getCode(), user);
            }
            return tmp;
        }

        @Override
        public CacheKey getKey() {
            return getKeyInternal();
        }

        @Override
        public CapabilitiesResponse getValue() {
            return capRes;
        }

        @Override
        public void setValue(final CapabilitiesResponse value) {
            capRes = value;
        }

        @Override
        public Class<CapabilitiesResponse> getEntryClass() {
            return CapabilitiesResponse.class;
        }
    }

    /**
     * Gets cached capabilities for given IMAP store.
     *
     * @param imapStore The IMAP store
     * @param imapConfig The IMAP configuration
     * @param session The session providing the session-bound cache
     * @param accontId The account ID
     * @return The cached capabilities or <code>null</code>
     * @throws MessagingException If <code>MYRIGHTS</code> command fails
     */
    public static CapabilitiesResponse getCapabilitiesResponse(final IMAPStore imapStore, final IMAPConfig imapConfig, final Session session, final int accontId) throws MessagingException {
        final CapsCacheEntry entry = new CapsCacheEntry(session.getUserId());
        final SessionMailCache mailCache = SessionMailCache.getInstance(session, accontId);
        mailCache.get(entry);
        if (null == entry.getValue()) {
            final IMAPCapabilities imapCaps = new IMAPCapabilities();
            /*
             * Get as map
             */
            @SuppressWarnings("unchecked") final Map<String, String> map = imapStore.getCapabilities();
            imapCaps.setACL(map.containsKey(IMAPCapabilities.CAP_ACL));
            imapCaps.setThreadReferences(map.containsKey(IMAPCapabilities.CAP_THREAD_REFERENCES));
            imapCaps.setThreadOrderedSubject(map.containsKey(IMAPCapabilities.CAP_THREAD_ORDEREDSUBJECT));
            imapCaps.setQuota(map.containsKey(IMAPCapabilities.CAP_QUOTA));
            boolean hasSort = map.containsKey(IMAPCapabilities.CAP_SORT);
            imapCaps.setSort(hasSort);
            imapCaps.setIMAP4(map.containsKey(IMAPCapabilities.CAP_IMAP4));
            imapCaps.setIMAP4rev1(map.containsKey(IMAPCapabilities.CAP_IMAP4_REV1));
            imapCaps.setUIDPlus(map.containsKey(IMAPCapabilities.CAP_UIDPLUS));
            imapCaps.setNamespace(map.containsKey(IMAPCapabilities.CAP_NAMESPACE));
            imapCaps.setIdle(map.containsKey(IMAPCapabilities.CAP_IDLE));
            imapCaps.setChildren(map.containsKey(IMAPCapabilities.CAP_CHILDREN));
            imapCaps.setHasSubscription(!MailProperties.getInstance().isIgnoreSubscription());
            imapCaps.setFileNameSearch(map.containsKey(IMAPCapabilities.CAP_SEARCH_FILENAME));
            if (hasSort && imapConfig.getIMAPProperties().isImapSort()) {
                // IMAP sort supported & enabled
                try {
                    imapCaps.setSortDisplay(map.containsKey(IMAPCapabilities.CAP_SORT_DISPLAY) && allowSORTDISPLAY(session, accontId));
                } catch (OXException e) {
                    throw new MessagingException("Failed to determine if SORT-DISPLAY extension is allowed", e);
                }
            } else {
                // The in-memory sorting does sort with primary respect to display name, the actual address
                imapCaps.setSortDisplay(true);
            }
            /*
             * ACL extension
             */
            final ACLExtension aclExtension = ACLExtensionFactory.getInstance().getACLExtension(map, imapConfig);
            /*
             * Set value
             */
            entry.setValue(new CapabilitiesResponse(aclExtension, imapCaps, map));
            mailCache.put(entry);
        }
        return entry.getValue();
    }

    /**
     * Removes cached capabilities.
     *
     * @param user The user identifier
     * @param session The session providing the session-bound cache
     * @param accontId The account ID
     */
    public static void removeCachedRights(final int user, final Session session, final int accontId) {
        SessionMailCache.getInstance(session, accontId).remove(new CapsCacheEntry(user));
    }

}

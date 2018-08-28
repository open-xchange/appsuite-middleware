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

package com.openexchange.contact.picture.finder;

import static com.openexchange.java.Strings.isNotEmpty;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheKeyService;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.contact.picture.ContactPicture;
import com.openexchange.contact.picture.ContactPictureRequestData;
import com.openexchange.contact.picture.UnmodifiableContactPictureRequestData;
import com.openexchange.event.CommonEvent;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.session.Session;

/**
 * {@link CacheAwareContactFinder} - Implements caching for a {@link ContactPictureFinder}. <b>ONLY</b> the email of a contact is cacheable
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public abstract class CacheAwareContactFinder implements ContactPictureFinder, EventHandler {

    private final static String TOPIC = "com/openexchange/groupware/contact/update";

    private final Cache<CacheKey, String> cache;

    private final CacheKeyService cacheKeyService;

    /**
     * Initializes a new {@link CacheAwareContactFinder}.
     * 
     * @param leanService The {@link LeanConfigurationService}
     * @param cacheKeyService The {@link CacheKeyService}
     */
    public CacheAwareContactFinder(LeanConfigurationService leanService, CacheKeyService cacheKeyService) {
        super();
        this.cache = CacheBuilder.newBuilder().expireAfterWrite(leanService.getIntProperty(ContactPictureProperties.CACHE_DURATION), TimeUnit.MINUTES).maximumSize(leanService.getIntProperty(ContactPictureProperties.CACHE_SIZE)).build();
        this.cacheKeyService = cacheKeyService;
    }

    @Override
    public ContactPicture getPicture(Session session, UnmodifiableContactPictureRequestData original, ContactPictureRequestData modified, boolean onlyETag) throws OXException {
        if (onlyETag && original.hasEmail()) {
            for (Iterator<String> iterator = original.getEmails().iterator(); iterator.hasNext();) {
                String eTag = cache.getIfPresent(cacheKeyService.newCacheKey(session.getContextId(), String.valueOf(session.getUserId()), iterator.next()));
                if (isNotEmpty(eTag)) {
                    return new ContactPicture(eTag);
                }
            }
        }

        return null;
    }

    /**
     * Puts an eTag in the cache.
     * 
     * @param email The email of the contact
     * @param eTag The eTag
     * @return <code>true</code> if the eTag was put into the cache, <code>false</code> otherwise
     */
    protected boolean put(Session session, String email, String eTag) {
        if (isNotEmpty(eTag) && isNotEmpty(email)) {
            CacheKey cacheKey = cacheKeyService.newCacheKey(session.getContextId(), String.valueOf(session.getUserId()), email);
            cache.put(cacheKey, eTag);
            return true;
        }
        return false;
    }

    @Override
    public void handleEvent(Event event) {
        // --- Check basic conditions
        if (null == event || false == TOPIC.equals(event.getTopic())) {
            return;
        }
        Object property = event.getProperty(CommonEvent.EVENT_KEY);
        if (null == property || false == CommonEvent.class.isAssignableFrom(property.getClass())) {
            return;
        }
        CommonEvent commonEvent = (CommonEvent) property;

        Object actionObj = commonEvent.getActionObj();
        Object oldObj = commonEvent.getOldObj();
        if (null == actionObj || false == Contact.class.isAssignableFrom(actionObj.getClass()) || null == oldObj || false == Contact.class.isAssignableFrom(oldObj.getClass())) {
            return;
        }

        Contact updated = (Contact) actionObj;
        Contact original = (Contact) oldObj;
        // ---

        if (isAboutPictureChange(original, updated) || false == FinderUtil.checkEmails(original, updated)) {
            cache.invalidate(cacheKeyService.newCacheKey(commonEvent.getContextId(), String.valueOf(commonEvent.getUserId()), original.getEmail1()));
            cache.invalidate(cacheKeyService.newCacheKey(commonEvent.getContextId(), String.valueOf(commonEvent.getUserId()), original.getEmail2()));
            cache.invalidate(cacheKeyService.newCacheKey(commonEvent.getContextId(), String.valueOf(commonEvent.getUserId()), original.getEmail3()));
        }
    }

    private boolean isAboutPictureChange(Contact original, Contact updated) {
        if (null == original.getImage1() && null == updated.getImage1()) {
            // Nothing changed
            return false;
        }
        // Check bytes
        return Arrays.equals(original.getImage1(), updated.getImage1());
    }

}

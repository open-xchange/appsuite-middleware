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

package com.openexchange.contact.picture.impl;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Properties;
import com.openexchange.contact.picture.ContactPicture;
import com.openexchange.contact.picture.ContactPictureRequestData;
import com.openexchange.contact.picture.ContactPictureService;
import com.openexchange.event.CommonEvent;
import com.openexchange.groupware.container.Contact;
import com.openexchange.session.Session;

/**
 * {@link ContactPictureCachingService}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class ContactPictureCachingService implements ContactPictureService, EventHandler, Reloadable {

    private final static Logger LOGGER = LoggerFactory.getLogger(ContactPictureCachingService.class);

    private final static String TOPIC = "com/openexchange/groupware/contact/update";

    private final static AtomicReference<Cache<ContactPictureRequestData, ContactPicture>> REF = new AtomicReference<Cache<ContactPictureRequestData, ContactPicture>>();

    private final LeanConfigurationService leanService;

    private final ContactPictureService delegate;

    /**
     * Initializes a new {@link ContactPictureCachingService}.
     * 
     * @param delegate The service to delegate the actual request to
     * @param leanService The {@link LeanConfigurationService}
     * 
     */
    public ContactPictureCachingService(ContactPictureService delegate, LeanConfigurationService leanService) {
        super();
        this.delegate = delegate;
        this.leanService = leanService;
        loadCache(leanService);
    }

    @Override
    public ContactPicture getPicture(Session session, ContactPictureRequestData data) {
        try {
            Cache<ContactPictureRequestData, ContactPicture> cache = REF.get();
            if (null == cache) {
                return delegate.getPicture(session, data);
            }
            return cache.get(data, () -> {
                return delegate.getPicture(session, data);
            });
        } catch (Exception e) {
            LOGGER.warn("Exception while receiving contact picture", e);
        }
        return ContactPicture.FALLBACK_PICTURE;
    }

    @Override
    public void handleEvent(Event event) {
        // --- Check basic conditions
        Cache<ContactPictureRequestData, ContactPicture> cache = REF.get();
        if (null == cache || null == event || false == TOPIC.equals(event.getTopic())) {
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
        if (false == isAboutPictureChange(original, updated)) {
            return;
        }
        // ---

        // Invalidate cache
        Map<Integer, Set<Integer>> affectedUsersWithFolder = commonEvent.getAffectedUsersWithFolder();
        for (Iterator<ContactPictureRequestData> iterator = cache.asMap().keySet().iterator(); iterator.hasNext();) {
            ContactPictureRequestData next = iterator.next();
            if (next.hasUser() && affectedUsersWithFolder.containsKey(next.getUserId())) {
                cache.invalidate(next);
            }
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

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        loadCache(leanService);
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties(Properties.getPropertyNames(ContactPictureProperties.values()));
    }

    private void loadCache(LeanConfigurationService leanService) {
        Cache<ContactPictureRequestData, ContactPicture> toSet;
        long cacheDuration = leanService.getLongProperty(ContactPictureProperties.CACHE_DURATION);
        if (cacheDuration > 0) {
            toSet = CacheBuilder.newBuilder().expireAfterWrite(cacheDuration, TimeUnit.SECONDS).maximumSize(leanService.getIntProperty(ContactPictureProperties.CACHE_SIZE)).build();
        } else {
            toSet = null;
        }
        REF.set(toSet);
    }
}

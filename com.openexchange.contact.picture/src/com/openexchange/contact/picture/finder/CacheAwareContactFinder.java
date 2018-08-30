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

import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.java.Autoboxing.l;
import static com.openexchange.java.Strings.isEmpty;
import java.util.concurrent.TimeUnit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheKeyService;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.contact.picture.ContactPicture;
import com.openexchange.contact.picture.ContactPictureRequestData;
import com.openexchange.contact.picture.UnmodifiableContactPictureRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;

/**
 * {@link CacheAwareContactFinder} - Implements caching for a {@link ContactPictureFinder}.
 * This class should be used for services where getting the picture can be considered expensive, e.g. external resources
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public abstract class CacheAwareContactFinder implements ContactPictureFinder {

    private final Cache<CacheKey, FetchResult> cache;

    private final CacheKeyService cacheKeyService;

    /**
     * Initializes a new {@link CacheAwareContactFinder}.
     *
     * @param leanConfigurationService The {@link LeanConfigurationService}
     * @param cacheKeyService The {@link CacheKeyService}
     * @throws OXException
     *
     */
    public CacheAwareContactFinder(LeanConfigurationService leanConfigurationService, CacheKeyService cacheKeyService) throws OXException {
        if(leanConfigurationService == null) {
            throw ServiceExceptionCode.absentService(LeanConfigurationService.class);
        }

        if(cacheKeyService == null) {
            throw ServiceExceptionCode.absentService(CacheKeyService.class);
        }

        long duration = l(ContactPictureProperties.CACHE_DURATION.getDefaultValue(Long.class));
        int maximumSize = i(ContactPictureProperties.CACHE_SIZE.getDefaultValue(Integer.class));
        duration = leanConfigurationService.getLongProperty(ContactPictureProperties.CACHE_DURATION);
        maximumSize = leanConfigurationService.getIntProperty(ContactPictureProperties.CACHE_SIZE);
        this.cacheKeyService = cacheKeyService;
        this.cache = CacheBuilder.newBuilder().expireAfterWrite(duration < 1 ? 1 : duration, TimeUnit.MINUTES).maximumSize(maximumSize).build();
    }

    @Override
    public ContactPicture getPicture(Session session, UnmodifiableContactPictureRequestData original, ContactPictureRequestData modified, boolean onlyETag) throws OXException {
        FetchResult result = cache.getIfPresent(generateCacheKey(session, original, modified));
        if (null != result) {
            // The request was already performed, now get the cached data
            if (Status.MISS.equals(result.result)) {
                return null;
            } else if (onlyETag) {
                return new ContactPicture(result.eTag);
            }
        }

        // Cache miss, get the picture
        ContactPicture picture = fetchPicture(session, original, modified, onlyETag);
        put(generateCacheKey(session, original, modified), null != picture && picture.containsContactPicture() ? picture.getETag() : null);

        return picture;
    }

    /**
     * Puts an eTag in the cache.
     *
     * @param key The {@link CacheKey}
     * @param eTag The eTag
     */
    protected void put(CacheKey key, String eTag) {
        if (null != key) {
            if (isEmpty(eTag)) {
                cache.put(key, new FetchResult(null, Status.MISS));
            } else {
                cache.put(key, new FetchResult(eTag, Status.FOUND));
            }
        }
    }

    protected CacheKey generateCacheKey(Session session, UnmodifiableContactPictureRequestData original, ContactPictureRequestData modified) {
        return cacheKeyService.newCacheKey(session.getContextId(), String.valueOf(session.getUserId()), original.toString(), modified.toString());
    }

    // -----------------------------------------------------------------------------------------------

    /**
     * Fetches a picture from a resource
     *
     * @param session The {@link Session}
     * @param original The {@link UnmodifiableContactPictureRequestData}
     * @param modified The {@link ContactPictureRequestData}
     * @param onlyETag <code>true</code> If only the eTag should be generated
     * @return The {@link ContactPicture} or <code>null</code>
     * @throws OXException On error
     */
    public abstract ContactPicture fetchPicture(Session session, UnmodifiableContactPictureRequestData original, ContactPictureRequestData modified, boolean onlyETag) throws OXException;

    // -----------------------------------------------------------------------------------------------

    private final class FetchResult {

        final String eTag;

        final Status result;

        public FetchResult(String eTag, Status result) {
            super();
            this.eTag = eTag;
            this.result = result;
        }
    }

    // -----------------------------------------------------------------------------------------------

    private enum Status {
        MISS,
        FOUND;
    }

}

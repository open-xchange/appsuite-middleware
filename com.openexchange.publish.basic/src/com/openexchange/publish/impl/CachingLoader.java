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

package com.openexchange.publish.impl;

import java.io.Serializable;
import java.util.Collection;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.ElementAttributes;
import com.openexchange.exception.OXException;
import com.openexchange.publish.EscapeMode;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationDataLoaderService;
import com.openexchange.server.ServiceLookup;


/**
 * {@link CachingLoader}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class CachingLoader implements PublicationDataLoaderService {

    private static final String CACHE_REGION = "com.openexchange.publish.dataloader";

    private final PublicationDataLoaderService delegate;
    private final CacheService cachingService;

    /**
     * Initializes a new {@link CachingLoader}.
     *
     * @param services The OSGi service look-up
     * @param delegate The delegate service
     */
    public CachingLoader(final ServiceLookup services, final PublicationDataLoaderService delegate) {
        super();
        this.delegate = delegate;
        this.cachingService = services.getService(CacheService.class);
    }

    @Override
    public Collection<? extends Object> load(Publication publication, EscapeMode escapeMode) throws OXException {
        Cache cache = getCache();
        if (null == cache) {
            return delegate.load(publication, escapeMode);
        }


        Collection<? extends Object> fromCache = tryCache(publication, escapeMode, cache);
        if (fromCache == null) {
            Collection<? extends Object> loaded = delegate.load(publication, escapeMode);
            remember(publication, escapeMode, loaded, cache);
            fromCache = loaded;
        }
        return fromCache;
   }

    private void remember(Publication publication, EscapeMode escapeMode, Collection<? extends Object> loaded, Cache cache) throws OXException {
        if (!allElementsAreSerializable(loaded)) {
            return;
        }

        cache.put(new PublicationKey(publication, escapeMode), (Serializable) loaded, modifyAttributes(cache.getDefaultElementAttributes()), false);
    }

    private ElementAttributes modifyAttributes(final ElementAttributes attributes) {

        if(attributes.getMaxLifeSeconds() == -1) {
            attributes.setMaxLifeSeconds(30);
        }
        if(attributes.getIdleTime() == -1) {
            attributes.setIdleTime(30);
        }
        attributes.setIsSpool(false);
        attributes.setIsLateral(false);
        attributes.setIsRemote(false);
        return attributes;
    }

    private boolean allElementsAreSerializable(final Collection<? extends Object> loaded) {
        for (final Object object : loaded) {
            if(!Serializable.class.isInstance(object)) {
                return false;
            }
        }
        return true;
    }

    private Collection<? extends Object> tryCache(Publication publication, EscapeMode escapeMode, Cache cache) throws OXException {
        return (Collection<? extends Object>) cache.get(new PublicationKey(publication, escapeMode));
    }

    private Cache getCache() throws OXException {
        return cachingService.getCache(CACHE_REGION);
    }

    private static final class PublicationKey implements Serializable {

        private static final long serialVersionUID = 7259709320829767274L;

        private final int id;
        private final int ctxId;
        private final int escape;
        private final int hash;

        PublicationKey(final Publication publication, EscapeMode escapeMode) {
            super();
            this.escape = escapeMode.ordinal();
            this.id = publication.getId();
            this.ctxId = publication.getContext().getContextId();

            int prime = 31;
            int result = prime * 1 + escape;
            result = prime * result + ctxId;
            result = prime * result + id;
            this.hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof PublicationKey)) {
                return false;
            }
            PublicationKey other = (PublicationKey) obj;
            if (escape != other.escape) {
                return false;
            }
            if (ctxId != other.ctxId) {
                return false;
            }
            if (id != other.id) {
                return false;
            }
            return true;
        }
    }

}

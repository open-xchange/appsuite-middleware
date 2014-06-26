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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

    private PublicationDataLoaderService delegate = null;
    private final CacheService cachingService;

    public CachingLoader(final ServiceLookup services, final PublicationDataLoaderService delegate) {
        this.delegate = delegate;
        this.cachingService = services.getService(CacheService.class);
    }

    @Override
    public Collection<? extends Object> load(final Publication publication) throws OXException {
        try {
            final Collection<? extends Object> fromCache = tryCache(publication);
            if(fromCache == null) {
                final Collection<? extends Object> loaded = delegate.load(publication);
                remember(publication, loaded);
                return loaded;
            }
            return fromCache;

        } catch (final OXException x) {
            throw x;
        }
   }

    private void remember(final Publication publication, final Collection<? extends Object> loaded) throws OXException {
        if(! allElementsAreSerializable(loaded)) {
            return;
        }
        final Cache cache = getCache();
        if(cache == null) {
            return;
        }
        cache.put(new PublicationKey(publication), (Serializable) loaded, modifyAttributes(cache.getDefaultElementAttributes()), false);
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

    private Collection<? extends Object> tryCache(final Publication publication) throws OXException {
        final Cache cache = getCache();
        if(cache == null) {
            return null;
        }
        return (Collection<? extends Object>) cache.get(new PublicationKey(publication));
    }

    private Cache getCache() throws OXException {
        return cachingService.getCache(CACHE_REGION);
    }

    private static final class PublicationKey implements Serializable {
        private final int id;
        private final int ctxId;

        public PublicationKey(final Publication publication) {
            this.id = publication.getId();
            this.ctxId = publication.getContext().getContextId();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ctxId;
            result = prime * result + id;
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PublicationKey other = (PublicationKey) obj;
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

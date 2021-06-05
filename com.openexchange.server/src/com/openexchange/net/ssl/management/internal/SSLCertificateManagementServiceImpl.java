/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.net.ssl.management.internal;

import java.util.List;
import java.util.concurrent.TimeUnit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.exception.OXException;
import com.openexchange.net.ssl.management.Certificate;
import com.openexchange.net.ssl.management.SSLCertificateManagementService;
import com.openexchange.net.ssl.management.exception.SSLCertificateManagementExceptionCode;
import com.openexchange.net.ssl.management.storage.SSLCertificateManagementSQL;
import com.openexchange.server.ServiceLookup;

/**
 * {@link SSLCertificateManagementServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SSLCertificateManagementServiceImpl implements SSLCertificateManagementService {

    private final SSLCertificateManagementSQL storage;

    private final Cache<CertificateKey, Certificate> certificateCache;

    /**
     * Initialises a new {@link SSLCertificateManagementServiceImpl}.
     *
     * @param services The {@link ServiceLookup} instance
     */
    public SSLCertificateManagementServiceImpl(ServiceLookup services) {
        super();
        storage = new SSLCertificateManagementSQL(services);
        certificateCache = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(2, TimeUnit.MINUTES).expireAfterAccess(2, TimeUnit.MINUTES).build();
    }

    @Override
    public boolean isTrusted(int userId, int contextId, String hostname, String fingerprint) throws OXException {
        return storage.isTrusted(userId, contextId, hostname, fingerprint);
    }

    @Override
    public Certificate get(int userId, int contextId, String hostname, String fingerprint) throws OXException {
        return storage.get(userId, contextId, hostname, fingerprint);
    }

    @Override
    public List<Certificate> get(int userId, int contextId, String fingerprint) throws OXException {
        return storage.get(userId, contextId, fingerprint);
    }

    @Override
    public List<Certificate> getAll(int userId, int contextId) throws OXException {
        return storage.getAll(userId, contextId);
    }

    @Override
    public boolean contains(int userId, int contextId, String hostname, String fingerprint) throws OXException {
        return storage.contains(userId, contextId, hostname, fingerprint);
    }

    @Override
    public void store(int userId, int contextId, Certificate certificate) throws OXException {
        storage.store(userId, contextId, certificate);
        certificateCache.invalidate(new CertificateKey(userId, contextId, certificate.getFingerprint()));
    }

    @Override
    public void delete(int userId, int contextId, String fingerprint) throws OXException {
        storage.delete(userId, contextId, fingerprint);
    }

    @Override
    public void delete(int userId, int contextId, String hostname, String fingerprint) throws OXException {
        storage.delete(userId, contextId, hostname, fingerprint);
    }
    
    @Override
    public void deleteAll(int userId, int contextId) throws OXException {
        storage.deleteAll(userId, contextId);
    }

    @Override
    public void cache(int userId, int contextId, final Certificate certificate) throws OXException {
        certificateCache.put(new CertificateKey(userId, contextId, certificate.getFingerprint()), certificate);
    }

    @Override
    public Certificate requireCached(int userId, int contextId, String fingerprint) throws OXException {
        Certificate certificate = certificateCache.getIfPresent(new CertificateKey(userId, contextId, fingerprint));
        if (certificate == null) {
            throw SSLCertificateManagementExceptionCode.NOT_CACHED.create(fingerprint);
        }
        return certificate;
    }

    @Override
    public Certificate optCached(int userId, int contextId, String fingerprint) throws OXException {
        return certificateCache.getIfPresent(new CertificateKey(userId, contextId, fingerprint));
    }
}

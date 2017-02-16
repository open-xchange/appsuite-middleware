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

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.net.ssl.management.SSLCertificateManagementService#isTrusted(int, int, java.lang.String)
     */
    @Override
    public boolean isTrusted(int userId, int contextId, String hostname, String fingerprint) throws OXException {
        return storage.isTrusted(userId, contextId, hostname, fingerprint);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.net.ssl.management.SSLCertificateManagementService#get(int, int, java.lang.String)
     */
    @Override
    public Certificate get(int userId, int contextId, String hostname, String fingerprint) throws OXException {
        return storage.get(userId, contextId, hostname, fingerprint);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.net.ssl.management.SSLCertificateManagementService#get(int, int, java.lang.String)
     */
    @Override
    public List<Certificate> get(int userId, int contextId, String fingerprint) throws OXException {
        return storage.get(userId, contextId, fingerprint);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.net.ssl.management.SSLCertificateManagementService#getAll(int, int)
     */
    @Override
    public List<Certificate> getAll(int userId, int contextId) throws OXException {
        return storage.getAll(userId, contextId);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.net.ssl.management.SSLCertificateManagementService#contains(int, int, java.lang.String)
     */
    @Override
    public boolean contains(int userId, int contextId, String hostname, String fingerprint) throws OXException {
        return storage.contains(userId, contextId, hostname, fingerprint);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.net.ssl.management.SSLCertificateManagementService#store(int, int, com.openexchange.net.ssl.management.Certificate)
     */
    @Override
    public void store(int userId, int contextId, Certificate certificate) throws OXException {
        storage.store(userId, contextId, certificate);
        certificateCache.invalidate(new CertificateKey(userId, contextId, certificate.getFingerprint()));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.net.ssl.management.SSLCertificateManagementService#delete(int, int, java.lang.String)
     */
    @Override
    public void delete(int userId, int contextId, String fingerprint) throws OXException {
        storage.delete(userId, contextId, fingerprint);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.net.ssl.management.SSLCertificateManagementService#delete(int, int, java.lang.String)
     */
    @Override
    public void delete(int userId, int contextId, String hostname, String fingerprint) throws OXException {
        storage.delete(userId, contextId, hostname, fingerprint);
    }
    
    /* (non-Javadoc)
     * @see com.openexchange.net.ssl.management.SSLCertificateManagementService#deleteAll(int, int)
     */
    @Override
    public void deleteAll(int userId, int contextId) throws OXException {
        storage.deleteAll(userId, contextId);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.net.ssl.management.SSLCertificateManagementService#cache(int, int, com.openexchange.net.ssl.management.Certificate)
     */
    @Override
    public void cache(int userId, int contextId, final Certificate certificate) throws OXException {
        certificateCache.put(new CertificateKey(userId, contextId, certificate.getFingerprint()), certificate);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.net.ssl.management.SSLCertificateManagementService#getCached(int, int, java.lang.String)
     */
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

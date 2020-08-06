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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.file.storage.owncloud;

import java.util.Optional;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.CompositeFileStorageAccountManagerProvider;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.webdav.AbstractWebDAVFileStorageService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link OwnCloudFileStorageService}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.4
 */
public class OwnCloudFileStorageService extends AbstractWebDAVFileStorageService {

    private static final String SERVICE_ID = "owncloud";
    private static final String DISPLAY_NAME = "Owncloud file Storage Service";

    /**
     * Initializes a new {@link OwnCloudFileStorageService}.
     *
     * @param services The {@link ServiceLookup} instance
     */
    public OwnCloudFileStorageService(ServiceLookup services) {
        super(services, DISPLAY_NAME, SERVICE_ID);
    }

    /**
     * Initializes a new {@link OwnCloudFileStorageService}.
     *
     * @param services The {@link ServiceLookup} instance
     * @param displayName The display name of the service
     * @param serviceId The service identifier
     */
    public OwnCloudFileStorageService(ServiceLookup services, String displayName, String serviceId) {
        super(services, displayName, serviceId);
    }

    /**
     * Initializes a new {@link OwnCloudFileStorageService}.
     *
     * @param services The {@link ServiceLookup} instance
     * @param displayName The display name of the service
     * @param serviceId The service identifier
     * @param compositeFileStorageAccountManagerProvider The {@link CompositeFileStorageAccountManagerProvider}
     */
    public OwnCloudFileStorageService(ServiceLookup services, String displayName, String serviceId, CompositeFileStorageAccountManagerProvider compositeFileStorageAccountManagerProvider) {
        super(services,displayName,serviceId,compositeFileStorageAccountManagerProvider);
    }

    @SuppressWarnings("null")
    @Override
    public FileStorageAccountAccess getAccountAccess(String accountId, Session session) throws OXException {
        checkCapability(session);
        final FileStorageAccount account = getAccountAccess(session, accountId);
        return new OwnCloudAccountAccess(this, account, session);
    }

    /**
     * Gets an optional {@link OwnCloudEntityResolver}
     *
     * @return The optional {@link OwnCloudEntityResolver}
     */
    public Optional<OwnCloudEntityResolver> getResolver() {
        return Optional.ofNullable(services.getOptionalService(OwnCloudEntityResolver.class));
    }

    @Override
    public Optional<String> getCapability() {
        return Optional.of(CAPABILITY_PREFIX+getId());
    }
}

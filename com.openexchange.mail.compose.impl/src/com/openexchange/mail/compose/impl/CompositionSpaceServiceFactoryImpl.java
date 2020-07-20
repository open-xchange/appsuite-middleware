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

package com.openexchange.mail.compose.impl;

import com.openexchange.exception.OXException;
import com.openexchange.mail.compose.AttachmentStorageService;
import com.openexchange.mail.compose.CompositionSpaceService;
import com.openexchange.mail.compose.CompositionSpaceServiceFactory;
import com.openexchange.mail.compose.CompositionSpaceStorageService;
import com.openexchange.mail.compose.impl.security.CompositionSpaceKeyStorageServiceImpl;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link CompositionSpaceServiceFactoryImpl} - The composition space service factory implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class CompositionSpaceServiceFactoryImpl implements CompositionSpaceServiceFactory {

    private final ServiceLookup services;
    private final CompositionSpaceStorageService storageService;
    private final AttachmentStorageService attachmentStorageService;
    private final CompositionSpaceKeyStorageServiceImpl keyStorageService;

    /**
     * Initializes a new {@link CompositionSpaceServiceFactoryImpl}.
     *
     * @param storageService The storage service
     * @param attachmentStorageService The attachment storage service
     * @param keyStorageService The key storage service
     * @param services The service look-up
     */
    public CompositionSpaceServiceFactoryImpl(CompositionSpaceStorageService storageService, AttachmentStorageService attachmentStorageService, CompositionSpaceKeyStorageServiceImpl keyStorageService, ServiceLookup services) {
        super();
        this.keyStorageService = keyStorageService;
        if (null == storageService) {
            throw new IllegalArgumentException("Storage service must not be null");
        }
        if (null == attachmentStorageService) {
            throw new IllegalArgumentException("Attachment storage service must not be null");
        }
        if (null == services) {
            throw new IllegalArgumentException("Service registry must not be null");
        }
        this.storageService = storageService;
        this.attachmentStorageService = attachmentStorageService;
        this.services = services;
    }

    @Override
    public CompositionSpaceService createServiceFor(Session session) throws OXException {
        CompositionSpaceServiceImpl serviceImpl = new CompositionSpaceServiceImpl(session, storageService, attachmentStorageService, services);
        return new CryptoCompositionSpaceService(session, serviceImpl, keyStorageService, services);
    }

}

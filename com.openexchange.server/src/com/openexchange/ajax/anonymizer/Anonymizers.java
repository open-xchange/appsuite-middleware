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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.ajax.anonymizer;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;


/**
 * {@link Anonymizers} - Utility class for anonymization.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public final class Anonymizers {

    /**
     * Initializes a new {@link Anonymizers}.
     */
    private Anonymizers() {
        super();
    }

    /**
     * Optionally anonymizes given entity.
     *
     * @param entity The entity to anonymize
     * @param module The module
     * @param session The associated session
     * @return The eventually anonymized entity
     * @throws OXException If a fatal error occurs
     */
    public static <E> E optAnonymize(E entity, Module module, Session session) throws OXException {
        AnonymizerRegistryService registry = ServerServiceRegistry.getInstance().getService(AnonymizerRegistryService.class);
        if (null == registry) {
            return entity;
        }

        AnonymizerService anonymizer = registry.getAnonymizerFor(module);
        if (null == anonymizer) {
            return entity;
        }

        return anonymizer.anonymize(entity, session);
    }

    /**
     * Optionally anonymizes given entity only if specified session denotes a guest user.
     *
     * @param entity The entity to anonymize
     * @param module The module
     * @param session The associated session
     * @return The eventually anonymized entity
     * @throws OXException If a fatal error occurs
     */
    public static <E> E optAnonymizeIfGuest(E entity, Module module, Session session) throws OXException {
        if (false == UserStorage.getInstance().isGuest(session.getUserId(), session.getContextId())) {
            return entity;
        }

        AnonymizerRegistryService registry = ServerServiceRegistry.getInstance().getService(AnonymizerRegistryService.class);
        if (null == registry) {
            return entity;
        }

        AnonymizerService anonymizer = registry.getAnonymizerFor(module);
        if (null == anonymizer) {
            return entity;
        }

        return anonymizer.anonymize(entity, session);
    }

    /**
     * (Forced) Anonymization for given entity.
     *
     * @param entity The entity to anonymize
     * @param module The module
     * @param session The associated session
     * @return The anonymized entity
     * @throws OXException If entity cannot be anonymized
     */
    public static <E> E anonymize(E entity, Module module, Session session) throws OXException {
        AnonymizerRegistryService registry = ServerServiceRegistry.getInstance().getService(AnonymizerRegistryService.class);
        if (null == registry) {
            throw ServiceExceptionCode.absentService(AnonymizerRegistryService.class);
        }

        AnonymizerService anonymizer = registry.getAnonymizerFor(module);
        if (null == anonymizer) {
            throw AnonymizeExceptionCodes.NO_SUCH_ANONYMIZER.create(module.getName());
        }

        return anonymizer.anonymize(entity, session);
    }

    /**
     * (Forced) Anonymization for given entity only if specified session denotes a guest user.
     *
     * @param entity The entity to anonymize
     * @param module The module
     * @param session The associated session
     * @return The anonymized entity or the entity as-is if specified session does not denote a guest user
     * @throws OXException If entity cannot be anonymized
     */
    public static <E> E anonymizeIfGuest(E entity, Module module, Session session) throws OXException {
        if (false == UserStorage.getInstance().isGuest(session.getUserId(), session.getContextId())) {
            return entity;
        }

        AnonymizerRegistryService registry = ServerServiceRegistry.getInstance().getService(AnonymizerRegistryService.class);
        if (null == registry) {
            throw ServiceExceptionCode.absentService(AnonymizerRegistryService.class);
        }

        AnonymizerService anonymizer = registry.getAnonymizerFor(module);
        if (null == anonymizer) {
            throw AnonymizeExceptionCodes.NO_SUCH_ANONYMIZER.create(module.getName());
        }

        return anonymizer.anonymize(entity, session);
    }

}

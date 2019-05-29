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

package com.openexchange.gdpr.dataexport.provider.mail.osgi;

import org.osgi.framework.BundleContext;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.provider.mail.generator.SessionGenerator;
import com.openexchange.gdpr.dataexport.provider.mail.internal.SessionGeneratorRegistry;
import com.openexchange.java.Strings;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.session.Session;

/**
 * {@link SessionGeneratorRegistryImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class SessionGeneratorRegistryImpl extends RankingAwareNearRegistryServiceTracker<SessionGenerator> implements SessionGeneratorRegistry {

    /**
     * Initializes a new {@link SessionGeneratorRegistryImpl}.
     */
    public SessionGeneratorRegistryImpl(BundleContext context) {
        super(context, SessionGenerator.class);
    }

    @Override
    public SessionGenerator getGenerator(Session session) throws OXException {
        if (session == null) {
            throw new IllegalArgumentException("Session must not be null");
        }
        for (SessionGenerator generator : this) {
            if (generator.isApplicable(session)) {
                return generator;
            }
        }
        throw OXException.general("No suitable generator");
    }

    @Override
    public SessionGenerator getGeneratorById(String generatorId) throws OXException {
        if (Strings.isEmpty(generatorId)) {
            throw new IllegalArgumentException("Generator identifier must not be null or empty");
        }
        for (SessionGenerator generator : this) {
            if (generatorId.equals(generator.getId())) {
                return generator;
            }
        }
        throw OXException.general("No such generator: " + generatorId);
    }
}

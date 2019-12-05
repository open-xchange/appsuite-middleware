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

package com.openexchange.gdpr.dataexport.provider.mail.generator.impl;

import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.GeneratedSession;
import com.openexchange.gdpr.dataexport.provider.mail.MailDataExportPropertyNames;
import com.openexchange.gdpr.dataexport.provider.mail.generator.AbstractSessionGenerator;
import com.openexchange.mail.api.MailConfig.PasswordSource;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.session.Session;

/**
 * {@link LoginAuthSessionGenerator} - The session generator for common login/password access.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class LoginAuthSessionGenerator extends AbstractSessionGenerator {

    /**
     * Initializes a new {@link LoginAuthSessionGenerator}.
     */
    public LoginAuthSessionGenerator(ServiceLookup services) {
        super("28ed502e78fa4a2c9aaae5c4fa6f72bf", services);
    }

    @Override
    protected boolean isApplicable(boolean isOAuthEnabled, Session session) throws OXException {
        return !isOAuthEnabled && (MailProperties.getInstance().getPasswordSource(session.getUserId(), session.getContextId()) != PasswordSource.GLOBAL);
    }

    @Override
    protected void enhanceExtendedProperties(Map<String, Object> extendedProperties, Session session) throws OXException {
        String password = session.getPassword();
        if (password != null) {
            ObfuscatorService obfuscatorService = services.getServiceSafe(ObfuscatorService.class);
            extendedProperties.put(MailDataExportPropertyNames.PROP_PASSWORD, obfuscatorService.obfuscate(password));
        }
    }

    @Override
    protected void enhanceGeneratedSession(GeneratedSession session, Map<String, Object> moduleProperties) throws OXException {
        String password = (String) moduleProperties.get(MailDataExportPropertyNames.PROP_PASSWORD);
        if (password != null) {
            ObfuscatorService obfuscatorService = services.getServiceSafe(ObfuscatorService.class);
            session.setPassword(obfuscatorService.unobfuscate(password));
        }
    }

}

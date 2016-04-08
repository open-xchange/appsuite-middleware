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

package com.openexchange.mail.transport.config.impl;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.slf4j.Logger;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.transport.config.NoReplyConfig;
import com.openexchange.mail.transport.config.NoReplyConfig.SecureMode;
import com.openexchange.mail.transport.config.NoReplyConfigFactory;

/**
 * {@link DefaultNoReplyConfigFactory}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class DefaultNoReplyConfigFactory implements NoReplyConfigFactory {

    private final ContextService contextService;
    private final ConfigViewFactory configViewFactory;

    public DefaultNoReplyConfigFactory(ContextService contextService, ConfigViewFactory configViewFactory) {
        super();
        this.contextService = contextService;
        this.configViewFactory = configViewFactory;
    }

    @Override
    public NoReplyConfig getNoReplyConfig(final int contextId) throws OXException {
        return loadNoReplyConfig(contextId);
    }

    public NoReplyConfig loadNoReplyConfig(int contextId) throws OXException {
        Logger logger = org.slf4j.LoggerFactory.getLogger(NoReplyConfig.class);

        /*-
         * At least one user is needed from the specified context in order to properly support context-set-level properties:
         *
         * Seems to be "as designed".
         *
         * - "server", "context", "user" is set as precedence; consciously ignoring "contextSets"
         * - Even directly accessing the provider for "contextSets" statically returns special constant "com.openexchange.config.cascade.ConfigProviderService.NO_PROPERTY"
         *   when invoking "com.openexchange.config.cascade.context.ContextSetConfigProvider.get(String, Context, int)"
         *
         *
         * The reason is determined by the implementation logic for "com.openexchange.config.cascade.context.ContextSetConfigProvider.get(String, Context, int)":
         *
         * The user-sensitive UserPermissionBits instance is needed in order to retrieve the applicable "specification" (the set of tags that do apply).
         *
         */

        int contextAdminId = contextService.getContext(contextId).getMailadmin();
        ConfigView view = configViewFactory.getView(contextAdminId, contextId);
        DefaultNoReplyConfig config = new DefaultNoReplyConfig();

        {
            String sAddress = view.get("com.openexchange.noreply.address", String.class);
            InternetAddress address;
            if (Strings.isEmpty(sAddress)) {
                String msg = "Missing no-reply address";
                logger.error(msg, new Throwable(msg));
                address = null;
            } else {
                try {
                    address = new QuotedInternetAddress(sAddress, false);
                } catch (AddressException e) {
                    logger.error("Invalid no-reply address", e);
                    address = null;
                }
            }

            config.setAddress(address);
        }

        {
            String str = view.get("com.openexchange.noreply.login", String.class);
            if (Strings.isEmpty(str)) {
                config.setLogin(null);
            } else {
                config.setLogin(str.trim());
            }
        }

        {
            String str = view.get("com.openexchange.noreply.password", String.class);
            if (Strings.isEmpty(str)) {
                config.setPassword(null);
            } else {
                config.setPassword(str.trim());
            }
        }

        {
            String str = view.get("com.openexchange.noreply.server", String.class);
            if (Strings.isEmpty(str)) {
                String msg = "Missing no-reply server";
                logger.error(msg, new Throwable(msg));
            } else {
                config.setServer(str.trim());
            }
        }

        {
            String str = view.get("com.openexchange.noreply.port", String.class);
            int port;
            if (Strings.isEmpty(str)) {
                logger.info("Missing no-reply port. Using 25 as fall-back value.");
                port = 25;
            } else {
                int p = Strings.parseInt(str.trim());
                if (p < 0) {
                    logger.warn("Invalid no-reply port: {}. Using 25 as fall-back value.", str);
                    port = 25;
                } else {
                    port = p;
                }
            }
            config.setPort(port);
        }

        {
            String str = view.get("com.openexchange.noreply.secureMode", String.class);
            SecureMode secureMode;
            if (Strings.isEmpty(str)) {
                logger.info("Missing no-reply secure mode. Using \"plain\" as fall-back value.");
                secureMode = SecureMode.PLAIN;
            } else {
                SecureMode tmp = SecureMode.secureModeFor(str.trim());
                if (null == tmp) {
                    logger.warn("Invalid no-reply secure mode: {}. Using \"plain\" as fall-back value.", str);
                    secureMode = SecureMode.PLAIN;
                } else {
                    secureMode = tmp;
                }
            }
            config.setSecureMode(secureMode);
        }

        if (!config.isValid()) {
            throw MailExceptionCode.CONFIG_ERROR.create("The no-reply mail configuration is invalid. Make sure to set all necessary values in noreply.properties and the according context-scope overrides.");
        }

        return config;
    }

}

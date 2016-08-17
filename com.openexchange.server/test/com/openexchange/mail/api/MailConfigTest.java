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

package com.openexchange.mail.api;

import junit.framework.TestCase;
import com.openexchange.exception.OXException;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mailaccount.internal.CustomMailAccount;
import com.openexchange.secret.SecretEncryptionFactoryService;
import com.openexchange.secret.SecretEncryptionService;
import com.openexchange.secret.SecretEncryptionStrategy;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.session.SimSession;

/**
 * {@link MailConfigTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailConfigTest extends TestCase {

    /**
     * Initializes a new {@link MailConfigTest}.
     */
    public MailConfigTest() {
        super();
    }

    public void testForBug32296() {
        try {
            // If proxy auth is enabled, bare login should be used for external accounts though
            MailConfig mailConfig = new MailConfig() {

                @Override
                public MailCapabilities getCapabilities() {
                    return null;
                }

                @Override
                public int getPort() {
                    return 143;
                }

                @Override
                public String getServer() {
                    return "imap.somewhere.com";
                }

                @Override
                public boolean isSecure() {
                    return false;
                }

                @Override
                public void setPort(int port) {
                    // Nothing
                }

                @Override
                public void setSecure(boolean secure) {
                    // Nothing
                }

                @Override
                public void setServer(String server) {
                    // Nothing
                }

                @Override
                public IMailProperties getMailProperties() {
                    return null;
                }

                @Override
                public void setMailProperties(IMailProperties mailProperties) {
                    // Nothing
                }

                @Override
                protected void parseServerURL(UrlInfo urlInfo) throws OXException {
                    // Nothing
                }
            };

            SimSession session = new SimSession(1, 1);

            final CustomMailAccount account = new CustomMailAccount(0);
            account.setLogin("login");
            account.setPassword("password");

            ServerServiceRegistry.getInstance().addService(SecretEncryptionFactoryService.class, new SecretEncryptionFactoryService() {

                @Override
                public <T> SecretEncryptionService<T> createService(SecretEncryptionStrategy<T> strategy) {
                    return new SecretEncryptionService() {

                        @Override
                        public String encrypt(Session session, String toEncrypt) throws OXException {
                            return null;
                        }

                        @Override
                        public String decrypt(Session session, String toDecrypt) throws OXException {
                            return toDecrypt;
                        }

                        @Override
                        public String decrypt(Session session, String toDecrypt, Object customizationNote) throws OXException {
                            return toDecrypt;
                        }
                    };
                }

            });

            MailProperties.getInstance().setAuthProxyDelimiter("#");

            MailConfig.fillLoginAndPassword(mailConfig, session, "user#001", account);

            assertEquals("Unexpected login", "login", mailConfig.getLogin());
            assertEquals("Unexpected password", "password", mailConfig.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}

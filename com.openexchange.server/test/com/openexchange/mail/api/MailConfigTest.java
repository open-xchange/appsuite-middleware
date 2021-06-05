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

package com.openexchange.mail.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
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
public final class MailConfigTest {
    /**
     * Initializes a new {@link MailConfigTest}.
     */
    public MailConfigTest() {
        super();
    }

         @Test
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
                    return new SecretEncryptionService<T>() {

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

            MailConfig.fillLoginAndPassword(mailConfig, session, "user#001", account, true);

            assertEquals("Unexpected login", "login", mailConfig.getLogin());
            assertEquals("Unexpected password", "password", mailConfig.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}

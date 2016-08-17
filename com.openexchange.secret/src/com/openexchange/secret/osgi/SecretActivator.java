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

package com.openexchange.secret.osgi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.crypto.CryptoService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.secret.SecretEncryptionFactoryService;
import com.openexchange.secret.SecretService;
import com.openexchange.secret.SecretUsesPasswordChecker;
import com.openexchange.secret.impl.CryptoSecretEncryptionFactoryService;
import com.openexchange.secret.impl.LiteralToken;
import com.openexchange.secret.impl.ReservedToken;
import com.openexchange.secret.impl.Token;
import com.openexchange.secret.impl.TokenBasedSecretService;
import com.openexchange.secret.impl.TokenList;
import com.openexchange.secret.impl.TokenRow;
import com.openexchange.secret.osgi.tools.WhiteboardSecretService;

/**
 * {@link SecretActivator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SecretActivator extends HousekeepingActivator implements Reloadable {

    private volatile WhiteboardSecretService whiteboardSecretService;

    /**
     * Initializes a new {@link SecretActivator}.
     */
    public SecretActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, CryptoService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final ConfigurationService configurationService = getService(ConfigurationService.class);
        reinit(configurationService, false);
    }

    @Override
    protected void stopBundle() throws Exception {
        TokenBasedSecretService.RANDOM.set("unknown");
        final WhiteboardSecretService whiteboardSecretService = this.whiteboardSecretService;
        if (null != whiteboardSecretService) {
            whiteboardSecretService.close();
            this.whiteboardSecretService = null;
        }
        super.stopBundle();
    }

    @Override
    public void reloadConfiguration(final ConfigurationService configurationService) {
        reinit(configurationService, true);
    }

    private void reinit(final ConfigurationService configurationService, final boolean performShutDown) {
        final Logger logger = org.slf4j.LoggerFactory.getLogger(SecretActivator.class);
        if (performShutDown) {
            try {
                stopBundle();
            } catch (final Exception e) {
                logger.warn("Secret module could not be shut down.", e);
            }
        }
        /*
         * Initialize plain SessionSecretService
         */
        final TokenList tokenList;
        {
            TokenBasedSecretService.RANDOM.set(configurationService.getProperty("com.openexchange.secret.secretRandom", "unknown"));
            /*
             * Get pattern from configuration
             */
            String pattern = configurationService.getProperty("com.openexchange.secret.secretSource", "\"<password>\"");
            if (pattern.charAt(0) == '"') {
                pattern = pattern.substring(1);
            }
            if (pattern.charAt(pattern.length() - 1) == '"') {
                pattern = pattern.substring(0, pattern.length() - 1);
            }
            /*
             * Check for "list"
             */
            final TokenBasedSecretService tokenBasedSecretService;
            if ("<list>".equals(pattern)) {
                String text = configurationService.getText("secrets");
                if (null == text) {
                    text = "\"<user-id> + '-' +  <random> + '-' + <context-id>\"";
                }
                tokenList = TokenList.parseText(text);
                // SecretService based on last token in token list
                tokenBasedSecretService = new TokenBasedSecretService(tokenList);
            } else {
                final String[] tokens = pattern.split(" *\\+ *");
                final List<Token> tl = new ArrayList<Token>(tokens.length);
                for (String token : tokens) {
                    token = token.trim();
                    final boolean isReservedToken = ('<' == token.charAt(0));
                    if (isReservedToken || ('\'' == token.charAt(0))) {
                        token = token.substring(1);
                        token = token.substring(0, token.length() - 1);
                    }
                    final ReservedToken rt = ReservedToken.reservedTokenFor(token);
                    if (null == rt) {
                        if (isReservedToken) {
                            throw new IllegalStateException("Unknown reserved token: " + token);
                        }
                        tl.add(new LiteralToken(token));
                    } else {
                        tl.add(rt);
                    }
                }
                tokenBasedSecretService = new TokenBasedSecretService(new TokenRow(tl));
                tokenList = TokenList.newInstance(Collections.singleton(tl));
            }
            // Checks if SecretService is configured to use a password
            final boolean usesPassword = tokenList.isUsesPassword();
            registerService(SecretUsesPasswordChecker.class, new SecretUsesPasswordChecker() {

                @Override
                public boolean usesPassword() {
                    return usesPassword;
                }

                @Override
                public SecretService passwordUsingSecretService() {
                    return usesPassword ? tokenBasedSecretService : null;
                }
            });

            Hashtable<String, Object> properties = new Hashtable<String, Object>(1);
            properties.put(Constants.SERVICE_RANKING, Integer.valueOf(tokenBasedSecretService.getRanking()));
            registerService(SecretService.class, tokenBasedSecretService, properties);
        }
        /*
         * Create & open whiteboard service
         */
        final WhiteboardSecretService whiteboardSecretService = new WhiteboardSecretService(context);
        this.whiteboardSecretService = whiteboardSecretService;
        whiteboardSecretService.open();
        /*
         * Register CryptoSecretEncryptionFactoryService
         */
        final CryptoService crypto = getService(CryptoService.class);
        final CryptoSecretEncryptionFactoryService service = new CryptoSecretEncryptionFactoryService(crypto, whiteboardSecretService, tokenList);
        registerService(SecretEncryptionFactoryService.class, service);
        /*
         * Register Reloadable (again)
         */
        registerService(Reloadable.class, this);

        logger.info("(Re-)Initialized 'com.openexchange.secret' bundle.");
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForFiles("secret.properties");
    }

}

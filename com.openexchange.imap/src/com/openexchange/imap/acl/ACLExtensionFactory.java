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

package com.openexchange.imap.acl;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.mailaccount.MailAccount;

/**
 * {@link ACLExtensionFactory} - Factory for ACL extension.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ACLExtensionFactory {

    private static volatile ACLExtensionFactory instance;

    static void createInstance() {
        instance = new ACLExtensionFactory();
    }

    static void releaseInstance() {
        instance = null;
    }

    /**
     * Gets the factory instance.
     *
     * @return The factory instance.
     */
    public static ACLExtensionFactory getInstance() {
        ACLExtensionFactory tmp = instance;
        if (null == tmp) {
            synchronized (ACLExtensionFactory.class) {
                tmp = instance;
                if (null == tmp) {
                    tmp = new ACLExtensionFactory();
                    instance = tmp;
                }
            }
        }
        return tmp;
    }

    private final AtomicBoolean instantiated;

    private ACLExtension configured;

    /**
     * Initializes a new {@link ACLExtensionFactory}.
     */
    private ACLExtensionFactory() {
        super();
        instantiated = new AtomicBoolean();
    }

    /**
     * Gets the appropriate ACL extension for the IMAP server denoted by specified IMAP configuration.
     *
     * @param imapConfig The IMAP configuration providing needed access data.
     * @return The appropriate ACL extension
     */
    public ACLExtension getACLExtension(IMAPConfig imapConfig) {
        if (instantiated.get() && MailAccount.DEFAULT_ID == imapConfig.getAccountId()) {
            return configured;
        }
        return ACLExtensionAutoDetector.getACLExtension(imapConfig);
    }

    /**
     * Gets the appropriate ACL extension for the IMAP server denoted by specified IMAP configuration.
     *
     * @param capabilities The capabilities map
     * @param imapConfig The IMAP configuration providing needed access data.
     * @return The appropriate ACL extension
     */
    public ACLExtension getACLExtension(Map<String, String> capabilities, IMAPConfig imapConfig) {
        if (!instantiated.get()) {
            return ACLExtensionAutoDetector.getACLExtension(capabilities, imapConfig);
        }
        return configured;
    }

    /**
     * Resets this factory instance.
     */
    void resetACLExtensionFactory() {
        configured = null;
        instantiated.set(false);
    }

    /**
     * Only invoked if auto-detection is turned off.
     *
     * @param singleton The singleton instance of {@link ACLExtension}
     */
    void setACLExtensionInstance(ACLExtension singleton) {
        configured = singleton;
        instantiated.set(true);
    }
}

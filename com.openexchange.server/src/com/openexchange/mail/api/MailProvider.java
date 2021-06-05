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

import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.exception.OXException;
import com.openexchange.mail.Protocol;
import com.openexchange.mail.permission.DefaultMailPermission;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.SpamHandler;
import com.openexchange.spamhandler.SpamHandlerRegistry;

/**
 * {@link MailProvider} - The main intention of the provider class is to make the implementing classes available which define the abstract
 * classes of mail API.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class MailProvider {

    private boolean deprecated;

    private final AtomicBoolean startupFlag;

    /**
     * Initializes a new {@link MailProvider}
     */
    protected MailProvider() {
        super();
        startupFlag = new AtomicBoolean();
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof MailProvider)) {
            return false;
        }
        final Protocol thisProtocol = getProtocol();
        final Protocol otherProtocol = ((MailProvider) obj).getProtocol();
        if (thisProtocol == null) {
            if (otherProtocol != null) {
                return false;
            }
        } else if (!thisProtocol.equals(otherProtocol)) {
            return false;
        }
        return true;
    }

    @Override
    public final int hashCode() {
        return getProtocol().hashCode();
    }

    /**
     * Checks if this provider is deprecated; any cached references should be discarded
     *
     * @return <code>true</code> if deprecated; otherwise <code>false</code>
     */
    public boolean isDeprecated() {
        return deprecated;
    }

    /**
     * Sets the deprecated flag
     *
     * @param deprecated <code>true</code> if deprecated; otherwise <code>false</code>
     */
    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    /**
     * Performs provider's start-up
     *
     * @throws OXException If start-up fails
     */
    public void startUp() throws OXException {
        if (!startupFlag.compareAndSet(false, true)) {
            /*
             * Already started...
             */
            return;
        }
        getProtocolProps().loadProperties();
        final MailAccess<?, ?> access = createNewMailAccess(null);
        if (null != access) {
            MailAccess.startupImpl(access);
        }
    }

    /**
     * Performs provider's shut-down
     *
     * @throws OXException if shut-down fails
     */
    public void shutDown() throws OXException {
        if (!startupFlag.compareAndSet(true, false)) {
            /*
             * Already shut down...
             */
            return;
        }
        final MailAccess<?, ?> access = createNewMailAccess(null);
        if (null != access) {
            MailAccess.shutdownImpl(access);
        }
        getProtocolProps().resetProperties();
    }

    /**
     * Gets a newly created {@link MailPermission mail permission}.
     * <p>
     * Returns a {@link DefaultMailPermission default permission} instance if mailing system does not support permission(s). Overwrite if
     * needed.
     *
     * @param session The session
     * @param accountId The account identifier
     * @return A newly created {@link MailPermission mail permission}.
     */
    public MailPermission createNewMailPermission(Session session, int accountId) {
        return new DefaultMailPermission();
    }

    /**
     * Gets the spam handler used by this mail provider.
     *
     * @return The spam handler
     */
    public SpamHandler getSpamHandler() {
        return SpamHandlerRegistry.getSpamHandler(getSpamHandlerName());
    }

    /**
     * Gets this mail provider's protocol
     *
     * @return The protocol
     */
    public abstract Protocol getProtocol();

    /**
     * Gets the unique registration name of the spam handler that shall be used by this mail provider.
     * <p>
     * If {@link SpamHandler#SPAM_HANDLER_FALLBACK} is returned, no spam handler is going to be used; meaning all spam-related actions are
     * ignored..
     *
     * @return The registration name of the spam handler
     */
    protected String getSpamHandlerName() {
        return SpamHandler.SPAM_HANDLER_FALLBACK;
    }

    /**
     * Checks if this mail provider supports the given protocol (which is either in secure or non-secure notation).
     * <p>
     * This is a convenience method that invokes {@link Protocol#isSupported(String)}
     *
     * @param protocol The protocol
     * @return <code>true</code> if supported; otherwise <code>false</code>
     */
    public final boolean supportsProtocol(String protocol) {
        return getProtocol().isSupported(protocol);
    }

    /**
     * Gets a newly created {@link MailAccess mail access} with default account ID.
     * <p>
     * If specified session is <code>null</code>, a dummy instance for initialization purpose is supposed to be returned. Implementation may
     * return <code>null</code> in this case if no start-up/shut-down actions are needed.
     *
     * @param session The session providing needed user data; may be <code>null</code> to obtain a dummy instance for initialization purpose
     * @return The newly created {@link MailAccess mail access}.
     * @throws OXException If new {@link MailAccess mail access} instance cannot be created
     */
    public abstract MailAccess<?, ?> createNewMailAccess(Session session) throws OXException;

    /**
     * Gets a newly created {@link MailAccess mail access}.
     * <p>
     * If specified session is <code>null</code>, a dummy instance for initialization purpose is supposed to be returned. Implementation may
     * return <code>null</code> in this case if no start-up/shut-down actions are needed.
     *
     * @param session The session providing needed user data; may be <code>null</code> to obtain a dummy instance for initialization purpose
     * @param accountId The account ID
     * @return The newly created {@link MailAccess mail access}.
     * @throws OXException If new {@link MailAccess mail access} instance cannot be created
     */
    public abstract MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> createNewMailAccess(Session session, int accountId) throws OXException;

    /**
     * Gets the protocol properties
     *
     * @return The protocol properties
     */
    protected abstract AbstractProtocolProperties getProtocolProperties();

    /**
     * Gets the protocol properties
     *
     * @return The protocol properties
     */
    public AbstractProtocolProperties getProtocolProps() {
        return getProtocolProperties();
    }

}

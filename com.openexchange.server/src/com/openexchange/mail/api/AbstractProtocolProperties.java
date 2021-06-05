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

/**
 * {@link AbstractProtocolProperties} - Super class of protocol-specific global properties
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractProtocolProperties {

    /**
     * <code>"true"</code>
     */
    protected static final String STR_TRUE = "true";

    /**
     * <code>"false"</code>
     */
    protected static final String STR_FALSE = "false";

    private final AtomicBoolean loaded;

    /**
     * Initializes a new {@link AbstractProtocolProperties}
     */
    protected AbstractProtocolProperties() {
        super();
        loaded = new AtomicBoolean();
    }

    /**
     * Exclusively loads protocol's global properties
     * @throws OXException If loading properties fails
     */
    public void loadProperties() throws OXException {
        if (!loaded.get()) {
            synchronized (loaded) {
                if (!loaded.get()) {
                    loadProperties0();
                    loaded.set(true);
                    loaded.notifyAll();
                }
            }
        }
    }

    /**
     * Exclusively resets protocol's global properties
     */
    public void resetProperties() {
        if (loaded.get()) {
            synchronized (loaded) {
                if (loaded.get()) {
                    resetFields();
                    loaded.set(false);
                }
            }
        }
    }

    /**
     * Waits for loading this properties.
     *
     * @throws InterruptedException If another thread interrupted the current thread before or while the current thread was waiting for
     *             loading the properties.
     */
    public final void waitForLoading() throws InterruptedException {
        if (!loaded.get()) {
            synchronized (loaded) {
                while (!loaded.get()) {
                    loaded.wait();
                }
            }
        }
    }

    /**
     * Loads protocol's global properties
     * @throws OXException If loading of protocol's global properties fails
     */
    protected abstract void loadProperties0() throws OXException;

    /**
     * Resets protocol's global properties' fields
     */
    protected abstract void resetFields();
}

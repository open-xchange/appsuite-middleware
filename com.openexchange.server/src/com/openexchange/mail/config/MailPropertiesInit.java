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

package com.openexchange.mail.config;

import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.exception.OXException;
import com.openexchange.server.Initialization;

/**
 * {@link MailPropertiesInit} - Initializes global mail configuration implementation for mailing system.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailPropertiesInit implements Initialization {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailPropertiesInit.class);

    private final AtomicBoolean started = new AtomicBoolean();

    private static final MailPropertiesInit instance = new MailPropertiesInit();

    /**
     * No instantiation
     */
    private MailPropertiesInit() {
        super();
    }

    /**
     * @return The singleton instance
     */
    public static MailPropertiesInit getInstance() {
        return instance;
    }

    @Override
    public void start() throws OXException {
        if (started.get()) {
            LOG.error("{} already started", this.getClass().getName());
            return;
        }
        MailProperties.getInstance().loadProperties();
        started.set(true);
    }

    @Override
    public void stop() {
        if (!started.get()) {
            LOG.error("{} cannot be stopped since it has not been started before", this.getClass().getName());
            return;
        }
        MailProperties.getInstance().resetProperties();
        MailProperties.releaseInstance();
        started.set(false);
    }

}

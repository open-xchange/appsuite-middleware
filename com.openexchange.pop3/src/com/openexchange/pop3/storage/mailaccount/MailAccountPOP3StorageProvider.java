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

package com.openexchange.pop3.storage.mailaccount;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.MailAccountDeleteListener;
import com.openexchange.pop3.POP3Access;
import com.openexchange.pop3.storage.POP3Storage;
import com.openexchange.pop3.storage.POP3StorageProperties;
import com.openexchange.pop3.storage.POP3StorageProvider;
import com.openexchange.pop3.storage.mailaccount.util.StorageDeleteListener;

/**
 * {@link MailAccountPOP3StorageProvider} - Primary mail account POP3 storage provider.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountPOP3StorageProvider implements POP3StorageProvider {

    /**
     * The provider name for mail account POP3 storage provider.
     */
    public static final String NAME = "mailaccount";

    private final List<MailAccountDeleteListener> deleteListeners;

    /**
     * Initializes a new {@link MailAccountPOP3StorageProvider}.
     */
    public MailAccountPOP3StorageProvider() {
        super();
        final List<MailAccountDeleteListener> tmp = new ArrayList<MailAccountDeleteListener>(1);
        tmp.add(new StorageDeleteListener());
        deleteListeners = Collections.unmodifiableList(tmp);
    }

    @Override
    public POP3Storage getPOP3Storage(final POP3Access pop3Access, final POP3StorageProperties properties) throws OXException {
        return new MailAccountPOP3Storage(pop3Access, properties);
    }

    @Override
    public String getPOP3StorageName() {
        return NAME;
    }

    @Override
    public POP3StorageProperties getPOP3StorageProperties(final POP3Access pop3Access) throws OXException {
        return SessionPOP3StorageProperties.getInstance(pop3Access);
    }

    @Override
    public List<MailAccountDeleteListener> getDeleteListeners() {
        return deleteListeners;
    }

    @Override
    public boolean unregisterDeleteListenersOnAbsence() {
        return false;
    }

}

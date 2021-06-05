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

package com.openexchange.mail.cache;

import java.util.concurrent.ConcurrentMap;
import com.openexchange.concurrent.TimeoutConcurrentSet;
import com.openexchange.concurrent.TimeoutListener;
import com.openexchange.mail.api.MailAccess;

/**
 * {@link MailAccessTimeoutListener2} - The mail access event handler which preludes mail access closure if an instance of {@link MailAccess}
 * is removed from mail access cache.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccessTimeoutListener2 implements TimeoutListener<MailAccess<?, ?>> {

    private final ConcurrentMap<?, TimeoutConcurrentSet<MailAccess<?, ?>>> map;

    private final TimeoutConcurrentSet<MailAccess<?, ?>> set;

    private final Object key;

    /**
     * Default constructor
     */
    public MailAccessTimeoutListener2(Object key, TimeoutConcurrentSet<MailAccess<?, ?>> set, ConcurrentMap<?, TimeoutConcurrentSet<MailAccess<?, ?>>> map) {
        super();
        this.key = key;
        this.map = map;
        this.set = set;
    }

    @Override
    public void onTimeout(MailAccess<?, ?> mailAccess) {
        mailAccess.close(false);
        if (set.isEmpty()) {
            final TimeoutConcurrentSet<MailAccess<?, ?>> remove = map.remove(key);
            if (!remove.isEmpty()) {
                remove.timeoutAll();
                remove.dispose();
            }
            set.timeoutAll();
            set.dispose();
        }
    }

}

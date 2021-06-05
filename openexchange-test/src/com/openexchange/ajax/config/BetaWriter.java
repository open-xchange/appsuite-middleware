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

package com.openexchange.ajax.config;

import static com.openexchange.java.Autoboxing.B;
import java.util.Random;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.exception.OXException;
import com.openexchange.test.common.test.pool.TestUser;
import com.openexchange.user.UserExceptionCode;

/**
 * {@link Runnable} that constantly writes
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class BetaWriter extends AttributeWriter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BetaWriter.class);

    private final Random rand;

    private final boolean ignoreConcurrentModification;

    public BetaWriter(TestUser user) {
        this(user, false);
    }

    public BetaWriter(TestUser user, boolean ignoreConcurrentModification) {
        super(Tree.Beta, user);
        rand = new Random(System.currentTimeMillis());
        this.ignoreConcurrentModification = ignoreConcurrentModification;
    }

    @Override
    protected Object getValue() {
        return B(rand.nextBoolean());
    }

    @Override
    protected Throwable handleError(Throwable t) {
        if (ignoreConcurrentModification && t instanceof OXException && UserExceptionCode.UPDATE_ATTRIBUTES_FAILED.equals((OXException) t)) {
            LOG.warn(t.getMessage());
            return null;
        }

        return t;
    }
}

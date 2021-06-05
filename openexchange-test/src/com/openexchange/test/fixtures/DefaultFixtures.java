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

package com.openexchange.test.fixtures;

import java.util.Date;
import java.util.Map;
import com.openexchange.test.fixtures.transformators.IntegerTransformator;
import com.openexchange.test.fixtures.transformators.JChronicDateTransformator;
import com.openexchange.test.fixtures.transformators.LongTransformator;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public abstract class DefaultFixtures<T> extends AbstractFixtures<T> {

    public DefaultFixtures(final Class<T> klass, final Map<String, Map<String, String>> values, FixtureLoader fixtureLoader) {
        super(klass, values);
        addTransformator(new JChronicDateTransformator(fixtureLoader), Date.class);
        addTransformator(new IntegerTransformator(), Integer.class);
        addTransformator(new IntegerTransformator(), int.class);
        addTransformator(new LongTransformator(), Long.class);
        addTransformator(new LongTransformator(), long.class);
    }
}

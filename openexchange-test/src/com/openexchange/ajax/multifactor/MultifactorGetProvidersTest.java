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

package com.openexchange.ajax.multifactor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.testing.httpclient.models.MultifactorProvider;

/**
 * {@link MultifactorGetProvidersTest}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.1
 */
public class MultifactorGetProvidersTest extends AbstractMultifactorTest {

    @Test
    public void testNoMultifactorDeviceExistsOnInitialSetup () throws Exception {
        List<MultifactorProvider> providers = getProviders();
        Assert.assertThat("At least one multifactor provider should be present for the test", providers, is(not(empty())));
        assertThat("No multifactor device should be present for the test", getDevices(), is(empty()));
    }
}

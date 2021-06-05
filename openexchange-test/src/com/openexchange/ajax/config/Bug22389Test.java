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

import static org.junit.Assert.assertTrue;
import java.util.regex.Pattern;
import org.junit.Test;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.GetResponse;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AbstractAJAXSession;

/**
 * Tests is the server reports always the correct version.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug22389Test extends AbstractAJAXSession {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Bug22389Test.class);
    private static final String EXPRESSION = "[67]\\.[0-9]+\\.[0-9]-Rev[0-9]+";
    private static final Pattern PATTERN = Pattern.compile(EXPRESSION);

    @Test
    public void testVersion() throws Exception {
        GetRequest request = new GetRequest(Tree.ServerVersion);
        GetResponse response = getClient().execute(request);
        String version = response.getString();
        LOG.trace("Server reported version: \"" + version + "\".");
        assertTrue("Server version does not match required pattern: \"" + version + "\"", PATTERN.matcher(version).matches());
    }
}

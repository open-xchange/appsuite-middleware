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

package com.openexchange.mail.autoconfig.xmlparser;

import static org.junit.Assert.assertTrue;
import java.io.FileInputStream;
import org.junit.Before;
import org.junit.Test;

/**
 * {@link AutoconfigParserTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class AutoconfigParserTest {

    private FileInputStream autoconfigFile;

    @Before
    public void setUp() throws Exception {
        autoconfigFile = new FileInputStream("./testdata/freenet.de.xml");
    }

    /**
     * Just checking if parsing throws an error.
     * 
     * @throws Exception
     */
    @Test
    public void testFullFile() throws Exception {
        new AutoconfigParser().getConfig(autoconfigFile);
        assertTrue(true);
    }
}

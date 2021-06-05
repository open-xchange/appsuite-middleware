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

package com.openexchange.ajax.resource;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.ajax.ResourceTest;
import com.openexchange.test.concurrent.ParallelSuite;

/**
 * Suite for the resource tests.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
    ResourceAllAJAXTest.class,
    ResourceDeleteAJAXTest.class,
    ResourceGetAJAXTest.class,
    ResourceListAJAXTest.class,
    ResourceNewAJAXTest.class,
    ResourceUpdateAJAXTest.class,
    ResourceUpdatesAJAXTest.class,
    ResourceTest.class,
    ResourceUseCountTest.class

})
public class ResourceSuite  {

}

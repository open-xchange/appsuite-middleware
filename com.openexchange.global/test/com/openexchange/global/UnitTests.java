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

package com.openexchange.global;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.exception.interception.Bug50893Test;
import com.openexchange.exception.interception.OXExceptionInterceptorRegistrationTest;
import com.openexchange.global.tools.id.IDManglerTest;
import com.openexchange.global.tools.iterator.MergingSearchIteratorTest;
import com.openexchange.i18n.I18nServiceRegistryTest;
import com.openexchange.sessiond.SessionFilterTest;
import com.openexchange.tools.filename.Bug53791Test;
import com.openexchange.tools.filename.Bug55271Test;
import com.openexchange.tools.filename.MWB_692;

/**
 * {@link UnitTests}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    IDManglerTest.class,
    MergingSearchIteratorTest.class,
    OXExceptionInterceptorRegistrationTest.class,
    SessionFilterTest.class,
    Bug50893Test.class,
    Bug53791Test.class,
    Bug55271Test.class,
    com.openexchange.tools.filename.Bug56499Test.class,
    com.openexchange.tools.filename.Bug58052Test.class,
    I18nServiceRegistryTest.class,
    MWB_692.class,
    org.json.JSONInputStreamTest.class
})
public class UnitTests {
}

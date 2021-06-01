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

package com.openexchange.ajax.mail.filter;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.ajax.mail.filter.apiclient.ApplyMailFilterTest;
import com.openexchange.ajax.mail.filter.tests.api.AuxiliaryAPITest;
import com.openexchange.ajax.mail.filter.tests.api.ConfigTest;
import com.openexchange.ajax.mail.filter.tests.api.NewTest;
import com.openexchange.ajax.mail.filter.tests.api.ReorderTest;
import com.openexchange.ajax.mail.filter.tests.api.UpdateTest;
import com.openexchange.ajax.mail.filter.tests.api.VacationTest;
import com.openexchange.ajax.mail.filter.tests.bug.Bug11519Test;
import com.openexchange.ajax.mail.filter.tests.bug.Bug18490Test;
import com.openexchange.ajax.mail.filter.tests.bug.Bug31253Test;
import com.openexchange.ajax.mail.filter.tests.bug.Bug44363Test;
import com.openexchange.ajax.mail.filter.tests.bug.Bug46589Test;
import com.openexchange.ajax.mail.filter.tests.bug.Bug46714Test;
import com.openexchange.test.concurrent.ParallelSuite;

/**
 * {@link MailFilterTestSuite}
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 *
 */
@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
    Bug11519Test.class,
    Bug18490Test.class,
    Bug31253Test.class,
    Bug44363Test.class,
    Bug46589Test.class,
    Bug46714Test.class,
    ConfigTest.class,
    NewTest.class,
    UpdateTest.class,
    VacationTest.class,
    // Deactivated the PGPTest because the email server of the test environment does not support the custom pgp plugin
    // PGPTest.class,
    ReorderTest.class,
    AuxiliaryAPITest.class,

    // new apiclient tests ---------
    ApplyMailFilterTest.class

})
public final class MailFilterTestSuite {

}

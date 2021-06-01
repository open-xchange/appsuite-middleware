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

package com.openexchange.mail.authenticity.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.mail.authenticity.test.matrix.TestMailAuthenticityStatusMatrixA;
import com.openexchange.mail.authenticity.test.matrix.TestMailAuthenticityStatusMatrixB;
import com.openexchange.mail.authenticity.test.matrix.TestMailAuthenticityStatusMatrixC;
import com.openexchange.mail.authenticity.test.matrix.TestMailAuthenticityStatusMatrixD1;
import com.openexchange.mail.authenticity.test.matrix.TestMailAuthenticityStatusMatrixD2;
import com.openexchange.mail.authenticity.test.matrix.TestMailAuthenticityStatusMatrixD3;
import com.openexchange.mail.authenticity.test.matrix.TestMailAuthenticityStatusMatrixE1;
import com.openexchange.mail.authenticity.test.matrix.TestMailAuthenticityStatusMatrixE2;
import com.openexchange.mail.authenticity.test.matrix.TestMailAuthenticityStatusMatrixE3;
import com.openexchange.mail.authenticity.test.matrix.TestMailAuthenticityStatusMatrixF1;
import com.openexchange.mail.authenticity.test.matrix.TestMailAuthenticityStatusMatrixF2;
import com.openexchange.mail.authenticity.test.matrix.TestMailAuthenticityStatusMatrixF3;

/**
 * {@link TestMailAuthenticityStatusMatrix}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@RunWith(Suite.class)
//@formatter:off
@Suite.SuiteClasses({
    TestMailAuthenticityStatusMatrixA.class,
    TestMailAuthenticityStatusMatrixB.class,
    TestMailAuthenticityStatusMatrixC.class,
    TestMailAuthenticityStatusMatrixD1.class,
    TestMailAuthenticityStatusMatrixD2.class,
    TestMailAuthenticityStatusMatrixD3.class,
    TestMailAuthenticityStatusMatrixE1.class,
    TestMailAuthenticityStatusMatrixE2.class,
    TestMailAuthenticityStatusMatrixE3.class,
    TestMailAuthenticityStatusMatrixF1.class,
    TestMailAuthenticityStatusMatrixF2.class,
    TestMailAuthenticityStatusMatrixF3.class,
})
//@formatter:on
public class TestMailAuthenticityStatusMatrix {

    /**
     * Initialises a new {@link TestMailAuthenticityStatusMatrix}.
     */
    public TestMailAuthenticityStatusMatrix() {
        super();
    }

}

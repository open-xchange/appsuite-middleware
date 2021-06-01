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

package com.openexchange.ajax.onboarding;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.ajax.onboarding.tests.ConfigTest;
import com.openexchange.ajax.onboarding.tests.DAVSyncProfileTest;
import com.openexchange.ajax.onboarding.tests.DownloadLinkTest;
import com.openexchange.ajax.onboarding.tests.DownloadTest;
import com.openexchange.ajax.onboarding.tests.EASSyncProfileTest;
import com.openexchange.ajax.onboarding.tests.EMClientURLTest;
import com.openexchange.ajax.onboarding.tests.MailSyncProfileTest;
import com.openexchange.ajax.onboarding.tests.PlistSMSRateLimitTest;
import com.openexchange.ajax.onboarding.tests.PlistSMSTest;
import com.openexchange.ajax.onboarding.tests.PlistSMSUserLimitTest;

/**
 * {@link OnboardingAJAXSuite}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ConfigTest.class,
    DAVSyncProfileTest.class,
    EASSyncProfileTest.class,
    EMClientURLTest.class,
    PlistSMSTest.class,
    PlistSMSUserLimitTest.class,
    PlistSMSRateLimitTest.class,
    MailSyncProfileTest.class,
    DownloadTest.class,
    DownloadLinkTest.class,

})
public class OnboardingAJAXSuite {


}

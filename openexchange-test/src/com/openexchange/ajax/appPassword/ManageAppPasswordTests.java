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

package com.openexchange.ajax.appPassword;

import static com.openexchange.java.Autoboxing.I;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import java.util.List;
import org.junit.Test;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.AppPassword;
import com.openexchange.testing.httpclient.models.AppPasswordApplication;
import com.openexchange.testing.httpclient.models.AppPasswordRegistrationResponseData;

/**
 * {@link ManageAppPasswordTests}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.3
 */
public class ManageAppPasswordTests extends AbstractAppPasswordTest {

    @Test
    public void testAddRemovePasswords() throws ApiException {
        removeAll();  // Verify cleanup

        // Get list
        List<AppPasswordApplication> apps = getApps();
        assertThat(I(apps.size()), greaterThan(I(1)));
        String type = apps.get(0).getName();
        // Add password
        AppPasswordRegistrationResponseData loginData = addPassword(type);
        assertThat(I(loginData.getPassword().length()), is(I(19)));

        // Check exists in list now
        List<AppPassword> passwordList = getList();
        assertThat(I(passwordList.size()), is(I(1)));
        assertThat(passwordList.get(0).getScope(), is(type));

        // Try removing

        removePassword(passwordList.get(0).getUUID());
        List<AppPassword> removedPasswordList = getList();
        assertThat(I(removedPasswordList.size()), is(I(0)));

    }

}

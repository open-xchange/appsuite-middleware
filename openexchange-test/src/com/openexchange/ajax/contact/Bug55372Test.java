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

package com.openexchange.ajax.contact;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import org.junit.Test;
import com.openexchange.ajax.contact.action.UpdateRequest;
import com.openexchange.ajax.contact.action.UpdateResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;

/**
 * {@link Bug55372Test}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class Bug55372Test extends AbstractContactTest {

    @Test
    public void withoutNamesTest() throws Exception {

        // Set own surname, first and display name to an empty value
        Contact ownContact = loadUser(userId);
        ownContact.setSurName(" ");
        ownContact.setGivenName(" ");
        ownContact.setDisplayName(" ");

        // Update on server
        UpdateRequest request = new UpdateRequest(ownContact, false);
        UpdateResponse response = getClient().execute(request);

        assertThat("Error should have been thrown", response.hasError());
        assertThat("No error message!", response.getErrorMessage(), is(not(nullValue())));
        OXException exception = response.getException();
        assertThat("Should be a different excpetion message ", exception, is(not(nullValue())));
        // Should be CON-175 since it get checked first, but CON-164 is right too
        assertThat("Wrong excpetion code!", exception.getErrorCode(), anyOf(equalTo("CON-0164"), equalTo("CON-0175")));
    }

    @Test
    public void withoutSurNameTest() throws Exception {

        // Set surname to an empty value
        Contact ownContact = loadUser(userId);
        ownContact.setSurName(" ");

        // Update on server
        UpdateRequest request = new UpdateRequest(ownContact, false);
        UpdateResponse response = getClient().execute(request);

        assertThat("Error should have been thrown", response.hasError());
        assertThat("No error message!", response.getErrorMessage(), is(not(nullValue())));
        OXException exception = response.getException();
        assertThat("Should be a different excpetion message ", exception, is(not(nullValue())));
        assertThat("Wrong excpetion code!", exception.getErrorCode(), is("CON-0175"));
    }

    @Test
    public void withoutGivenNameTest() throws Exception {

        // Set given name to an empty value
        Contact ownContact = loadUser(userId);
        ownContact.setGivenName(" ");

        // Update on server
        UpdateRequest request = new UpdateRequest(ownContact, false);
        UpdateResponse response = getClient().execute(request);

        assertThat("Error should have been thrown", response.hasError());
        assertThat("No error message!", response.getErrorMessage(), is(not(nullValue())));
        OXException exception = response.getException();
        assertThat("Should be a different excpetion message ", exception, is(not(nullValue())));
        assertThat("Wrong excpetion code!", exception.getErrorCode(), is("CON-0164"));
    }

    @Test
    public void withoutDisplayNameTest() throws Exception {

        // Set display name to an empty value
        Contact ownContact = loadUser(userId);
        ownContact.setDisplayName(" ");

        // Update on server
        UpdateRequest request = new UpdateRequest(ownContact, false);
        UpdateResponse response = getClient().execute(request);

        assertThat("Error should have been thrown", response.hasError());
        assertThat("No error message!", response.getErrorMessage(), is(not(nullValue())));
        OXException exception = response.getException();
        assertThat("Should be a different excpetion message ", exception, is(not(nullValue())));
        assertThat("Wrong excpetion code!", exception.getErrorCode(), is("CON-0166"));
    }

}

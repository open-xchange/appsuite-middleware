/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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

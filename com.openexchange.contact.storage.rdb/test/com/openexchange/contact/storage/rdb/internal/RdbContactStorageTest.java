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

package com.openexchange.contact.storage.rdb.internal;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.groupware.contact.helpers.ContactField;

/**
 * {@link RdbContactStorageTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class RdbContactStorageTest {

    private RdbContactStorage rdbContactStorage;

    @Before
    public void setUp() throws Exception {
        rdbContactStorage = new RdbContactStorage();
    }

    @Test
    public void testSupports_allSupported_returnTrue() {
        boolean supports = rdbContactStorage.supports(ContactField.SUR_NAME, ContactField.TITLE);
        assertTrue(supports);
    }

    @Test
    public void testSupports_providedOneSupported_returnTrue() {
        boolean supports = rdbContactStorage.supports(ContactField.CELLULAR_TELEPHONE1);
        assertTrue(supports);
    }

    @Test
    public void testSupports_providedOneNotSupported_returnFalse() {
        boolean supports = rdbContactStorage.supports(ContactField.IMAGE1_URL);
        assertFalse(supports);
    }

    @Test
    public void testSupports_multipleProvidedOneNotSupported_returnFlase() {
        boolean supports = rdbContactStorage.supports(ContactField.CELLULAR_TELEPHONE1, ContactField.CITY_HOME, ContactField.USERFIELD15, ContactField.TELEPHONE_CALLBACK, ContactField.LAST_MODIFIED_OF_NEWEST_ATTACHMENT, ContactField.DEFAULT_ADDRESS, ContactField.TELEPHONE_PRIMARY);
        assertFalse(supports);
    }

    @Test
    public void testSupports_allFieldsProvidedThatContainNotSupportedOnes_returnFalse() {
        boolean supports = rdbContactStorage.supports(ContactField.values());
        assertFalse(supports);
    }

}

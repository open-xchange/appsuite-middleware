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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.scheduling.changes.impl.desc;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.scheduling.changes.Description;
import com.openexchange.chronos.service.EventUpdate;

/**
 * {@link AbstractDescriptionTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public abstract class AbstractDescriptionTest {

    private static final String FORMAT = "text";

    final EventField field;

    @Mock
    protected EventUpdate eventUpdate;

    @Mock
    protected Set<EventField> fields;

    @Mock
    protected Event original;

    @Mock
    protected Event updated;

    private String descriptionMessage;

    /**
     * Initializes a new {@link AbstractDescriptionTest}.
     * 
     * @param field The field to test
     * @param descriptionMessage The introduction message of the description
     */
    public AbstractDescriptionTest(EventField field, String descriptionMessage) {
        super();
        this.field = field;
        this.descriptionMessage = descriptionMessage;
    }

    protected EventField getTestedField() {
        return field;
    }

    @SuppressWarnings("unused")
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.when(eventUpdate.getOriginal()).thenReturn(original);
        PowerMockito.when(eventUpdate.getUpdate()).thenReturn(updated);

        PowerMockito.when(eventUpdate.getUpdatedFields()).thenReturn(fields);
        PowerMockito.when(B(fields.contains(getTestedField()))).thenReturn(Boolean.TRUE);
    }

    protected void testDescription(Description description) {
        assertThat("Should not be null", description, notNullValue());
        assertThat("Not matching size", I(description.getChangedFields().size()), is((I(1))));
        assertThat("Wrong field", description.getChangedFields().get(0), is(getTestedField()));
    }

    protected void checkMessage(Description description, String containee) {
        String message = description.getSentences().get(0).getMessage(FORMAT, Locale.ENGLISH, TimeZone.getDefault(), null);
        assertThat("Not matching size", I(description.getSentences().size()), is((I(1))));
        assertTrue(message.startsWith(descriptionMessage));
        assertTrue(message.contains(containee));
    }

}

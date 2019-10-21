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
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.Supplier;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.scheduling.changes.Description;
import com.openexchange.chronos.scheduling.changes.impl.ChangeDescriber;

/**
 * {@link AbstractDescriptionTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public abstract class AbstractDescriptionTest extends AbstractDescriptionTestMocking {

    final EventField field;

    protected String descriptionMessage;

    protected ChangeDescriber describer;

    private Supplier<ChangeDescriber> supplier;

    /**
     * Initializes a new {@link AbstractDescriptionTest}.
     * 
     * @param field The field to test
     * @param descriptionMessage The introduction message of the description
     */
    public AbstractDescriptionTest(EventField field, String descriptionMessage, Supplier<ChangeDescriber> supplier) {
        super();
        this.field = field;
        this.descriptionMessage = descriptionMessage;
        this.supplier = supplier;
    }



    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.describer = supplier.get();
        PowerMockito.when(B(fields.contains(field))).thenReturn(Boolean.TRUE);

    }

    @Test
    public void testDescriber_NoValues_DescriptionUnavailable() {
        PowerMockito.when(B(fields.contains(field))).thenReturn(Boolean.FALSE);

        Description description = describer.describe(eventUpdate);
        assertThat(description, nullValue());
    }

    protected void testDescription(Description description) {
        assertThat("Should not be null", description, notNullValue());
        assertThat("Not matching size", I(description.getChangedFields().size()), is((I(1))));
        assertThat("Wrong field", description.getChangedFields().get(0), is(field));
    }

    protected void checkMessageStart(Description description, String containee) {
        checkMessageStart(description);
        assertTrue(getMessage(description, 0).contains(containee));
    }

    protected void checkMessageStart(Description description) {
        assertThat("Not matching size", I(description.getSentences().size()), is((I(1))));
        assertTrue(getMessage(description, 0).startsWith(descriptionMessage));
    }

    protected void checkMessageEnd(Description description, String containee) {
        checkMessageEnd(description);
        assertTrue(getMessage(description, 0).contains(containee));
    }

    protected void checkMessageEnd(Description description) {
        assertThat("Not matching size", I(description.getSentences().size()), is((I(1))));
        assertTrue(getMessage(description, 0).endsWith(descriptionMessage));
    }

    protected String getMessage(Description description, int sentenceIndex) {
        return description.getSentences().get(sentenceIndex).getMessage(FORMAT, Locale.ENGLISH, TimeZone.getDefault(), null);
    }

}

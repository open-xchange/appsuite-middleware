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

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
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.scheduling.changes.Description;

/**
 * {@link DescriptionDescriptionTest}
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Ottersbach</a>
 * @since v7.10.3
 */
@RunWith(PowerMockRunner.class)
public class DescriptionDescriptionTest extends AbstractDescriptionTest {

    private static final String OLD_DESCRIPTION = "Old description";
    private static final String NEW_DESCRIPTION = "New description";

    /**
     * Initializes a new {@link DescriptionDescriptionTest}.
     */
    public DescriptionDescriptionTest() {
        super(EventField.DESCRIPTION, "The appointment description has changed");
    }

    private DescriptionDescriber describer;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        describer = new DescriptionDescriber();
    }

    @Test
    public void testDescription_SetNewDescription_DescriptionAvailable() {
        setDescription(null, NEW_DESCRIPTION);

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessage(description);
    }

    @Test
    public void testDescription_removeDescription_DescriptionAvailable() {
        setDescription(OLD_DESCRIPTION, null);

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessage(description);
    }

    @Test
    public void testDescription_ChangeDescription_DescriptionAvailable() {
        setDescription(OLD_DESCRIPTION, NEW_DESCRIPTION);

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessage(description);
    }

    @Test
    public void testSummary_NoValues_DescriptionUnavailable() {
        PowerMockito.when(B(fields.contains(getTestedField()))).thenReturn(Boolean.FALSE);

        Description description = describer.describe(eventUpdate);
        assertThat(description, nullValue());
    }

    // -------------------- HELPERS --------------------
    private void setDescription(String originalDescription, String updatedDescription) {
        PowerMockito.when(original.getDescription()).thenReturn(originalDescription);
        PowerMockito.when(updated.getDescription()).thenReturn(updatedDescription);
    }
}

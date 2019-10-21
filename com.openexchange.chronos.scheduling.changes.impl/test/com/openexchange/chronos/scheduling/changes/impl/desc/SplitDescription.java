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

import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import org.dmfs.rfc5545.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RelatedTo;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.scheduling.changes.Description;
import com.openexchange.regional.RegionalSettings;
import com.openexchange.regional.RegionalSettingsUtil;

/**
 * {@link SplitDescription}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
@PrepareForTest({CalendarUtils.class, RegionalSettingsUtil.class, RegionalSettings.class})
@RunWith(PowerMockRunner.class)
public class SplitDescription extends AbstractDescriptionTest {

    /**
     * Initializes a new {@link SplitDescription}.
     */
    public SplitDescription() {
        super(EventField.RELATED_TO, "The appointment series was updated, beginning at", () -> {
            return new SplitDescriber();
        });
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        describer = new SplitDescriber();
        PowerMockito.spy(CalendarUtils.class);
        PowerMockito.doReturn(Boolean.TRUE).when(CalendarUtils.class, "isSeriesMaster", updated);
        PowerMockito.mockStatic(RegionalSettings.class);
    }

    @Test
    public void testSplit_newValue_DescriptionAvailable() {
        DateTime time = new DateTime(System.currentTimeMillis());
        setStartDate(time);
        setRelateTo(null, new RelatedTo("X-CALENDARSERVER-RECURRENCE-SET", null));

        Description description = describer.describe(eventUpdate);
        testDescription(description);
        String date = DateFormat.getDateInstance(DateFormat.LONG, Locale.ENGLISH).format(new Date(time.getTimestamp()));
        checkMessageStart(description, date);
    }

    @Test
    public void testSplit_NoValues_DescriptionUnavailable() {
        setRelateTo(null, null);

        Description description = describer.describe(eventUpdate);
        assertThat(description, nullValue());
    }

    // -------------------- HELPERS --------------------
    private void setStartDate(DateTime time) {
        PowerMockito.when(updated.getStartDate()).thenReturn(time);
    }

    private void setRelateTo(RelatedTo originalRT, RelatedTo updatedRT) {
        PowerMockito.when(original.getRelatedTo()).thenReturn(originalRT);
        PowerMockito.when(updated.getRelatedTo()).thenReturn(updatedRT);
    }

}

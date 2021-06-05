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

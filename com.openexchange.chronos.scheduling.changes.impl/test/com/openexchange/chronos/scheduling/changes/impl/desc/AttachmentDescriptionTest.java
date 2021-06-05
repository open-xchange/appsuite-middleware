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

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.scheduling.changes.Description;

/**
 * {@link AttachmentDescriptionTest}
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Ottersbach</a>
 * @since v7.10.3
 */
@RunWith(PowerMockRunner.class)
public class AttachmentDescriptionTest extends AbstractDescriptionTest {

    private static List<Attachment> attachments;
    private static String addMessage = "The appointment has a new attachment";
    private static String removeMessage = "was removed from the appointment.";

    /**
     * Initializes a new {@link AttachmentDescriptionTest}.
     * 
     * @param field The field to test
     * @param descriptionMessage The introduction message of the description
     */
    public AttachmentDescriptionTest() {
        super(EventField.ATTACHMENTS, "", () -> {
            return new AttachmentDescriber();
        });
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        attachments = new ArrayList<Attachment>();
        Attachment attachment = new Attachment();
        attachment.setFilename("NewAttachment");
        attachments.add(attachment);
    }

    @Test
    public void testAttachment_AddNewAttachment_DescriptionAvailable() {
        setAttachments(null, attachments);

        descriptionMessage = addMessage;
        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessageStart(description, attachments.get(0).getFilename());
    }

    @Test
    public void testAttachment_RemoveAttachment_DescriptionAvailable() {
        setAttachments(attachments, null);

        descriptionMessage = removeMessage;
        Description description = describer.describe(eventUpdate);
        testDescription(description);
        checkMessageEnd(description, attachments.get(0).getFilename());
    }

    // -------------------- HELPERS --------------------

    private void setAttachments(List<Attachment> originalAttachments, List<Attachment> updatedAttachments) {
        PowerMockito.when(eventUpdate.getAttachmentUpdates()).thenReturn(CalendarUtils.getAttachmentUpdates(originalAttachments, updatedAttachments));
        PowerMockito.when(original.getAttachments()).thenReturn(originalAttachments);
        PowerMockito.when(updated.getAttachments()).thenReturn(updatedAttachments);
    }

}

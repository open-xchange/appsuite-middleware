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

package com.openexchange.groupware.update.tasks;

import java.util.Arrays;
import com.openexchange.groupware.update.SimpleConvertUtf8ToUtf8mb4UpdateTask;

/**
 * {@link SequenceTablesUtf8Mb4UpdateTask}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SequenceTablesUtf8Mb4UpdateTask extends SimpleConvertUtf8ToUtf8mb4UpdateTask {

    /**
     * Initialises a new {@link SequenceTablesUtf8Mb4UpdateTask}.
     */
    public SequenceTablesUtf8Mb4UpdateTask() {
        //@formatter:off
        super(Arrays.asList("sequence_id", "sequence_principal", "sequence_resource", "sequence_resource_group",
            "sequence_folder", "sequence_calendar", "sequence_contact", "sequence_task", "sequence_project",
            "sequence_infostore", "sequence_forum", "sequence_pinboard", "sequence_attachment", "sequence_gui_setting",
            "sequence_reminder", "sequence_ical", "sequence_webdav", "sequence_uid_number", "sequence_gid_number",
            "sequence_mail_service"));
        //@formatter:on
    }
}

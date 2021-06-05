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

package com.openexchange.chronos.scheduling.changes;

import java.util.Date;
import java.util.List;
import com.openexchange.annotation.Nullable;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.scheduling.RecipientSettings;

/**
 * {@link ScheduleChange}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 * @see <a href="https://github.com/apple/ccs-calendarserver/blob/master/doc/Extensions/caldav-schedulingchanges.txt">Proposal caldav-schedulingchanges</a>
 */
public interface ScheduleChange {

    /**
     * The date when the change was created
     *
     * @return The date of the change
     */
    Date getTimeStamp();

    /**
     * 
     * The change action
     *
     * @return The {@link ChangeAction}
     */
    ChangeAction getAction();

    /**
     * 
     * Get the actual changes
     *
     * @return The changes or an empty list
     */
    List<Change> getChanges();
    
    
    /**
     * Get the participant status of the originator
     *
     * @return The participant status of the originator, or <code>null</code> if not set
     */
    @Nullable
    ParticipationStatus getOriginatorPartStat();

    /**
     * Renders a representation in plain text of the schedule changes for a specific recipient.
     * 
     * @param recipientSettings The recipient settings for the rendered schedule change
     * @return The description of the schedule changes
     */
    String getText(RecipientSettings recipientSettings);

    /**
     * Renders a representation in HTML of the schedule changes for a specific recipient.
     * 
     * @param recipientSettings The recipient settings for the rendered schedule change
     * @return The description of the schedule changes
     */
    String getHtml(RecipientSettings recipientSettings);

    /*
     * Get the changes describes in XML format
     *
     * @param recipientSettings The recipient settings for the rendered schedule change
     * 
     * @return The changes described
     */
    //    String getXml(RecipientSettings recipientSettings);

}

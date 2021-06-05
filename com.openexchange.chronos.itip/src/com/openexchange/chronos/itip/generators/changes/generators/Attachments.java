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

package com.openexchange.chronos.itip.generators.changes.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.itip.generators.ArgumentType;
import com.openexchange.chronos.itip.generators.Sentence;
import com.openexchange.chronos.itip.generators.changes.ChangeDescriptionGenerator;
import com.openexchange.chronos.itip.tools.ITipEventUpdate;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tools.mappings.common.SimpleCollectionUpdate;
import com.openexchange.java.Strings;

/**
 * {@link Attachments}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class Attachments implements ChangeDescriptionGenerator {

    @Override
    public EventField[] getFields() {
        return new EventField[] { EventField.ATTACHMENTS };
    }

    @Override
    public List<Sentence> getDescriptions(Context ctx, Event original, Event updated, ITipEventUpdate diff, Locale locale, TimeZone timezone) throws OXException {
        SimpleCollectionUpdate<Attachment> updates = diff.getAttachmentUpdates();
        if (null == updates || updates.isEmpty()) {
            return null;
        }
        /*
         * Describe added and removed attachments
         */
        ArrayList<Sentence> sentences = new ArrayList<Sentence>(updates.getAddedItems().size() + updates.getRemovedItems().size());
        for (Attachment attachment : updates.getAddedItems()) {
            if (null != attachment && Strings.isNotEmpty(attachment.getFilename())) {
                sentences.add(new Sentence(Messages.HAS_ADDED_ATTACHMENT).add(attachment.getFilename(), ArgumentType.ITALIC));
            }
        }
        for (Attachment attachment : updates.getRemovedItems()) {
            if (null != attachment && Strings.isNotEmpty(attachment.getFilename())) {
                sentences.add(new Sentence(Messages.HAS_REMOVED_ATTACHMENT).add(attachment.getFilename(), ArgumentType.ITALIC));
            }
        }
        return sentences;
    }

}

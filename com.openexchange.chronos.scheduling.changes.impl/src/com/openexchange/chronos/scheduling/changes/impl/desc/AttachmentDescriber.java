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
import com.openexchange.annotation.NonNull;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.itip.generators.ArgumentType;
import com.openexchange.chronos.scheduling.changes.Description;
import com.openexchange.chronos.scheduling.changes.impl.ChangeDescriber;
import com.openexchange.chronos.scheduling.changes.impl.SentenceImpl;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.groupware.tools.mappings.common.SimpleCollectionUpdate;
import com.openexchange.java.Strings;

/**
 * {@link AttachmentDescriber}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class AttachmentDescriber implements ChangeDescriber {

    /**
     * Initializes a new {@link AttachmentDescriber}.
     */
    public AttachmentDescriber() {}

    @Override
    @NonNull
    public EventField[] getFields() {
        return new EventField[] { EventField.ATTACHMENTS };
    }

    @Override
    public Description describe(EventUpdate eventUpdate) {
        SimpleCollectionUpdate<Attachment> updates = eventUpdate.getAttachmentUpdates();
        if (null == updates || updates.isEmpty()) {
            return null;
        }
        /*
         * Describe added and removed attachments
         */
        ArrayList<SentenceImpl> sentences = new ArrayList<SentenceImpl>(updates.getAddedItems().size() + updates.getRemovedItems().size());
        for (Attachment attachment : updates.getAddedItems()) {
            if (null != attachment && Strings.isNotEmpty(attachment.getFilename())) {
                sentences.add(new SentenceImpl(Messages.HAS_ADDED_ATTACHMENT).add(attachment.getFilename(), ArgumentType.ITALIC));
            }
        }
        for (Attachment attachment : updates.getRemovedItems()) {
            if (null != attachment && Strings.isNotEmpty(attachment.getFilename())) {
                sentences.add(new SentenceImpl(Messages.HAS_REMOVED_ATTACHMENT).add(attachment.getFilename(), ArgumentType.ITALIC));
            }
        }
        return new DefaultDescription(sentences, EventField.ATTACHMENTS);
    }
}

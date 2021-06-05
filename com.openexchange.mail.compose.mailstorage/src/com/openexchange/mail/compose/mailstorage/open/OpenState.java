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

package com.openexchange.mail.compose.mailstorage.open;

import java.util.List;
import java.util.UUID;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.MessageDescription;
import com.openexchange.mail.compose.Meta;
import com.openexchange.mail.compose.SharedFolderReference;

/**
 * {@link OpenState} - Simple helper class to remember the state during opening a composition space.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class OpenState {

    // Invariants
    public final UUID compositionSpaceId;
    public final MessageDescription message;
    public final Meta.Builder metaBuilder;
    public final Boolean encrypt;

    // Variants
    public List<Attachment> attachments = null;
    public MailServletInterface mailInterface = null;
    public SharedFolderReference sharedFolderRef = null;
    public boolean referencesOpenCompositionSpace = false;

    /**
     * Initializes a new {@link OpenState}.
     *
     * @param compositionSpaceId The composition space identifier
     * @param message The message to compose
     * @param encrypt Whether the composition space to open shall be encrypted
     * @param metaBuilder The meta-data builder
     */
    public OpenState(UUID compositionSpaceId, MessageDescription message, Boolean encrypt, Meta.Builder metaBuilder) {
        super();
        this.compositionSpaceId = compositionSpaceId;
        this.encrypt = encrypt;
        this.metaBuilder = metaBuilder;
        this.message = message;
    }

}
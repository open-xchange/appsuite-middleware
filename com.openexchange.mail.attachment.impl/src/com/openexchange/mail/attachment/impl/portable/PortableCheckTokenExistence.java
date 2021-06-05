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

package com.openexchange.mail.attachment.impl.portable;

import java.io.IOException;
import java.util.concurrent.Callable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;
import com.openexchange.mail.attachment.AttachmentToken;
import com.openexchange.mail.attachment.impl.AttachmentTokenRegistry;


/**
 * {@link PortableCheckTokenExistence}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public class PortableCheckTokenExistence extends AbstractCustomPortable implements Callable<PortableAttachmentToken> {

    private static final String FIELD_ID = "tokenId";
    private static final String FIELD_CHUNKED = "chunked";

    private String tokenId;
    private boolean chunked;

    /**
     * Initializes a new {@link PortableCheckTokenExistence}.
     */
    public PortableCheckTokenExistence() {
        super();
    }

    /**
     * Initializes a new {@link PortableCheckTokenExistence}.
     *
     * @param tokenId The associated token identifier
     */
    public PortableCheckTokenExistence(String tokenId, boolean chunked) {
        super();
        this.tokenId = tokenId;
        this.chunked = chunked;
    }

    @Override
    public PortableAttachmentToken call() throws Exception {
        AttachmentTokenRegistry registry = AttachmentTokenRegistry.getInstance();
        if (null == registry) {
            return new PortableAttachmentToken(null);
        }
        AttachmentToken token = registry.getToken(tokenId, chunked, false);
        return new PortableAttachmentToken(token);
    }

    @Override
    public int getClassId() {
        return 107;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeUTF(FIELD_ID, tokenId);
        writer.writeBoolean(FIELD_CHUNKED, chunked);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        this.tokenId = reader.readUTF(FIELD_ID);
        this.chunked = reader.readBoolean(FIELD_CHUNKED);
    }

}

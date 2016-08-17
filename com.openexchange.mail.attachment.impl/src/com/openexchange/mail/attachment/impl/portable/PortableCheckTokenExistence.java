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
 *    trademarks of the OX Software GmbH group of companies.
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

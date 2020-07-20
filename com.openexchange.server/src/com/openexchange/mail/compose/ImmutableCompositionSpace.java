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

package com.openexchange.mail.compose;

import java.util.Date;
import java.util.Optional;
import com.openexchange.mail.MailPath;

/**
 * {@link ImmutableCompositionSpace}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public class ImmutableCompositionSpace implements CompositionSpace {

    private final CompositionSpaceId id;
    private final MailPath mailPath;
    private final Message message;
    private final long lastModified;

    /**
     * Initializes a new {@link ImmutableCompositionSpace}.
     *
     * @param id The composition space identifier
     * @param mailPath The optional mail path associated with composition space or <code>null</code>
     * @param message The message
     * @param lastModified The last-modified time stamp; the number of milliseconds since January 1, 1970, 00:00:00 GMT
     */
    public ImmutableCompositionSpace(CompositionSpaceId id, MailPath mailPath, Message message, long lastModified) {
        super();
        this.id = id;
        this.mailPath = mailPath;
        this.message = message;
        this.lastModified = lastModified;
    }

    @Override
    public CompositionSpaceId getId() {
        return id;
    }

    @Override
    public Optional<MailPath> getMailPath() {
        return Optional.ofNullable(mailPath);
    }

    @Override
    public Message getMessage() {
        return message;
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (id != null) {
            builder.append("id=").append(id).append(", ");
        }
        if (mailPath != null) {
            builder.append("mailPath=").append(mailPath).append(", ");
        }
        if (message != null) {
            builder.append("message=").append(message).append(", ");
        }
        builder.append("lastModified=").append(new Date(lastModified)).append("]");
        return builder.toString();
    }

}

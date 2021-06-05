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

package com.openexchange.mail.search;

import java.util.Collection;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.converters.MimeMessageConverter;

/**
 * {@link FlagTerm}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FlagTerm extends SearchTerm<Integer> {

    private static final long serialVersionUID = -6887694637971347838L;

    private final boolean set;

    private final int flags;

    /**
     * Initializes a new {@link FlagTerm}
     */
    public FlagTerm(int flag, boolean set) {
        super();
        flags = flag;
        this.set = set;
    }

    @Override
    public void accept(SearchTermVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Gets the flags pattern: Either a positive integer if enabled or a negative integer if disabled
     *
     * @return The flags pattern
     */
    @Override
    public Integer getPattern() {
        return set ? Integer.valueOf(flags) : Integer.valueOf(flags * -1);
    }

    @Override
    public void addMailField(Collection<MailField> col) {
        col.add(MailField.FLAGS);
    }

    @Override
    public boolean matches(MailMessage mailMessage) {
        if (set) {
            return ((mailMessage.getFlags() & flags) == flags);
        }
        return ((mailMessage.getFlags() & flags) == 0);
    }

    @Override
    public boolean matches(Message msg) throws OXException {
        final Flags flagsObj = MimeMessageConverter.convertMailFlags(flags);
        final Flags msgFlags;
        try {
            msgFlags = msg.getFlags();
        } catch (MessagingException e) {
            org.slf4j.LoggerFactory.getLogger(FlagTerm.class).warn("Error during search.", e);
            return false;
        }
        return set ? msgFlags.contains(flagsObj) : !msgFlags.contains(flagsObj);
    }

    @Override
    public javax.mail.search.SearchTerm getJavaMailSearchTerm() {
        return new javax.mail.search.FlagTerm(MimeMessageConverter.convertMailFlags(flags), set);
    }

    @Override
    public javax.mail.search.SearchTerm getNonWildcardJavaMailSearchTerm() {
        return getJavaMailSearchTerm();
    }

    @Override
    public void contributeTo(FetchProfile fetchProfile) {
        if (!fetchProfile.contains(FetchProfile.Item.FLAGS)) {
            fetchProfile.add(FetchProfile.Item.FLAGS);
        }
    }

}

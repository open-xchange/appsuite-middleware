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
    public FlagTerm(final int flag, final boolean set) {
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
    public void addMailField(final Collection<MailField> col) {
        col.add(MailField.FLAGS);
    }

    @Override
    public boolean matches(final MailMessage mailMessage) {
        if (set) {
            return ((mailMessage.getFlags() & flags) == flags);
        }
        return ((mailMessage.getFlags() & flags) == 0);
    }

    @Override
    public boolean matches(final Message msg) throws OXException {
        final Flags flagsObj = MimeMessageConverter.convertMailFlags(flags);
        final Flags msgFlags;
        try {
            msgFlags = msg.getFlags();
        } catch (final MessagingException e) {
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

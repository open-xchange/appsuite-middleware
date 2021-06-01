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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link UserFlagTerm}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UserFlagTerm extends SearchTerm<String[]> {

    private static final long serialVersionUID = -6887694637971347838L;

    private final boolean set;
    private final String[] userFlags;

    /**
     * Initializes a new {@link UserFlagTerm}.
     *
     * @throws IllegalArgumentException If arguments are invalid
     */
    public UserFlagTerm(String userFlag, boolean set) {
        super();
        if (Strings.isEmpty(userFlag)) {
            throw new IllegalArgumentException("User flag must not be null or empty");
        }
        this.userFlags = new String[] { userFlag };
        this.set = set;
    }

    /**
     * Initializes a new {@link UserFlagTerm}.
     *
     * @throws IllegalArgumentException If arguments are invalid
     */
    public UserFlagTerm(String[] userFlags, boolean set) {
        super();
        if (null == userFlags || 0 == userFlags.length) {
            throw new IllegalArgumentException("User flags must not be null or empty");
        }
        for (int i = userFlags.length; i-- > 0;) {
            if (Strings.isEmpty(userFlags[i])) {
                throw new IllegalArgumentException("User flag must not be null or empty");
            }
        }
        this.userFlags = userFlags;
        this.set = set;
    }

    @Override
    public void accept(SearchTermVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Gets the <code>set</code> flag.
     *
     * @return The <code>set</code> flag
     */
    public boolean isSet() {
        return set;
    }

    /**
     * Gets the user flags pattern. See also {@link #isSet()}.
     *
     * @return The flags pattern
     */
    @Override
    public String[] getPattern() {
        return userFlags;
    }

    @Override
    public void addMailField(Collection<MailField> col) {
        col.add(MailField.FLAGS);
    }

    @Override
    public boolean matches(MailMessage mailMessage) {
        String[] userFlags = mailMessage.getUserFlags();
        if (null == userFlags) {
            return false;
        }

        String[] thisUserFlags = this.userFlags;
        for (int i = thisUserFlags.length; i-- > 0;) {
            if (set ? !contains(thisUserFlags[i], userFlags) : contains(thisUserFlags[i], userFlags)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean matches(Message msg) throws OXException {
        if (set) {
            Flags flagsObj = new Flags();
            String[] thisUserFlags = this.userFlags;
            for (int i = thisUserFlags.length, k = 0; i-- > 0;) {
                flagsObj.add(thisUserFlags[k++]);
            }

            Flags msgFlags;
            try {
                msgFlags = msg.getFlags();
            } catch (MessagingException e) {
                org.slf4j.LoggerFactory.getLogger(UserFlagTerm.class).warn("Error during search.", e);
                return false;
            }
            return msgFlags.contains(flagsObj);
        } else {
            List<Flags> flagsList = new ArrayList<>(this.userFlags.length);
            String[] thisUserFlags = this.userFlags;
            for (int i = thisUserFlags.length, k = 0; i-- > 0;) {
                Flags f = new Flags();
                f.add(thisUserFlags[k++]);
                flagsList.add(f);
            }

            Flags msgFlags;
            try {
                msgFlags = msg.getFlags();
            } catch (MessagingException e) {
                org.slf4j.LoggerFactory.getLogger(UserFlagTerm.class).warn("Error during search.", e);
                return false;
            }

            for (Flags f : flagsList) {
                if (msgFlags.contains(f)) {
                    return false;
                }
            }

            return true;

        }
    }

    @Override
    public javax.mail.search.SearchTerm getJavaMailSearchTerm() {
        Flags flagsObj = new Flags();
        String[] thisUserFlags = this.userFlags;
        for (int i = thisUserFlags.length, k = 0; i-- > 0;) {
            flagsObj.add(thisUserFlags[k++]);
        }
        return new javax.mail.search.FlagTerm(flagsObj, set);
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

    private boolean contains(String userFlag, String[] userFlags) {
        if (null != userFlag) {
            for (int i = userFlags.length; i-- > 0;) {
                if (userFlag.equals(userFlags[i])) {
                    return true;
                }
            }
        }
        return false;
    }

}

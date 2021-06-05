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

package com.openexchange.api.client.common.calls.user;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.java.Strings;

/**
 * {@link UserInformation}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class UserInformation {

    private final long lastModified;
    private final long lastModifiedUtc;
    private final int numberOfImages;
    private final String folderId;
    private final String sortName;
    private final int userId;
    private final int createdBy;
    private final int modifiedBy;
    private final int id;
    private final String email1;
    private final long creationDate;
    private final List<Integer> groups;
    private final String locale;
    private final int contactId;
    private final int guestCreatedBy;
    private final String timezone;
    private final List<String> aliases;

    /**
     * 
     * Initializes a new {@link UserInformation}.
     * 
     * @param builder The builder
     */
    UserInformation(Builder builder) {
        this.lastModified = builder.lastModified;
        this.lastModifiedUtc = builder.lastModifiedUtc;
        this.numberOfImages = builder.numberOfImages;
        this.folderId = builder.folderId;
        this.sortName = builder.sortName;
        this.userId = builder.userId;
        this.createdBy = builder.createdBy;
        this.modifiedBy = builder.modifiedBy;
        this.id = builder.id;
        this.email1 = builder.email1;
        this.creationDate = builder.creationDate;
        this.groups = builder.groups;
        this.locale = builder.locale;
        this.contactId = builder.contactId;
        this.guestCreatedBy = builder.guestCreatedBy;
        this.timezone = builder.timezone;
        this.aliases = builder.aliases;
    }

    /**
     * Gets the lastModified
     *
     * @return The lastModified
     */
    public long getLastModified() {
        return lastModified;
    }

    /**
     * Gets the lastModifiedUtc
     *
     * @return The lastModifiedUtc
     */
    public long getLastModifiedUtc() {
        return lastModifiedUtc;
    }

    /**
     * Gets the numberOfImages
     *
     * @return The numberOfImages
     */
    public int getNumberOfImages() {
        return numberOfImages;
    }

    /**
     * Gets the folderId
     *
     * @return The folderId
     */
    public String getFolderId() {
        return folderId;
    }

    /**
     * Gets the sortName
     *
     * @return The sortName
     */
    public String getSortName() {
        return sortName;
    }

    /**
     * Gets the userId
     *
     * @return The userId
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the createdBy
     *
     * @return The createdBy
     */
    public int getCreatedBy() {
        return createdBy;
    }

    /**
     * Gets the modifiedNy
     *
     * @return The modifiedNy
     */
    public int getModifiedBy() {
        return modifiedBy;
    }

    /**
     * Gets the id
     *
     * @return The id
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the email1
     *
     * @return The email1
     */
    public String getEmail1() {
        return email1;
    }

    /**
     * Gets the creationDate
     *
     * @return The creationDate
     */
    public long getCreationDate() {
        return creationDate;
    }

    /**
     * Gets the groups
     *
     * @return The groups
     */
    public List<Integer> getGroups() {
        return groups;
    }

    /**
     * Gets the locale
     *
     * @return The locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Gets the contactId
     *
     * @return The contactId
     */
    public int getContactId() {
        return contactId;
    }

    /**
     * Gets the guestCreatedBy
     *
     * @return The guestCreatedBy
     */
    public int getGuestCreatedBy() {
        return guestCreatedBy;
    }

    /**
     * Gets the timezone
     *
     * @return The timezone
     */
    public String getTimezone() {
        return timezone;
    }

    /**
     * Gets the aliases
     *
     * @return The aliases
     */
    public List<String> getAliases() {
        return aliases;
    }

    /**
     * 
     * {@link Builder}
     *
     * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
     * @since v7.10.5
     */
    public static class Builder {

        long lastModified;
        long lastModifiedUtc;
        int numberOfImages;
        String folderId;
        String sortName;
        int userId;
        int createdBy;
        int modifiedBy;
        int id;
        String email1;
        long creationDate;
        List<Integer> groups = new ArrayList<Integer>();
        String locale;
        int contactId;
        int guestCreatedBy;
        String timezone;
        List<String> aliases = new ArrayList<String>();

        /**
         * 
         * Initializes a new {@link Builder}.
         */
        public Builder() {}

        public Builder lastModified(long lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public Builder lastModifiedUtc(long lastModifiedUtc) {
            this.lastModifiedUtc = lastModifiedUtc;
            return this;
        }

        public Builder numberOfImages(int numberOfImages) {
            this.numberOfImages = numberOfImages;
            return this;
        }

        public Builder folderId(String folderId) {
            this.folderId = folderId;
            return this;
        }

        public Builder sortName(String sortName) {
            this.sortName = sortName;
            return this;
        }

        public Builder userId(int userId) {
            this.userId = userId;
            return this;
        }

        public Builder createdBy(int createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder modifiedBy(int modifiedBy) {
            this.modifiedBy = modifiedBy;
            return this;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder email1(String email1) {
            this.email1 = email1;
            return this;
        }

        public Builder creationDate(long creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public Builder group(List<Integer> group) {
            this.groups = group;
            return this;
        }

        public Builder addGroup(int group) {
            if (group < 0) {
                return this;
            }
            this.groups.add(I(group));
            return this;
        }

        public Builder locale(String locale) {
            this.locale = locale;
            return this;
        }

        public Builder contactId(int contactId) {
            this.contactId = contactId;
            return this;
        }

        public Builder guestCreatedBy(int guestCreatedBy) {
            this.guestCreatedBy = guestCreatedBy;
            return this;
        }

        public Builder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        public Builder aliases(List<String> aliases) {
            this.aliases = aliases;
            return this;
        }

        public Builder addAliases(String alias) {
            if (Strings.isNotEmpty(alias)) {
                this.aliases.add(alias);
            }
            return this;
        }

        public UserInformation build() {

            return new UserInformation(this);
        }
    }

}

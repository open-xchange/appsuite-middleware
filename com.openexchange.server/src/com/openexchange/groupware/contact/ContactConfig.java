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

package com.openexchange.groupware.contact;

import java.util.Properties;
import com.openexchange.config.ConfigurationService;

/**
 * Configuration class for contact options.
 * <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class ContactConfig {

    private static final String FILENAME = "contact.properties";

    private static final ContactConfig SINGLETON = new ContactConfig();

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactConfig.class);

    private final Properties props = new Properties();

    private long maxImageSize = 33750000;

    /**
     * Prevent instantiation.
     */
    private ContactConfig() {
        super();
    }

    public static ContactConfig getInstance() {
        return SINGLETON;
    }

    /**
     * @param configuration the configuration service.
     */
    public void initialize(ConfigurationService configuration) {
        final Properties props = configuration.getFile(FILENAME);
        if (null == props) {
            LOG.info("Configuration file {} is missing. Using defaults.", FILENAME);
        } else {
            this.props.clear();
            this.props.putAll(props);
            LOG.info("Read configuration file {}.", FILENAME);
        }
        parse();
    }

    private void parse() {
        try {
            maxImageSize = Long.parseLong(getString(Property.MAX_IMAGE_SIZE));
        } catch (NumberFormatException e) {
            LOG.error("Unable to parse value of property {} in {}.", Property.MAX_IMAGE_SIZE.propertyName, FILENAME, e);
            maxImageSize = 33750000;
        }
    }

    public String getProperty(final String key) {
        logNotInitialized();
        return props.getProperty(key);
    }

    /**
     * Gets the value of a property from the file.
     * 
     * @param key name of the property.
     * @return the value of the property.
     */
    public String getString(final Property key) {
        logNotInitialized();
        return props.getProperty(key.propertyName, key.defaultValue);
    }

    /**
     * Gets the value of a property from the file.
     * 
     * @param key name of the property.
     * @return the value of the property.
     */
    public Boolean getBoolean(final Property key) {
        logNotInitialized();
        return Boolean.valueOf(props.getProperty(key.propertyName, key.defaultValue));
    }

    private void logNotInitialized() {
        if (props.isEmpty()) {
            LOG.info("Configuration file {} not read. Using defaults.", FILENAME);
        }
    }

    public long getMaxImageSize() {
        logNotInitialized();
        return maxImageSize;
    }

    /**
     * Properties of the contact properties file.
     */
    public enum Property {
        /**
         * Determines if a search for emailable contact is triggered on opened recipient dialog.
         */
        AUTO_SEARCH("com.openexchange.contact.mailAddressAutoSearch", Boolean.TRUE.toString()),
        /**
         * Searching for contacts can be done in a single folder or globally across all folders. Searching across all folders can cause high
         * server and database load because first all visible folders must be determined and if a user has object read permissions in that
         * folders. Software internal default is true to prevent high load if the property is not defined. Default here is true because it
         * is easier for the user to find contacts.
         */
        SINGLE_FOLDER_SEARCH("com.openexchange.contact.singleFolderSearch", Boolean.TRUE.toString()),
        /**
         * Determines the field that will be used if contacts should be searched by a starting letter.
         */
        LETTER_FIELD("contact_first_letter_field", "field02"),
        /**
         * Enables/Disables the start letter based quick select of contacts
         */
        CHARACTER_SEARCH("com.openexchange.contacts.characterSearch", Boolean.TRUE.toString()),
        /**
         * The auto complete search for email addresses may be triggered easily and quite often if a new email is written and a part of a
         * recipients address is written. This can lead to high load on the database system if a context has a lot of users and a lot of
         * contacts. Therefore the scope if this search can be configured. Set this parameter to true and the auto complete search looks in
         * every readable contact folder for contacts with emails addresses matching the already typed letters. If this parameter is
         * configured to false, only three folders are considered for the search: the users private default contact folder, his contact
         * folder for collected contacts and the global address book if that is enabled for the user.
         */
        ALL_FOLDERS_FOR_AUTOCOMPLETE("com.openexchange.contacts.allFoldersForAutoComplete", Boolean.TRUE.toString()),
        /**
         * Maximum size in bytes of an image that may be stored.
         */
        MAX_IMAGE_SIZE("max_image_size", "33750000"),

        /**
         * Check the entered email address from a new contact for correctness
         * (syntactic check user@domain.tld)
         */
        VALIDATE_CONTACT_EMAIL("validate_contact_email", Boolean.TRUE.toString()),

        /**
         * Enables / Disables scaling of contact images
         */
        SCALE_IMAGES("com.openexchange.contact.image.scaleImages", Boolean.TRUE.toString()),

        /**
         * Defines the width of scaled contact images
         */
        SCALED_IMAGE_WIDTH("com.openexchange.contact.image.maxWidth", "250"),

        /**
         * Defines the height of scaled contact images
         */
        SCALED_IMAGE_HEIGHT("com.openexchange.contact.image.maxHeight", "250"),

        /**
         * Defines the scale type
         */
        SCALE_TYPE("com.openexchange.contact.image.scaleType", "2");

        /**
         * Name of the property in the participant.properties file.
         */
        private String propertyName;

        /**
         * Default value of the property.
         */
        private String defaultValue;

        /**
         * Default constructor.
         * 
         * @param keyName Name of the property in the participant.properties
         *            file.
         * @param value Default value of the property.
         */
        private Property(final String keyName, final String value) {
            this.propertyName = keyName;
            this.defaultValue = value;
        }
    }
}

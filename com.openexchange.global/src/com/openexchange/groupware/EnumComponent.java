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

package com.openexchange.groupware;

import java.util.Map;
import com.google.common.collect.ImmutableMap;

public enum EnumComponent implements Component {

    /**
     * Module APPOINTMENT
     */
    APPOINTMENT("APP"),
    /**
     * Module TASK
     */
    TASK("TSK"),
    /**
     * Module CONTACT
     */
    CONTACT("CON"),
    /**
     * Module KNOWKLEDGE
     */
    KNOWLEDGE("KNW"),
    /**
     * Module DOCUMENT
     */
    DOCUMENT("DOC"),
    /**
     * Module BOOKMARKS
     */
    BOOKMARKS("BKM"),
    /**
     * Module MAIL
     */
    MAIL("MSG"),
    /**
     * Module FOLDER
     */
    FOLDER("FLD"),
    /**
     * Module USER_SETTING
     */
    USER_SETTING("USS"),
    /**
     * Module LINKING
     */
    LINKING("LNK"),
    /**
     * Module REMINDER
     */
    REMINDER("REM"),
    /**
     * Module ICAL
     */
    ICAL("ICA"),
    /**
     * Module VCARD
     */
    VCARD("VCR"),
    /**
     * Module PARTICIPANT
     */
    PARTICIPANT("PAR"),
    /**
     * Module GROUPUSER
     */
    GROUPUSER("GRU"),
    /**
     * Module USER
     */
    USER("USR"),
    /**
     * Module GROUP
     */
    GROUP("GRP"),
    /**
     * Module PRINCIPAL
     */
    PRINCIPAL("PRP"),
    /**
     * Module RESOURCE
     */
    RESOURCE("RES"),
    /**
     * Module INFOSTORE
     */
    INFOSTORE("IFO"),
    /**
     * Module LOGIN. Especially for the external login methods.
     */
    LOGIN("LGI"),
    /**
     * Database connection pooling.
     */
    DB_POOLING("DBP"),
    /**
     * Module NONE
     */
    NONE("NON"),
    /**
     * Context module.
     */
    CONTEXT("CTX"),
    /**
     * Cache module.
     */
    CACHE("CAC"),
    /**
     * File storage module.
     */
    FILESTORE("FLS"),
    /**
     * Admin daemon user module.
     */
    ADMIN_USER("ADM_USR"),
    /**
     * Admin daemon context module.
     */
    ADMIN_CONTEXT("ADM_CTX"),
    /**
     * Admin daemon group module.
     */
    ADMIN_GROUP("ADM_GRP"),
    /**
     * Admin daemon resource module.
     */
    ADMIN_RESOURCE("ADM_RES"),
    /**
     * Admin daemon util module.
     */
    ADMIN_UTIL("ADM_UTL"),
    /**
     * LDAP methods.
     */
    LDAP("LDP"),
    /**
     * Problem in the servlet engine.
     */
    SERVLET("SVL"),
    /**
     * Configuration system.
     */
    CONFIGURATION("CFG"),
    /**
     * Transaction System used in Attachments and Infostore
     */
    TRANSACTION("TAX"),
    /**
     * Attachments
     */
    ATTACHMENT("ATT"),
    /**
     * Import and export (with CSV, iCal, TNEF and whatever)
     */
    IMPORT_EXPORT("I_E"),
    /**
     * Upload
     */
    UPLOAD("UPL"),
    /**
     * Update
     */
    UPDATE("UPD"),
    /**
     * Session.
     */
    SESSION("SES"),
    /**
     * SyncML
     */
    SYNCML("SYN"),
    /**
     * Spell Check
     */
    PUSHUDP("PUSHUDP"),
    /**
     * Spell Check
     */
    SPELLCHECK("SPC"),
    /**
     * ACL related error
     */
    ACL_ERROR("ACL"),
    /**
     * PERMISSION related error
     */
    PERMISSION("PERMISSION"),
    /**
     * Any errors related to handling a <code></code>
     */
    DELETE_EVENT("DEL"),
    /**
     * Any errors related to mail filter <code></code>
     */
    MAIL_FILTER("MAIL_FILTER"),
    /**
     * Any errors related to an OSGi service
     */
    SERVICE("SRV"),
    /**
     * Any errors related to handling a downgrade event
     */
    DOWNGRADE_EVENT("DOW"),
    /**
     * Consistency Tool
     */
    CONSISTENCY("CSTY"),
    /**
     * Axis2 related errors
     */
    AXIS2("AXIS2"),
    /**
     * WebDAV/XML related errors
     */
    WEBDAV("WEBDAV"),
    /**
     * HTTP CLIENT
     */
    HTTP_CLIENT("HC"),
    /**
     * Internet free busy relevant errors
     */
    FREEBUSY("FREEBUSY")
    ;

    /**
     * The abbreviation for components.
     */
    private String abbreviation;

    /**
     * Default constructor.
     *
     * @param abbrevation Abbreviation for the component.
     */
    private EnumComponent(final String abbrevation) {
        abbreviation = abbrevation;
    }

    public static EnumComponent byAbbreviation(final String abbrev) {
        return ABBREV2COMPONENT.get(abbrev);
    }

    @Override
    public String getAbbreviation() {
        return abbreviation;
    }

    private static final Map<String, EnumComponent> ABBREV2COMPONENT;

    static {
        ImmutableMap.Builder<String, EnumComponent> tmp = ImmutableMap.builder();
        for (final EnumComponent component : values()) {
            tmp.put(component.abbreviation, component);
        }
        ABBREV2COMPONENT = tmp.build();
    }
}

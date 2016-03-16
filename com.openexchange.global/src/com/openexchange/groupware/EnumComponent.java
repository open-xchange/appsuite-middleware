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

package com.openexchange.groupware;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

    /*
     * (non-Javadoc)
     * @see com.openexchange.groupware.Component#getAbbreviation()
     */
    @Override
    public String getAbbreviation() {
        return abbreviation;
    }

    private static final Map<String, EnumComponent> ABBREV2COMPONENT;

    static {
        final Map<String, EnumComponent> tmp = new HashMap<String, EnumComponent>(values().length, 1F);
        for (final EnumComponent component : values()) {
            tmp.put(component.abbreviation, component);
        }
        ABBREV2COMPONENT = Collections.unmodifiableMap(tmp);
    }
}

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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.index.solr.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.openexchange.config.ConfigurationService;
import com.openexchange.index.mail.MailIndexField;
import com.openexchange.index.solr.internal.Services;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.solr.SolrProperties;

/**
 * {@link SolrMailField}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public enum SolrMailField {

    UUID("UUID", MailIndexField.UUID, "param1"),
    TIMESTAMP("TIMESTAMP", MailIndexField.TIMESTAMP, "param2"),
    ACCOUNT("ACCOUNT", MailIndexField.ACCOUNT, "param3"),
    FULL_NAME("FULL_NAME", MailIndexField.FULL_NAME, "param4"),
    ID("ID", MailIndexField.ID, "param5"),
    COLOR_LABEL("COLOR_LABEL", MailIndexField.COLOR_LABEL, "param6"),
    ATTACHMENT("ATTACHMENT", MailIndexField.ATTACHMENT, "param7"),
    RECEIVED_DATE("RECEIVED_DATE", MailIndexField.RECEIVED_DATE, "param8"),
    SENT_DATE("SENT_DATE", MailIndexField.SENT_DATE, "param9"),
    SIZE("SIZE", MailIndexField.SIZE, "param10"),
    FLAG_ANSWERED("FLAG_ANSWERED", MailIndexField.FLAG_ANSWERED, "param11"),
    FLAG_DELETED("FLAG_DELETED", MailIndexField.FLAG_DELETED, "param12"),
    FLAG_DRAFT("FLAG_DRAFT", MailIndexField.FLAG_DRAFT, "param13"),
    FLAG_FLAGGED("FLAG_FLAGGED", MailIndexField.FLAG_FLAGGED, "param14"),
    FLAG_RECENT("FLAG_RECENT", MailIndexField.FLAG_RECENT, "param15"),
    FLAG_SEEN("FLAG_SEEN", MailIndexField.FLAG_SEEN, "param16"),
    FLAG_USER("FLAG_USER", MailIndexField.FLAG_USER, "param17"),
    FLAG_SPAM("FLAG_SPAM", MailIndexField.FLAG_SPAM, "param18"),
    FLAG_FORWARDED("FLAG_FORWARDED", MailIndexField.FLAG_FORWARDED, "param19"),
    FLAG_READ_ACK("FLAG_READ_ACK", MailIndexField.FLAG_READ_ACK, "param20"),
    USER_FLAGS("USER_FLAGS", MailIndexField.USER_FLAGS, "param21"),
    FROM("FROM", MailIndexField.FROM, "param22"),
    TO("TO", MailIndexField.TO, "param23"),
    CC("CC", MailIndexField.CC, "param24"),
    BCC("BCC", MailIndexField.BCC, "param25"),
    SUBJECT("SUBJECT", MailIndexField.SUBJECT, "param26"),
    CONTENT_FLAG("CONTENT_FLAG", MailIndexField.CONTENT_FLAG, "param27"),
    CONTENT("CONTENT", MailIndexField.CONTENT, "param28");

    private static final String PROP_FILE = "solr_mailfields.properties";

    private static final Map<MailIndexField, SolrMailField> fieldMapping = new EnumMap<MailIndexField, SolrMailField>(MailIndexField.class);

    private static final Set<MailIndexField> indexedFields;

    private static Properties properties = null;

    private final String propertyName;

    private final MailIndexField indexField;
    
    private final String customParameter;

    static {
        checkProperties();
        final Set<MailIndexField> set = EnumSet.noneOf(MailIndexField.class);
        for (final SolrMailField field : values()) {
            fieldMapping.put(field.indexField, field);

            if (null == field.solrName()) {
                set.add(field.indexField);
            }
        }
        indexedFields = Collections.unmodifiableSet(set);
    }

    private SolrMailField(final String propertyName, final MailIndexField indexField, final String customParameter) {
        this.propertyName = propertyName;
        this.indexField = indexField;
        this.customParameter = customParameter;
    }

    public String solrName() {
        final String value = properties.getProperty(propertyName);
        if (StringUtils.isEmpty(value)) {
            return null;
        }

        return value;
    }
    
    public String parameterName() {
        return customParameter;
    }
    
    public boolean isIndexed() {
        return !StringUtils.isEmpty(properties.getProperty(propertyName));
    }

    public static Set<MailIndexField> getIndexedFields() {
        return indexedFields;
    }

    public static String[] solrNamesFor(final SolrMailField[] fields) {
        final List<String> names = new ArrayList<String>();
        for (final SolrMailField field : fields) {
            final String solrName = field.solrName();
            if (solrName != null) {
                names.add(solrName);
            }
        }

        return names.toArray(new String[names.size()]);
    }

    public static SolrMailField solrMailFieldFor(final MailIndexField indexField) {
        final SolrMailField solrField = fieldMapping.get(indexField);
        return solrField;
    }

    public static SolrMailField[] solrMailFieldsFor(final Set<MailIndexField> indexFields) {
        final List<SolrMailField> solrFields = new ArrayList<SolrMailField>();
        for (final MailIndexField indexField : indexFields) {
            final SolrMailField solrField = fieldMapping.get(indexField);
            if (solrField != null) {
                solrFields.add(solrField);
            }
        }

        return solrFields.toArray(new SolrMailField[solrFields.size()]);
    }

    public Object getValueFromMail(final MailMessage mail) {
        switch (this) {
            case COLOR_LABEL:
                return mail.getColorLabel();

            case FLAG_ANSWERED:
                return Boolean.valueOf((mail.getFlags() & MailMessage.FLAG_FORWARDED) > 0);

            case FLAG_DELETED:
                return Boolean.valueOf((mail.getFlags() & MailMessage.FLAG_FORWARDED) > 0);

            case FLAG_DRAFT:
                return Boolean.valueOf((mail.getFlags() & MailMessage.FLAG_FORWARDED) > 0);

            case FLAG_FLAGGED:
                return Boolean.valueOf((mail.getFlags() & MailMessage.FLAG_FORWARDED) > 0);

            case FLAG_RECENT:
                return Boolean.valueOf((mail.getFlags() & MailMessage.FLAG_FORWARDED) > 0);

            case FLAG_SEEN:
                return Boolean.valueOf((mail.getFlags() & MailMessage.FLAG_FORWARDED) > 0);

            case FLAG_USER:
                return Boolean.valueOf((mail.getFlags() & MailMessage.FLAG_FORWARDED) > 0);

            case FLAG_SPAM:
                return Boolean.valueOf((mail.getFlags() & MailMessage.FLAG_FORWARDED) > 0);

            case FLAG_FORWARDED:
                return Boolean.valueOf((mail.getFlags() & MailMessage.FLAG_FORWARDED) > 0);

            case FLAG_READ_ACK:
                return Boolean.valueOf((mail.getFlags() & MailMessage.FLAG_FORWARDED) > 0);

            case USER_FLAGS:
                final String[] userFlags = mail.getUserFlags();
                if (null != userFlags && userFlags.length > 0) {
                    return Arrays.asList(userFlags);
                } else {
                    return null;
                }

            default:
                return null;
        }
    }

    private static synchronized void checkProperties() {
        if (properties == null) {
            final ConfigurationService config = Services.getService(ConfigurationService.class);
            final String solrConfDir = config.getProperty(SolrProperties.CONFIG_DIR);

            properties = new Properties();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(solrConfDir + File.separator + PROP_FILE);
                properties.load(fis);
            } catch (final FileNotFoundException e) {
                throw new IllegalStateException("Could not load solr mail fields from property file.", e);
            } catch (final IOException e) {
                throw new IllegalStateException("Could not load solr mail fields from property file.", e);
            } finally {
                IOUtils.closeQuietly(fis);
            }
        }
    }

}

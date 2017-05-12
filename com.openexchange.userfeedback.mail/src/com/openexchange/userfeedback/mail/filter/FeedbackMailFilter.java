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

package com.openexchange.userfeedback.mail.filter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.userfeedback.FeedbackMetaData;
import com.openexchange.userfeedback.filter.FeedbackFilter;

/**
 * {@link FeedbackMailFilter}
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since 7.8.4
 */
public class FeedbackMailFilter implements FeedbackFilter {

    private final String ctxGroup;
    private final Map<String, String> recipients;
    private final Map<String, String> pgpKeys;
    private final String subject;
    private final String body;
    private final long start;
    private final long end;
    private final String type;
    private final boolean compress;

    private static final String DEFAULT_BODY = "";
    private static final String DEFAULT_CONTEXT_GROUP = "default";
    private static final String DEFAULT_TYPE = "star-rating-v1";
    private static final String DEFAULT_SUBJECT = "User feedback report: %s";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public FeedbackMailFilter(String ctxGroup, Map<String, String> recipients, String subject, String body, long start, long end, String type, boolean compress) {
        this(ctxGroup, recipients, null, subject, body, start, end, type, compress);
    }

    public FeedbackMailFilter(String ctxGroup, Map<String, String> recipients, Map<String, String> pgpKeys, String subject, String body, long start, long end, String type, boolean compress) {
        super();
        this.ctxGroup = ctxGroup == null ? DEFAULT_CONTEXT_GROUP : ctxGroup;
        this.recipients = recipients == null ? new HashMap<String, String>(0) : recipients;
        this.pgpKeys = pgpKeys == null ? new HashMap<String, String>(0) : pgpKeys;;
        this.subject = subject == null ? String.format(DEFAULT_SUBJECT, getTimerangeString()) : subject;
        this.body = body == null ? DEFAULT_BODY : body;
        this.start = start == 0 ? Long.MIN_VALUE : start;
        this.end = end == 0 ? Long.MAX_VALUE : end;
        this.type = type == null ? DEFAULT_TYPE : type;
        this.compress = compress;
    }

    @Override
    public boolean accept(FeedbackMetaData feedback) {
        if ((start == 0) && (end == 0)) {
            return true;
        }
        long feedbackDate = feedback.getDate();
        if (start == 0) {
            return feedbackDate < end;
        } else if (end == 0) {
            return feedbackDate > start;
        } else {
            return (feedbackDate < end) && (feedbackDate > start);
        }
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Long start() {
        return start;
    }

    @Override
    public Long end() {
        return end;
    }

    public String getCtxGroup() {
        return ctxGroup;
    }

    public Map<String, String> getRecipients() {
        return recipients;
    }

    public Map<String, String> getPgpKeys() {
        return pgpKeys;
    }

    public String getSubject() {
        return subject;
    }

    private String getTimerangeString() {
        String result = "";
        if (start > 0) {
            result = dateFormat.format(new Date(start));
        }
        if (end > 0) {
            String endDate = dateFormat.format(new Date(end));
            result = result.length() > 0 ? result + " - " + endDate : endDate;
        }
        if (result.length() == 0) {
            result = "no timerange selected";
        }

        return result;
    }

    public String getBody() {
        return body;
    }

    public boolean isCompress() {
        return compress;
    }

    // --------------------------------------------------------------------------------------

    public static class FeedBackMailFilterBuilder {

        private final String builderCtxGroup;
        private Map<String, String> builderRecipients;
        private Map<String, String> builderPgpKeys;
        private String builderSubject;
        private String builderBody;
        private long builderStart;
        private long builderEnd;
        private final String builerType;
        private boolean compress;

        public FeedBackMailFilterBuilder(String ctxGroup, String type) {
            this.builderCtxGroup = ctxGroup;
            this.builerType = type;
        }

        public FeedBackMailFilterBuilder timerange(long start, long end) {
            this.builderStart = start;
            this.builderEnd = end;
            return this;
        }

        public FeedBackMailFilterBuilder subject(String subject) {
            this.builderSubject = subject;
            return this;
        }

        public FeedBackMailFilterBuilder body(String body) {
            this.builderBody = body;
            return this;
        }

        public FeedBackMailFilterBuilder recipients(Map<String, String> recipients) {
            this.builderRecipients = recipients;
            return this;
        }

        public FeedBackMailFilterBuilder pgpKeys(Map<String, String> pgpKeys) {
            this.builderPgpKeys = pgpKeys;
            return this;
        }

        public FeedBackMailFilterBuilder compress(boolean compress) {
            this.compress = compress;
            return this;
        }

        public FeedbackMailFilter build() {
            return new FeedbackMailFilter(builderCtxGroup, builderRecipients, builderPgpKeys, builderSubject, builderBody, builderStart, builderEnd, builerType, compress);
        }
    }
}

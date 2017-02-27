
package com.openexchange.userfeedback.mail.filter;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.userfeedback.FeedbackMetaData;
import com.openexchange.userfeedback.filter.FeedbackFilter;

public class FeedbackMailFilter implements FeedbackFilter {

    private String ctxGroup;
    private Map<String, String> recipients;
    private String subject;
    private String body;
    private long start;
    private long end;
    private String type;

    public FeedbackMailFilter(String ctxGroup, Map<String, String> recipients, String subject, String body, long start, long end, String type) {
        super();
        this.ctxGroup = ctxGroup;
        this.recipients = recipients;
        this.subject = subject;
        this.body = body;
        this.start = start;
        this.end = end;
        this.type = type;
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
        if (start == 0) {
            return Long.MIN_VALUE;
        }
        return start;
    }

    @Override
    public Long end() {
        if (end == 0) {
            return Long.MAX_VALUE;
        }
        return end;
    }

    public String getCtxGroup() {
        if (ctxGroup == null) {
            ctxGroup = "";
        }
        return ctxGroup;
    }

    public Map<String, String> getRecipients() {
        if (recipients == null) {
            recipients = new HashMap<>();
        }
        return recipients;
    }

    public String getSubject() {
        if (subject == null) {
            subject = "";
        }
        return subject;
    }

    public String getBody() {
        if (body == null) {
            body = "";
        }
        return body;
    }

    public static class FeedBackMailFilterBuilder {

        private String builderCtxGroup;
        private Map<String, String> builderRecipients;
        private String builderSubject;
        private String builderBody;
        private long builderStart;
        private long builderEnd;
        private String builerType;

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

        public FeedbackMailFilter build() {
            return new FeedbackMailFilter(builderCtxGroup, builderRecipients, builderSubject, builderBody, builderStart, builderEnd, builerType);
        }
    }
}

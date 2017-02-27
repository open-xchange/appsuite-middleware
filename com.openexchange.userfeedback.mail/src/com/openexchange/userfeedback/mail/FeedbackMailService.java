package com.openexchange.userfeedback.mail;

import com.openexchange.userfeedback.mail.filter.FeedbackMailFilter;

public interface FeedbackMailService {

    public String sendFeedbackMail(FeedbackMailFilter filter);
}

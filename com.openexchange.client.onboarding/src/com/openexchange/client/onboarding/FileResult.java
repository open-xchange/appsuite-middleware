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

package com.openexchange.client.onboarding;

import java.util.Collections;
import java.util.Map;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.client.onboarding.notification.mail.OnboardingProfileCreatedNotificationMail;
import com.openexchange.client.onboarding.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.notification.mail.MailAttachment;
import com.openexchange.notification.mail.MailAttachments;
import com.openexchange.notification.mail.MailData;
import com.openexchange.notification.mail.NotificationMailFactory;
import com.openexchange.session.Session;

/**
 * {@link ObjectResult} - A result for a file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class FileResult implements Result {

    private final IFileHolder file;

    /**
     * Initializes a new {@link ObjectResult}.
     *
     * @param file The file
     */
    public FileResult(IFileHolder file) {
        super();
        this.file = file;
    }

    /**
     * Gets the file
     *
     * @return The file
     */
    public IFileHolder getFile() {
        return file;
    }

    @Override
    public ResultObject getResultObject(OnboardingRequest request, Session session) throws OXException {
        OnboardingAction action = request.getAction();
        switch (action) {
            case DOWNLOAD:
                return new SimpleResultObject(file, "file");
            case EMAIL:
                return sendEmailResult(request, session);
            default:
                throw OnboardingExceptionCodes.UNSUPPORTED_ACTION.create(action.getId());
        }
    }

    @Override
    public ResultReply getReply() {
        return ResultReply.ACCEPT;
    }

    // --------------------------------------------- E-Mail utils --------------------------------------------------------------

    private TransportProvider getTransportProvider() {
        return TransportProviderRegistry.getTransportProvider("smtp");
    }

    private ResultObject sendEmailResult(OnboardingRequest request, Session session) throws OXException {
        Map<String, Object> input = request.getInput();
        if (null == input) {
            throw OnboardingExceptionCodes.MISSING_INPUT_FIELD.create(CommonInput.EMAIL_ADDRESS.getFirstElementName());
        }

        String emailAddress = (String) input.get(CommonInput.EMAIL_ADDRESS.getFirstElementName());
        if (Strings.isEmpty(emailAddress)) {
            throw OnboardingExceptionCodes.MISSING_INPUT_FIELD.create(CommonInput.EMAIL_ADDRESS.getFirstElementName());
        }

        boolean error = true;
        MailTransport transport = getTransportProvider().createNewNoReplyTransport(session.getContextId());
        try {
            NotificationMailFactory notify = Services.getService(NotificationMailFactory.class);

            MailData data = OnboardingProfileCreatedNotificationMail.createProfileNotificationMail(emailAddress, request.getHostData().getHost(), file.getName(), session);
            MailAttachment mailAttachment = MailAttachments.newMailAttachment(file);
            // TODO: Add Content-Id?

            ComposedMailMessage message = notify.createMail(data, Collections.singleton(mailAttachment));
            transport.sendMailMessage(message, ComposeType.NEW);

            ResultObject resultObject = new SimpleResultObject(OnboardingUtility.getTranslationFor(OnboardingStrings.RESULT_MAIL_SENT, session), "string");
            error = false;
            return resultObject;
        } catch (RuntimeException e) {
            throw OnboardingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            transport.close();
            if (error) {
                Streams.close(file);
            }
        }
    }

}

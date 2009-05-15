
package com.openexchange.mail.json.parser;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.groupware.upload.impl.UploadUtility;
import com.openexchange.groupware.userconfiguration.UserConfigurationException;
import com.openexchange.mail.MailException;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.session.Session;

/**
 * {@link AbortAttachmentHandler} - An {@link IAttachmentHandler attachment handler} that throws a {@link MailException} on exceeded quota
 * (either overall or per-file quota).
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class AbortAttachmentHandler implements IAttachmentHandler {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AbortAttachmentHandler.class);

    private final List<MailPart> attachments;

    private final boolean doAction;

    private final long uploadQuota;

    private final long uploadQuotaPerFile;

    private TextBodyMailPart textPart;

    private long consumed;

    /**
     * Initializes a new {@link AbortAttachmentHandler}.
     * 
     * @param session The session providing needed user information
     * @throws MailException If initialization fails
     */
    public AbortAttachmentHandler(final Session session) throws MailException {
        super();
        attachments = new ArrayList<MailPart>(4);
        try {
            final UserSettingMail usm = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), session.getContextId());
            if (usm.getUploadQuota() >= 0) {
                this.uploadQuota = usm.getUploadQuota();
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Upload quota is less than zero. Using global server property \"MAX_UPLOAD_SIZE\" instead.");
                }
                long tmp;
                try {
                    tmp = ServerConfig.getInteger(ServerConfig.Property.MAX_UPLOAD_SIZE);
                } catch (final ConfigurationException e) {
                    LOG.error(e.getMessage(), e);
                    tmp = 0;
                }
                this.uploadQuota = tmp;
            }
            this.uploadQuotaPerFile = usm.getUploadQuotaPerFile();
            doAction = ((uploadQuotaPerFile > 0) || (uploadQuota > 0));
        } catch (final UserConfigurationException e) {
            throw new MailException(e);
        }
    }

    public void addAttachment(final MailPart attachment) throws MailException {
        if (doAction) {
            final long size = attachment.getSize();
            if (size <= 0 && LOG.isDebugEnabled()) {
                LOG.debug(new StringBuilder("Missing size: ").append(size).toString(), new Throwable());
            }
            if (uploadQuotaPerFile > 0 && size > uploadQuotaPerFile) {
                final String fileName = attachment.getFileName();
                throw new MailException(MailException.Code.UPLOAD_QUOTA_EXCEEDED_FOR_FILE, UploadUtility.getSize(
                    uploadQuotaPerFile,
                    2,
                    false,
                    true), null == fileName ? "" : fileName, UploadUtility.getSize(size, 2, false, true));
            }
            /*
             * Add current file size
             */
            consumed += size;
            if (uploadQuota > 0 && consumed > uploadQuota) {
                throw new MailException(MailException.Code.UPLOAD_QUOTA_EXCEEDED, UploadUtility.getSize(uploadQuota, 2, false, true));
            }
        }
        attachments.add(attachment);
    }

    public void fillComposedMail(final ComposedMailMessage composedMail) throws MailException {
        composedMail.setBodyPart(textPart);
        for (final MailPart attachment : attachments) {
            composedMail.addEnclosedPart(attachment);
        }
    }

    public void setTextPart(final TextBodyMailPart textPart) {
        this.textPart = textPart;
    }

}

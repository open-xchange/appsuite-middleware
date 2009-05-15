
package com.openexchange.mail.json.parser;

import static com.openexchange.groupware.upload.impl.UploadUtility.getSize;
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
 * {@link PublishAttachmentHandler} - An {@link IAttachmentHandler attachment handler} that throws a {@link MailException} on exceeded quota
 * (either overall or per-file quota).
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class PublishAttachmentHandler implements IAttachmentHandler {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(PublishAttachmentHandler.class);

    private final List<MailPart> attachments;

    private final boolean doAction;

    private final long uploadQuota;

    private final long uploadQuotaPerFile;

    private boolean exceeded;

    private TextBodyMailPart textPart;

    private long consumed;

    /**
     * Initializes a new {@link PublishAttachmentHandler}.
     * 
     * @param session The session providing needed user information
     * @throws MailException If initialization fails
     */
    public PublishAttachmentHandler(final Session session) throws MailException {
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
        if (doAction && !exceeded) {
            final long size = attachment.getSize();
            if (size <= 0 && LOG.isDebugEnabled()) {
                LOG.debug(new StringBuilder("Missing size: ").append(size).toString(), new Throwable());
            }
            if (uploadQuotaPerFile > 0 && size > uploadQuotaPerFile) {
                if (LOG.isDebugEnabled()) {
                    final String fileName = attachment.getFileName();
                    final MailException e = new MailException(MailException.Code.UPLOAD_QUOTA_EXCEEDED_FOR_FILE, UploadUtility.getSize(
                        uploadQuotaPerFile,
                        2,
                        false,
                        true), null == fileName ? "" : fileName, UploadUtility.getSize(size, 2, false, true));
                    LOG.debug(new StringBuilder(64).append("Per-file quota (").append(getSize(uploadQuotaPerFile, 2, false, true)).append(
                        ") exceeded. Message is going to be sent with links to publishing infostore folder.").toString(), e);
                }
                exceeded = true;
            } else {
                /*
                 * Add current file size
                 */
                consumed += size;
                if (uploadQuota > 0 && consumed > uploadQuota) {
                    if (LOG.isDebugEnabled()) {
                        final MailException e = new MailException(MailException.Code.UPLOAD_QUOTA_EXCEEDED, UploadUtility.getSize(
                            uploadQuota,
                            2,
                            false,
                            true));
                        LOG.debug(new StringBuilder(64).append("Overall quota (").append(getSize(uploadQuota, 2, false, true)).append(
                            ") exceeded. Message is going to be sent with links to publishing infostore folder.").toString(), e);
                    }
                    exceeded = true;
                }
            }
        }
        attachments.add(attachment);
    }

    public void fillComposedMail(final ComposedMailMessage composedMail) throws MailException {
        if (exceeded) {
            // TODO: Put all attachments into publishing infostore folder and add publish links to text
            for (final MailPart attachment : attachments) {
                // TODO: Put current attachment to publishing infostore folder
                textPart.append("<br /><br /><a href=\"foo\">Link to attachment &quot;" + attachment.getFileName() + "&quot;</a>");
            }
            composedMail.setBodyPart(textPart);
        } else {
            composedMail.setBodyPart(textPart);
            for (final MailPart attachment : attachments) {
                composedMail.addEnclosedPart(attachment);
            }
        }
    }

    public void setTextPart(final TextBodyMailPart textPart) {
        this.textPart = textPart;
    }

}

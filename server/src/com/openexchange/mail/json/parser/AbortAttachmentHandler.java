
package com.openexchange.mail.json.parser;

import com.openexchange.groupware.upload.impl.UploadUtility;
import com.openexchange.mail.MailException;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.session.Session;

/**
 * {@link AbortAttachmentHandler} - An {@link IAttachmentHandler attachment handler} that throws a {@link MailException} on exceeded quota
 * (either overall or per-file quota).
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class AbortAttachmentHandler extends AbstractAttachmentHandler {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AbortAttachmentHandler.class);

    private TextBodyMailPart textPart;

    private long consumed;

    /**
     * Initializes a new {@link AbortAttachmentHandler}.
     * 
     * @param session The session providing needed user information
     * @throws MailException If initialization fails
     */
    public AbortAttachmentHandler(final Session session) throws MailException {
        super(session);
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

    public ComposedMailMessage[] generateComposedMails(final ComposedMailMessage source) throws MailException {
        source.setBodyPart(textPart);
        for (final MailPart attachment : attachments) {
            source.addEnclosedPart(attachment);
        }
        return new ComposedMailMessage[] { source };
    }

    public void setTextPart(final TextBodyMailPart textPart) {
        this.textPart = textPart;
    }

}

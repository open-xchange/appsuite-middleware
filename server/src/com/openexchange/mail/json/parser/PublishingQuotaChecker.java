
package com.openexchange.mail.json.parser;

import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.groupware.upload.impl.UploadUtility;
import com.openexchange.groupware.userconfiguration.UserConfigurationException;
import com.openexchange.mail.MailException;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.session.Session;

/**
 * {@link PublishingQuotaChecker} - A {@link IQuotaChecker quota checker} that publishes exceeded attachments to a dedicated infostore
 * folder.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class PublishingQuotaChecker implements IQuotaChecker {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(PublishingQuotaChecker.class);

    private final boolean doAction;

    private final long uploadQuota;

    private final long uploadQuotaPerFile;

    private long consumed;

    /**
     * Initializes a new {@link PublishingQuotaChecker}.
     * 
     * @param session The session providing needed user information
     * @throws MailException If initialization fails
     */
    public PublishingQuotaChecker(final Session session) throws MailException {
        super();
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

    public void addConsumed(final long size, final String fileName) throws MailException {
        if (!doAction) {
            return;
        }
        if (size <= 0 && LOG.isDebugEnabled()) {
            LOG.debug(new StringBuilder("Missing size: ").append(size).toString(), new Throwable());
        }
        if (uploadQuotaPerFile > 0 && size > uploadQuotaPerFile) {
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

}


package com.openexchange.mail.json.parser;

import com.openexchange.mail.MailException;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;

/**
 * {@link IAttachmentHandler} - Tracks mail parts when parsing a mail.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
interface IAttachmentHandler {

    /**
     * Sets the text part which may be modified.
     * 
     * @param textBodyPart The text part to set
     */
    public void setTextPart(TextBodyMailPart textBodyPart);

    /**
     * Adds specified attachment.
     * 
     * @param attachment The attachment to add
     * @throws MailException Depending on implementation it may indicate an exceeded quota, but other errors as well
     */
    public void addAttachment(MailPart attachment) throws MailException;

    /**
     * Fills attachments into specified composed mail.
     * 
     * @param composedMail The composed mail to fill
     * @throws MailException If an error occurs while filling mail
     */
    public void fillComposedMail(ComposedMailMessage composedMail) throws MailException;
}

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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.share.notification.mail.impl;

import static com.openexchange.osgi.Tools.requireService;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.html.HtmlService;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.java.Strings;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.ContentAwareComposedMailMessage;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.DriveTargetProxyType;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.share.groupware.TargetProxyType;
import com.openexchange.share.notification.LinkProvider;
import com.openexchange.share.notification.PasswordResetConfirmNotification;
import com.openexchange.share.notification.ShareCreatedNotification;
import com.openexchange.share.notification.impl.NotificationStrings;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateService;
import com.openexchange.user.UserService;


/**
 * {@link MailComposer}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class MailComposer {

    private static final String FIELD_PW_RESET_CONFIRM_INTRO = "pw_reset_confirm_intro";

    private static final String FIELD_PW_RESET_CONFIRM_LINK = "pw_reset_confirm_link";
    
    // shareCreated
    
    private static final String SUBJECT = "subject";
    
    private static final String HAS_SHARED_ITEMS = "has_shared_items";
    
    private static final String USER_MESSAGE = "user_message";
    
    private static final String VIEW_ITEMS_LINK = "view_items_link";
    
    private static final String VIEW_ITEMS_LABLE = "view_items_lable";
    
    private static final String WILL_EXPIRE = "will_expire";

    private final ServiceLookup services;

    public MailComposer(ServiceLookup services) {
        super();
        this.services = services;
    }

    public ComposedMailMessage buildPasswordResetConfirmMail(PasswordResetConfirmNotification<InternetAddress> notification) throws OXException, MessagingException, UnsupportedEncodingException {
        Translator translator = getTranslator(notification.getLocale());
        String subject = translator.translate(NotificationStrings.SUBJECT_RESET_PASSWORD_CONFIRM);
        Map<String, Object> vars = preparePasswordResetConfirmVars(notification, translator);
        MimeMessage mail = prepareEnvelope(subject, null, notification.getTransportInfo());
        mail.setContent(prepareContent( "notify.share.pwreset.confirm.mail.html.tmpl", vars));
        mail.saveChanges();
        return new ContentAwareComposedMailMessage(mail, notification.getContextID());
    }

    private Map<String, Object> preparePasswordResetConfirmVars(PasswordResetConfirmNotification<InternetAddress> notification, Translator translator) {
        Map<String, Object> vars = new HashMap<String, Object>();
        LinkProvider linkProvider = notification.getLinkProvider();
        String confirmLink = linkProvider.getPasswordResetConfirmUrl(notification.getConfirm());
        vars.put(FIELD_PW_RESET_CONFIRM_INTRO, translator.translate(NotificationStrings.RESET_PASSWORD_CONFIRM_INTRO));
        vars.put(FIELD_PW_RESET_CONFIRM_LINK, confirmLink);
        return vars;
    }

    public static final class PWResetIntro {

        private final String shareUrl;
        private final String pwResetIntro;
        private final String[] pwResetIntroSplit;

        public PWResetIntro(String pwResetIntro, String shareUrl) {
            super();
            this.shareUrl = shareUrl;
            this.pwResetIntro = pwResetIntro;
            this.pwResetIntroSplit = pwResetIntro.split(shareUrl);
        }

        public String pre() {
            return pwResetIntroSplit[0];
        }

        public String in() {
            return shareUrl;
        }

        public String post() {
            if (pwResetIntroSplit.length > 1) {
                return pwResetIntroSplit[1];
            }

            return "";
        }

        @Override
        public String toString() {
            return pwResetIntro;
        }
    }

    /**
     * Build a new mail for a freshly created share based on the given {@link ShareCreatedNotification}
     * @param notification The given {@link ShareCreatedNotification}
     * @return The new {@link ComposedMailMessage}
     * @throws OXException
     * @throws UnsupportedEncodingException
     * @throws MessagingException
     */
    public ComposedMailMessage buildShareCreatedMail(ShareCreatedNotification<InternetAddress> notification) throws OXException, UnsupportedEncodingException, MessagingException {
        User user = getUserService().getUser(notification.getSession().getUserId(), notification.getSession().getContextId());
        Map<String, Object> vars = prepareShareCreatedVars(notification, user);
        String subject = (String) vars.get(SUBJECT);
        MimeMessage mail = prepareEnvelope(subject, new Address[] { getSenderAddress(notification.getSession(), user) }, notification.getTransportInfo());
        mail.addHeader("X-Open-Xchange-Share-Type", "share-created");
        mail.addHeader("X-Open-Xchange-Share-URL", notification.getLinkProvider().getShareUrl());
        mail.addHeader("X-Open-Xchange-Share-Access", buildAccessHeader(notification.getAuthMode(), notification.getUsername(), notification.getPassword()));
        
        // Select the com.openexchange.share.create.mail.tmpl template configured for the current user and render it based on current share 
        String templateName = getShareCreatedTemplate(services, notification);
        mail.setContent(prepareContent(templateName, vars));
        mail.saveChanges();
        
        return new ContentAwareComposedMailMessage(mail, notification.getSession(), notification.getSession().getContextId());
    }
    
    public String prepareSubject() {
        return null;
    }

    /**
     * Gets the name of the template that should be used when rendering notification mails for created shares.
     *   
     * @param lookup The {@link ServiceLookup} to use
     * @param notification The 
     * @return The name of the template that should be used when rendering notification mails
     * @throws OXException
     */
    private String getShareCreatedTemplate(ServiceLookup lookup, ShareCreatedNotification<InternetAddress> notification) throws OXException {
        String templateName = null;
        
        final ConfigViewFactory configviews = services.getService(ConfigViewFactory.class);
        ConfigView configView = configviews.getView(notification.getSession().getUserId(), notification.getSession().getContextId());
        ComposedConfigProperty<String> templateNameProperty = configView.property("com.openexchange.share.create.mail.tmpl", String.class);
        if(templateNameProperty.isDefined()) {
            templateName = templateNameProperty.get();
        } else {
            templateName = services.getService(ConfigurationService.class).getProperty("com.openexchange.share.create.mail.tmpl", "notify.share.create.mail.html.tmpl");
        }
        
        return templateName;    
    }
    
    private static String buildAccessHeader(AuthenticationMode authMode, String username, String password) {
        String accessHeader = null;
        if (authMode == AuthenticationMode.GUEST_PASSWORD && !Strings.isEmpty(username)) {
            accessHeader = com.openexchange.tools.encoding.Base64.encode(username);
        } else if (authMode == AuthenticationMode.ANONYMOUS_PASSWORD && !Strings.isEmpty(password)) {
            accessHeader = com.openexchange.tools.encoding.Base64.encode(password);
        }

        return accessHeader;
    }

    /**
     * Prepares a mapping from template keywords to actual textual values that will be used during template rendering.
     * 
     * @param notification The {@link ShareCreatedNotification} containing infos about the created share
     * @param user The {@link User} that created a new share
     * @param translator The {@link Translator} used for adapting the textual template values to the recipients locale 
     * @return A mapping from template keywords to actual textual values
     * @throws OXException
     */
    private Map<String, Object> prepareShareCreatedVars(ShareCreatedNotification<InternetAddress> notification, User user) throws OXException {
        Map<String, Object> vars = new HashMap<String, Object>();
        User guest = getUserService().getUser(notification.getGuestID(), notification.getGuestContextID());
        Translator translator = getTranslator(guest.getLocale());

        boolean hasMessage = !Strings.isEmpty(notification.getMessage());
        String shareUrl = notification.getLinkProvider().getShareUrl();
        String email = user.getMail();
        List<ShareTarget> shareTargets = notification.getShareTargets();
        String fullName = FullNameBuilder.buildFullName(user, translator);
        boolean causedGuestCreation = notification.getCreationDetails().causedGuestCreation();
        String productName = notification.getCreationDetails().getProductName();
        
        ModuleSupport moduleSupport = services.getService(ModuleSupport.class);
        Map<ShareTarget, TargetProxy> proxyMap = new HashMap<>(shareTargets.size());
        Set<TargetProxyType> targetTypes = new HashSet<>(shareTargets.size());
        for (ShareTarget target : shareTargets) {
            TargetProxy targetProxy = moduleSupport.load(target, notification.getSession());
            TargetProxyType proxyType = targetProxy.getProxyType();
            proxyMap.put(target, targetProxy);
            targetTypes.add(proxyType);
        }
        
        boolean hasMultipleTargets = shareTargets.size() > 1;
        
        if(!hasMultipleTargets) {
            ShareTarget shareTarget = shareTargets.get(0);
            TargetProxy targetProxy = proxyMap.get(shareTarget);
            TargetProxyType targetProxyType = targetProxy.getProxyType();
            String proxyTitle = targetProxy.getTitle();
            addSubjectToVars(vars, causedGuestCreation, productName, targetProxyType, translator, fullName, 1, proxyTitle);
            addSharedItemToVars(vars, targetProxyType, hasMessage, translator, fullName, email, proxyTitle);
            addViewItemsToVars(vars, targetProxyType, translator, false, shareUrl);
        } else {//multiple shares
            int count = shareTargets.size();
            
            if(targetTypes.size() > 1) {//multiple shares of different types
                if(causedGuestCreation) {
                    vars.put(SUBJECT,String.format(translator.translate(NotificationStrings.SUBJECT_WELCOME_INVITE_TO_PRODUCT), fullName, productName));
                } else {
                    vars.put(SUBJECT,String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_ITEMS), fullName, count));
                }
                if (hasMessage) {
                    vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_ITEMS_AND_MESSAGE), fullName, email, count));
                } else {
                    vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_ITEMS), fullName, email, count));
                }
                addViewItemsToVars(vars, null, translator, true, shareUrl);
            } else {//multiple shares of single type
                TargetProxyType targetProxyType = targetTypes.iterator().next();
                addSubjectToVars(vars, causedGuestCreation, productName, targetProxyType, translator, fullName, count, null);
                addSharedItemsToVars(vars, targetProxyType, hasMessage, translator, fullName, email, count);
                addViewItemsToVars(vars, targetProxyType, translator, true, shareUrl);
            }
        }
        
        if(hasMessage) {
            vars.put(USER_MESSAGE, notification.getMessage());
        }
        
        Date expiryDate = notification.getShareTargets().iterator().next().getExpiryDate();
        if(expiryDate != null) {
            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, guest.getLocale());
            Date localExpiry = new Date(expiryDate.getTime() + TimeZone.getTimeZone(guest.getTimeZone()).getOffset(expiryDate.getTime()));
            vars.put(WILL_EXPIRE, String.format(translator.translate(NotificationStrings.LINK_EXPIRE), dateFormat.format(localExpiry)));
        }
        
        return vars;
    }
    
    private Map<String, Object> addSubjectToVars(Map<String, Object> vars, boolean causedGuestCreation, String productName, TargetProxyType targetProxyType, Translator translator, String fullName, int count, String itemName) {
        if(causedGuestCreation) {
            vars.put(SUBJECT,String.format(translator.translate(NotificationStrings.SUBJECT_WELCOME_INVITE_TO_PRODUCT), fullName, productName));
        } else {
            if (DriveTargetProxyType.IMAGE.equals(targetProxyType)) {
                if (count == 1) {
                    vars.put(SUBJECT, String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_PHOTO), fullName, itemName));
                } else {
                    vars.put(SUBJECT, String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_PHOTOS), fullName, count));
                }
            } else if (DriveTargetProxyType.FILE.equals(targetProxyType)) {
                if (count == 1) {
                    vars.put(SUBJECT, String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_FILE), fullName, itemName));
                } else {
                    vars.put(SUBJECT, String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_FILES), fullName, count));
                }
            } else if (DriveTargetProxyType.FOLDER.equals(targetProxyType)) {
                if (count == 1) {
                    vars.put(SUBJECT, String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_FOLDER), fullName, itemName));
                } else {
                    vars.put(SUBJECT, String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_FOLDERS), fullName, count));
                }
            } else {
                //fall back to item for other types
                if (count == 1) {
                    vars.put(SUBJECT, String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_ITEM), fullName, itemName));
                } else {
                    vars.put(SUBJECT, String.format(translator.translate(NotificationStrings.SUBJECT_SHARED_ITEMS), fullName));
                }
            }
        }
        return vars;
    }

    private void addViewItemsToVars(Map<String, Object> vars, TargetProxyType targetProxyType, Translator translator, boolean multipleShares, String shareLink) {
        vars.put(VIEW_ITEMS_LINK, shareLink);
        if (DriveTargetProxyType.IMAGE.equals(targetProxyType)) {
            if (multipleShares) {
                vars.put(VIEW_ITEMS_LABLE, translator.translate(NotificationStrings.VIEW_PHOTOS));
            } else {
                vars.put(VIEW_ITEMS_LABLE, translator.translate(NotificationStrings.VIEW_PHOTO));
            }
        } else if (DriveTargetProxyType.FILE.equals(targetProxyType)) {
            if (multipleShares) {
                vars.put(VIEW_ITEMS_LABLE, translator.translate(NotificationStrings.VIEW_FILES));
            } else {
                vars.put(VIEW_ITEMS_LABLE, translator.translate(NotificationStrings.VIEW_FILE));
            }
        } else if (DriveTargetProxyType.FOLDER.equals(targetProxyType)) {
            if (multipleShares) {
                vars.put(VIEW_ITEMS_LABLE, translator.translate(NotificationStrings.VIEW_FOLDERS));
            } else {
                vars.put(VIEW_ITEMS_LABLE, translator.translate(NotificationStrings.VIEW_FOLDER));
            }
        } else {
            //fall back to item for other types
            if (multipleShares) {
                vars.put(VIEW_ITEMS_LABLE, translator.translate(NotificationStrings.VIEW_ITEMS));
            } else {
                vars.put(VIEW_ITEMS_LABLE, translator.translate(NotificationStrings.VIEW_ITEM));
            }
        }
    }

    private void addSharedItemToVars(Map<String, Object> vars, TargetProxyType targetProxyType, boolean hasMessage, Translator translator, String fullName, String email, String filename) {
        if (DriveTargetProxyType.IMAGE.equals(targetProxyType)) {
            if (hasMessage) {
                vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_PHOTO_AND_MESSAGE), fullName, email, filename));
            } else {
                vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_PHOTO), fullName, email, filename));
            }
        } else if (DriveTargetProxyType.FILE.equals(targetProxyType)) {
            if (hasMessage) {
                vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_FILE_AND_MESSAGE), fullName, email, filename));
            } else {
                vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_FILE), fullName, email, filename));
            }
        } else if (DriveTargetProxyType.FOLDER.equals(targetProxyType)) {

            if (hasMessage) {
                vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_FOLDER_AND_MESSAGE), fullName, email, filename));
            } else {
                vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_FOLDER), fullName, email, filename));
            }
        } else {
            //fall back to item for other types
            if (hasMessage) {
                vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_ITEM_AND_MESSAGE), fullName, email, filename));
            } else {
                vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_ITEM), fullName, email, filename));
            }
        }
    }
    
    private Map<String, Object> addSharedItemsToVars(Map<String, Object> vars, TargetProxyType targetProxyType, boolean hasMessage, Translator translator, String fullName, String email, int count) {
        if(DriveTargetProxyType.IMAGE.equals(targetProxyType)) {
                if(hasMessage) {
                    vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_PHOTOS_AND_MESSAGE), fullName , email , count));
                } else {
                    vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_PHOTOS), fullName , email , count));
                }

        } else if(DriveTargetProxyType.FILE.equals(targetProxyType)) {

                if(hasMessage) {
                    vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_FILES_AND_MESSAGE), fullName , email , count));
                } else {
                    vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_FILES), fullName , email , count));
                }
        }  else if(DriveTargetProxyType.FOLDER.equals(targetProxyType)) {
                if(hasMessage) {
                    vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_FOLDERS_AND_MESSAGE), fullName , email , count));
                } else {
                    vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_FOLDERS), fullName , email , count));
                }                
        } else {
            //fall back to item for other types
                if(hasMessage) {
                    vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_ITEMS_AND_MESSAGE), fullName , email , count));
                } else {
                    vars.put(HAS_SHARED_ITEMS, String.format(translator.translate(NotificationStrings.HAS_SHARED_ITEMS), fullName , email , count));
                }
        }
        return vars;
    }

    private MimeMessage prepareEnvelope(String subject, Address[] senderAddresses, InternetAddress recipient) throws MessagingException {
        MimeMessage mail = new MimeMessage(MimeDefaultSession.getDefaultSession());
        mail.addFrom(senderAddresses);
        mail.addRecipient(RecipientType.TO, recipient);
        mail.setSubject(subject, "UTF-8");
        return mail;
    }

    /**
     * Prepares the mail parts by taking the text and html templates provided as parameters.
     * 
     * @param htmlTemplate The text/html template name
     * @param htmlVars The variables used when rendering the text/html template
     * @return A {@link MimeMultipart} containing the rendered plain and html parts
     * @throws MessagingException
     * @throws OXException
     * @throws UnsupportedEncodingException
     */
    private MimeMultipart prepareContent(String htmlTemplate, Map<String, Object> htmlVars) throws MessagingException, OXException, UnsupportedEncodingException {
        TemplateService templateService = getTemplateService();
        StringWriter writer = new StringWriter();

        OXTemplate template = templateService.loadTemplate(htmlTemplate);
        writer = new StringWriter();
        template.process(htmlVars, writer);
        BodyPart htmlPart = prepareHtmlPart(writer);
        MimeMultipart multipart = new MimeMultipart("alternative");
        multipart.addBodyPart(htmlPart);
        return multipart;
    }

    private BodyPart prepareHtmlPart(Writer writer) throws OXException, UnsupportedEncodingException, MessagingException {
        MimeBodyPart htmlPart = new MimeBodyPart();
        ContentType ct = new ContentType();
        ct.setPrimaryType("text");
        ct.setSubType("html");
        ct.setCharsetParameter("UTF-8");
        String contentType = ct.toString();

        String conformContent = getHtmlService().getConformHTML(writer.toString(), "UTF-8");
        htmlPart.setDataHandler(new DataHandler(new MessageDataSource(conformContent, ct)));
        htmlPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
        htmlPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, contentType);

        return htmlPart;
    }

    private Address getSenderAddress(Session session, User user) throws OXException, UnsupportedEncodingException {
        ConfigurationService configService = getConfigService();
        String fromSource = configService.getProperty("com.openexchange.notification.fromSource", "primaryMail");
        String from = null;
        if ("defaultSenderAddress".equals(fromSource)) {
            UserSettingMail mailSettings = UserSettingMailStorage.getInstance().getUserSettingMail(session);
            if (mailSettings != null) {
                from = mailSettings.getSendAddr();
            }
        }

        if (from == null) {
            from = user.getMail();
        }

        InternetAddress[] parsed = MimeMessageUtility.parseAddressList(from, true);
        if (parsed == null || parsed.length == 0) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create("User " + user.getId() + " in context " + session.getContextId() + " seems to have no valid mail address.");
        }

        InternetAddress senderAddress = parsed[0];
        if (senderAddress.getPersonal() == null) {
            senderAddress.setPersonal(user.getDisplayName());
        }

        return senderAddress;
    }

    private UserService getUserService() throws OXException {
        return requireService(UserService.class, services);
    }

    private TemplateService getTemplateService() throws OXException {
        return requireService(TemplateService.class, services);
    }

    private HtmlService getHtmlService() throws OXException {
        return requireService(HtmlService.class, services);
    }

    private ConfigurationService getConfigService() throws OXException {
        return requireService(ConfigurationService.class, services);
    }

    private ModuleSupport getModuleSupport() throws OXException {
        return requireService(ModuleSupport.class, services);
    }

    private Translator getTranslator(Locale locale) throws OXException {
        TranslatorFactory translatorFactory = requireService(TranslatorFactory.class, services);
        Translator translator = translatorFactory.translatorFor(locale);
        return translator;
    }

}

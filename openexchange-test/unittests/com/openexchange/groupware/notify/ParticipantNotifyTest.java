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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.groupware.notify;

import com.openexchange.exception.OXException;
import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import junit.framework.TestCase;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ical4j.ICal4JParser;
import com.openexchange.group.Group;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.mail.MailObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.MockGroupLookup;
import com.openexchange.groupware.ldap.MockResourceLookup;
import com.openexchange.groupware.ldap.MockUserLookup;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserConfigurationFactory;
import com.openexchange.groupware.notify.NotificationConfig.NotificationProperty;
import com.openexchange.groupware.notify.State.Type;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.i18n.tools.StringTemplate;
import com.openexchange.i18n.tools.Template;
import com.openexchange.i18n.tools.TemplateListResourceBundle;
import com.openexchange.i18n.tools.TemplateReplacement;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.resource.Resource;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.test.TestInit;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

public class ParticipantNotifyTest extends TestCase {

    protected static final MockGroupLookup GROUP_STORAGE = new MockGroupLookup();
    protected static final MockUserLookup USER_STORAGE = new MockUserLookup();
    protected static final MockResourceLookup RESOURCE_STORAGE = new MockResourceLookup();

    protected static final UserConfigurationFactory USER_CONFIGS = new UserConfigurationFactory();


    public static final int EN = 0;
    public static final int DE = 1;
    public static final int FR = 2;


    protected final TestParticipantNotify notify = new TestParticipantNotify();

    protected final Date start = new Date(System.currentTimeMillis()+ 2*Constants.MILLI_DAY);
    // end date must be in the future for creating notifications. See bug 12063.
    protected final Date end = new Date(System.currentTimeMillis() + 3*Constants.MILLI_DAY);
    protected ServerSession session = null;

    // TODO: Reactivate if translations are available
    public void notestResolveGroup() throws Exception{
        final Participant[] participants = getParticipants(U(),G(2),S(), R());
        final Task t = getTask(participants);
        t.setUsers((UserParticipant[])null); // If the user participants are not set, fall back to resolving groups in ParticipantNotify
        notify.taskCreated(t,session);

        final Message msg = notify.getMessages().get(0);

        final String[] participantNames = parseParticipants( msg );

        assertAddresses( notify.getMessages(), "user2@test.invalid", "user4@test.invalid", "user6@test.invalid", "user8@test.invalid");
        assertLanguage( DE , msg );
        assertNames( participantNames, "User 2 (waiting)", "User 4 (waiting)", "User 6 (waiting)", "User 8 (waiting)" );
    }

    // TODO: Reactivate if translations are available
    public void notestNoSendDouble() throws Exception{
        final Participant[] participants = getParticipants(U(3),G(2),S("user2@test.invalid"), R());
        final Task t = getTask(participants);

        notify.taskCreated(t,session);

        final Message msg = notify.getMessages().get(0);

        final String[] participantNames = parseParticipants( msg );
        assertAddresses( notify.getMessages(), "user2@test.invalid", "user4@test.invalid", "user6@test.invalid", "user8@test.invalid");
        assertLanguage( DE , msg );
        assertNames( participantNames, "User 2", "User 4", "User 6", "User 8" );
    }





    public Appointment convertFromICal(final InputStream icalFile) throws Exception{
        ICal4JParser parser = new ICal4JParser();
        List<CalendarDataObject> apps = parser.parseAppointments(icalFile, TimeZone.getDefault(), session.getContext(), new LinkedList<ConversionError>(), new LinkedList<ConversionWarning>());
        return apps.get(0);
    }






    public static final void assertLanguage(final int lang, final Message msg) {
        assertEquals(lang,guessLanguage(msg));
    }

    public static final void assertNames(final String[] names, final String... expected) {
        assertNames(Arrays.asList(names), expected);
    }

    public static final void assertNames(final Iterable<String> names, final String... expected) {
        final Set<String> expectSet = new HashSet<String>(Arrays.asList(expected));
        for(final String name : names) {
            assertTrue("'" + name + "' is not in '" + expectSet.toString() + "'", expectSet.remove(name));
        }
        assertTrue("Didn't find " + expectSet, expectSet.isEmpty());
    }

    protected static void assertIsSubstring(final String expected, final String string) {
        assertTrue("Could not find '"+expected+"' in '"+string+"'", string.indexOf(expected) != -1);
    }


    public static final void assertAddresses(final Collection<Message> messages, final String...addresses) {
        final List<String> collected = new ArrayList<String>();
        for(final Message msg : messages) {
            collected.addAll(msg.addresses);
        }
        assertNames(collected,addresses);
    }


    public Task getTask(final Participant[] participants) throws OXException, OXException {
        final Task task = new Task();
        task.setStartDate(start);
        task.setEndDate(end);
        task.setTitle("TestSimple");
        task.setCreatedBy(session.getUserId());
        task.setNotification(true);
        //task.setModifiedBy(session.getUserObject().getId());
        task.setStatus(Task.NOT_STARTED);
        task.setPercentComplete(0);


        task.setParticipants(participants);

        final List<UserParticipant> userParticipants = new ArrayList<UserParticipant>();
        for(final Participant p : participants) {
            switch(p.getType()){
            case Participant.USER :
                userParticipants.add((UserParticipant)p);
                break;
            case Participant.GROUP :
                final int[] memberIds = G(p.getIdentifier())[0].getMember();
                final User[] asUsers = U(memberIds);
                final Participant[] userParticipantsFromGroup = getParticipants(asUsers, G(), S(), R());
                for(final Participant up : userParticipantsFromGroup) {
                    userParticipants.add((UserParticipant)up);
                }
                break;
            }
        }

        task.setUsers(userParticipants);

        return task;
    }

    public static User[] U(final int...ids) throws OXException {
        final User[] users = new User[ids.length];
        int i = 0;
        for(final int id : ids) {
            users[i++] = USER_STORAGE.getUser(id);
        }
        return users;
    }

    public static final Group[] G(final int...ids) throws OXException {
        final Group[] groups = new Group[ids.length];
        int i = 0;
        for(final int id : ids) {
            groups[i++] = GROUP_STORAGE.getGroup(id);
        }
        return groups;
    }

    public static final String[] S(final String...strings) {
        return strings;
    }

    public static final Resource[] R(final int...ids) {
        final Resource[] resources = new Resource[ids.length];
        int i = 0;
        for(final int id : ids) {
            resources[i++] = RESOURCE_STORAGE.getResource(id);
        }
        return resources;
    }

    public static final Participant[] getParticipants(final User[] users, final Group[] groups, final String[] external, final Resource[] resources) {
        final Participant[] participants = new Participant[users.length+groups.length+external.length+resources.length];

        int i = 0;

        for(final User user : users) {
            final UserParticipant p = new UserParticipant(user.getId());
            p.setDisplayName(user.getDisplayName());
            p.setEmailAddress(user.getMail());
            p.setPersonalFolderId(user.getId()*100); // Imaginary
            participants[i++] = p;
        }

        for(final Group group : groups) {
            final Participant p = new GroupParticipant(group.getIdentifier());
            p.setDisplayName(group.getDisplayName());
            participants[i++] = p;
        }

        for(final String externalMail : external) {
            final Participant p = new ExternalUserParticipant(externalMail);
            p.setDisplayName(externalMail);
            participants[i++] = p;
        }

        for(final Resource resource : resources) {
            final Participant p = new ResourceParticipant(resource.getIdentifier());
            p.setDisplayName(resource.getDisplayName());
            participants[i++] = p;
        }

        return participants;
    }

    @Override
    public void setUp() throws Exception {

        Init.startServer();
        final String templates = TestInit.getTestProperty("templatePath");
        TemplateListResourceBundle.setTemplatePath(new File(templates));


        final Context ctx = ContextStorage.getInstance().getContext(ContextStorage.getInstance().getContextId("defaultcontext"));
        final SessionObject sessObj = new SessionObject("bla");
        sessObj.setUsername("2000");
        sessObj.setContextId(ctx.getContextId());

        session = new ServerSessionAdapter(sessObj,ctx);


        // inject test instance as message sender
        // session.setUserConfiguration(new UserConfigurationFactory().getConfiguration(1));

        ParticipantNotify.messageSender = notify;

        // Set default Sender Address for Testuser
        UserSettingMail usm = new UserSettingMail(session.getUserId(), session.getContextId());
        usm.setSendAddr("default@test");

        NotificationPool.getInstance().clear();
    }

    @Override
    public void tearDown() throws Exception {
        notify.clearMessages();
        Init.stopServer();
    }

    protected String[] parseParticipants(final Message msg) {
        final List<String> participants = new ArrayList<String>();
        String[] lines = getLines(msg, "Teilnehmer", "Ressourcen");
        for(final String line : lines) {
            participants.add(line);
        }
        lines = getLines(msg, "Participants", "Resources");
        for(final String line : lines) {
            participants.add(line);
        }
        return participants.toArray(new String[participants.size()]);
    }

    protected static int guessLanguage(final Message msg) {
        final String[] german = new String[]{"Aufgabe", "erstellt", "ge\u00fcndert", "entfernt"};
        final String[] french = new String[]{"t\u00e2che", "cr\u00e9e", "modifi\u00e9", "supprim\u00e9" };
        for(final String g : german) {
            if(msg.messageTitle.contains(g)) {
                return DE;
            }
        }
        for(final String f : french) {
            if(msg.messageTitle.contains(f)) {
                return FR;
            }
        }
        return EN;
    }

    protected String[] getLines(final Message msg, final String from, final String to) {
        boolean collect = false;
        final List<String> collector = new ArrayList<String>();
        final String[] allLines = ((String) msg.message).split("\n");
        for(String line : allLines) {
            line = line.trim();
            if(line.startsWith(to)) {
                break;
            }

            if(collect) {
                if(!"".equals(line)&&!line.matches("=+")) {
                    collector.add(line);
                }
            }

            if(line.startsWith(from)) {
                collect = true;
            }

        }
        return collector.toArray(new String[collector.size()]);
    }


    protected static final class Message {
        public String messageTitle;
        public Object message;
        public List<String> addresses;
        public int folderId;
        protected final boolean internal;
        public Type overrideType;
        public String fromAddr;

        public void setFromAddr(String addr) {
            fromAddr = addr;
        }

        public String getFromAddr() {
            return fromAddr;
        }

        public Message(final String messageTitle, final Object message, final List<String>addresses, final int folderId, final boolean internal, State.Type overrideType) {
            this.messageTitle = messageTitle;
            this.message = message;
            this.addresses = addresses;
            this.folderId = folderId;
            this.internal = internal;
            this.overrideType = overrideType;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("Title: ");
            sb.append(messageTitle);
            sb.append('\n');
            sb.append("Addresses: ");
            for (final String address : addresses) {
                sb.append(address);
                sb.append(',');
            }
            sb.setCharAt(sb.length() - 1, '\n');
            sb.append("Message:\n");
            sb.append(message);
            return sb.toString();
        }
    }

    protected static final class TestParticipantNotify extends ParticipantNotify {

        protected final List<Message> messageCollector = new ArrayList<Message>();

        public boolean realUsers = false;

        @Override
        protected Group[] resolveGroups(final Context ctx, final int... ids) throws OXException {
            return G(ids);
        }

        @Override
        protected User[] resolveUsers(final Context ctx, final int... ids) throws OXException {
            if (realUsers) {
                return super.resolveUsers(ctx, ids);
            }
            return U(ids);
        }

        @Override
        protected Resource[] resolveResources(final Context ctx, final int...ids){
            return R(ids);
        }

        public List<Message> getMessages(){
            NotificationPool.getInstance().sendAllMessages();
            return messageCollector;
        }

        public void clearMessages(){
            messageCollector.clear();
        }

        @Override
        protected UserConfiguration getUserConfiguration(final int id, final int[] groups, final Context context) {
            return USER_CONFIGS.getConfiguration(id);
        }

        @Override
        protected UserSettingMail getUserSettingMail(final int id, final Context context) {
            return USER_CONFIGS.getSetting(id);
        }

        @Override
        protected void sendMessage(MailMessage msg, final ServerSession session, final CalendarObject obj, final State state, final boolean suppressOXReminderHeader) {
            Message m = new Message(msg.title, msg.message, msg.addresses, msg.folderId, msg.internal, msg.overrideType);

            String fromAddr;
            String senderSource = NotificationConfig.getProperty(NotificationProperty.FROM_SOURCE, "primaryMail");
            if (senderSource.equals("defaultSenderAddress")) {
                fromAddr = getUserSettingMail(session.getUserId(), session.getContext()).getSendAddr();
            } else {
                try {
                    fromAddr = USER_STORAGE.getUser(session.getUserId()).getMail();
                } catch (OXException e) {
                    fromAddr = "User not found";
                }
            }

            m.setFromAddr(fromAddr);
            messageCollector.add(m);
        }

        @Override
        protected String getFolderName(final int folderId, final Locale locale, final OXFolderAccess access) {
            return "FOLDER";
        }

        public Message getMessageForUser(int uid) throws OXException {
            String mailAddress = U(uid)[0].getMail();
            for (Message message : messageCollector) {
                if(message.addresses.contains(mailAddress)) {
                    return message;
                }
            }
            return null;
        }
    }

    protected static final class TestLinkableState extends LinkableState {

        protected int module;

        public void setModule(final int module) {
            this.module = module;
}
        @Override
        public int getModule() {
            return module;
        }

        @Override
        public void modifyInternal(final MailObject mail, final CalendarObject obj, final ServerSession sess) {
            //NOT IMPLEMENTED YET
        }

        @Override
        public void modifyExternal(final MailObject mail, final CalendarObject obj, final ServerSession sess) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean sendMail(final UserSettingMail userSettingMail, int owner, int participant, int modificationUser) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void loadTemplate() {
            //NOT IMPLEMENTED YET
        }

        public void setTemplateString(final String template) {
            object_link_template = new StringTemplate(template);
        }
        @Override
        public DateFormat getDateFormat(final Locale locale) {
            // TODO Auto-generated method stub
            return null;
        }
        @Override
        public TemplateReplacement getAction() {
            // TODO Auto-generated method stub
            return null;
        }
        @Override
        public TemplateReplacement getConfirmationAction() {
            // TODO Auto-generated method stub
            return null;
        }
        @Override
        public Template getTemplate() {
            // TODO Auto-generated method stub
            return null;
        }
        @Override
        public Type getType() {
            // TODO Auto-generated method stub
            return null;
        }
        @Override
        public boolean onlyIrrelevantFieldsChanged(CalendarObject oldObj, CalendarObject newObj) {
            // TODO Auto-generated method stub
            return false;
        }

    }

    protected static class TestMailObject extends MailObject{
        protected InputStream theInputStream;
        protected String theFilename;
        protected ContentType theContentType;

        protected TestMailObject() {
            super(null, 0, 0, 0, "New");
        }


        @Override
        public void addFileAttachment(final ContentType contentType, final String fileName, final InputStream inputStream) {
            this.theContentType = contentType;
            this.theFilename = fileName;
            this.theInputStream = inputStream;
        }

        public InputStream getTheInputStream() {
            return theInputStream;
        }

        public String getTheFilename() {
            return theFilename;
        }

        public ContentType getTheContentType() {
            return theContentType;
        }
    }
}

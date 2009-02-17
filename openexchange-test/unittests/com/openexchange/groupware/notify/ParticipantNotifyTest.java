package com.openexchange.groupware.notify;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import junit.framework.TestCase;

import com.openexchange.api2.OXException;
import com.openexchange.group.Group;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.mail.MailObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.MockGroupLookup;
import com.openexchange.groupware.ldap.MockResourceLookup;
import com.openexchange.groupware.ldap.MockUserLookup;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserConfigurationFactory;
import com.openexchange.groupware.ldap.UserException;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.i18n.tools.StringTemplate;
import com.openexchange.i18n.tools.Template;
import com.openexchange.i18n.tools.TemplateListResourceBundle;
import com.openexchange.i18n.tools.TemplateReplacement;
import com.openexchange.mail.MailException;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.resource.Resource;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.test.TestInit;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.versit.ICalendar;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.OXContainerConverter;

public class ParticipantNotifyTest extends TestCase{
	
	private static final MockGroupLookup GROUP_STORAGE = new MockGroupLookup();
	private static final MockUserLookup USER_STORAGE = new MockUserLookup();
	private static final MockResourceLookup RESOURCE_STORAGE = new MockResourceLookup();
	
	private static final UserConfigurationFactory USER_CONFIGS = new UserConfigurationFactory();
	
	
	public static final int EN = 0;
	public static final int DE = 1;
	public static final int FR = 2;
	
	
	private final TestParticipantNotify notify = new TestParticipantNotify();

	private final Date start = new Date(System.currentTimeMillis()+ 2*Constants.MILLI_DAY);
	// end date must be in the future for creating notifications. See bug 12063.
	private final Date end = new Date(System.currentTimeMillis() + 3*Constants.MILLI_DAY);
	private ServerSession session = null;



    // Bug 7507
	public void testGenerateLink() {
		final EmailableParticipant p = new EmailableParticipant(0, 0, 0, null, "", "", null, null, 0, 23, 0, null, false); //FolderId: 23
		final Task task = new Task();
		task.setObjectID(42);
		final AppointmentObject appointment = new AppointmentObject();
		appointment.setObjectID(43);
		String hostname = null;
		try {
			hostname = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (final UnknownHostException e) {
			fail("Don't know my hostname");
		}
	
	
		final TestLinkableState state = new TestLinkableState();
		state.setTemplateString("[hostname]");
		state.setModule(Types.TASK);
		String link = state.generateLink(task, p);
		assertEquals(hostname,link);
		
		state.setTemplateString("[module]");
		link = state.generateLink(task, p);
		assertEquals("task",link);
		
		state.setTemplateString("[object]");
		link = state.generateLink(task, p);
		assertEquals("42",link);
		
		state.setTemplateString("[folder]");
		link = state.generateLink(task, p);
		assertEquals("23",link);
		
		state.setModule(Types.APPOINTMENT);
		state.setTemplateString("[hostname]");
		link = state.generateLink(appointment, p);
		assertEquals(hostname,link);

		state.setTemplateString("[module]");
		link = state.generateLink(appointment, p);
		assertEquals("calendar",link);
		
		state.setTemplateString("[object]");
		link = state.generateLink(appointment, p);
		assertEquals("43",link);
		
		state.setTemplateString("[folder]");
		link = state.generateLink(appointment, p);
		assertEquals("23",link);
		
		state.setTemplateString("http://[hostname]/ox6/#m=[module]&i=[object]&f=[folder]");
		link = state.generateLink(appointment, p);
		assertEquals("http://"+hostname+"/ox6/#m=calendar&i=43&f=23",link);
		
		p.folderId = -1;
		appointment.setParentFolderID(25);
		state.setTemplateString("[folder]");
		link = state.generateLink(appointment, p);
		assertEquals("25",link);
		
		
		
	}
	
	
	// Bug 9204
	public void testDateFormat(){
		final Locale locale = Locale.GERMANY;
		final Calendar calendar = Calendar.getInstance(locale);
		
		calendar.set(2017, 4, 2, 13, 30,0);
		String expect = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss, z").format(calendar.getTime());
		
		DateFormat df = new AppointmentState(null, null, null).getDateFormat(locale);
		
		assertEquals(expect,df.format(calendar.getTime()));
		
		calendar.set(2017, 4, 2, 0, 0, 0);
		expect = "02.05.2017";
		
		df = new TaskState(null, null, null).getDateFormat(locale);
		
		assertEquals(expect,df.format(new Date(calendar.getTimeInMillis())));
		
	}
	
	public void testSimple() throws Exception{
		Participant[] participants = getParticipants(U(2),G(),S(), R());
		Task t = getTask(participants);
		
		notify.taskCreated(t,session);
		
		Message msg = notify.getMessages().get(0);
		
		String[] participantNames = parseParticipants( msg );
		
		assertNames( msg.addresses, "user1@test.invalid" );
		assertLanguage( EN , msg );
		assertNames( participantNames,"User 1 (waiting)" );
		assertEquals(200, msg.folderId);
		
		notify.clearMessages();
		
		participants = getParticipants(U(4), G(),S(), R());
		t = getTask(participants);
		
		notify.taskCreated(t,session);
		
		msg = notify.getMessages().get(0);
		participantNames = parseParticipants( msg );
		
		assertNames( msg.addresses, "user3@test.invalid" );
		assertLanguage( EN , msg );
		assertNames( participantNames,"User 3 (waiting)" );
		assertEquals(400, msg.folderId);
        assertTrue(msg.internal);


    }
	
	public void testOnlyResources() throws Exception {
		final Participant[] participants = getParticipants(U(),G(),S(), R(1));
		final Task t = getTask(participants);
		
		notify.taskCreated(t,session);
		
		final Message msg = notify.getMessages().get(0);
		
		assertNames( msg.addresses, "resource_admin1@test.invalid" );
        assertTrue(msg.internal);

	}
	
	public void testExternal() throws Exception{
		final Participant[] participants = getParticipants(U(),G(),S("don.external@external.invalid"), R());
		final Task t = getTask(participants);
		
		notify.taskCreated(t,session);
		
		final Message msg = notify.getMessages().get(0);
		
		final String[] participantNames = parseParticipants( msg );
		
		assertNames( msg.addresses, "don.external@external.invalid" );
		assertNames( participantNames,"don.external@external.invalid" );
        assertFalse(msg.internal);
    }
	
	// Bug 6524
	public void testAlphabetical() throws Exception {
		final Participant[] participants = getParticipants(U(2,3,4),G(),S(), R());
		final Task t = getTask(participants);
		
		notify.taskCreated(t,session);
		
		final Message msg = notify.getMessages().get(0);
		
		final String[] participantNames = parseParticipants( msg );
		assertEquals("Can't find 3 participants in " + guessLanguage(msg)
		    + "message \"" + msg + "\"", 3, participantNames.length);
		assertIsSubstring("User 1", participantNames[0]);
		assertIsSubstring("User 2", participantNames[1]);
		assertIsSubstring("User 3", participantNames[2]);
	}

    // Bug 9256
	public void testNullTitle() throws Exception {
		final Participant[] participants = getParticipants(U(2,3,4),G(),S(), R());
		final Task t = getTask(participants);
		t.setTitle(null);
		notify.taskCreated(t,session);
		
		final Message msg = notify.getMessages().get(0);
		assertFalse(msg.messageTitle, msg.messageTitle.contains("null"));
	}
	
	public void testNoSend() throws Exception{
		final Participant[] participants = getParticipants(U(6,2),G(),S(), R());
		final Task t = getTask(participants);
		
		notify.taskCreated(t,session);
		
		final Message msg = notify.getMessages().get(0);
		
		final String[] participantNames = parseParticipants( msg );
		
		assertNames( msg.addresses, "user1@test.invalid" );
		assertLanguage( EN , msg );
		assertNames( participantNames,"User 5 (waiting)", "User 1 (waiting)" );
	}
	
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
	
	public void testResources() throws Exception {
		final Participant[] participants = getParticipants(U(2),G(),S(),R(1));
		
		final Task t = getTask(participants);
		
		notify.taskCreated(t,session);
		assertAddresses(notify.getMessages(), "user1@test.invalid","resource_admin1@test.invalid");
	}

    public void testAddICalAttachment(){
        final AppointmentState state = new AppointmentState(null, null, null);
        final TestMailObject mailObject = new TestMailObject();
        final AppointmentObject obj = new AppointmentObject();

        obj.setCreatedBy(5);
        obj.setStartDate(new Date(0));
        obj.setEndDate(new Date(2*3600000));
        obj.setTitle("Test Appointment");

        state.modifyExternal(mailObject, obj, session);

        final ContentType ct = mailObject.getTheContentType();

        assertEquals(ct.getCharsetParameter(),"utf-8");
        assertEquals(ct.getPrimaryType(), "text");
        assertEquals(ct.getSubType(), "calendar");

        assertEquals("appointment.ics", mailObject.getTheFilename());

        try {

            final AppointmentObject obj2 = convertFromICal(mailObject.getTheInputStream());

            assertEquals(obj.getStartDate().getTime(), obj2.getStartDate().getTime());
            assertEquals(obj.getEndDate().getTime(), obj2.getEndDate().getTime());
            assertEquals(obj.getTitle(), obj2.getTitle());
        } catch (final Exception x) {
            x.printStackTrace();
            fail(x.getMessage());
        }

    }

    public AppointmentObject convertFromICal(final InputStream icalFile) throws Exception{
        OXContainerConverter oxContainerConverter = null;

	    oxContainerConverter = new OXContainerConverter(session.getContext(), TimeZone.getDefault());
        final VersitDefinition def = ICalendar.definition;
		final VersitDefinition.Reader versitReader = def.getReader(icalFile, "UTF-8");
		final VersitObject rootVersitObject = def.parseBegin(versitReader);
        VersitObject versitObject = null;
        versitObject = def.parseChild(versitReader, rootVersitObject);
        return oxContainerConverter.convertAppointment(versitObject);
    }


    // Bug 9950

    public void testShouldNotifyOldAndNewParticipants() throws Exception{
        NotificationPool.getInstance().clear();
        final Participant[] oldParticipants = getParticipants(U(2,4,10),G(),S(), R());
		final Task oldTask = getTask(oldParticipants);

        final Participant[] newParticipants = getParticipants(U(4, 8, 10), G(), S(), R());
        final Task newTask = getTask(newParticipants);

        notify.taskModified(oldTask, newTask, session);



        final List<Message> messages = notify.getMessages();

        final List<String> mailAddresses = new LinkedList<String>();
        for(final Message message : messages) { mailAddresses.addAll(message.addresses); }

        final List<PooledNotification> pooledNotifications = NotificationPool.getInstance().getNotifications();
        for (final PooledNotification pooledNotification : pooledNotifications) {
            mailAddresses.add(pooledNotification.getParticipant().email);
        }


		assertNames( mailAddresses, "user1@test.invalid", "user3@test.invalid", "user7@test.invalid", "user9@test.invalid" );
	}
    
    // Bug 12985
    public void testShouldNotNotifyOnAlarmChanges() throws LdapException, UserException {
        final Participant[] participants = getParticipants(U(2,4,10),G(),S(), R());
        final Task oldTask = getTask(participants);
        final Task newTask = getTask(participants);
        
        newTask.setAlarm(new Date());
        newTask.setAlarmFlag(true);
        
        notify.taskModified(oldTask, newTask, session);

        final List<Message> messages = notify.getMessages();
        assertTrue("The system has generated notifications. Didn't expect any.", messages.isEmpty());
        
        final List<PooledNotification> pooledNotifications = NotificationPool.getInstance().getNotifications();
        assertTrue("The system has generated notifications. Didn't expect any.", pooledNotifications.isEmpty());
    }

    public static final void assertLanguage(final int lang, final Message msg) {
		assertEquals(lang,guessLanguage(msg));
	}
	
	public static final void assertNames(final String[] names, final String...expected) {
		assertNames(Arrays.asList(names),expected);
	}
	
	public static final void assertNames(final Iterable<String> names, final String...expected) {
		final Set<String> expectSet = new HashSet<String>(Arrays.asList(expected));
		
		for(final String name : names) {
			assertTrue(names.toString(), expectSet.remove(name));
		}
        assertTrue("Didn't find "+ expectSet, expectSet.isEmpty());
    }

    private static void assertIsSubstring(final String expected, final String string) {
        assertTrue("Could not find '"+expected+"' in '"+string+"'", string.indexOf(expected) != -1);
    }
    
	
    public static final void assertAddresses(final Collection<Message> messages, final String...addresses) {
		final List<String> collected = new ArrayList<String>();
		for(final Message msg : messages) {
			collected.addAll(msg.addresses);
		}
		assertNames(collected,addresses);
	}


    public Task getTask(final Participant[] participants) throws LdapException, UserException {
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
	
	public static User[] U(final int...ids) throws UserException {
		final User[] users = new User[ids.length];
		int i = 0;
		for(final int id : ids) {
			users[i++] = USER_STORAGE.getUser(id);
		}
		return users;
	}
	
	public static final Group[] G(final int...ids) throws LdapException {
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

        ParticipantNotify.messageSender = notify;
        //session.setUserConfiguration(new UserConfigurationFactory().getConfiguration(1));
	    NotificationPool.getInstance().clear();
    }
	
	@Override
	public void tearDown() throws Exception {
		notify.clearMessages();
        Init.stopServer();
    }
	
	private String[] parseParticipants(final Message msg) {
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

	private static int guessLanguage(final Message msg) {
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
	
	private String[] getLines(final Message msg, final String from, final String to) {
		boolean collect = false;
		final List<String> collector = new ArrayList<String>();
		final String[] allLines = msg.message.split("\n");
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


	private static final class Message {
		public String messageTitle;
		public String message;
		public List<String> addresses;
		public int folderId;
        private final boolean internal;

        public Message(final String messageTitle, final String message, final List<String>addresses, final int folderId, final boolean internal) {
			this.messageTitle = messageTitle;
			this.message = message;
			this.addresses = addresses;
			this.folderId = folderId;
            this.internal = internal;
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
	
	private static final class TestParticipantNotify extends ParticipantNotify {

		private final List<Message> messageCollector = new ArrayList<Message>();
		
		@Override
		protected Group[] resolveGroups(final Context ctx, final int... ids) throws LdapException {
			return G(ids);
		}

		@Override
		protected User[] resolveUsers(final Context ctx, final int... ids) throws LdapException {
			try {
                return U(ids);
            } catch (final UserException e) {
                throw new LdapException(e);
            }
		}

		@Override
		protected Resource[] resolveResources(final Context ctx, final int...ids) throws LdapException{
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
		protected UserConfiguration getUserConfiguration(final int id, final int[] groups, final Context context) throws SQLException {
			return USER_CONFIGS.getConfiguration(id);
		}
		
		@Override
		protected UserSettingMail getUserSettingMail(final int id, final Context context) throws OXException {
			return USER_CONFIGS.getSetting(id);
		}

		@Override
		protected void sendMessage(final String messageTitle, final String message, final List<String> name, final ServerSession session, final CalendarObject obj, final int folderId, final State state, final boolean suppressOXReminderHeader, final boolean internal) {
			messageCollector.add(new Message(messageTitle,message,name, folderId, internal));
		}

        @Override
        protected String getFolderName(final int folderId, final Locale locale, final OXFolderAccess access) {
            return "FOLDER";
        }
    }

	private static final class TestLinkableState extends LinkableState {

		private int module;

		public void setModule(final int module) {
			this.module = module;
}
		public int getModule() {
			return module;
		}

        public void modifyInternal(final MailObject mail, final CalendarObject obj, final ServerSession sess) {

        }

        public void modifyExternal(final MailObject mail, final CalendarObject obj, final ServerSession sess) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public boolean sendMail(final UserSettingMail userSettingMail, int owner, int participant, int modificationUser) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void loadTemplate() {
		}
		
		public void setTemplateString(final String template) {
			object_link_template = new StringTemplate(template);
		}
		public DateFormat getDateFormat(final Locale locale) {
			// TODO Auto-generated method stub
			return null;
		}
		public TemplateReplacement getAction() {
			// TODO Auto-generated method stub
			return null;
		}
		public TemplateReplacement getConfirmationAction() {
			// TODO Auto-generated method stub
			return null;
		}
		public Template getTemplate() {
			// TODO Auto-generated method stub
			return null;
		}
		public Type getType() {
			// TODO Auto-generated method stub
			return null;
		}
        public boolean onlyIrrelevantFieldsChanged(CalendarObject oldObj, CalendarObject newObj) {
            // TODO Auto-generated method stub
            return false;
        }
		
	}

    private static class TestMailObject extends MailObject{
        private InputStream theInputStream;
        private String theFilename;
        private ContentType theContentType;

        private TestMailObject() {
            super(null, 0, 0, 0, "New");
        }


        @Override
		public void addFileAttachment(final ContentType contentType, final String fileName, final InputStream inputStream) throws MailException {
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

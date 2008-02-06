package com.openexchange.groupware.notify;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.*;
import com.openexchange.groupware.container.mail.MailObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.*;
import com.openexchange.groupware.notify.ParticipantNotify.EmailableParticipant;
import com.openexchange.groupware.notify.ParticipantNotify.LinkableState;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.i18n.tools.StringTemplate;
import com.openexchange.i18n.tools.TemplateListResourceBundle;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.MailException;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.versit.converter.OXContainerConverter;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.ICalendar;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.test.TestInit;
import com.openexchange.session.Session;
import junit.framework.TestCase;

import java.io.File;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.*;

public class ParticipantNotifyTest extends TestCase{
	
	private static final MockGroupLookup GROUP_STORAGE = new MockGroupLookup();
	private static final MockUserLookup USER_STORAGE = new MockUserLookup();
	private static final MockResourceLookup RESOURCE_STORAGE = new MockResourceLookup();
	
	private static final UserConfigurationFactory USER_CONFIGS = new UserConfigurationFactory();
	
	
	public static final int EN = 0;
	public static final int DE = 1;
	public static final int FR = 2;
	
	
	private TestParticipantNotify notify = new TestParticipantNotify();
	
	private Date start = new Date();
	private Date end = new Date();
	private ServerSession session = null;



    // Bug 7507
	public void testGenerateLink() {
		EmailableParticipant p = new EmailableParticipant(0, 0, null, "", "", null, null, 0, 23); //FolderId: 23
		Task task = new Task();
		task.setObjectID(42);
		AppointmentObject appointment = new AppointmentObject();
		appointment.setObjectID(43);
		String hostname = null;
		try {
			hostname = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			fail("Don't know my hostname");
		}
	
	
		TestLinkableState state = new TestLinkableState();
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
		Locale locale = Locale.GERMANY;
		Calendar calendar = Calendar.getInstance(locale);
		
		calendar.set(2017, 4, 2, 13, 30,0);
		String expect = "02.05.2017 13:30:00, CEST";
		
		DateFormat df = new ParticipantNotify.AppointmentState().getDateFormat(locale);
		
		assertEquals(expect,df.format(new Date(calendar.getTimeInMillis())));
		
		calendar.set(2017, 4, 2, 0, 0, 0);
		expect = "02.05.2017";
		
		df = new ParticipantNotify.TaskState().getDateFormat(locale);
		
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
		assertNames( participantNames,"User 1" );
		assertEquals(200, msg.folderId);
		
		notify.clearMessages();
		
		participants = getParticipants(U(4), G(),S(), R());
		t = getTask(participants);
		
		notify.taskCreated(t,session);
		
		msg = notify.getMessages().get(0);
		participantNames = parseParticipants( msg );
		
		assertNames( msg.addresses, "user3@test.invalid" );
		assertLanguage( DE , msg );
		assertNames( participantNames,"User 3" );
		assertEquals(400, msg.folderId);
        assertTrue(msg.internal);


    }
	
	public void testOnlyResources() throws Exception {
		Participant[] participants = getParticipants(U(),G(),S(), R(1));
		Task t = getTask(participants);
		
		notify.taskCreated(t,session);
		
		Message msg = notify.getMessages().get(0);
		
		assertNames( msg.addresses, "resource_admin1@test.invalid" );
        assertTrue(msg.internal);

	}
	
	public void testExternal() throws Exception{
		Participant[] participants = getParticipants(U(),G(),S("don.external@external.invalid"), R());
		Task t = getTask(participants);
		
		notify.taskCreated(t,session);
		
		Message msg = notify.getMessages().get(0);
		
		String[] participantNames = parseParticipants( msg );
		
		assertNames( msg.addresses, "don.external@external.invalid" );
		assertNames( participantNames,"don.external@external.invalid" );
        assertFalse(msg.internal);
    }
	
	// Bug 6524
	public void testAlphabetical() throws Exception {
		Participant[] participants = getParticipants(U(2,3,4),G(),S(), R());
		Task t = getTask(participants);
		
		notify.taskCreated(t,session);
		
		Message msg = notify.getMessages().get(0);
		
		String[] participantNames = parseParticipants( msg );
		assertEquals("User 1", participantNames[0]);
		assertEquals("User 2", participantNames[1]);
		assertEquals("User 3", participantNames[2]);
	}

	// Bug 9256
	public void testNullTitle() throws Exception {
		Participant[] participants = getParticipants(U(2,3,4),G(),S(), R());
		Task t = getTask(participants);
		t.setTitle(null);
		notify.taskCreated(t,session);
		
		Message msg = notify.getMessages().get(0);
		assertFalse(msg.messageTitle, msg.messageTitle.contains("null"));
	}
	
	public void testNoSend() throws Exception{
		Participant[] participants = getParticipants(U(6,2),G(),S(), R());
		Task t = getTask(participants);
		
		notify.taskCreated(t,session);
		
		Message msg = notify.getMessages().get(0);
		
		String[] participantNames = parseParticipants( msg );
		
		assertNames( msg.addresses, "user1@test.invalid" );
		assertLanguage( EN , msg );
		assertNames( participantNames,"User 5", "User 1" );
	}
	
	public void testResolveGroup() throws Exception{
		Participant[] participants = getParticipants(U(),G(2),S(), R());
		Task t = getTask(participants);
		t.setUsers((UserParticipant[])null); // If the user participants are not set, fall back to resolving groups in ParticipantNotify
		notify.taskCreated(t,session);
		
		Message msg = notify.getMessages().get(0);
		
		String[] participantNames = parseParticipants( msg );
		
		assertAddresses( notify.getMessages(), "user2@test.invalid", "user4@test.invalid", "user6@test.invalid", "user8@test.invalid");
		assertLanguage( DE , msg );
		assertNames( participantNames, "User 2", "User 4", "User 6", "User 8" );
	}

	public void testNoSendDouble() throws Exception{
		Participant[] participants = getParticipants(U(3),G(2),S("user2@test.invalid"), R());
		Task t = getTask(participants);
		
		notify.taskCreated(t,session);
		
		Message msg = notify.getMessages().get(0);
		
		String[] participantNames = parseParticipants( msg );
		assertAddresses( notify.getMessages(), "user2@test.invalid", "user4@test.invalid", "user6@test.invalid", "user8@test.invalid");
		assertLanguage( DE , msg );
		assertNames( participantNames, "User 2", "User 4", "User 6", "User 8" );
	}
	
	public void testResources() throws Exception {
		Participant[] participants = getParticipants(U(2),G(),S(),R(1));
		
		Task t = getTask(participants);
		
		notify.taskCreated(t,session);
		assertAddresses(notify.getMessages(), "user1@test.invalid","resource_admin1@test.invalid");
	}

    public void testAddICalAttachment(){
        ParticipantNotify.AppointmentState state = new ParticipantNotify.AppointmentState();
        TestMailObject mailObject = new TestMailObject();
        AppointmentObject obj = new AppointmentObject();

        obj.setCreatedBy(5);
        obj.setStartDate(new Date(0));
        obj.setEndDate(new Date(2*3600000));
        obj.setTitle("Test Appointment");

        state.modifyExternal(mailObject, obj, session);

        ContentType ct = mailObject.getTheContentType();

        assertEquals(ct.getCharsetParameter(),"utf-8");
        assertEquals(ct.getPrimaryType(), "text");
        assertEquals(ct.getSubType(), "calendar");

        assertEquals("appointment.ics", mailObject.getTheFilename());

        try {

            AppointmentObject obj2 = convertFromICal(mailObject.getTheInputStream());

            assertEquals(obj.getStartDate().getTime(), obj2.getStartDate().getTime());
            assertEquals(obj.getEndDate().getTime(), obj2.getEndDate().getTime());
            assertEquals(obj.getTitle(), obj2.getTitle());
        } catch (Exception x) {
            x.printStackTrace();
            fail(x.getMessage());
        }

    }

    public AppointmentObject convertFromICal(InputStream icalFile) throws Exception{
        OXContainerConverter oxContainerConverter = null;

	    oxContainerConverter = new OXContainerConverter(session, session.getContext(), TimeZone.getDefault());
        final VersitDefinition def = ICalendar.definition;
		final VersitDefinition.Reader versitReader = def.getReader(icalFile, "UTF-8");
		final VersitObject rootVersitObject = def.parseBegin(versitReader);
        VersitObject versitObject = null;
        versitObject = def.parseChild(versitReader, rootVersitObject);
        return oxContainerConverter.convertAppointment(versitObject);
    }



    public static final void assertLanguage(int lang, Message msg) {
		assertEquals(lang,guessLanguage(msg));
	}
	
	public static final void assertNames(String[] names, String...expected) {
		assertNames(Arrays.asList(names),expected);
	}
	
	public static final void assertNames(Iterable<String> names, String...expected) {
		Set<String> expectSet = new HashSet<String>(Arrays.asList(expected));
		
		for(String name : names) {
			assertTrue(names.toString(), expectSet.remove(name));
		}
	}
	
	public static final void assertAddresses(Collection<Message> messages, String...addresses) {
		List<String> collected = new ArrayList<String>();
		for(Message msg : messages) {
			collected.addAll(msg.addresses);
		}
		assertNames(collected,addresses);
	}


    public Task getTask(Participant[] participants) throws LdapException {
		Task task = new Task();
		task.setStartDate(start);
		task.setEndDate(end);
		task.setTitle("TestSimple");
		task.setCreatedBy(session.getUserId());
		task.setNotification(true);
		//task.setModifiedBy(session.getUserObject().getId());
		
		
		task.setParticipants(participants);
		
		List<UserParticipant> userParticipants = new ArrayList<UserParticipant>();
		for(Participant p : participants) {
			switch(p.getType()){
			case Participant.USER :
				userParticipants.add((UserParticipant)p);
				break;
			case Participant.GROUP :
				int[] memberIds = G(p.getIdentifier())[0].getMember();
				User[] asUsers = U(memberIds);
				Participant[] userParticipantsFromGroup = getParticipants(asUsers, G(), S(), R());
				for(Participant up : userParticipantsFromGroup) {
					userParticipants.add((UserParticipant)up);
				}
				break;
			}
		}
		
		task.setUsers(userParticipants);
		
		return task;
	}
	
	public static User[] U(int...ids) throws LdapException {
		User[] users = new User[ids.length];
		int i = 0;
		for(int id : ids) {
			users[i++] = USER_STORAGE.getUser(id);
		}
		return users;
	}
	
	public static final Group[] G(int...ids) throws LdapException {
		Group[] groups = new Group[ids.length];
		int i = 0;
		for(int id : ids) {
			groups[i++] = GROUP_STORAGE.getGroup(id);       
		}
		return groups;
	}
	
	public static final String[] S(String...strings) {
		return strings;
	}
	
	public static final Resource[] R(int...ids) throws LdapException {
		Resource[] resources = new Resource[ids.length];
		int i = 0;
		for(int id : ids) {
			resources[i++] = RESOURCE_STORAGE.getResource(id);
		}
		return resources;
	}
	
	public static final Participant[] getParticipants(User[] users, Group[] groups, String[] external, Resource[] resources) {
		Participant[] participants = new Participant[users.length+groups.length+external.length+resources.length];
		
		int i = 0;
		
		for(User user : users) {
			UserParticipant p = new UserParticipant(user.getId());
			p.setDisplayName(user.getDisplayName());
			p.setEmailAddress(user.getMail());
			p.setPersonalFolderId(user.getId()*100); // Imaginary
			participants[i++] = p;
		}
		
		for(Group group : groups) {
			Participant p = new GroupParticipant(group.getIdentifier());
			p.setDisplayName(group.getDisplayName());
			participants[i++] = p;	
		}
		
		for(String externalMail : external) {
			Participant p = new ExternalUserParticipant(externalMail);
			p.setDisplayName(externalMail);
			participants[i++] = p;
		}
		
		for(Resource resource : resources) {
			Participant p = new ResourceParticipant(resource.getIdentifier());
			p.setDisplayName(resource.getDisplayName());
			participants[i++] = p;
		}
		
		return participants;
	}

    public void setUp() throws Exception {
		
		Init.startServer();
        String templates = TestInit.getTestProperty("templatePath");
		TemplateListResourceBundle.setTemplatePath(new File(templates));
		

        Context ctx = ContextStorage.getInstance().getContext(ContextStorage.getInstance().getContextId("defaultcontext"));
        SessionObject sessObj = new SessionObject("bla");
        sessObj.setUsername("1");
        sessObj.setContextId(ctx.getContextId());

        session = new ServerSessionAdapter(sessObj,ctx);

        //session.setUserConfiguration(new UserConfigurationFactory().getConfiguration(1));
	}
	
	public void tearDown() throws Exception {
		notify.clearMessages();
        Init.stopServer();
    }
	
	private String[] parseParticipants(Message msg) {
		int language = guessLanguage(msg);
		switch(language) {
		case DE: return getLines(msg,"Teilnehmer","Ressourcen");
		case EN: return getLines(msg,"Participants", "Resources");
		case FR: return getLines(msg, "Participants", "Ressources");
		default: return null;
		}
	}

	private static int guessLanguage(Message msg) {
		String[] german = new String[]{"Aufgabe", "erstellt", "geändert", "entfernt"};
		String[] french = new String[]{"tâche", "cr\u00e9e", "modifi\u00e9", "supprim\u00e9" };
		for(String g : german) {
			if(msg.messageTitle.contains(g))
				return DE;
		}
		for(String f : french) {
			if(msg.messageTitle.contains(f)) {
				return FR;
			}
		}
		return EN;
	}
	
	private String[] getLines(Message msg, String from, String to) {
		boolean collect = false;
		List<String> collector = new ArrayList<String>();
		String[] allLines = msg.message.split("\n");
		for(String line : allLines) {
			line = line.trim();
			if(line.startsWith(to)) {
				break;
			}
			
			if(collect) {
				if(!"".equals(line)&&!line.matches("=+"))
					collector.add(line);
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
        private boolean internal;

        public Message(String messageTitle, String message, List<String>addresses, int folderId, boolean internal) {
			this.messageTitle = messageTitle;
			this.message = message;
			this.addresses = addresses;
			this.folderId = folderId;
            this.internal = internal;
        }
	}
	
	private static final class TestParticipantNotify extends ParticipantNotify {

		private List<Message> messageCollector = new ArrayList<Message>();
		private EmailableParticipant p;
		
		@Override
		protected Group[] resolveGroups(Context ctx, int... ids) throws LdapException {
			return G(ids);
		}

		@Override
		protected User[] resolveUsers(Context ctx, int... ids) throws LdapException {
			return U(ids);
		}

		protected Resource[] resolveResources(Context ctx, int...ids) throws LdapException{
			return R(ids);
		}
		
		public List<Message> getMessages(){
			return messageCollector;
		}
		
		public void clearMessages(){
			messageCollector.clear();
		}	
		
		@Override
		protected UserConfiguration getUserConfiguration(int id, int[] groups, Context context) throws SQLException {
			return USER_CONFIGS.getConfiguration(id);
		}
		
		@Override
		protected UserSettingMail getUserSettingMail(final int id, final Context context) throws OXException {
			return USER_CONFIGS.getSetting(id);
		}

		@Override
		protected void sendMessage(final String messageTitle, final String message, final List<String> name, final ServerSession session, final CalendarObject obj, int folderId, final State state, final boolean suppressOXReminderHeader, boolean internal) {
			messageCollector.add(new Message(messageTitle,message,name, folderId, internal));
		}

    }

	private static final class TestLinkableState extends LinkableState {

		private int module;

		public void setModule(int module) {
			this.module = module;
}
		public int getModule() {
			return module;
		}

        public void modifyInternal(MailObject mail, CalendarObject obj, ServerSession sess) {

        }

        public void modifyExternal(MailObject mail, CalendarObject obj, ServerSession sess) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public boolean sendMail(final UserSettingMail userSettingMail) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void loadTemplate() {
		}
		
		public void setTemplateString(String template) {
			object_link_template = new StringTemplate(template);
		}
		public DateFormat getDateFormat(Locale locale) {
			// TODO Auto-generated method stub
			return null;
		}
		
		
		
	}

    private static class TestMailObject extends MailObject{
        private InputStream theInputStream;
        private String theFilename;
        private ContentType theContentType;

        private TestMailObject() {
            super(null, 0, 0, 0);
        }


        public void addFileAttachment(ContentType contentType, String fileName, InputStream inputStream) throws MailException {
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

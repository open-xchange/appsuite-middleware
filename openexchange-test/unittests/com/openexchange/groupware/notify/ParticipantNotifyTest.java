package com.openexchange.groupware.notify;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import junit.framework.TestCase;

import com.openexchange.api2.OXException;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.ldap.Group;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.MockGroupLookup;
import com.openexchange.groupware.ldap.MockResourceLookup;
import com.openexchange.groupware.ldap.MockUserLookup;
import com.openexchange.groupware.ldap.Resource;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserConfigurationFactory;
import com.openexchange.groupware.notify.ParticipantNotify.EmailableParticipant;
import com.openexchange.groupware.notify.ParticipantNotify.LinkableState;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.i18n.StringTemplate;
import com.openexchange.i18n.TemplateListResourceBundle;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.session.Session;
import com.openexchange.test.TestInit;

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
	private SessionObject session = null;
	
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
		
	}
	
	public void testOnlyResources() throws Exception {
		Participant[] participants = getParticipants(U(),G(),S(), R(1));
		Task t = getTask(participants);
		
		notify.taskCreated(t,session);
		
		Message msg = notify.getMessages().get(0);
		
		assertNames( msg.addresses, "resource_admin1@test.invalid" );
		
	}
	
	public void testExternal() throws Exception{
		Participant[] participants = getParticipants(U(),G(),S("don.external@external.invalid"), R());
		Task t = getTask(participants);
		
		notify.taskCreated(t,session);
		
		Message msg = notify.getMessages().get(0);
		
		String[] participantNames = parseParticipants( msg );
		
		assertNames( msg.addresses, "don.external@external.invalid" );
		assertNames( participantNames,"don.external@external.invalid" );	
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
		
		notify.clearMessages();
		
		participants = getParticipants(U(), G(1),S(), R());
		t = getTask(participants);
		
		notify.taskCreated(t,session);
		
		
		List<String> deAddresses = new ArrayList<String>();
		List<String> enAddresses = new ArrayList<String>();
		
		
		for(Message message : notify.getMessages()){
			assertNames(parseParticipants(message), "The Mailadmin", "User 1", "User 2", "User 3", "User 4", "User 5", "User 6", "User 7","User 8","User 9");
			int lang = guessLanguage(message);
			switch(lang) {
			case DE:
				deAddresses.addAll(message.addresses);
				break;
			case EN:
				enAddresses.addAll(message.addresses);
				break;
			}
		}
		
		if (Locale.getDefault().getLanguage().equalsIgnoreCase("de")) {
			assertNames(deAddresses, "user2@test.invalid","user3@test.invalid", "user4@test.invalid", "user6@test.invalid", "user8@test.invalid","user9@test.invalid");
			assertNames(enAddresses, "mailadmin@test.invalid", "user1@test.invalid", "user7@test.invalid", "user5@test.invalid");	
		} else {
			assertNames(deAddresses, "user2@test.invalid","user3@test.invalid", "user4@test.invalid", "user6@test.invalid", "user8@test.invalid");
			assertNames(enAddresses, "mailadmin@test.invalid", "user1@test.invalid", "user7@test.invalid", "user5@test.invalid","user9@test.invalid");	
		}
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
			UserParticipant p = new UserParticipant();
			p.setDisplayName(user.getDisplayName());
			p.setEmailAddress(user.getMail());
			p.setIdentifier(user.getId());
			p.setPersonalFolderId(user.getId()*100); // Imaginary
			participants[i++] = p;
		}
		
		for(Group group : groups) {
			Participant p = new GroupParticipant();
			p.setDisplayName(group.getDisplayName());
			p.setIdentifier(group.getIdentifier());
			participants[i++] = p;	
		}
		
		for(String externalMail : external) {
			Participant p = new ExternalUserParticipant();
			p.setDisplayName(externalMail);
			p.setEmailAddress(externalMail);
			participants[i++] = p;
		}
		
		for(Resource resource : resources) {
			Participant p = new ResourceParticipant();
			p.setIdentifier(resource.getIdentifier());
			p.setDisplayName(resource.getDisplayName());
			participants[i++] = p;
		}
		
		return participants;
	}
	
	public void setUp() throws Exception {
		
		System.setProperty("openexchange.propfile", TestInit.getTestProperty("openexchange.propfile"));
		SystemConfig.init();
		String templates = TestInit.getTestProperty("templatePath");
		TemplateListResourceBundle.setTemplatePath(new File(templates));
		
		session = new SessionObject("my_fake_sessionid");
		
		session.setContext(new ContextImpl(1));
		session.setUsername("1");  // getUserId parses this string. 
		//session.setUserConfiguration(new UserConfigurationFactory().getConfiguration(1));
	}
	
	public void tearDown() throws Exception {
		notify.clearMessages();
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
		
		public Message(String messageTitle, String message, List<String>addresses, int folderId) {
			this.messageTitle = messageTitle;
			this.message = message;
			this.addresses = addresses;
			this.folderId = folderId;
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
		protected void sendMessage(final String messageTitle, final String message, final List<String> name, final Session session, final CalendarObject obj, int folderId, final State state, final boolean suppressOXReminderHeader) {
			messageCollector.add(new Message(messageTitle,message,name, folderId));
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
}

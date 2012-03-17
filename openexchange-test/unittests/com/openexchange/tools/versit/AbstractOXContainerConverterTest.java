package com.openexchange.tools.versit;

import java.io.ByteArrayInputStream;
import junit.framework.TestCase;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.test.AjaxInit;
import com.openexchange.tools.versit.converter.OXContainerConverter;

public abstract class AbstractOXContainerConverterTest extends TestCase {

	public static SessionObject getSession() throws Exception {

		final UserStorage uStorage = UserStorage.getInstance();
		final int userId = uStorage.getUserId(AjaxInit.getAJAXProperty("login"), new ContextImpl(1));
		final SessionObject sessObj = SessionObjectWrapper.createSessionObject(userId, 1, "csv-tests");
		return sessObj;
	}

	public static User getUserParticipant() throws OXException {

		final UserStorage uStorage = UserStorage.getInstance();
		final Context ctx = new ContextImpl(1);
		final int uid = uStorage.getUserId(AjaxInit.getAJAXProperty("user_participant1"), ctx);
		return uStorage.getUser(uid, ctx);
	}

	@Override
	public void setUp() throws Exception {
	    Init.startServer();
	}

	@Override
	public void tearDown() throws Exception {
	    Init.stopServer();
	}

	public AbstractOXContainerConverterTest() {
		super();
	}

	public Task convertTask(final String versitData) throws Exception {
		final VersitDefinition def = ICalendar.definition;

		final VersitDefinition.Reader versitReader = def.getReader(
				new ByteArrayInputStream(versitData.getBytes(com.openexchange.java.Charsets.UTF_8)), "UTF-8");

		final VersitObject rootVersitObject = def.parseBegin(versitReader);
		final VersitObject versitObject = def.parseChild(versitReader, rootVersitObject);

		final OXContainerConverter oxContainerConverter = new OXContainerConverter(getSession());
		return oxContainerConverter.convertTask(versitObject);
	}

	public CalendarDataObject convertAppointment(final String versitData) throws Exception {
		final VersitDefinition def = ICalendar.definition;

		final VersitDefinition.Reader versitReader = def.getReader(
				new ByteArrayInputStream(versitData.getBytes(com.openexchange.java.Charsets.UTF_8)), "UTF-8");

		final VersitObject rootVersitObject = def.parseBegin(versitReader);
		final VersitObject versitObject = def.parseChild(versitReader, rootVersitObject);

		final OXContainerConverter oxContainerConverter = new OXContainerConverter(getSession());
		return oxContainerConverter.convertAppointment(versitObject);
	}

	public boolean isFlaggedAsPrivate(final String versitData) throws Exception {
		final VersitDefinition def = ICalendar.definition;

		final VersitDefinition.Reader versitReader = def.getReader(
				new ByteArrayInputStream(versitData.getBytes(com.openexchange.java.Charsets.UTF_8)), "UTF-8");

		final VersitObject rootVersitObject = def.parseBegin(versitReader);
		final VersitObject versitObject = def.parseChild(versitReader, rootVersitObject);

		final OXContainerConverter oxContainerConverter = new OXContainerConverter(getSession());

		final Appointment appointmentObj = oxContainerConverter.convertAppointment(versitObject);
		return appointmentObj.getPrivateFlag();
	}

	public AbstractOXContainerConverterTest(final String name) {
		super(name);
	}

}

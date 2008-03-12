package com.openexchange.ajax.mail.filter;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.mail.filter.actions.AbstractMailFilterRequest;
import com.openexchange.ajax.mail.filter.actions.AllRequest;
import com.openexchange.ajax.mail.filter.actions.AllResponse;
import com.openexchange.ajax.mail.filter.actions.DeleteRequest;
import com.openexchange.ajax.mail.filter.actions.DeleteResponse;
import com.openexchange.ajax.mail.filter.actions.InsertRequest;
import com.openexchange.ajax.mail.filter.actions.InsertResponse;
import com.openexchange.ajax.mail.filter.actions.UpdateRequest;
import com.openexchange.ajax.mail.filter.actions.UpdateResponse;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.mail.filter.Rule;
import com.openexchange.mail.filter.action.AbstractAction;
import com.openexchange.mail.filter.ajax.parser.action.ActionParserFactory;
import com.openexchange.mail.filter.ajax.parser.action.AddFlagsParserImpl;
import com.openexchange.mail.filter.ajax.parser.action.MoveParserImpl;
import com.openexchange.mail.filter.ajax.parser.action.RedirectParserImpl;
import com.openexchange.mail.filter.ajax.parser.action.RejectParserImpl;
import com.openexchange.mail.filter.ajax.parser.action.SimpleActionParserImpl;
import com.openexchange.mail.filter.ajax.parser.action.VacationParserImpl;
import com.openexchange.mail.filter.ajax.parser.comparison.ComparisonParserFactory;
import com.openexchange.mail.filter.ajax.parser.comparison.ContainsParserImpl;
import com.openexchange.mail.filter.ajax.parser.comparison.IsParserImpl;
import com.openexchange.mail.filter.ajax.parser.comparison.MatchesParserImpl;
import com.openexchange.mail.filter.ajax.parser.comparison.RegexParserImpl;
import com.openexchange.mail.filter.ajax.parser.comparison.SizeParserImpl;
import com.openexchange.mail.filter.ajax.parser.test.AddressParserImpl;
import com.openexchange.mail.filter.ajax.parser.test.AllOfParserImpl;
import com.openexchange.mail.filter.ajax.parser.test.AnyOfParserImpl;
import com.openexchange.mail.filter.ajax.parser.test.EnvelopeParserImpl;
import com.openexchange.mail.filter.ajax.parser.test.HeaderParserImpl;
import com.openexchange.mail.filter.ajax.parser.test.NotParserImpl;
import com.openexchange.mail.filter.ajax.parser.test.TestParserFactory;
import com.openexchange.mail.filter.ajax.parser.test.TrueParserImpl;
import com.openexchange.mail.filter.ajax.writer.action.ActionWriterFactory;
import com.openexchange.mail.filter.ajax.writer.action.AddFlagsWriterImpl;
import com.openexchange.mail.filter.ajax.writer.action.MoveWriterImpl;
import com.openexchange.mail.filter.ajax.writer.action.RedirectWriterImpl;
import com.openexchange.mail.filter.ajax.writer.action.RejectWriterImpl;
import com.openexchange.mail.filter.ajax.writer.action.SimpleActionWriterImpl;
import com.openexchange.mail.filter.ajax.writer.action.VacationWriterImpl;
import com.openexchange.mail.filter.ajax.writer.comparison.ComparisonWriterFactory;
import com.openexchange.mail.filter.ajax.writer.comparison.ContainsWriterImpl;
import com.openexchange.mail.filter.ajax.writer.comparison.IsWriterImpl;
import com.openexchange.mail.filter.ajax.writer.comparison.MatchesWriterImpl;
import com.openexchange.mail.filter.ajax.writer.comparison.RegexWriterImpl;
import com.openexchange.mail.filter.ajax.writer.comparison.SizeWriterImpl;
import com.openexchange.mail.filter.ajax.writer.test.AddressWriterImpl;
import com.openexchange.mail.filter.ajax.writer.test.AllOfWriterImpl;
import com.openexchange.mail.filter.ajax.writer.test.AnyOfWriterImpl;
import com.openexchange.mail.filter.ajax.writer.test.EnvelopeWriterImpl;
import com.openexchange.mail.filter.ajax.writer.test.HeaderWriterImpl;
import com.openexchange.mail.filter.ajax.writer.test.NotWriterImpl;
import com.openexchange.mail.filter.ajax.writer.test.TestWriterFactory;
import com.openexchange.mail.filter.ajax.writer.test.TrueWriterImpl;
import com.openexchange.mail.filter.test.AbstractTest;
import com.openexchange.test.OXTestToolkit;

public class AbstractMailFilterTest extends AbstractAJAXSession {

	private static final Log LOG = LogFactory.getLog(AbstractMailFilterTest.class);

	public static final int[] cols = { DataObject.OBJECT_ID };

	public AbstractMailFilterTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		// parser
		ActionParserFactory.addParser("addflags", new AddFlagsParserImpl());
		ActionParserFactory.addParser("discard", new SimpleActionParserImpl());
		ActionParserFactory.addParser("keep", new SimpleActionParserImpl());
		ActionParserFactory.addParser("move", new MoveParserImpl());
		ActionParserFactory.addParser("redirect", new RedirectParserImpl());
		ActionParserFactory.addParser("reject", new RejectParserImpl());
		ActionParserFactory.addParser("stop", new SimpleActionParserImpl());
		ActionParserFactory.addParser("vacation", new VacationParserImpl());
		
		TestParserFactory.addParser("address", new AddressParserImpl());
		TestParserFactory.addParser("allof", new AllOfParserImpl());
		TestParserFactory.addParser("anyof", new AnyOfParserImpl());
		TestParserFactory.addParser("envelope", new EnvelopeParserImpl());
		TestParserFactory.addParser("header", new HeaderParserImpl());
		TestParserFactory.addParser("not", new NotParserImpl());
		TestParserFactory.addParser("true", new TrueParserImpl());
		
		ComparisonParserFactory.addParser("is", new IsParserImpl());
		ComparisonParserFactory.addParser("matches", new MatchesParserImpl());
		ComparisonParserFactory.addParser("contains", new ContainsParserImpl());
		ComparisonParserFactory.addParser("regex", new RegexParserImpl());
		ComparisonParserFactory.addParser("size", new SizeParserImpl());
		
		// writer
		ActionWriterFactory.addWriter("addflags", new AddFlagsWriterImpl());
		ActionWriterFactory.addWriter("discard", new SimpleActionWriterImpl());
		ActionWriterFactory.addWriter("keep", new SimpleActionWriterImpl());
		ActionWriterFactory.addWriter("move", new MoveWriterImpl());
		ActionWriterFactory.addWriter("redirect", new RedirectWriterImpl());
		ActionWriterFactory.addWriter("reject", new RejectWriterImpl());
		ActionWriterFactory.addWriter("stop", new SimpleActionWriterImpl());
		ActionWriterFactory.addWriter("vacation", new VacationWriterImpl());
		
		TestWriterFactory.addWriter("address", new AddressWriterImpl());
		TestWriterFactory.addWriter("allof", new AllOfWriterImpl());
		TestWriterFactory.addWriter("anyof", new AnyOfWriterImpl());
		TestWriterFactory.addWriter("envelope", new EnvelopeWriterImpl());
		TestWriterFactory.addWriter("header", new HeaderWriterImpl());
		TestWriterFactory.addWriter("not", new NotWriterImpl());
		TestWriterFactory.addWriter("true", new TrueWriterImpl());
		
		ComparisonWriterFactory.addWriter("is", new IsWriterImpl());
		ComparisonWriterFactory.addWriter("matches", new MatchesWriterImpl());
		ComparisonWriterFactory.addWriter("contains", new ContainsWriterImpl());
		ComparisonWriterFactory.addWriter("regex", new RegexWriterImpl());
		ComparisonWriterFactory.addWriter("size", new SizeWriterImpl());
	}
	
	public static void deleteAllExistingRules(final String forUser, final AJAXSession ajaxSession) throws Exception {
		String[] idArray = getIdArray(forUser, ajaxSession);
		if (idArray != null) {
			for (int a = 0; a < idArray.length; a++) {
				final Date modified = new Date(getLastModified(forUser, ajaxSession).getTime()+1);
				deleteRule(idArray[a], forUser, modified, ajaxSession);
			}
		}
	}

	public static String insertRule(final Rule rule, final String forUser, final AJAXSession ajaxSession) throws Exception {
		final InsertRequest insertRequest = new InsertRequest(rule, forUser);
		final InsertResponse insertResponse = (InsertResponse) Executor.execute(ajaxSession,
				insertRequest);
		return insertResponse.getId();
	}
	
	public static void updateRule(final Rule rule, final String forUser, final Date lastModified, final AJAXSession ajaxSession) throws Exception {
		final UpdateRequest updateRequest = new UpdateRequest(rule, forUser, lastModified);
		final UpdateResponse updateResponse = (UpdateResponse) Executor.execute(ajaxSession, updateRequest);
	}
	
	public static void deleteRule(final String id, final String forUser, final Date lastModified, final AJAXSession ajaxSession) throws Exception {
		final DeleteRequest deleteRequest = new DeleteRequest(id, lastModified);
		final DeleteResponse deleteResponse = (DeleteResponse) Executor.execute(ajaxSession, deleteRequest);
	}
	
	public static String[] getIdArray(final String forUser, final AJAXSession ajaxSession) throws Exception {
		final int[] cols = { Rule.ID };
		
		final AllRequest allRequest = new AllRequest(AbstractMailFilterRequest.URL, cols);
		final CommonAllResponse commonAllResponse = (CommonAllResponse) Executor.execute(ajaxSession, allRequest);
		
		final Object[][] responseArray = commonAllResponse.getArray();
		final String[] idArray = new String[responseArray.length];
		for (int a = 0; a < responseArray.length; a++) {
			idArray[a] = responseArray[a][0].toString();
		}
		return idArray;
	}
	
	public static Date getLastModified(final String forUser, final AJAXSession ajaxSession) throws Exception {
		final int[] cols = { Rule.ID };
		
		final AllRequest allRequest = new AllRequest(AbstractMailFilterRequest.URL, cols);
		final CommonAllResponse commonAllResponse = (CommonAllResponse) Executor.execute(ajaxSession, allRequest);
		return commonAllResponse.getTimestamp();
	}
	
	public static Rule loadRules(final String forUser, final String id, final AJAXSession ajaxSession) throws Exception {
		final Rule[] rules = listRules(forUser, ajaxSession);
		for (int a = 0; a < rules.length; a++) {
			if (rules[a].getId().equals(id)) {
				return rules[a];
			}
		}
		
		return null;
	}	
	
	public static Rule[] listRules(final String forUser, final AJAXSession ajaxSession) throws Exception {
		final int[] cols = { Rule.ID, Rule.RULENAME, Rule.ACTIVE, Rule.FLAGS, Rule.ACTIONCMDS, Rule.TEST, Rule.POSITION };
		final AllRequest allRequest = new AllRequest(AbstractMailFilterRequest.URL, cols);
		final AllResponse allResponse = (AllResponse) Executor.execute(ajaxSession, allRequest);
		
		return allResponse.getRules();
	}
	
	public static void compareRule(final Rule rule1, final Rule rule2) throws Exception {
		OXTestToolkit.assertEqualsAndNotNull("id is not equals", rule1.getId(), rule2.getId());
		OXTestToolkit.assertEqualsAndNotNull("name is not equals", rule1.getName(), rule2.getName());
		assertEquals("active is not equals", rule1.isActive(), rule2.isActive());
		OXTestToolkit.assertEqualsAndNotNull("position is not equals", rule1.getPosition(), rule2.getPosition());
		compareFlags(rule1.getFlags(), rule2.getFlags());
		compareActionCmds(rule1.getActioncmds(), rule2.getActioncmds());
		compareTest(rule1.getTest(), rule2.getTest());
	}
	
	public static void compareFlags(final String[] flags1, final String[] flags2) throws Exception {
		if (flags1 != null) {
			assertNotNull("flags are null", flags2);
			assertEquals("flags size is not equals", flags1.length, flags2.length);
			
			for (int a = 0; a < flags1.length; a++) {
				OXTestToolkit.assertEqualsAndNotNull("flag at position " + a + " is not equals", flags1[a], flags2[a]);
			}
		} 
	}
	
	public static void compareActionCmds(final AbstractAction[] abstractAction1, final AbstractAction[] abstractAction2) throws Exception {
		if (abstractAction1 != null) {
			assertNotNull("abstract action array null", abstractAction2);
			assertEquals("abstract action size is not equals", abstractAction1.length, abstractAction2.length);
			
			for (int a = 0; a < abstractAction1.length; a++) {
				OXTestToolkit.assertEqualsAndNotNull("abstract action at position " + a + " is not equals", abstractAction1[a], abstractAction2[a]);
			}
		}
	}
	
	public static void compareTest(final AbstractTest abstractTest1, final AbstractTest abstractTest2) throws Exception {
		OXTestToolkit.assertEqualsAndNotNull("abstract test is not equals", abstractTest1, abstractTest2);
	}
}

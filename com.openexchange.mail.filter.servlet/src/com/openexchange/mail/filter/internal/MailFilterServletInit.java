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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.mail.filter.internal;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.ServletException;

import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import com.openexchange.mail.filter.MailFilterException;
import com.openexchange.mail.filter.ajax.MailFilterServlet;
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
import com.openexchange.mail.filter.osgi.MailFilterHttpServiceHolder;
import com.openexchange.server.Initialization;

/**
 * {@link MailFilterServletInit}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class MailFilterServletInit implements Initialization {

	private static final String SC_SRVLT_ALIAS = "ajax/mailfilter";

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(MailFilterServletInit.class);

	private static final MailFilterServletInit instance = new MailFilterServletInit();

	public static MailFilterServletInit getInstance() {
		return instance;
	}

	private final AtomicBoolean started;

	/**
	 * Initializes a new {@link MailFilterServletInit}
	 */
	private MailFilterServletInit() {
		super();
		started = new AtomicBoolean();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.server.Initialization#start()
	 */
	public void start() throws MailFilterException {
		if (!started.compareAndSet(false, true)) {
			LOG.error("MailFilterServlet already started.");
			return;
		}
		
		// adding parser to ParserFactory
		if (LOG.isDebugEnabled()) {
			LOG.debug("add action parser implementations");
		}
		
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
		
		// adding writer to WriterFactory
		if (LOG.isDebugEnabled()) {
			LOG.debug("add action writer implementations");
		}
		
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
		
		final HttpService httpService = MailFilterHttpServiceHolder.getInstance().getService();
		if (httpService == null) {
			LOG.error("HTTP service is null. Mail Filter servlet cannot be registered");
			return;
		}
		try {
			/*
			 * Register mail filter servlet
			 */
			httpService.registerServlet(SC_SRVLT_ALIAS, new MailFilterServlet(), null, null);
		} catch (final ServletException e) {
			throw new MailFilterException(MailFilterException.Code.SERVLET_REGISTRATION_FAILED, e, e
					.getLocalizedMessage());
		} catch (final NamespaceException e) {
			throw new MailFilterException(MailFilterException.Code.SERVLET_REGISTRATION_FAILED, e, e
					.getLocalizedMessage());
		} finally {
			MailFilterHttpServiceHolder.getInstance().ungetService(httpService);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.server.Initialization#stop()
	 */
	public void stop() {
		if (!started.compareAndSet(true, false)) {
			LOG.error("Spell check has not been started.");
			return;
		}
		final HttpService httpService = MailFilterHttpServiceHolder.getInstance().getService();
		if (httpService == null) {
			LOG.error("HTTP service is null. Mail Filter servlet cannot be unregistered");
		} else {
			try {
				/*
				 * Unregister mail filter servlet
				 */
				httpService.unregister(SC_SRVLT_ALIAS);
			} finally {
				MailFilterHttpServiceHolder.getInstance().ungetService(httpService);
			}
		}
	}
	
	/**
	 * Checks if {@link SessiondInit} is started
	 * 
	 * @return <code>true</code> if {@link SessiondInit} is started; otherwise
	 *         <code>false</code>
	 */
	public boolean isStarted() {
		return started.get();
	}
}

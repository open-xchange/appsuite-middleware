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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
package com.openexchange.test.fixtures;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeSessionPropertyNames;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.test.fixtures.transformators.BooleanTransformator;
import com.openexchange.test.fixtures.transformators.EMailFlagsTransformator;
import com.openexchange.test.fixtures.transformators.InternetAddressTransformator;
import com.openexchange.test.fixtures.transformators.JChronicDateTransformator;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class EMailFixtureFactory implements FixtureFactory<MailMessage> {
	private final File datapath;
	private final FixtureLoader fixtureLoader;

	public EMailFixtureFactory(final File datapath, FixtureLoader fixtureLoader){
		this.datapath = datapath;
		this.fixtureLoader = fixtureLoader;
	}

    @Override
    public Fixtures<MailMessage> createFixture(final String fixtureName, final Map<String, Map<String, String>> entries) {
        return new EMailFixture(fixtureName, entries, datapath, fixtureLoader);
    }

    private class EMailFixture extends DefaultFixtures<MailMessage> implements Fixtures<MailMessage> {
    	private final File dataPath;
    	final Map<String, Fixture<MailMessage>> mails = new HashMap<String, Fixture<MailMessage>>();
        private final Map<String, Map<String, String>> entries;

        public EMailFixture(final String fixtureName, final Map<String, Map<String, String>> entries, File datapath, FixtureLoader fixtureLoader) {
            super(MailMessage.class, entries, fixtureLoader);
            this.entries = entries;
            this.dataPath = datapath;

            addTransformator(new InternetAddressTransformator(fixtureLoader), "from");
            addTransformator(new InternetAddressTransformator(fixtureLoader), "to");
            addTransformator(new InternetAddressTransformator(fixtureLoader), "cc");
            addTransformator(new InternetAddressTransformator(fixtureLoader), "bcc");
            addTransformator(new JChronicDateTransformator(fixtureLoader), "received_date");
            addTransformator(new JChronicDateTransformator(fixtureLoader), "sent_date");
            addTransformator(new EMailFlagsTransformator(), "flags");
            addTransformator(new BooleanTransformator(), "append_v_card");
            addTransformator(new BooleanTransformator(), "prev_seen");
        }

        @Override
        public Fixture<MailMessage> getEntry(final String entryName) throws OXException {
            if (mails.containsKey(entryName)) {
                return mails.get(entryName);
            }
            final Map<String, String> values = entries.get(entryName);

            if(values == null) {
                throw new FixtureException("Entry with name " + entryName + " not found");
            }

            if (false == values.containsKey("eml")) {
                throw new FixtureException("Mandatory value \"eml\" missing in entry " + entryName);
            }

            final File mailpath =  new File(new File(dataPath, "emails"), values.get("eml"));

            final MailMessage mail;

            if (values.containsKey("append_v_card") && "true".equals(String.valueOf(values.get("append_v_card")))) {
                final String emlAsString;
                try {
                    emlAsString = readFileAsString(mailpath);
                } catch (IOException e) {
                    throw new FixtureException(e);
                }

                final String from = values.get("from").substring(values.get("from").indexOf(":") + 1);
                final Contact contact = fixtureLoader.getFixtures("users", SimpleCredentials.class).getEntry(from).getEntry().asContact();

                final String displayName;

                if (contact.containsDisplayName()) {
                    displayName = contact.getDisplayName();
                } else {
                    displayName = contact.getGivenName() + contact.getSurName();
                }

                mail = getMessage(emlAsString.replace("TobiasFriedrich.vcf", displayName.replace(" ", "") + ".vcf")
                                             .replace("FN:Tobias Friedrich", "FN:" + displayName)
                                             .replace("N:Friedrich;Tobias", "N:" + contact.getSurName() + ";" + contact.getGivenName())
                                             .replace("EMAIL:tfriedrich@oxhemail.open-xchange.com", "EMAIL:" + contact.getEmail1())
                                 );
            } else {
                mail = getMessage(mailpath);
            }


            if (values.containsKey("to")) { mail.removeTo(); }
            if (values.containsKey("cc")) { mail.removeCc(); }
            if (values.containsKey("bcc")) { mail.removeBcc(); }
            if (values.containsKey("from")) {
                mail.removeFrom();
                mail.removeHeader(MessageHeaders.HDR_RETURN_PATH);
                mail.removeHeader(MessageHeaders.HDR_REPLY_TO);
            }
            if (values.containsKey("sent_date")) { mail.removeSentDate(); }
            if (values.containsKey("received_date")) {
            	mail.removeReceivedDate();
            	mail.removeHeader(MessageHeaders.HDR_RECEIVED);
            }
            apply(mail, values);
            final Fixture<MailMessage> fixture = new Fixture<MailMessage>(mail, values.keySet().toArray(new String[values.size()]), values);
            mails.put(entryName, fixture);
            return fixture;
        }

        /**
         * Reads a file and converts it into a String
         *
         * @param filePath the name of the file to open.
         * @throws IOException
         */
        private String readFileAsString(File file) throws IOException {
            final StringBuffer fileData = new StringBuffer();
            final BufferedReader reader = new BufferedReader(new FileReader(file));
            final char[] buf = new char[1024];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                fileData.append(buf, 0, numRead);
            }
            reader.close();
            return fileData.toString();
        }


    	private MailMessage getMessage(final File fdir) throws OXException {
            final MimeMessage msg;
            final Session session = Session.getInstance(getDefaultSessionProperties());
            InputStream in = null;
            try {
                in = new FileInputStream(fdir);
                msg = new MimeMessage(session, in);
            } catch (FileNotFoundException e) {
                throw new FixtureException("File not found: "+fdir.getAbsolutePath(), e);
            } catch (MessagingException e) {
                throw new FixtureException(e);
            } finally {
                if (null != in) {
                    try {
                        in.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            MailMessage retval;
            try {
                retval = MimeMessageConverter.convertMessage(msg);
            } catch (OXException e) {
                throw new FixtureException(e);
            }
            return retval;
        }

        private MailMessage getMessage(final String string) throws OXException {
            final MimeMessage msg;
            final Session session = Session.getInstance(getDefaultSessionProperties());
            InputStream in = null;
            try {
                in =  new ByteArrayInputStream(string.getBytes());
                msg = new MimeMessage(session, in);
            } catch (MessagingException e) {
                throw new FixtureException(e);
            } finally {
                if (null != in) {
                    try {
                        in.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            MailMessage retval;
            try {
                retval = MimeMessageConverter.convertMessage(msg);
            } catch (OXException e) {
                throw new FixtureException(e);
            }
            return retval;
        }

    	/**
		 * Gets the default session properties
		 *
		 * @return The default session properties
		 */
    	protected final Properties getDefaultSessionProperties() {

    		Properties sessionProperties = null;
    		final String STR_TRUE = "true";
    		final String STR_FALSE = "false";

    		synchronized (EMailFixture.class) {
    			if (sessionProperties == null) {
    				/*
    				 * Define session properties
    				 */
    				System.getProperties().put(MimeSessionPropertyNames.PROP_MAIL_MIME_BASE64_IGNOREERRORS, STR_TRUE);
    				System.getProperties().put(MimeSessionPropertyNames.PROP_ALLOWREADONLYSELECT, STR_TRUE);
    				System.getProperties().put(MimeSessionPropertyNames.PROP_MAIL_MIME_ENCODEEOL_STRICT, STR_TRUE);
    				System.getProperties().put(MimeSessionPropertyNames.PROP_MAIL_MIME_DECODETEXT_STRICT, STR_FALSE);
    				System.getProperties().put(MimeSessionPropertyNames.PROP_MAIL_MIME_CHARSET, "UTF-8");
    				/*
    				 * Define imap session properties
    				 */
    				sessionProperties = ((Properties) (System.getProperties().clone()));
    				/*
    				 * A connected AccessedIMAPStore maintains a pool of IMAP protocol
    				 * objects for use in communicating with the IMAP server. The
    				 * AccessedIMAPStore will create the initial AUTHENTICATED connection
    				 * and seed the pool with this connection. As folders are opened
    				 * and new IMAP protocol objects are needed, the AccessedIMAPStore will
    				 * provide them from the connection pool, or create them if none
    				 * are available. When a folder is closed, its IMAP protocol
    				 * object is returned to the connection pool if the pool is not
    				 * over capacity.
    				 */
    				sessionProperties.put(MimeSessionPropertyNames.PROP_MAIL_IMAP_CONNECTIONPOOLSIZE, "1");
    				/*
    				 * A mechanism is provided for timing out idle connection pool
    				 * IMAP protocol objects. Timed out connections are closed and
    				 * removed (pruned) from the connection pool.
    				 */
    				sessionProperties.put(MimeSessionPropertyNames.PROP_MAIL_IMAP_CONNECTIONPOOLTIMEOUT, "1000");
    				return sessionProperties;
    			}
    			return sessionProperties;
    		}
    	}

    }
}

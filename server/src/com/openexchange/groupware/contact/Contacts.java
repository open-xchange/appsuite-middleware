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

package com.openexchange.groupware.contact;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeResources;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.getStatement;
import static com.openexchange.tools.sql.DBUtils.rollback;
import gnu.trove.TIntObjectHashMap;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.mail.internet.AddressException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.api.OXConflictException;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.OXConcurrentModificationException;
import com.openexchange.api2.OXException;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.database.DBPoolingException;
import com.openexchange.event.EventException;
import com.openexchange.event.impl.EventClient;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contact.database.PrivateFlag;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.LinkEntryObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.data.Check;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.encoding.Charsets;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderAdminHelper;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link Contacts}
 * 
 * @author <a href="mailto:ben.pahne@comfire.de">Benjamin Frederic Pahne</a>
 */
public final class Contacts {

    private static final String PROP_SCALE_IMAGE_HEIGHT = "scale_image_height";

    private static final String PROP_SCALE_IMAGE_WIDTH = "scale_image_width";

    private static final String PROP_VALIDATE_CONTACT_EMAIL = "validate_contact_email";

    private static final String PROP_SCALE_IMAGES = "scale_images";

    private static final String PROP_MAX_IMAGE_SIZE = "max_image_size";

    public static final int DATA_TRUNCATION = 54;

    static final Log LOG = LogFactory.getLog(Contacts.class);

    private static final boolean DEBUG = LOG.isDebugEnabled();

    /**
     * All available mappers as an array.
     */
    public static final Mapper[] mapping;

    private Contacts() {
        super();
    }

    private static void validateEmailAddress(final Contact co) throws ContactException {
        if (Boolean.TRUE.toString().equalsIgnoreCase(ContactConfig.getInstance().getProperty(PROP_VALIDATE_CONTACT_EMAIL))) {
            String email = null;
            try {
                if (co.containsEmail1() && ((email = co.getEmail1()) != null)) {
                    new QuotedInternetAddress(email).validate();
                }
                if (co.containsEmail2() && ((email = co.getEmail2()) != null)) {
                    new QuotedInternetAddress(email).validate();
                }
                if (co.containsEmail3() && ((email = co.getEmail3()) != null)) {
                    new QuotedInternetAddress(email).validate();
                }
            } catch (final AddressException e) {
                throw ContactExceptionCodes.INVALID_EMAIL.create(e, email);
            }
        }
    }

    private static byte[] scaleContactImage(final byte[] img, final String mime) throws ContactException {
        if (null == mime) {
            throw ContactExceptionCodes.MIME_TYPE_NOT_DEFINED.create();
        }
        final int scaledWidth = Integer.parseInt(ContactConfig.getInstance().getProperty(PROP_SCALE_IMAGE_WIDTH));
        final int scaledHeight = Integer.parseInt(ContactConfig.getInstance().getProperty(PROP_SCALE_IMAGE_HEIGHT));
        final long max_size = Long.parseLong(ContactConfig.getInstance().getProperty(PROP_MAX_IMAGE_SIZE));

        final String myMime;
        if ((mime.toLowerCase().indexOf("jpg") != -1) || (mime.toLowerCase().indexOf("jpeg") != -1)) {
            myMime = "image/jpg";
        } else if ((mime.toLowerCase().indexOf("bmp") != -1)) {
            myMime = "image/bmp";
        } else if (mime.toLowerCase().indexOf("png") != -1) {
            myMime = "image/png";
        } else {
            myMime = mime;
        }

        final String fileType = myMime.substring(myMime.indexOf('/') + 1);
        boolean check = false;
        {
            final Set<String> allowedMime = new HashSet<String>(Arrays.asList(ImageIO.getReaderFormatNames()));
            check = allowedMime.contains(fileType);
        }
        if (myMime.toLowerCase().contains("gif")) {
            check = true;
        }
        if (img.length > max_size) {
            check = false;
        }
        if (!check) {
            throw ContactExceptionCodes.IMAGE_SCALE_PROBLEM.create(myMime, I(img.length), L(max_size));
        }
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(new UnsynchronizedByteArrayInputStream(img));
            if (null == bi) {
                // No appropriate ImageReader found
                final BufferedImage targetImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.SCALE_SMOOTH);
                final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(8192);
                ImageIO.write(targetImage, fileType, out);
                bi = ImageIO.read(new UnsynchronizedByteArrayInputStream(out.toByteArray()));
            }
        } catch (final IOException e) {
            throw ContactExceptionCodes.IMAGE_DOWNSCALE_FAILED.create(e);
        }

        final int origHeigh = bi.getHeight();
        final int origWidth = bi.getWidth();
        int origType = bi.getType();

        if (DEBUG) {
            final StringBuilder logi = new StringBuilder(128).append("OUR IMAGE -> mime=").append(myMime).append(" / type=").append(origType).append(
                " / width=").append(origWidth).append(" / height=").append(origHeigh).append(" / byte[] size=").append(img.length);
            LOG.debug(logi.toString());
        }
        if ((origHeigh > scaledHeight) || (origWidth > scaledWidth)) {
            float ratio = 0;
            int sWd = 0;
            int sHd = 0;

            if (origWidth == origHeigh) {
                ratio = 1;
                if (scaledHeight < scaledWidth) {
                    sWd = scaledHeight;
                    sHd = scaledHeight;
                } else if (scaledHeight > scaledWidth) {
                    sWd = scaledWidth;
                    sHd = scaledWidth;
                } else if (scaledHeight == scaledWidth) {
                    sWd = scaledWidth;
                    sHd = scaledWidth;
                }
                if (DEBUG) {
                    LOG.debug(new StringBuilder(64).append("IMAGE SCALE Picture Heigh ").append(origHeigh).append(" Width ").append(
                        origWidth).append(" -> Scale down to Heigh ").append(sHd).append(" Width ").append(sWd).append(" Ratio ").append(
                        ratio).toString());
                }
            } else if (origWidth > origHeigh) {
                final float w1 = origWidth;
                final float h1 = origHeigh;
                ratio = w1 / h1;

                float widthFloat = scaledWidth;
                float heighFloat = widthFloat / ratio;

                if (heighFloat > scaledHeight) {
                    heighFloat = scaledHeight;
                    widthFloat = heighFloat * ratio;
                }
                sWd = Math.round(widthFloat);
                sHd = Math.round(heighFloat);

                if (DEBUG) {
                    LOG.debug(new StringBuilder(64).append("IMAGE SCALE Picture Heigh ").append(origHeigh).append(" Width ").append(
                        origWidth).append(" -> Scale down to Heigh ").append(sHd).append(" Width ").append(sWd).append(" Ratio ").append(
                        ratio).toString());
                }

            } else if (origWidth < origHeigh) {
                final float w1 = origWidth;
                final float h1 = origHeigh;
                ratio = h1 / w1;

                float heighFloat = scaledHeight;
                float widthFloat = heighFloat / ratio;

                if (widthFloat > scaledWidth) {
                    widthFloat = scaledWidth;
                    heighFloat = widthFloat * ratio;
                }

                sWd = Math.round(widthFloat);
                sHd = Math.round(heighFloat);
                if (DEBUG) {
                    LOG.debug(new StringBuilder(64).append("IMAGE SCALE Picture Heigh ").append(origHeigh).append(" Width ").append(
                        origWidth).append(" -> Scale down to Heigh ").append(sHd).append(" Width ").append(sWd).append(" Ratio ").append(
                        ratio).toString());
                }
            }

            if (origType == 0) {
                origType = BufferedImage.TYPE_INT_RGB;
            }

            final BufferedImage scaledBufferedImage = new BufferedImage(sWd, sHd, origType);
            final Graphics2D g2d = scaledBufferedImage.createGraphics();
            g2d.drawImage(bi, 0, 0, sWd, sHd, null);
            g2d.dispose();

            final ByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream();
            try {
                ImageIO.write(scaledBufferedImage, fileType, baos);
            } catch (final Exception fallback) {
                // This is just a basic fallback i try when he is not able to scale the image with the given mimetype. then we try the
                // common jpg.
                LOG.debug("Unable to Scale the Image with default Parameters. Gonna try fallback");
            } finally {
                if (baos.toByteArray().length < 1) {
                    try {
                        ImageIO.write(scaledBufferedImage, "JPG", baos);
                    } catch (final IOException e) {
                        throw ContactExceptionCodes.IMAGE_DOWNSCALE_FAILED.create(e);
                    }
                }
            }
            final byte[] back = baos.toByteArray();
            return back;
        }
        return img;
    }

    private static void checkImageSize(final int imageSize, final int maxSize) throws ContactException {
        if (maxSize > 0 && imageSize > maxSize) {
            throw ContactExceptionCodes.IMAGE_TOO_LARGE.create(I(imageSize), I(maxSize));
        }
    }

    public static void performContactStorageInsert(final Contact contact, final int user, final Session session, final boolean override) throws OXConflictException, ContactException {

        final StringBuilder insert_fields = new StringBuilder();
        final StringBuilder insert_values = new StringBuilder();

        ContactSql contactSql = null;
        Connection writecon = null;
        Connection readcon = null;

        Context context = null;

        try {
            contactSql = new ContactMySql(session);
            context = ContextStorage.getStorageContext(session.getContextId());

            readcon = DBPool.pickup(context);
            validateEmailAddress(contact);

            final int fid = contact.getParentFolderID();

            final OXFolderAccess oxfs = new OXFolderAccess(readcon, context);

            final FolderObject contactFolder = oxfs.getFolderObject(fid);
            if (contactFolder.getModule() != FolderObject.CONTACT) {
                throw new OXConflictException(ContactExceptionCodes.NON_CONTACT_FOLDER.create(I(fid), I(session.getContextId()), I(user)));
            }

            final EffectivePermission oclPerm = oxfs.getFolderPermission(
                fid,
                user,
                UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), context));

            if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
                throw new OXPermissionException(ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(fid), I(session.getContextId()), I(user)));
            }
            if (!oclPerm.canCreateObjects()) {
                throw new OXPermissionException(ContactExceptionCodes.NO_CREATE_PERMISSION.create(I(fid), I(session.getContextId()), I(user)));
            }

            if ((contactFolder.getType() != FolderObject.PRIVATE) && contact.getPrivateFlag()) {
                throw new OXConflictException(ContactExceptionCodes.PFLAG_IN_PUBLIC_FOLDER.create(I(fid), I(session.getContextId()), I(user)));
            }
            if (!contact.containsFileAs()) {
                contact.setFileAs(contact.getDisplayName());
            }
            contact.removeContextID();
            contact.removeLastModified();
            contact.removeCreationDate();
            contact.removeCreatedBy();
            contact.removeModifiedBy();
            contact.removeObjectID();
            contact.setNumberOfAttachments(0);

            /*
             * Check for bad characters inside strings
             */
            checkCharacters(contact);

            for (int i = 0; i < 650; i++) {
                if ((mapping[i] != null) && mapping[i].containsElement(contact) && (i != Contact.DISTRIBUTIONLIST) && (i != Contact.LINKS) && (i != Contact.OBJECT_ID) && (i != Contact.IMAGE_LAST_MODIFIED) && (i != Contact.IMAGE1_CONTENT_TYPE)) {
                    insert_fields.append(mapping[i].getDBFieldName()).append(',');
                    insert_values.append("?,");
                }
            }
        } catch (final ContextException d) {
            throw new ContactException(d);
        } catch (final DBPoolingException d) {
            throw ContactExceptionCodes.INIT_CONNECTION_FROM_DBPOOL.create(d);
        } catch (final OXConflictException oe) {
            throw oe;
        } catch (final OXException e) {
            throw new ContactException(e);
        } finally {
            try {
                DBPool.closeReaderSilent(context, readcon);
            } catch (final Exception ex) {
                LOG.error("Unable to close READ Connection");
            }
        }

        PreparedStatement ps = null;

        try {
            /*
             * AutoCommit false for the IDGenerator!
             */
            writecon = DBPool.pickupWriteable(context);
            writecon.setAutoCommit(false);

            final int id = IDGenerator.getId(context, Types.CONTACT, writecon);
            if (LOG.isTraceEnabled()) {
                LOG.trace("Got ID from Generator -> " + id);
            }
            if (id == -1) {
                throw ContactExceptionCodes.ID_GENERATION_FAILED.create();
            }
            contact.setObjectID(id);

            final long lmd = System.currentTimeMillis();

            StringBuilder insert = contactSql.iFperformContactStorageInsert(insert_fields, insert_values, user, lmd, session.getContextId(), id);
            if(override) {
                insert = contactSql.iFperformOverridingContactStorageInsert(insert_fields, insert_values, user, lmd, session.getContextId(), id);
            }
            
            ps = writecon.prepareStatement(insert.toString());
            int counter = 1;
            for (int i = 2; i < 650; i++) {
                if ((mapping[i] != null) && mapping[i].containsElement(contact) && (i != Contact.DISTRIBUTIONLIST) && (i != Contact.LINKS) && (i != Contact.OBJECT_ID) && (i != Contact.IMAGE_LAST_MODIFIED) && (i != Contact.IMAGE1_CONTENT_TYPE)) {
                    mapping[i].fillPreparedStatement(ps, counter, contact);
                    counter++;
                }
            }
            final Date ddd = new Date(lmd);
            contact.setLastModified(ddd);

            if (DEBUG) {
                LOG.debug(new StringBuilder(64).append("INFO: YOU WANT TO INSERT THIS: cid=").append(session.getContextId()).append(" oid=").append(
                    contact.getObjectID()).append(" -> ").append(ps.toString()).toString());
            }

            ps.execute();

            if (contact.containsNumberOfDistributionLists() && (contact.getSizeOfDistributionListArray() > 0)) {
                writeDistributionListArrayInsert(contact.getDistributionList(), contact.getObjectID(), session.getContextId(), writecon);
            }
            if (contact.containsNumberOfLinks() && (contact.getSizeOfLinks() > 0)) {
                writeContactLinkArrayInsert(contact.getLinks(), contact.getObjectID(), session.getContextId(), writecon);
            }
            if (contact.containsImage1()) {
                if (ContactConfig.getInstance().getProperty(PROP_SCALE_IMAGES).equalsIgnoreCase("true")) {
                    try {
                        contact.setImage1(scaleContactImage(contact.getImage1(), contact.getImageContentType()));
                    } catch (final Exception e) {
                        throw ContactExceptionCodes.NOT_VALID_IMAGE.create(e);
                    }
                } else {
                    checkImageSize(contact.getImage1().length, Integer.parseInt(ContactConfig.getInstance().getProperty(PROP_MAX_IMAGE_SIZE)));
                }

                writeContactImage(contact.getObjectID(), contact.getImage1(), session.getContextId(), contact.getImageContentType(), writecon);
            }
            writecon.commit();
        } catch (final DBPoolingException e) {
            // A DBPoolingException occurs when trying to fetch a connection from pool, therefore corresponding "writecon" variable can only
            // be null at this location: No rollback needed/possible on connection
            throw ContactExceptionCodes.INIT_CONNECTION_FROM_DBPOOL.create(e);
        } catch (final DataTruncation se) {
            rollback(writecon);
            throw Contacts.getTruncation(writecon, se, "prg_contacts", contact);
        } catch (final SQLException se) {
            rollback(writecon);
            throw ContactExceptionCodes.SQL_PROBLEM.create(se);
        } finally {
            closeSQLStuff(ps);
            if (null != writecon) {
                autocommit(writecon);
                try {
                    DBPool.closeWriterSilent(context, writecon);
                } catch (final Exception ex) {
                    LOG.error("Unable to close WRITE Connection");
                }
            }
        }
    }

    public static void performContactStorageUpdate(final Contact co, final int fid, final java.util.Date client_date, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws ContactException, OXConflictException, OXObjectNotFoundException, OXConcurrentModificationException, OXPermissionException {
        validateEmailAddress(co);

        boolean can_edit_only_own = false;
        boolean can_delete_only_own = false;

        if (!co.containsParentFolderID() || (co.getParentFolderID() == 0)) {
            co.setParentFolderID(fid);
        }

        final ContactSql cs = new ContactMySql(ctx, user);

        Contact original = null;
        Connection readcon = null;
        try {
            readcon = DBPool.pickup(ctx);

            try {
                original = getContactById(co.getObjectID(), user, group, ctx, uc, readcon);
            } catch (final OXException e) {
                throw new OXObjectNotFoundException(ContactExceptionCodes.LOAD_OLD_CONTACT_FAILED.create(
                    e,
                    I(ctx.getContextId()),
                    I(co.getObjectID())));
            }

            // Check if contact really exists in specified folder
            if (fid != original.getParentFolderID()) {
                throw new OXPermissionException(ContactExceptionCodes.NOT_IN_FOLDER.create(
                    I(co.getObjectID()),
                    I(fid),
                    I(ctx.getContextId())));
            }

            if (FolderObject.SYSTEM_LDAP_FOLDER_ID == fid && co.containsEmail1() && ctx.getMailadmin() != user && original.getInternalUserId() == user) {
                // User tries to edit his primary email address which is allowed by administrator only since this email address is used in
                // various places throughout the system. Therefore it is denied.
                throw new OXPermissionException(ContactExceptionCodes.NO_PRIMARY_EMAIL_EDIT.create(
                    I(ctx.getContextId()),
                    I(co.getObjectID()),
                    I(user)));
            }

            // Check Rights for Source Folder
            final int folder_whereto = co.getParentFolderID();
            final int folder_comesfrom = fid;

            final FolderObject contactFolder = new OXFolderAccess(readcon, ctx).getFolderObject(folder_comesfrom);
            if (contactFolder.getModule() != FolderObject.CONTACT) {
                throw new OXConflictException(ContactExceptionCodes.NON_CONTACT_FOLDER.create(
                    I(folder_comesfrom),
                    I(ctx.getContextId()),
                    I(user)));
            }
            final OXFolderAccess oxfs = new OXFolderAccess(readcon, ctx);
            final EffectivePermission oclPerm = oxfs.getFolderPermission(folder_comesfrom, user, uc);

            if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
                throw new OXPermissionException(ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(folder_comesfrom), I(ctx.getContextId()), I(user)));
            }
            if (!oclPerm.canWriteAllObjects()) {
                if (oclPerm.canWriteOwnObjects()) {
                    can_edit_only_own = true;
                } else {
                    throw new OXPermissionException(ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(co.getParentFolderID()), I(ctx.getContextId()), I(user)));
                }
            }

            // ++++ MOVE ++++ Check Rights for destination
            // Can delete from source?
            if (co.getParentFolderID() != fid) {
                if (!oclPerm.canDeleteAllObjects()) {
                    if (oclPerm.canDeleteOwnObjects()) {
                        can_delete_only_own = true;
                    } else {
                        throw new OXPermissionException(ContactExceptionCodes.NO_DELETE_PERMISSION.create(I(folder_comesfrom), I(ctx.getContextId()), I(user)));
                    }
                }

                final EffectivePermission op = oxfs.getFolderPermission(folder_whereto, user, uc);

                // Can create in destination?
                if (!op.canCreateObjects()) {
                    throw new OXPermissionException(ContactExceptionCodes.NO_CREATE_PERMISSION.create(I(folder_whereto), I(ctx.getContextId()), I(user)));
                }
                final FolderObject destination = new OXFolderAccess(readcon, ctx).getFolderObject(folder_whereto);
                if (destination.getModule() != FolderObject.CONTACT) {
                    throw new OXConflictException(ContactExceptionCodes.NON_CONTACT_FOLDER.create(I(folder_whereto), I(ctx.getContextId()), I(user)));
                }
                if (op.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
                    throw new OXPermissionException(ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(folder_whereto), I(ctx.getContextId()), I(user)));
                }
                if (!oclPerm.canCreateObjects()) {
                    throw new OXPermissionException(ContactExceptionCodes.NO_CREATE_PERMISSION.create(I(folder_whereto), I(ctx.getContextId()), I(user)));
                }
                // Following if-block should deal with all cases of move and private flag. Optimized with binary algebra
                if (contactFolder.getType() == FolderObject.PRIVATE && destination.getType() == FolderObject.PUBLIC) {
                    if (co.containsPrivateFlag() && co.getPrivateFlag() || !original.getPrivateFlag() && co.containsPrivateFlag()) {
                        throw new OXConflictException(ContactExceptionCodes.NO_PRIVATE_MOVE.create(I(ctx.getContextId()), I(co.getObjectID())));
                    }
                } else if (contactFolder.getType() == FolderObject.PUBLIC && destination.getType() == FolderObject.PUBLIC && co.containsPrivateFlag()) {
                    throw new OXConflictException(ContactExceptionCodes.MARK_PRIVATE_NOT_ALLOWED.create(I(ctx.getContextId()), I(co.getObjectID())));
                }
            }

            // ALL RIGHTS CHECK SO FAR, CHECK FOR READ ONLY OWN
            if (can_edit_only_own && (original.getCreatedBy() != user)) {
                throw new OXConflictException(ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(fid), I(ctx.getContextId()), I(user)));
            }
            if (can_delete_only_own && (original.getCreatedBy() != user)) {
                throw new OXConflictException(ContactExceptionCodes.NO_DELETE_PERMISSION.create(I(fid), I(ctx.getContextId()), I(user)));
            }
            if ((contactFolder.getType() != FolderObject.PRIVATE) && (co.getPrivateFlag())) {
                throw new OXConflictException(ContactExceptionCodes.MARK_PRIVATE_NOT_ALLOWED.create(I(ctx.getContextId()), I(co.getObjectID())));
            }
            if ((contactFolder.getType() == FolderObject.PRIVATE) && original.getPrivateFlag() && (original.getCreatedBy() != user)) {
                throw new OXPermissionException(ContactExceptionCodes.NO_CHANGE_PERMISSION.create(I(co.getObjectID()), I(ctx.getContextId())));
            }

            final java.util.Date server_date = original.getLastModified();
            if (DEBUG) {
                LOG.debug(new StringBuilder(
                    "Compare Dates for Contact Update\nClient-Date=" + client_date.getTime() + "\nServer-Date=" + server_date.getTime()));
            }
            if ((client_date != null) && (client_date.getTime() > -1) && (client_date.getTime() < server_date.getTime())) {
                throw new OXConcurrentModificationException(ContactExceptionCodes.OBJECT_HAS_CHANGED.create());
            }

            if (FolderObject.SYSTEM_LDAP_FOLDER_ID == co.getParentFolderID()) {
                if (co.containsDisplayName() && (null == co.getDisplayName() || "".equals(co.getDisplayName()))) {
                    throw ContactExceptionCodes.DISPLAY_NAME_MANDATORY.create();
                }
                if (co.containsSurName() && (null == co.getSurName() || "".equals(co.getSurName()))) {
                    throw ContactExceptionCodes.LAST_NAME_MANDATORY.create();
                }
                if (co.containsGivenName() && (null == co.getGivenName() || "".equals(co.getGivenName()))) {
                    throw ContactExceptionCodes.FIRST_NAME_MANDATORY.create();
                }
            }

            if ((co.getParentFolderID() == FolderObject.SYSTEM_LDAP_FOLDER_ID) && co.containsDisplayName() && (co.getDisplayName() != null)) {

                Statement stmt = null;
                ResultSet rs = null;

                final ContactSql csql = new ContactMySql(ctx, user);
                csql.setFolder(co.getParentFolderID());

                final ContactSearchObject cso = new ContactSearchObject();
                cso.setDisplayName(co.getDisplayName());
                cso.setIgnoreOwn(co.getObjectID());

                csql.setContactSearchObject(cso);
                csql.setSelect(csql.iFgetColsString(new int[] { 1, 20, }).toString());
                csql.setSearchHabit(" AND ");

                try {
                    stmt = csql.getSqlStatement(readcon);
                    rs = ((PreparedStatement) stmt).executeQuery();
                    if (rs.next()) {
                        throw ContactExceptionCodes.DISPLAY_NAME_IN_USE.create(I(ctx.getContextId()), I(co.getObjectID()));
                    }
                } catch (final SQLException e) {
                    throw ContactExceptionCodes.SQL_PROBLEM.create(e);
                } finally {
                    closeSQLStuff(rs, stmt);
                }
            }
            if ((!co.containsFileAs() || ((co.getFileAs() != null) && (co.getFileAs().length() > 0))) && (co.getDisplayName() != null)) {
                co.setFileAs(co.getDisplayName());
            }

            /*
             * Check for bad characters
             */
            checkCharacters(co);

        } catch (final DBPoolingException e) {
            throw ContactExceptionCodes.INIT_CONNECTION_FROM_DBPOOL.create(e);
        } catch (final OXConcurrentModificationException e) {
            throw e;
        } catch (final OXPermissionException e) {
            throw e;
        } catch (final OXConflictException e) {
            throw e;
        } catch (final OXObjectNotFoundException e) {
            throw e;
        } catch (final OXException e) {
            throw new ContactException(e);
        } finally {
            try {
                DBPool.closeReaderSilent(ctx, readcon);
            } catch (final Exception ex) {
                LOG.error("Unable to close READ Connection", ex);
            }
        }

        Connection writecon = null;
        PreparedStatement ps = null;
        final StringBuilder update = new StringBuilder();

        try {
            boolean modifiedDisplayName = false;
            final int[] mod = new int[650];
            int cnt = 0;
            for (int i = 0; i < 650; i++) {
                if ((mapping[i] != null) && !mapping[i].compare(co, original)) {
                    // Check if modified field is DISPLAY-NAME and contact denotes a system user
                    if (i == Contact.DISPLAY_NAME && original.getInternalUserId() > 0) {
                        modifiedDisplayName = true;
                    }
                    mod[cnt] = i;
                    cnt++;
                }
            }
            final int[] modtrim = new int[cnt];
            System.arraycopy(mod, 0, modtrim, 0, cnt);

            if (modtrim.length <= 0) {
                throw ContactExceptionCodes.NO_CHANGES.create(I(ctx.getContextId()), I(co.getObjectID()));
            }

            for (int i = 0; i < modtrim.length; i++) {
                if ((mapping[modtrim[i]] != null) && mapping[modtrim[i]].containsElement(co) && (modtrim[i] != Contact.DISTRIBUTIONLIST) && (modtrim[i] != Contact.LINKS) && (modtrim[i] != Contact.OBJECT_ID) && (i != Contact.IMAGE1_CONTENT_TYPE)) {
                    update.append(mapping[modtrim[i]].getDBFieldName()).append(" = ?,");
                }
            }
            final int id = co.getObjectID();
            if (id == -1) {
                throw new OXConflictException(ContactExceptionCodes.NEGATIVE_OBJECT_ID.create());
            }
            final long lmd = System.currentTimeMillis();

            final StringBuilder updater = cs.iFperformContactStorageUpdate(update, lmd, id, ctx.getContextId());

            writecon = DBPool.pickupWriteable(ctx);
            ps = writecon.prepareStatement(updater.toString());
            int counter = 1;
            for (int i = 0; i < modtrim.length; i++) {
                if ((mapping[modtrim[i]] != null) && mapping[modtrim[i]].containsElement(co) && (modtrim[i] != Contact.DISTRIBUTIONLIST) && (modtrim[i] != Contact.LINKS) && (modtrim[i] != Contact.OBJECT_ID) && (i != Contact.IMAGE1_CONTENT_TYPE)) {
                    mapping[modtrim[i]].fillPreparedStatement(ps, counter, co);
                    counter++;
                }
            }

            final Date ddd = new Date(lmd);
            co.setLastModified(ddd);

            writecon.setAutoCommit(false);

            if (DEBUG) {
                LOG.debug(new StringBuilder(
                    "INFO: YOU WANT TO UPDATE THIS: cid=" + ctx.getContextId() + " oid=" + co.getObjectID() + " -> " + ps.toString()));
            }

            if (co.getParentFolderID() != fid) {
                // Fake a deletion on MOVE operation for MS Outlook prior to performing actual UPDATE
                final Statement stmt = writecon.createStatement();
                try {
                    cs.iFbackupContact(stmt, ctx.getContextId(), co.getObjectID(), user);
                } finally {
                    try {
                        stmt.close();
                    } catch (final SQLException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }

            ps.execute();

            if (co.containsNumberOfDistributionLists() && (co.getSizeOfDistributionListArray() > 0)) {
                writeDistributionListArrayUpdate(
                    co.getDistributionList(),
                    original.getDistributionList(),
                    co.getObjectID(),
                    ctx.getContextId(),
                    writecon);
            }
            if (co.containsNumberOfLinks() && (co.getSizeOfLinks() > 0)) {
                writeContactLinkArrayUpdate(co.getLinks(), original.getLinks(), co.getObjectID(), ctx.getContextId(), writecon);
            }

            if (co.containsImage1()) {
                if (co.getImage1() != null) {
                    if (ContactConfig.getInstance().getProperty(PROP_SCALE_IMAGES).equalsIgnoreCase("true")) {
                        try {
                            co.setImage1(scaleContactImage(co.getImage1(), co.getImageContentType()));
                        } catch (final ContactException e) {
                            throw e;
                        } catch (final Exception e) {
                            throw ContactExceptionCodes.NOT_VALID_IMAGE.create(e);
                        }
                    } else {
                        checkImageSize(
                            co.getImage1().length,
                            Integer.parseInt(ContactConfig.getInstance().getProperty(PROP_MAX_IMAGE_SIZE)));
                    }

                    if (original.containsImage1()) {
                        updateContactImage(co.getObjectID(), co.getImage1(), ctx.getContextId(), co.getImageContentType(), writecon);
                    } else {
                        writeContactImage(co.getObjectID(), co.getImage1(), ctx.getContextId(), co.getImageContentType(), writecon);
                    }

                } else if (original.containsImage1()) {
                    try {
                        deleteImage(co.getObjectID(), ctx.getContextId(), writecon);
                    } catch (final SQLException oxee) {
                        LOG.error("Unable to delete Contact Image", oxee);
                    }
                }
            }
            // Check for DISPLAY-NAME update
            if (modifiedDisplayName) {
                OXFolderAdminHelper.propagateUserModification(
                    original.getInternalUserId(),
                    new int[] { Contact.DISPLAY_NAME },
                    System.currentTimeMillis(),
                    writecon,
                    writecon,
                    ctx.getContextId());
            }
            writecon.commit();
        } catch (final OXConflictException ox) {
            rollback(writecon);
            throw ox;
        } catch (final ContactException ox) {
            rollback(writecon);
            throw ox;
        } catch (final OXObjectNotFoundException ox) {
            rollback(writecon);
            throw ox;
        } catch (final OXConcurrentModificationException ox) {
            rollback(writecon);
            throw ox;
        } catch (final OXPermissionException ox) {
            rollback(writecon);
            throw ox;
        } catch (final DBPoolingException e) {
            throw ContactExceptionCodes.INIT_CONNECTION_FROM_DBPOOL.create(e);
        } catch (final DataTruncation se) {
            rollback(writecon);
            throw Contacts.getTruncation(writecon, se, "prg_contacts", co);
        } catch (final SQLException e) {
            rollback(writecon);
            throw ContactExceptionCodes.SQL_PROBLEM.create(e, getStatement(ps));
        } catch (final OXException e) {
            rollback(writecon);
            throw new ContactException(e);
        } finally {
            closeSQLStuff(ps);
            if (null != writecon) {
                autocommit(writecon);
                try {
                    DBPool.closeWriterSilent(ctx, writecon);
                } catch (final Exception ex) {
                    LOG.error("Unable to set return writeconnection");
                }
            }
        }
    }

    public static void performUserContactStorageUpdate(final Contact contact, final java.util.Date lastModified, final int userId, final int[] groups, final Context ctx, final UserConfiguration userConfig) throws ContactException, OXPermissionException, OXObjectNotFoundException, OXConflictException, OXConcurrentModificationException {
        validateEmailAddress(contact);
        if (!contact.containsParentFolderID() || (contact.getParentFolderID() == 0)) {
            contact.setParentFolderID(FolderObject.SYSTEM_LDAP_FOLDER_ID);
        }
        final ContactSql cs = new ContactMySql(ctx, userId);
        Contact original = null;
        Connection writecon = null;
        Connection readcon = null;
        try {
            readcon = DBPool.pickup(ctx);
            try {
                original = getContactById(contact.getObjectID(), userId, groups, ctx, userConfig, readcon);
            } catch (final Exception e) {
                throw new OXObjectNotFoundException(ContactExceptionCodes.LOAD_OLD_CONTACT_FAILED.create(e, I(ctx.getContextId()), I(contact.getObjectID())));
            }
            // Check if contact really exists in specified folder
            if (contact.containsEmail1() && ctx.getMailadmin() != userId && original.getInternalUserId() == userId) {
                // User tries to edit his primary email address which is allowed by administrator only since this email address is used in
                // various places throughout the system. Therefore it is denied.
                throw new OXPermissionException(ContactExceptionCodes.NO_PRIMARY_EMAIL_EDIT.create(I(ctx.getContextId()), I(contact.getObjectID()), I(userId)));
            }

            // user address operation
            if (contact.getParentFolderID() != FolderObject.SYSTEM_LDAP_FOLDER_ID) {
                throw new OXPermissionException(ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(FolderObject.SYSTEM_LDAP_FOLDER_ID), I(ctx.getContextId()), I(userId)));
            }
            // ALL RIGHTS CHECK SO FAR, CHECK FOR MODIFY ONLY OWN
            if (original.getCreatedBy() != userId) {
                throw new OXConflictException(ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(FolderObject.SYSTEM_LDAP_FOLDER_ID), I(ctx.getContextId()), I(userId)));
            }

            final java.util.Date server_date = original.getLastModified();
            if (DEBUG) {
                LOG.debug("Compare Dates for Contact Update\nClient-Date=" + lastModified.getTime() + "\nServer-Date=" + server_date.getTime());
            }
            if ((lastModified != null) && (lastModified.getTime() > -1) && (lastModified.getTime() < server_date.getTime())) {
                throw new OXConcurrentModificationException(ContactExceptionCodes.OBJECT_HAS_CHANGED.create());
            }
            if (contact.containsDisplayName() && (null == contact.getDisplayName() || "".equals(contact.getDisplayName()))) {
                throw ContactExceptionCodes.DISPLAY_NAME_MANDATORY.create();
            }
            if (contact.containsSurName() && (null == contact.getSurName() || "".equals(contact.getSurName()))) {
                throw ContactExceptionCodes.LAST_NAME_MANDATORY.create();
            }
            if (contact.containsGivenName() && (null == contact.getGivenName() || "".equals(contact.getGivenName()))) {
                throw ContactExceptionCodes.FIRST_NAME_MANDATORY.create();
            }

            // Check for duplicate display name.
            if (contact.containsDisplayName() && (contact.getDisplayName() != null)) {
                Statement stmt = null;
                ResultSet rs = null;
                final ContactSql csql = new ContactMySql(ctx, userId);
                csql.setFolder(contact.getParentFolderID());
                final ContactSearchObject cso = new ContactSearchObject();
                cso.setDisplayName(contact.getDisplayName());
                cso.setIgnoreOwn(contact.getObjectID());
                csql.setContactSearchObject(cso);
                final int[] cols = new int[] { Contact.OBJECT_ID, Contact.FOLDER_ID, Contact.DISPLAY_NAME };
                csql.setSelect(csql.iFgetColsString(cols).toString());
                csql.setSearchHabit(" AND ");
                try {
                    stmt = csql.getSqlStatement(readcon);
                    rs = ((PreparedStatement) stmt).executeQuery();
                    while (rs.next()) {
                        final String displayName = rs.getString(10); // ContactMySql.PREFIXED_FIELDS (7) + cols[3]
                        if (contact.getDisplayName().equalsIgnoreCase(displayName)) {
                            throw ContactExceptionCodes.DISPLAY_NAME_IN_USE.create(I(ctx.getContextId()), I(contact.getObjectID()));
                        }
                    }
                } catch (final SQLException e) {
                    throw ContactExceptionCodes.SQL_PROBLEM.create(e);
                } finally {
                    closeSQLStuff(rs, stmt);
                }
            }
            if ((!contact.containsFileAs() || ((contact.getFileAs() != null) && (contact.getFileAs().length() > 0))) && (contact.getDisplayName() != null)) {
                contact.setFileAs(contact.getDisplayName());
            }

            // Check for bad characters
            checkCharacters(contact);
        } catch (final DBPoolingException e) {
            throw ContactExceptionCodes.INIT_CONNECTION_FROM_DBPOOL.create(e);
        } finally {
            try {
                DBPool.closeReaderSilent(ctx, readcon);
            } catch (final Exception ex) {
                LOG.error("Unable to close READ Connection", ex);
            }
        }

        PreparedStatement ps = null;
        final StringBuilder update = new StringBuilder();
        try {
            boolean modifiedDisplayName = false;
            final int[] mod = new int[650];
            int cnt = 0;
            for (int i = 0; i < 650; i++) {
                if ((mapping[i] != null) && !mapping[i].compare(contact, original)) {
                    // Check if modified field is DISPLAY-NAME and contact denotes a system user
                    if (i == Contact.DISPLAY_NAME && original.getInternalUserId() > 0) {
                        modifiedDisplayName = true;
                    }
                    mod[cnt] = i;
                    cnt++;
                }
            }
            final int[] modtrim = new int[cnt];
            System.arraycopy(mod, 0, modtrim, 0, cnt);

            if (modtrim.length <= 0) {
                throw ContactExceptionCodes.NO_CHANGES.create(I(ctx.getContextId()), I(contact.getObjectID()));
            }

            for (int i = 0; i < modtrim.length; i++) {
                if ((mapping[modtrim[i]] != null) && mapping[modtrim[i]].containsElement(contact) && (modtrim[i] != Contact.DISTRIBUTIONLIST) && (modtrim[i] != Contact.LINKS) && (modtrim[i] != Contact.OBJECT_ID) && (i != Contact.IMAGE1_CONTENT_TYPE)) {
                    update.append(mapping[modtrim[i]].getDBFieldName()).append(" = ?,");
                }
            }
            final int id = contact.getObjectID();
            if (id == -1) {
                throw new OXConflictException(ContactExceptionCodes.NEGATIVE_OBJECT_ID.create());
            }
            final long lmd = System.currentTimeMillis();

            final StringBuilder updater = cs.iFperformContactStorageUpdate(update, lmd, id, ctx.getContextId());

            writecon = DBPool.pickupWriteable(ctx);
            ps = writecon.prepareStatement(updater.toString());
            int counter = 1;
            for (int i = 0; i < modtrim.length; i++) {
                if ((mapping[modtrim[i]] != null) && mapping[modtrim[i]].containsElement(contact) && (modtrim[i] != Contact.DISTRIBUTIONLIST) && (modtrim[i] != Contact.LINKS) && (modtrim[i] != Contact.OBJECT_ID) && (i != Contact.IMAGE1_CONTENT_TYPE)) {
                    mapping[modtrim[i]].fillPreparedStatement(ps, counter, contact);
                    counter++;
                }
            }

            final Date ddd = new Date(lmd);
            contact.setLastModified(ddd);

            writecon.setAutoCommit(false);

            if (DEBUG) {
                LOG.debug("INFO: YOU WANT TO UPDATE THIS: cid=" + ctx.getContextId() + " oid=" + contact.getObjectID() + " -> " + ps.toString());
            }
            ps.execute();

            if (contact.containsNumberOfDistributionLists() && (contact.getSizeOfDistributionListArray() > 0)) {
                writeDistributionListArrayUpdate(
                    contact.getDistributionList(),
                    original.getDistributionList(),
                    contact.getObjectID(),
                    ctx.getContextId(),
                    writecon);
            }
            if (contact.containsNumberOfLinks() && (contact.getSizeOfLinks() > 0)) {
                writeContactLinkArrayUpdate(contact.getLinks(), original.getLinks(), contact.getObjectID(), ctx.getContextId(), writecon);
            }

            if (contact.containsImage1()) {
                if (contact.getImage1() != null) {
                    if (ContactConfig.getInstance().getProperty(PROP_SCALE_IMAGES).equalsIgnoreCase("true")) {
                        try {
                            contact.setImage1(scaleContactImage(contact.getImage1(), contact.getImageContentType()));
                        } catch (final ContactException e) {
                            throw e;
                        } catch (final Exception e) {
                            throw ContactExceptionCodes.NOT_VALID_IMAGE.create(e);
                        }
                    } else {
                        checkImageSize(contact.getImage1().length, Integer.parseInt(ContactConfig.getInstance().getProperty(
                            PROP_MAX_IMAGE_SIZE)));
                    }
                    if (original.containsImage1()) {
                        updateContactImage(contact.getObjectID(), contact.getImage1(), ctx.getContextId(), contact.getImageContentType(), writecon);
                    } else {
                        writeContactImage(contact.getObjectID(), contact.getImage1(), ctx.getContextId(), contact.getImageContentType(), writecon);
                    }
                } else if (original.containsImage1()) {
                    try {
                        deleteImage(contact.getObjectID(), ctx.getContextId(), writecon);
                    } catch (final SQLException oxee) {
                        LOG.error("Unable to delete Contact Image", oxee);
                    }
                }
            }
            // Check for DISPLAY-NAME update
            if (modifiedDisplayName) {
                OXFolderAdminHelper.propagateUserModification(
                    original.getInternalUserId(),
                    new int[] { Contact.DISPLAY_NAME },
                    System.currentTimeMillis(),
                    writecon,
                    writecon,
                    ctx.getContextId());
            }
            writecon.commit();
        } catch (final OXConflictException e) {
            rollback(writecon);
            throw e;
        } catch (final OXPermissionException e) {
            rollback(writecon);
            throw e;
        } catch (final OXObjectNotFoundException e) {
            rollback(writecon);
            throw e;
        } catch (final OXConcurrentModificationException e) {
            rollback(writecon);
            throw e;
        } catch (final ContactException ox) {
            rollback(writecon);
            throw ox;
        } catch (final DBPoolingException e) {
            throw ContactExceptionCodes.INIT_CONNECTION_FROM_DBPOOL.create(e);
        } catch (final DataTruncation se) {
            rollback(writecon);
            throw Contacts.getTruncation(writecon, se, "prg_contacts", contact);
        } catch (final SQLException e) {
            rollback(writecon);
            throw ContactExceptionCodes.SQL_PROBLEM.create(e, getStatement(ps));
        } catch (final OXException e) {
            rollback(writecon);
            throw new ContactException(e);
        } finally {
            closeSQLStuff(ps);
            if (null != writecon) {
                autocommit(writecon);
                try {
                    DBPool.closeWriterSilent(ctx, writecon);
                } catch (final Exception ex) {
                    LOG.error("Unable to set return writeconnection");
                }
            }
        }
    }

    public static Contact getUserById(final int userId, final int user, final int[] memberInGroups, final Context ctx, final UserConfiguration uc, final Connection readCon) throws ContactException, OXObjectNotFoundException {
        final ContactSql contactSQL = new ContactMySql(ctx, user);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 650; i++) {
            if (mapping[i] != null) {
                sb.append(',');
                sb.append("co.");
                sb.append(mapping[i].getDBFieldName());
            }
        }
        sb.deleteCharAt(0);
        sb = contactSQL.iFgetContactById(sb.toString());
        contactSQL.setSelect(sb.toString());
        contactSQL.setInternalUser(userId);
        return fillContactObject(contactSQL, userId, user, memberInGroups, ctx, uc, readCon);
    }

    private static final int LIMIT = 1000;

    public static Contact[] getUsersById(final int[] userIds, final int user, final int[] memberInGroups, final Context ctx, final UserConfiguration uc, final Connection readCon) throws ContactException, OXObjectNotFoundException {
        final ContactSql contactSQL = new ContactMySql(ctx, user);
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 650; i++) {
            if (mapping[i] != null) {
                sb.append(',');
                sb.append("co.");
                sb.append(mapping[i].getDBFieldName());
            }
        }
        sb.deleteCharAt(0);
        contactSQL.setSelect(contactSQL.iFgetContactById(sb.toString()).toString());
        final TIntObjectHashMap<Contact> contacts = new TIntObjectHashMap<Contact>(userIds.length, 1);
        for (int i = 0; i < userIds.length; i += LIMIT) {
            contactSQL.setInternalUsers(com.openexchange.tools.arrays.Arrays.extract(userIds, i, LIMIT));
            contacts.putAll(fillContactObject(contactSQL, user, memberInGroups, ctx, uc, readCon));
        }
        final Contact[] retval = new Contact[userIds.length];
        for (int i = 0; i < userIds.length; i++) {
            final Contact contact = contacts.get(userIds[i]);
            if (null == contact) {
                throw new OXObjectNotFoundException(ContactExceptionCodes.CONTACT_NOT_FOUND.create(I(userIds[i]), I(ctx.getContextId())));
            }
            retval[i] = contact;
        }
        return retval;
    }

    public static Contact getContactById(final int objectId, final Session session) throws ContactException, ContextException, DBPoolingException, OXObjectNotFoundException {
        final Context ctx = ContextStorage.getStorageContext(session);
        final int[] groups = UserStorage.getStorageUser(session.getUserId(), ctx).getGroups();
        final Connection readCon = DBPool.pickup(ctx);
        final Contact co;
        try {
            final UserConfiguration uc = UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), ctx);
            co = getContactById(objectId, session.getUserId(), groups, ctx, uc, readCon);
        } finally {
            DBPool.closeReaderSilent(ctx, readCon);
        }
        return co;
    }

    public static Contact getContactById(final int objectId, final int userId, final int[] memberInGroups, final Context ctx, final UserConfiguration uc, final Connection readCon) throws ContactException, OXObjectNotFoundException {

        Contact co = null;
        final ContactSql contactSQL = new ContactMySql(ctx, userId);

        StringBuilder sb = new StringBuilder(512);
        for (int i = 0; i < 650; i++) {
            if (mapping[i] != null) {
                sb.append(',');
                sb.append("co.");
                sb.append(mapping[i].getDBFieldName());
            }
        }
        sb.deleteCharAt(0);
        sb = contactSQL.iFgetContactById(sb.toString());
        contactSQL.setSelect(sb.toString());
        contactSQL.setObjectID(objectId);
        co = fillContactObject(contactSQL, objectId, userId, memberInGroups, ctx, uc, readCon);

        return co;
    }

    private static Contact fillContactObject(final ContactSql contactSQL, final int objectId, final int user, final int[] group, final Context ctx, final UserConfiguration uc, final Connection con) throws ContactException, OXObjectNotFoundException {
        final Contact co = new Contact();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = contactSQL.getSqlStatement(con);
            rs = ((PreparedStatement) stmt).executeQuery();
            if (rs.next()) {
                int cnt = 1;
                for (int i = 0; i < 650; i++) {
                    if (mapping[i] != null) {
                        mapping[i].addToContactObject(rs, cnt, co, con, user, group, ctx, uc);
                        cnt++;
                    }
                }
            } else {
                throw new OXObjectNotFoundException(ContactExceptionCodes.CONTACT_NOT_FOUND.create( I(objectId), I(ctx.getContextId())));
            }
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            closeSQLStuff(rs, stmt);
        }
        return co;
    }

    private static TIntObjectHashMap<Contact> fillContactObject(final ContactSql contactSQL, final int user, final int[] group, final Context ctx, final UserConfiguration uc, final Connection con) throws ContactException {
        final TIntObjectHashMap<Contact> contacts = new TIntObjectHashMap<Contact>();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = contactSQL.getSqlStatement(con);
            result = stmt.executeQuery();
            while (result.next()) {
                final Contact contact = new Contact();
                int cnt = 1;
                for (int i = 0; i < 650; i++) {
                    if (mapping[i] != null) {
                        mapping[i].addToContactObject(result, cnt, contact, con, user, group, ctx, uc);
                        cnt++;
                    }
                }
                contacts.put(contact.getInternalUserId(), contact);
            }
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            closeSQLStuff(result, stmt);
        }
        return contacts;
    }

    public static void deleteContact(final int id, final int cid, final Connection writecon) throws ContactException {
        deleteContact(id, cid, writecon, false);
    }

    public static void deleteContact(final int id, final int cid, final Connection writecon, final boolean admin_delete) throws ContactException {
        Statement del = null;
        try {
            del = writecon.createStatement();
            trashDistributionList(id, cid, writecon, false);
            trashLinks(id, cid, writecon, false);
            trashImage(id, cid, writecon, false);

            final ContactSql cs = new ContactMySql(null);

            if (admin_delete) {
                cs.iFtrashTheAdmin(del, cid, id);
            } else {
                cs.iFdeleteContact(id, cid, del);
            }
        } catch (final ContextException d) {
            throw new ContactException(d);
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            closeSQLStuff(del);
        }
    }

    public static DistributionListEntryObject[] fillDistributionListArray(final int id, final int user, final Context ctx, final Connection readcon) throws ContactException {

        Statement stmt = null;
        ResultSet rs = null;
        DistributionListEntryObject[] r = null;

        try {

            final ContactSql cs = new ContactMySql(ctx, user);

            stmt = readcon.createStatement();
            rs = stmt.executeQuery(cs.iFfillDistributionListArray(id, ctx.getContextId()));

            rs.last();
            final int size = rs.getRow();
            rs.beforeFirst();

            final DistributionListEntryObject[] dleos = new DistributionListEntryObject[size];
            DistributionListEntryObject dleo = null;

            String displayname = null;
            String lastname = null;
            String firstname = null;
            int emailfield = 0;
            int objectid = 0;
            int folderid = 0;
            int cnt = 0;

            while (rs.next()) {
                dleo = new DistributionListEntryObject();

                displayname = rs.getString(5);
                if (!rs.wasNull()) {
                    dleo.setDisplayname(displayname);
                }
                lastname = rs.getString(6);
                if (!rs.wasNull()) {
                    dleo.setLastname(lastname);
                }
                firstname = rs.getString(7);
                if (!rs.wasNull()) {
                    dleo.setFirstname(firstname);
                }
                dleo.setEmailaddress(rs.getString(8));

                objectid = rs.getInt(2);
                if (!rs.wasNull() && (objectid > 0)) {
                    dleo.setEntryID(objectid);
                    /*
                     * if (!performContactReadCheckByID(objectid, user,group,so)){ continue; }
                     */
                }

                emailfield = rs.getInt(3);
                if (!rs.wasNull()) {
                    dleo.setEmailfield(emailfield);
                }
                folderid = rs.getInt(4);
                if (!rs.wasNull()) {
                    dleo.setFolderID(folderid);
                }
                dleos[cnt] = dleo;
                cnt++;
            }
            r = new DistributionListEntryObject[cnt];
            System.arraycopy(dleos, 0, r, 0, cnt);
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            closeSQLStuff(rs, stmt);
        }
        return r;
    }

    public static void writeDistributionListArrayInsert(final DistributionListEntryObject[] dleos, final int id, final int cid, final Connection writecon) throws ContactException {

        DistributionListEntryObject dleo = null;

        PreparedStatement ps = null;
        try {

            final ContactSql cs = new ContactMySql(null);
            ps = writecon.prepareStatement(cs.iFwriteDistributionListArrayInsert());
            for (int i = 0; i < dleos.length; i++) {
                dleo = dleos[i];
                ps.setInt(1, id);

                if (dleo.containsEntryID() && (dleo.getEntryID() > 0)) {
                    ps.setInt(2, dleo.getEntryID());
                    ps.setInt(3, dleo.getEmailfield());
                    ps.setInt(9, dleo.getFolderID());
                } else {
                    ps.setNull(2, java.sql.Types.INTEGER);
                    ps.setNull(3, java.sql.Types.INTEGER);
                    ps.setNull(9, java.sql.Types.INTEGER);
                }
                if (dleo.containsDisplayname()) {
                    ps.setString(4, dleo.getDisplayname());
                } else if ((dleo.containsLastname() && (dleo.getLastname() != null)) && (dleo.containsFistname() && (dleo.getFirstname() != null))) {
                    ps.setString(4, dleo.getLastname() + ", " + dleo.getFirstname());
                } else if ((dleo.containsLastname() && (dleo.getLastname() != null)) && !dleo.containsFistname()) {
                    ps.setString(4, dleo.getLastname());
                } else {
                    ps.setString(4, "unknown");
                }
                if (dleo.containsLastname() && (dleo.getLastname() != null)) {
                    ps.setString(5, dleo.getLastname());
                } else {
                    ps.setNull(5, java.sql.Types.VARCHAR);
                }
                if (dleo.containsFistname() && (dleo.getFirstname() != null)) {
                    ps.setString(6, dleo.getFirstname());
                } else {
                    ps.setNull(6, java.sql.Types.VARCHAR);
                }
                ps.setString(7, dleo.getEmailaddress());
                ps.setInt(8, cid);

                if (DEBUG) {
                    LOG.debug(new StringBuilder("WRITE DLIST ").append(ps.toString()));
                }

                ps.execute();
            }
        } catch (final ContextException d) {
            throw new ContactException(d);
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            closeSQLStuff(ps);
        }
    }

    public static void writeDistributionListArrayUpdate(final DistributionListEntryObject[] dleos, final DistributionListEntryObject[] old_dleos, final int id, final int cid, final Connection writecon) throws ContactException {
        DistributionListEntryObject new_one = null;
        DistributionListEntryObject old_one = null;

        final int sizey = (dleos == null ? 0 : dleos.length) + (old_dleos == null ? 0 : old_dleos.length);
        final DistributionListEntryObject[] inserts = new DistributionListEntryObject[sizey];
        final DistributionListEntryObject[] updates = new DistributionListEntryObject[sizey];
        final DistributionListEntryObject[] deletes = new DistributionListEntryObject[sizey];

        int insert_count = 0;
        int update_count = 0;
        int delete_count = 0;

        if (null != dleos) {
            for (int i = 0; i < dleos.length; i++) { // this for;next goes to all new entries from the client
                new_one = dleos[i];
        
                if (new_one.containsEntryID() && (new_one.getEntryID() > 0)) { // this is a real contact entry in the distributionlist
                    boolean actions = false;
        
                    if (old_dleos != null) {
                        for (int u = 0; u < old_dleos.length; u++) { // this for;next goes to all old entries from the server
                            if (old_dleos[u] != null) { // maybe we have some empty entries here from previous checks
                                old_one = old_dleos[u];
        
                                if (new_one.searchDlistObject(old_one)) { // this will search the current entry in the old dlist
                                    if (!new_one.compareDlistObject(old_one)) {
                                        // is this true the dlistentrie has not changed, is it false the dlistentry missmatches the old one
                                        // ok the dlist has changed and needs to get updated
                                        updates[insert_count] = new_one;
                                        update_count++;
                                        actions = true;
                                    } else {
                                        actions = true;
                                        // ignore this entry cuz it has not changed
                                    }
                                    // ok we have found a entry in the old list and we have done something with him no we must remove him
                                    // from the old list cuz maybe he needs get deleted
                                    old_dleos[u] = null;
                                    break; // when is the entry is found we can leave the old list for the nex new entry
                                }
                                // this old entry does not match the new one
                                actions = false;
                            }
                        }
                    }
                    // we checked the old list and nothing was found. this means we have to insert this entry cuz it is new
                    if (!actions) {
                        inserts[insert_count] = new_one;
                        insert_count++;
                    }
                } else { // this is an independent entry in a distributionlist and they get a normal insert
                    inserts[insert_count] = new_one;
                    insert_count++;
                }
            }
        }

        // the new list is fully checked, now we have to make sure that old entries get deleted
        if (old_dleos != null) {
            for (int u = 0; u < old_dleos.length; u++) { // this for;next goes to all old entries from the server
                old_one = old_dleos[u];
                if ((old_one != null) && old_one.containsEntryID() && (old_one.getEntryID() > 0)) {
                    // maybe we have some empty entries here from previous checks
                    deletes[delete_count] = old_one;
                    delete_count++;
                }
            }
        }
        // all is checked, we have 3 arrays now INSERT, UPDATE and DELETE. just make the stuff now

        final DistributionListEntryObject[] insertcut = new DistributionListEntryObject[insert_count];
        System.arraycopy(inserts, 0, insertcut, 0, insert_count);

        final DistributionListEntryObject[] updatecut = new DistributionListEntryObject[update_count];
        System.arraycopy(updates, 0, updatecut, 0, update_count);

        final DistributionListEntryObject[] deletecut = new DistributionListEntryObject[delete_count];
        System.arraycopy(deletes, 0, deletecut, 0, delete_count);

        deleteDistributionListEntriesByIds(id, deletecut, cid, writecon);
        updateDistributionListEntriesByIds(id, updatecut, cid, writecon);
        writeDistributionListArrayInsert(insertcut, id, cid, writecon);
    }

    public static void updateDistributionListEntriesByIds(final int id, final DistributionListEntryObject[] dleos, final int cid, final Connection writecon) throws ContactException {
        if (dleos.length > 0) {

            DistributionListEntryObject dleo = null;

            PreparedStatement ps = null;
            try {
                final ContactSql cs = new ContactMySql(null);
                ps = writecon.prepareStatement(cs.iFupdateDistributionListEntriesByIds());
                for (int i = 0; i < dleos.length; i++) {
                    dleo = dleos[i];

                    ps.setInt(1, id);
                    ps.setInt(9, id);

                    if (dleo.containsEntryID() && (dleo.getEntryID() > 0)) {
                        ps.setInt(2, dleo.getEntryID());
                        ps.setInt(3, dleo.getEmailfield());
                        ps.setInt(10, dleo.getEntryID());
                        ps.setInt(11, dleo.getEmailfield());
                        ps.setInt(4, dleo.getFolderID());
                    } else {
                        ps.setNull(2, java.sql.Types.INTEGER);
                        ps.setNull(3, java.sql.Types.INTEGER);
                        ps.setNull(10, java.sql.Types.INTEGER);
                        ps.setNull(11, java.sql.Types.INTEGER);
                        ps.setNull(4, java.sql.Types.INTEGER);
                    }
                    if (dleo.containsDisplayname()) {
                        ps.setString(5, dleo.getDisplayname());
                    } else if (dleo.containsLastname() && dleo.containsFistname()) {
                        ps.setString(5, dleo.getLastname() + ", " + dleo.getFirstname());
                    } else if (dleo.containsLastname() && !dleo.containsFistname()) {
                        ps.setString(5, dleo.getLastname());
                    } else {
                        ps.setString(5, "unknown");
                    }
                    if (dleo.containsLastname()) {
                        ps.setString(6, dleo.getLastname());
                    } else {
                        ps.setNull(6, java.sql.Types.VARCHAR);
                    }
                    if (dleo.containsFistname()) {
                        ps.setString(7, dleo.getFirstname());
                    } else {
                        ps.setNull(7, java.sql.Types.VARCHAR);
                    }
                    ps.setString(8, dleo.getEmailaddress());
                    ps.setInt(12, cid);

                    if (DEBUG) {
                        LOG.debug(new StringBuilder("UPDATE DLIST ").append(ps.toString()));
                    }

                    ps.execute();

                }
            } catch (final ContextException d) {
                throw new ContactException(d);
            } catch (final SQLException e) {
                throw ContactExceptionCodes.SQL_PROBLEM.create(e);
            } finally {
                closeSQLStuff(ps);
            }
        }
    }

    public static void deleteDistributionListEntriesByIds(final int id, final DistributionListEntryObject[] dleos, final int cid, final Connection writecon) throws ContactException {

        PreparedStatement ps = null;
        DistributionListEntryObject dleo = null;
        ContactSql cs = null;
        try {
            cs = new ContactMySql(null);
            ps = writecon.prepareStatement(cs.iFdeleteDistributionListEntriesByIds(cid));
            ps.setInt(1, id);
            if (DEBUG) {
                LOG.debug(new StringBuilder("DELETE FROM DLIST ").append(ps.toString()));
            }
            ps.execute();
        } catch (final ContextException d) {
            throw new ContactException(d);
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            closeSQLStuff(ps);
        }

        if (dleos.length > 0) {
            try {
                ps = writecon.prepareStatement(cs.iFdeleteDistributionListEntriesByIds2());
                for (int i = 0; i < dleos.length; i++) {
                    dleo = dleos[i];

                    ps.setInt(1, id);

                    if (dleo.containsEntryID() && (dleo.getEntryID() > 0)) {
                        ps.setInt(2, dleo.getEntryID());
                        ps.setInt(3, dleo.getEmailfield());
                    }
                    ps.setInt(4, cid);
                    if (DEBUG) {
                        LOG.debug(new StringBuilder("DELETE FROM DLIST ").append(ps.toString()));
                    }
                    ps.execute();
                }
            } catch (final SQLException e) {
                throw ContactExceptionCodes.SQL_PROBLEM.create(e, getStatement(ps));
            } finally {
                closeSQLStuff(ps);
            }
        }
    }

    public static LinkEntryObject[] fillLinkArray(final Contact co, final int user, final Context ctx, final Connection readcon) throws ContactException {

        Statement stmt = null;
        ResultSet rs = null;
        LinkEntryObject[] r = null;
        final ContactSql cs = new ContactMySql(ctx, user);

        try {
            final int id = co.getObjectID();

            stmt = readcon.createStatement();
            rs = stmt.executeQuery(cs.iFgetFillLinkArrayString(id, ctx.getContextId()));

            rs.last();
            final int size = rs.getRow();
            rs.beforeFirst();

            final LinkEntryObject[] leos = new LinkEntryObject[size];
            LinkEntryObject leo = null;

            String contact_displayname = null;
            String link_displayname = null;
            int linkid = 0;

            int cnt = 0;

            while (rs.next()) {
                leo = new LinkEntryObject();

                leo.setContactID(id);

                contact_displayname = rs.getString(3);
                if (!rs.wasNull()) {
                    leo.setContactDisplayname(contact_displayname);
                }
                link_displayname = rs.getString(4);
                if (!rs.wasNull()) {
                    leo.setLinkDisplayname(link_displayname);
                }
                linkid = rs.getInt(2);
                if (!rs.wasNull()) {
                    leo.setLinkID(linkid);
                }

                leos[cnt] = leo;
                cnt++;
            }

            r = new LinkEntryObject[cnt];
            System.arraycopy(leos, 0, r, 0, cnt);
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            closeSQLStuff(rs, stmt);
        }

        return r;
    }

    public static void writeContactLinkArrayInsert(final LinkEntryObject[] leos, final int id, final int cid, final Connection writecon) throws ContactException {
        LinkEntryObject leo = null;

        PreparedStatement ps = null;
        try {
            final ContactSql cs = new ContactMySql(null);
            ps = writecon.prepareStatement(cs.iFwriteContactLinkArrayInsert());
            for (int i = 0; i < leos.length; i++) {
                leo = leos[i];
                ps.setInt(1, id);
                ps.setInt(2, leo.getLinkID());
                ps.setString(3, leo.getContactDisplayname());
                ps.setString(4, leo.getLinkDisplayname());
                ps.setInt(5, cid);
                if (DEBUG) {
                    LOG.debug(new StringBuilder("INSERT LINKAGE ").append(ps.toString()));
                }
                ps.execute();
            }
        } catch (final ContextException d) {
            throw new ContactException(d);
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            closeSQLStuff(ps);
        }
    }

    public static void writeContactLinkArrayUpdate(final LinkEntryObject[] leos, final LinkEntryObject[] original, final int id, final int cid, final Connection writecon) throws ContactException {

        final int sizey = (null == leos ? 0 : leos.length) + (null == original ? 0 : original.length);
        final LinkEntryObject[] inserts = new LinkEntryObject[sizey];
        final LinkEntryObject[] deletes = new LinkEntryObject[sizey];
        int delete_count = 0;
        int insert_count = 0;

        if (null != leos) {
            for (int i = 0; i < leos.length; i++) {
                final LinkEntryObject new_leo = leos[i];
                boolean action = false;
    
                if (original != null) {
                    for (int u = 0; u < original.length; u++) {
                        final LinkEntryObject old_leo = original[u];
    
                        if (new_leo.compare(old_leo)) {
                            // found this link in the old ones
                            original[u] = null;
                            action = true;
                            break;
                        }
                        // this one don't equal
                        action = false;
                    }
                }
                if (!action) {
                    // nothing found so it is a new one
                    inserts[insert_count] = new_leo;
                    insert_count++;
                }
            }
        }
        if (original != null) {
            for (int i = 0; i < original.length; i++) {
                final LinkEntryObject del_leo = original[i];
                if (del_leo != null) {
                    deletes[delete_count] = del_leo;
                    delete_count++;
                }
            }
        }
        final LinkEntryObject[] deletecut = new LinkEntryObject[delete_count];
        System.arraycopy(deletes, 0, deletecut, 0, delete_count);

        final LinkEntryObject[] insertcut = new LinkEntryObject[insert_count];
        System.arraycopy(inserts, 0, insertcut, 0, insert_count);

        deleteLinkEntriesByIds(id, deletecut, cid, writecon);
        writeContactLinkArrayInsert(insertcut, id, cid, writecon);
    }

    public static void deleteLinkEntriesByIds(final int id, final LinkEntryObject[] leos, final int cid, final Connection writecon) throws ContactException {
        if (leos.length > 0) {
            LinkEntryObject leo = null;
            PreparedStatement ps = null;
            try {
                final ContactSql cs = new ContactMySql(null);
                ps = writecon.prepareStatement(cs.iFgetdeleteLinkEntriesByIdsString());
                for (int i = 0; i < leos.length; i++) {
                    leo = leos[i];
                    ps.setInt(1, id);
                    ps.setInt(2, leo.getLinkID());
                    ps.setInt(3, cid);
                    if (DEBUG) {
                        LOG.debug(new StringBuilder("DELETE LINKAGE ENTRY").append(ps.toString()));
                    }
                    ps.execute();
                }
            } catch (final ContextException d) {
                throw new ContactException(d);
            } catch (final SQLException e) {
                throw ContactExceptionCodes.SQL_PROBLEM.create(e);
            } finally {
                closeSQLStuff(ps);
            }
        }
    }

    public static Date getContactImageLastModified(final int id, final int cid, final Connection readcon) throws SQLException, ContactException {
        Date last_mod = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            final ContactSql cs = new ContactMySql(null);
            stmt = readcon.createStatement();
            rs = stmt.executeQuery(cs.iFgetContactImageLastModified(id, cid));
            if (rs.next()) {
                last_mod = new Date(rs.getLong(1));
            }
        } catch (final ContextException d) {
            throw new ContactException(d);
        } catch (final SQLException sxe) {
            throw sxe;
        } finally {
            closeSQLStuff(rs, stmt);
        }

        return last_mod;
    }

    public static String getContactImageContentType(final int id, final int cid, final Connection readcon) throws SQLException, ContactException {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            final ContactSql cs = new ContactMySql(null);
            stmt = readcon.createStatement();
            rs = stmt.executeQuery(cs.iFgetContactImageContentType(id, cid));
            if (rs.next()) {
                return rs.getString(1);
            }
            return null;
        } catch (final ContextException d) {
            throw new ContactException(d);
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    public static void getContactImage(final int contact_id, final Contact co, final int cid, final Connection readcon) throws ContactException {
        Date last_mod = null;

        Statement stmt = null;
        ResultSet rs = null;
        try {
            final ContactSql cs = new ContactMySql(null);
            stmt = readcon.createStatement();
            rs = stmt.executeQuery(cs.iFgetContactImage(contact_id, cid));
            if (rs.next()) {
                final byte[] bb = rs.getBytes(1);
                if (!rs.wasNull()) {
                    last_mod = new Date(rs.getLong(2));
                    co.setImageLastModified(last_mod);
                    co.setImage1(bb);
                    co.setImageContentType(rs.getString(3));
                }
            }
        } catch (final ContextException d) {
            throw new ContactException(d);
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    public static void writeContactImage(final int contact_id, final byte[] img, final int cid, final String mime, final Connection writecon) throws OXConflictException, ContactException {
        if ((contact_id < 1) || (img == null) || (img.length < 1) || (cid < 1) || (mime == null) || (mime.length() < 1)) {
            throw new OXConflictException(ContactExceptionCodes.IMAGE_BROKEN.create());
        }
        PreparedStatement ps = null;
        try {
            final ContactSql cs = new ContactMySql(null);
            ps = writecon.prepareStatement(cs.iFwriteContactImage());
            ps.setInt(1, contact_id);
            ps.setBytes(2, img);
            ps.setString(3, mime);
            ps.setInt(4, cid);
            if (DEBUG) {
                LOG.debug(new StringBuilder("INSERT IMAGE ").append(ps.toString()));
            }
            ps.execute();
        } catch (final ContextException d) {
            throw new ContactException(d);
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            closeSQLStuff(ps);
        }
    }

    public static void updateContactImage(final int contact_id, final byte[] img, final int cid, final String mime, final Connection writecon) throws OXConflictException, ContactException {
        if ((contact_id < 1) || (img == null) || (img.length < 1) || (cid < 1) || (mime == null) || (mime.length() < 1)) {
            throw new OXConflictException(ContactExceptionCodes.IMAGE_BROKEN.create());
        }
        PreparedStatement ps = null;
        try {
            final ContactSql cs = new ContactMySql(null);
            ps = writecon.prepareStatement(cs.iFupdateContactImageString());
            ps.setInt(1, contact_id);
            ps.setBytes(2, img);
            ps.setString(3, mime);
            ps.setInt(4, cid);
            ps.setInt(5, contact_id);
            ps.setInt(6, cid);
            if (DEBUG) {
                LOG.debug(new StringBuilder("UPDATE IMAGE ").append(ps.toString()));
            }
            ps.execute();
        } catch (final ContextException d) {
            throw new ContactException(d);
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            closeSQLStuff(ps);
        }
    }

    public static boolean performContactReadCheckByID(final int objectId, final int user, final Context ctx, final UserConfiguration uc) throws DBPoolingException {

        Connection readCon = null;
        ResultSet rs = null;
        Statement stmt = null;
        try {

            final ContactSql cs = new ContactMySql(ctx, user);
            cs.setSelect(cs.iFgetRightsSelectString());
            cs.setObjectID(objectId);

            readCon = DBPool.pickup(ctx);
            stmt = cs.getSqlStatement(readCon);
            rs = ((PreparedStatement) stmt).executeQuery();

            int fid = -1;
            int created_from = -1;
            boolean pflag = false;

            if (rs.next()) {
                fid = rs.getInt(5);
                created_from = rs.getInt(6);
                final int xx = rs.getInt(7);
                if (!rs.wasNull() && (xx == 1)) {
                    pflag = true;
                }
            } else {
                return false;
            }
            if (pflag && (created_from != user)) {
                return false;
            }
            if ((fid != -1) && (created_from != -1)) {
                return performContactReadCheck(fid, created_from, user, ctx, uc, readCon);
            }
            return false;
        } catch (final SQLException e) {
            LOG.error("UNABLE TO performContactReadCheckByID cid=" + ctx.getContextId() + " oid=" + objectId, e);
            return false;
        } finally {
            closeSQLStuff(rs, stmt);
            try {
                if (readCon != null) {
                    DBPool.closeReaderSilent(ctx, readCon);
                }
            } catch (final Exception see) {
                LOG.error("Unable to return Connection", see);
            }
        }
    }

    public static boolean performContactReadCheckByID(final int folderId, final int objectId, final int user, final Context ctx, final UserConfiguration uc) throws ContactException {
        if (ServerServiceRegistry.getInstance().getService(ContactInterfaceDiscoveryService.class).hasSpecificContactInterface(
            folderId,
            ctx.getContextId())) {
            return true;
        }

        Connection readCon = null;
        ResultSet rs = null;
        Statement stmt = null;
        try {

            final ContactSql cs = new ContactMySql(ctx, user);
            cs.setSelect(cs.iFgetRightsSelectString());
            cs.setObjectID(objectId);

            readCon = DBPool.pickup(ctx);
            stmt = cs.getSqlStatement(readCon);
            rs = ((PreparedStatement) stmt).executeQuery();

            int fid = -1;
            int created_from = -1;
            boolean pflag = false;

            if (rs.next()) {
                fid = rs.getInt(5);
                created_from = rs.getInt(6);
                final int xx = rs.getInt(7);
                if (!rs.wasNull() && (xx == 1)) {
                    pflag = true;
                }
            } else {
                return false;
            }
            if (pflag && (created_from != user)) {
                return false;
            }
            if (fid != folderId) {
                return false;
            }
            if ((fid != -1) && (created_from != -1)) {
                return performContactReadCheck(fid, created_from, user, ctx, uc, readCon);
            }
            return false;
        } catch (final DBPoolingException e) {
            throw ContactExceptionCodes.INIT_CONNECTION_FROM_DBPOOL.create(e);
        } catch (final SQLException e) {
            LOG.error("UNABLE TO performContactReadCheckByID cid=" + ctx.getContextId() + " oid=" + objectId, e);
            return false;
        } finally {
            closeSQLStuff(rs, stmt);
            try {
                if (readCon != null) {
                    DBPool.closeReaderSilent(ctx, readCon);
                }
            } catch (final Exception see) {
                LOG.error("Unable to return Connection", see);
            }
        }
    }

    public static boolean performContactReadCheck(final int folderId, final int created_from, final int user, final Context ctx, final UserConfiguration uc, final Connection readCon) {

        try {
            final FolderObject contactFolder = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
            if (contactFolder.getModule() != FolderObject.CONTACT) {
                return false;
            }
            final OXFolderAccess oxfs = new OXFolderAccess(readCon, ctx);
            final EffectivePermission oclPerm = oxfs.getFolderPermission(folderId, user, uc);

            if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
                return false;
            }
            if (!oclPerm.canReadAllObjects()) {
                if (oclPerm.canReadOwnObjects() && (created_from == user)) {
                    return true;
                }
                return false;
            }
            return true;
        } catch (final OXException e) {
            LOG.error("UNABLE TO PERFORM performContactReadCheck cid=" + ctx.getContextId() + " fid=" + folderId, e);
            return false;
        }
    }

    public static boolean performContactReadCheck(final FolderObject folder, final int user, final int createdFrom, final UserConfiguration uc, final Connection readCon) {
        try {
            if (folder.getModule() != FolderObject.CONTACT) {
                return false;
            }
            final EffectivePermission oclPerm = folder.getEffectiveUserPermission(user, uc, readCon);

            if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
                return false;
            }
            if (!oclPerm.canReadAllObjects()) {
                if (oclPerm.canReadOwnObjects() && (createdFrom == user)) {
                    return true;
                }
                return false;
            }
            return true;
        } catch (final DBPoolingException e) {
            final ContactException e1 = ContactExceptionCodes.INIT_CONNECTION_FROM_DBPOOL.create(e);
            LOG.error(e1.getMessage(), e1);
            return false;
        } catch (final SQLException e) {
            final ContactException e1 = ContactExceptionCodes.SQL_PROBLEM.create(e);
            LOG.error(e1.getMessage(), e1);
            return false;
        }
    }

    public static boolean performContactWriteCheckByID(final int folderId, final int objectId, final int user, final Context ctx, final UserConfiguration uc) throws ContactException {

        Connection readCon = null;
        ResultSet rs = null;
        Statement stmt = null;
        try {

            final ContactSql cs = new ContactMySql(ctx, user);
            cs.setSelect(cs.iFgetRightsSelectString());
            cs.setObjectID(objectId);

            readCon = DBPool.pickup(ctx);
            stmt = cs.getSqlStatement(readCon);
            rs = ((PreparedStatement) stmt).executeQuery();

            int fid = -1;
            int created_from = -1;
            boolean pflag = false;

            if (rs.next()) {
                fid = rs.getInt(5);
                created_from = rs.getInt(6);
                final int xx = rs.getInt(7);
                if (!rs.wasNull() && (xx == 1)) {
                    pflag = true;
                }
            } else {
                return false;
            }
            if (pflag && (created_from != user)) {
                return false;
            }
            if (fid != folderId) {
                return false;
            }

            if ((fid != -1) && (created_from != -1)) {
                return performContactWriteCheck(fid, created_from, user, ctx, uc, readCon);
            }
            return false;
        } catch (final DBPoolingException e) {
            throw ContactExceptionCodes.INIT_CONNECTION_FROM_DBPOOL.create(e);
        } catch (final SQLException e) {
            LOG.error("UNABLE TO performContactWriteCheckByID cid=" + ctx.getContextId() + " oid=" + objectId, e);
            return false;
        } finally {
            closeSQLStuff(rs, stmt);
            try {
                if (readCon != null) {
                    DBPool.closeReaderSilent(ctx, readCon);
                }
            } catch (final Exception see) {
                LOG.error("Unable to return Connection", see);
            }
        }
    }

    public static boolean performContactWriteCheck(final int folderId, final int created_from, final int user, final Context ctx, final UserConfiguration uc, final Connection readCon) {
        try {
            final FolderObject contactFolder = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
            if (contactFolder.getModule() != FolderObject.CONTACT) {
                return false;
            }
            final OXFolderAccess oxfs = new OXFolderAccess(readCon, ctx);
            final EffectivePermission oclPerm = oxfs.getFolderPermission(folderId, user, uc);
            if (oclPerm.getFolderPermission() <= OCLPermission.NO_PERMISSIONS) {
                return false;
            }
            if (!oclPerm.canWriteAllObjects()) {
                if (oclPerm.canWriteOwnObjects() && (created_from == user)) {
                    return true;
                }
                return false;
            }
            return true;
        } catch (final OXException e) {
            LOG.error("UNABLE TO PERFORM performContactWriteCheck cid=" + ctx.getContextId() + " fid=" + folderId, e);
            return false;
        }
    }

    public static boolean containsForeignObjectInFolder(final int fid, final int uid, final Session so) throws ContactException, DBPoolingException {
        Connection readCon = null;
        Context ct = null;
        try {
            ct = ContextStorage.getStorageContext(so.getContextId());
            readCon = DBPool.pickup(ct);
            return containsForeignObjectInFolder(fid, uid, so, readCon);
        } catch (final ContextException d) {
            throw new ContactException(d);
        } finally {
            DBPool.closeReaderSilent(ct, readCon);
        }
    }

    public static boolean containsForeignObjectInFolder(final int fid, final int uid, final Session so, final Connection readCon) throws ContactException {
        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = readCon.createStatement();
            final ContactSql cs = new ContactMySql(null);
            rs = stmt.executeQuery(cs.iFcontainsForeignObjectInFolder(fid, uid, so.getContextId()));
            if (rs.next()) {
                return true;
            }
            return false;
        } catch (final ContextException d) {
            throw new ContactException(d);
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    public static boolean containsAnyObjectInFolder(final int fid, final Context cx) throws ContactException {
        Connection readCon = null;
        ResultSet rs = null;
        Statement st = null;
        try {
            readCon = DBPool.pickup(cx);
            st = readCon.createStatement();
            final ContactSql cs = new ContactMySql(null);
            rs = st.executeQuery(cs.iFgetFolderSelectString(fid, cx.getContextId()));
            return (rs.next());
        } catch (final ContextException d) {
            throw new ContactException(d);
        } catch (final DBPoolingException se) {
            LOG.error("Unable to perform containsAnyObjectInFolder check. Cid: " + cx.getContextId() + " Fid: " + fid + " Cause:" + se);
            return false;
        } catch (final SQLException se) {
            LOG.error("Unable to perform containsAnyObjectInFolder check. Cid: " + cx.getContextId() + " Fid: " + fid + " Cause:" + se);
            return false;
        } finally {
            closeResources(rs, st, readCon, true, cx);
        }
    }

    public static boolean containsAnyObjectInFolder(final int fid, final Connection readCon, final Context cx) throws ContactException {
        ResultSet rs = null;
        Statement st = null;
        try {
            st = readCon.createStatement();
            final ContactSql cs = new ContactMySql(null);
            rs = st.executeQuery(cs.iFgetFolderSelectString(fid, cx.getContextId()));
            return (rs.next());
        } catch (final ContextException d) {
            throw new ContactException(d);
        } catch (final SQLException se) {
            LOG.error("Unable to perform containsAnyObjectInFolder check. Cid: " + cx.getContextId() + " Fid: " + fid + " Cause:" + se);
            return false;
        } finally {
            closeSQLStuff(rs, st);
        }
    }

    public static void deleteContactsFromFolder(final int fid, final Session so, final Connection readcon, final Connection writecon) throws ContactException {
        trashContactsFromFolder(fid, so, readcon, writecon, true);
    }

    public static void trashContactsFromFolder(final int fid, final Session so, final Connection readcon, final Connection writecon, final boolean delit) throws ContactException {

        Statement read = null;
        Statement del = null;
        ResultSet rs = null;
        boolean deleteIt = delit;

        try {
            del = writecon.createStatement();

            try {
                final Context ct = ContextStorage.getStorageContext(so.getContextId());
                final FolderObject contactFolder = new OXFolderAccess(readcon, ct).getFolderObject(fid);
                if (contactFolder.getModule() != FolderObject.CONTACT) {
                    throw new OXConflictException(ContactExceptionCodes.NON_CONTACT_FOLDER.create(
                        I(fid),
                        I(so.getContextId()),
                        I(so.getUserId())));
                }
                if (contactFolder.getType() == FolderObject.PRIVATE) {
                    deleteIt = true;
                }
            } catch (final ContextException d) {
                throw new ContactException(d);
            } catch (final OXException e) {
                throw new ContactException(e);
            }

            final ContactSql cs = new ContactMySql(so);
            cs.setFolder(fid);
            cs.setSelect(cs.iFgetRightsSelectString());

            read = cs.getSqlStatement(readcon);
            rs = ((PreparedStatement) read).executeQuery();

            final EventClient ec = new EventClient(so);

            int oid = 0;
            int dlist = 0;
            int link = 0;
            int image = 0;
            int created_from = 0;

            while (rs.next()) {

                oid = rs.getInt(1);
                dlist = rs.getInt(2);
                if (!rs.wasNull() && (dlist > 0)) {
                    trashDistributionList(oid, so.getContextId(), writecon, deleteIt);
                }
                link = rs.getInt(3);
                if (!rs.wasNull() && (link > 0)) {
                    trashLinks(oid, so.getContextId(), writecon, deleteIt);
                }
                image = rs.getInt(4);
                if (!rs.wasNull() && (image > 0)) {
                    trashImage(oid, so.getContextId(), writecon, deleteIt);
                }
                created_from = rs.getInt(6);

                cs.iFtrashContactsFromFolder(deleteIt, del, oid, so.getContextId());

                final Contact co = new Contact();
                co.setCreatedBy(created_from);
                co.setParentFolderID(fid);
                co.setObjectID(oid);

                ec.delete(co);
            }

            if (DEBUG) {
                LOG.debug(cs.iFtrashContactsFromFolderUpdateString(fid, so.getContextId()));
            }
            del.execute(cs.iFtrashContactsFromFolderUpdateString(fid, so.getContextId()));
        } catch (final ContextException d) {
            throw new ContactException(d);
        } catch (final EventException e) {
            throw ContactExceptionCodes.TRIGGERING_EVENT_FAILED.create(e, I(so.getContextId()), I(fid));
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } catch (final OXException e) {
            throw new ContactException(e);
        } finally {
            closeSQLStuff(rs, read);
            closeSQLStuff(del);
        }
    }

    public static void deleteDistributionList(final int id, final int cid, final Connection writecon) throws SQLException, ContactException {
        trashDistributionList(id, cid, writecon, true);
    }

    public static void trashDistributionList(final int id, final int cid, final Connection writecon, final boolean delete) throws SQLException, ContactException {
        final Statement stmt = writecon.createStatement();
        try {
            final ContactSql cs = new ContactMySql(null);
            cs.iFtrashDistributionList(delete, id, cid, stmt);
        } catch (final ContextException d) {
            throw new ContactException(d);
        } catch (final SQLException sxe) {
            throw sxe;
        } finally {
            closeSQLStuff(stmt);
        }
    }

    public static void deleteLinks(final int id, final int cid, final Connection writecon) throws SQLException, ContactException {
        trashLinks(id, cid, writecon, true);
    }

    public static void trashLinks(final int id, final int cid, final Connection writecon, final boolean delete) throws SQLException, ContactException {
        final Statement stmt = writecon.createStatement();
        try {
            final ContactSql cs = new ContactMySql(null);
            cs.iFtrashLinks(delete, stmt, id, cid);
        } catch (final ContextException d) {
            throw new ContactException(d);
        } catch (final SQLException sxe) {
            throw sxe;
        } finally {
            closeSQLStuff(stmt);
        }
    }

    public static void deleteImage(final int id, final int cid, final Connection writecon) throws SQLException, ContactException {
        trashImage(id, cid, writecon, true);
    }

    public static void trashImage(final int id, final int cid, final Connection writecon, final boolean delete) throws SQLException, ContactException {
        final Statement stmt = writecon.createStatement();
        try {
            final ContactSql cs = new ContactMySql(null);
            cs.iFtrashImage(delete, stmt, id, cid);
        } catch (final ContextException d) {
            throw new ContactException(d);
        } catch (final SQLException sxe) {
            throw sxe;
        } finally {
            closeSQLStuff(stmt);
        }
    }

    public static void trashAllUserContacts(final int uid, final Session so, final Connection readcon, final Connection writecon) throws ContactException {
        Statement stmt = null;
        Statement del = null;
        ResultSet rs = null;

        try {
            final Context ct = ContextStorage.getStorageContext(so.getContextId());
            final ContactSql cs = new ContactMySql(ct, uid);

            stmt = readcon.createStatement();
            del = writecon.createStatement();

            FolderObject contactFolder = null;

            /*
             * Get all contacts which were created by specified user. This includes the user's contact as well since the user is always the creator.
             */
            rs = stmt.executeQuery(cs.iFgetRightsSelectString(uid, so.getContextId()));

            int fid = 0;
            int oid = 0;
            int created_from = 0;
            boolean delete = false;
            int pflag = 0;

            final EventClient ec = new EventClient(so);
            OXFolderAccess oxfs = null;

            while (rs.next()) {
                delete = false;
                oid = rs.getInt(1);
                fid = rs.getInt(5);
                created_from = rs.getInt(6);
                pflag = rs.getInt(7);
                if (rs.wasNull()) {
                    pflag = 0;
                }

                boolean folder_error = false;

                try {
                    if (FolderCacheManager.isEnabled()) {
                        contactFolder = FolderCacheManager.getInstance().getFolderObject(fid, true, ct, readcon);
                    } else {
                        contactFolder = FolderObject.loadFolderObjectFromDB(fid, ct, readcon);
                    }
                    if (contactFolder.getModule() != FolderObject.CONTACT) {
                        throw ContactExceptionCodes.NON_CONTACT_FOLDER.create(I(fid), I(so.getContextId()), I(uid));
                    }
                    if (contactFolder.getType() == FolderObject.PRIVATE) {
                        delete = true;
                    }

                } catch (final Exception oe) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("WARNING: During the delete process 'delete all contacts from one user', a contact was found who has no folder." + "This contact will be modified and can be found in the administrator address book." + "Context " + so.getContextId() + " Folder " + fid + " User" + uid + " Contact" + oid);
                    }
                    folder_error = true;
                    delete = true;
                }

                if (folder_error && (pflag == 0)) {
                    try {
                        final int mailadmin = ct.getMailadmin();
                        if (null == oxfs) {
                            oxfs = new OXFolderAccess(readcon, ct);
                        }
                        final FolderObject xx = oxfs.getDefaultFolder(mailadmin, FolderObject.CONTACT);

                        final int admin_folder = xx.getObjectID();
                        cs.iFgiveUserContacToAdmin(del, oid, admin_folder, ct);
                    } catch (final Exception oxee) {
                        oxee.printStackTrace();
                        LOG.error("ERROR: It was not possible to move this contact (without paren folder) to the admin address book!." + "This contact will be deleted." + "Context " + so.getContextId() + " Folder " + fid + " User" + uid + " Contact" + oid);

                        folder_error = false;
                    }
                } else if (folder_error && (pflag != 0)) {
                    folder_error = false;
                }

                if (!folder_error) {
                    cs.iFtrashAllUserContacts(delete, del, so.getContextId(), oid, uid, rs, so);
                    final Contact co = new Contact();
                    try {
                        co.setCreatedBy(created_from);
                        co.setParentFolderID(fid);
                        co.setObjectID(oid);
                        ec.delete(co);
                    } catch (final Exception e) {
                        LOG.error(
                            "Unable to trigger delete event for contact delete: id=" + co.getObjectID() + " cid=" + co.getContextId(),
                            e);
                    }
                }
            }
            if (uid == ct.getMailadmin()) {
                cs.iFtrashAllUserContactsDeletedEntriesFromAdmin(del, so.getContextId(), uid);
            } else {
                cs.iFtrashAllUserContactsDeletedEntries(del, so.getContextId(), uid, ct);
            }
        } catch (final ContextException d) {
            throw new ContactException(d);
        } catch (final SQLException e) {
            throw ContactExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            closeSQLStuff(rs, stmt);
            closeSQLStuff(del);
        }
    }

    public static ContactException getTruncation(final Connection con, final DataTruncation se, final String table, final Contact co) {

        final String[] fields = DBUtils.parseTruncatedFields(se);
        final StringBuilder sFields = new StringBuilder();

        for (final String field : fields) {
            sFields.append(field);
            sFields.append(", ");
        }
        sFields.setLength(sFields.length() - 2);
        final OXException.Truncated[] truncateds = new OXException.Truncated[fields.length];
        for (int i = 0; i < fields.length; i++) {
            for (int j = 0; j < 650; j++) {
                if ((mapping[j] != null) && mapping[j].getDBFieldName().equals(fields[i])) {
                    int tmp = 0;
                    try {
                        tmp = DBUtils.getColumnSize(con, table, fields[i]);
                    } catch (final SQLException e) {
                        LOG.error(e.getMessage(), e);
                        tmp = 0;
                    }
                    final int maxSize = tmp;
                    final int attributeId = j;
                    truncateds[i] = new OXException.Truncated() {

                        public int getId() {
                            return attributeId;
                        }

                        public int getLength() {
                            return Charsets.getBytes(mapping[attributeId].getValueAsString(co), Charsets.UTF_8).length;
                        }

                        public int getMaxSize() {
                            return maxSize;
                        }
                    };
                }
            }
        }
        final ContactException retval;
        if (truncateds.length > 0) {
            retval = ContactExceptionCodes.DATA_TRUNCATION.create(
                se,
                sFields.toString(),
                I(truncateds[0].getMaxSize()),
                I(truncateds[0].getLength()));
        } else {
            retval = ContactExceptionCodes.DATA_TRUNCATION.create(se, sFields.toString(), I(-1), I(-1));
        }
        for (final OXException.Truncated truncated : truncateds) {
            retval.addProblematic(truncated);
        }
        return retval;
    }

    private static void checkCharacters(final Contact co) throws ContactException {
        for (int i = 0; i < 650; i++) {
            if ((mapping[i] != null) && (i != Contact.IMAGE1)) {
                String error = null;
                try {
                    error = Check.containsInvalidChars(mapping[i].getValueAsString(co));
                } catch (final NullPointerException npe) {
                    LOG.error("Null pointer detected", npe);
                }
                if (error != null) {
                    throw ContactExceptionCodes.BAD_CHARACTER.create(error, mapping[i].getReadableTitle());
                }
            }
        }

    }

    /**
     * Checks if specified strings are equal
     * 
     * @param string The first string
     * @param other The second string
     * @return <code>true</code> if both strings are considered equal; otherwise <code>false</code>
     */
    protected static boolean areEqual(final String string, final String other) {
        return null == string ? null == other : null == other ? false : string.equals(other);
    }

    public static interface Mapper {

        boolean containsElement(Contact co);

        void addToContactObject(ResultSet rs, int pos, Contact co, Connection readcon, int user, int[] group, Context ctx, UserConfiguration uc) throws SQLException;

        String getDBFieldName();

        void fillPreparedStatement(PreparedStatement ps, int position, Contact co) throws SQLException;

        boolean compare(Contact co, Contact original);

        String getValueAsString(Contact co);

        String getReadableTitle();
    }

    static {
        mapping = new Mapper[700];

        /** ************** * field01 * * ************ */
        mapping[Contact.DISPLAY_NAME] = new Mapper() {

            public String getDBFieldName() {
                return "field01";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setDisplayName(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsDisplayName();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getDisplayName());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getDisplayName();
                final String y = original.getDisplayName();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getDisplayName();
            }

            public String getReadableTitle() {
                return "Display name";
            }
        };
        /** ************** * field02 * * ************ */
        mapping[Contact.SUR_NAME] = new Mapper() {

            public String getDBFieldName() {
                return "field02";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setSurName(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsSurName();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getSurName());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getSurName();
                final String y = original.getSurName();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getSurName();
            }

            public String getReadableTitle() {
                return "Sur name";
            }
        };
        /** ************** * field03 * * ************ */
        mapping[Contact.GIVEN_NAME] = new Mapper() {

            public String getDBFieldName() {
                return "field03";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setGivenName(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsGivenName();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getGivenName());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getGivenName();
                final String y = original.getGivenName();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getGivenName();
            }

            public String getReadableTitle() {
                return "Given name";
            }
        };
        /** ************** * field04 * * ************ */
        mapping[Contact.MIDDLE_NAME] = new Mapper() {

            public String getDBFieldName() {
                return "field04";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setMiddleName(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsMiddleName();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getMiddleName());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getMiddleName();
                final String y = original.getMiddleName();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {

                return co.getMiddleName();
            }

            public String getReadableTitle() {

                return "Middle name";
            }
        };
        /** ************** * field05 * * ************ */
        mapping[Contact.SUFFIX] = new Mapper() {

            public String getDBFieldName() {
                return "field05";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setSuffix(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsSuffix();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getSuffix());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getSuffix();
                final String y = original.getSuffix();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getSuffix();
            }

            public String getReadableTitle() {
                return "Suffix";
            }
        };
        /** ************** * field06 * * ************ */
        mapping[Contact.TITLE] = new Mapper() {

            public String getDBFieldName() {
                return "field06";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setTitle(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsTitle();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getTitle());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getTitle();
                final String y = original.getTitle();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getTitle();
            }

            public String getReadableTitle() {
                return "Title";
            }
        };
        /** ************** * field07 * * ************ */
        mapping[Contact.STREET_HOME] = new Mapper() {

            public String getDBFieldName() {
                return "field07";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setStreetHome(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsStreetHome();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getStreetHome());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getStreetHome();
                final String y = original.getStreetHome();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getStreetHome();
            }

            public String getReadableTitle() {
                return "Street home";
            }
        };
        /** ************** * field08 * * ************ */
        mapping[Contact.POSTAL_CODE_HOME] = new Mapper() {

            public String getDBFieldName() {
                return "field08";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setPostalCodeHome(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsPostalCodeHome();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getPostalCodeHome());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getPostalCodeHome();
                final String y = original.getPostalCodeHome();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getPostalCodeHome();
            }

            public String getReadableTitle() {
                return "Postal code home";
            }
        };
        /** ************** * field09 * * ************ */
        mapping[Contact.CITY_HOME] = new Mapper() {

            public String getDBFieldName() {
                return "field09";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setCityHome(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsCityHome();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getCityHome());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getCityHome();
                final String y = original.getCityHome();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getCityHome();
            }

            public String getReadableTitle() {
                return "City home";
            }
        };
        /** ************** * field10 * * ************ */
        mapping[Contact.STATE_HOME] = new Mapper() {

            public String getDBFieldName() {
                return "field10";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setStateHome(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsStateHome();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getStateHome());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getStateHome();
                final String y = original.getStateHome();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getStateHome();
            }

            public String getReadableTitle() {
                return "State home";
            }
        };
        /** ************** * field11 * * ************ */
        mapping[Contact.COUNTRY_HOME] = new Mapper() {

            public String getDBFieldName() {
                return "field11";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setCountryHome(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsCountryHome();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getCountryHome());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getCountryHome();
                final String y = original.getCountryHome();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getCountryHome();
            }

            public String getReadableTitle() {
                return "Country home";
            }
        };

        /** ************** * field12 * * ************ */
        mapping[Contact.MARITAL_STATUS] = new Mapper() {

            public String getDBFieldName() {
                return "field12";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setMaritalStatus(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsMaritalStatus();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getMaritalStatus());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getMaritalStatus();
                final String y = original.getMaritalStatus();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getMaritalStatus();
            }

            public String getReadableTitle() {
                return "Martial status";
            }
        };
        /** ************** * field13 * * ************ */
        mapping[Contact.NUMBER_OF_CHILDREN] = new Mapper() {

            public String getDBFieldName() {
                return "field13";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setNumberOfChildren(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsNumberOfChildren();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getNumberOfChildren());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getNumberOfChildren();
                final String y = original.getNumberOfChildren();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getNumberOfChildren();
            }

            public String getReadableTitle() {
                return "Number of children";
            }
        };
        /** ************** * field14 * * ************ */
        mapping[Contact.PROFESSION] = new Mapper() {

            public String getDBFieldName() {
                return "field14";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setProfession(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsProfession();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getProfession());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getProfession();
                final String y = original.getProfession();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getProfession();
            }

            public String getReadableTitle() {
                return "Profession";
            }
        };
        /** ************** * field15 * * ************ */
        mapping[Contact.NICKNAME] = new Mapper() {

            public String getDBFieldName() {
                return "field15";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setNickname(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsNickname();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getNickname());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getNickname();
                final String y = original.getNickname();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getNickname();
            }

            public String getReadableTitle() {
                return "Nickname";
            }
        };
        /** ************** * field16 * * ************ */
        mapping[Contact.SPOUSE_NAME] = new Mapper() {

            public String getDBFieldName() {
                return "field16";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setSpouseName(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsSpouseName();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getSpouseName());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getSpouseName();
                final String y = original.getSpouseName();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getSpouseName();
            }

            public String getReadableTitle() {
                return "Spouse name";
            }
        };
        /** ************** * field17 * * ************ */
        mapping[Contact.NOTE] = new Mapper() {

            public String getDBFieldName() {
                return "field17";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setNote(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsNote();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getNote());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getNote();
                final String y = original.getNote();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getNote();
            }

            public String getReadableTitle() {
                return "Note";
            }
        };
        /** ************** * field18 * * ************ */
        mapping[Contact.COMPANY] = new Mapper() {

            public String getDBFieldName() {
                return "field18";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setCompany(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsCompany();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getCompany());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getCompany();
                final String y = original.getCompany();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getCompany();
            }

            public String getReadableTitle() {
                return "Company";
            }
        };
        /** ************** * field19 * * ************ */
        mapping[Contact.DEPARTMENT] = new Mapper() {

            public String getDBFieldName() {
                return "field19";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setDepartment(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsDepartment();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getDepartment());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getDepartment();
                final String y = original.getDepartment();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getDepartment();
            }

            public String getReadableTitle() {
                return "Department";
            }
        };
        /** ************** * field20 * * ************ */
        mapping[Contact.POSITION] = new Mapper() {

            public String getDBFieldName() {
                return "field20";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setPosition(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsPosition();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getPosition());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getPosition();
                final String y = original.getPosition();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getPosition();
            }

            public String getReadableTitle() {
                return "Position";
            }
        };
        /** ************** * field21 * * ************ */
        mapping[Contact.EMPLOYEE_TYPE] = new Mapper() {

            public String getDBFieldName() {
                return "field21";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setEmployeeType(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsEmployeeType();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getEmployeeType());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getEmployeeType();
                final String y = original.getEmployeeType();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getEmployeeType();
            }

            public String getReadableTitle() {
                return "Employee type";
            }
        };
        /** ************** * field22 * * ************ */
        mapping[Contact.ROOM_NUMBER] = new Mapper() {

            public String getDBFieldName() {
                return "field22";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setRoomNumber(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsRoomNumber();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getRoomNumber());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getRoomNumber();
                final String y = original.getRoomNumber();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getRoomNumber();
            }

            public String getReadableTitle() {
                return "Room number";
            }
        };
        /** ************** * field23 * * ************ */
        mapping[Contact.STREET_BUSINESS] = new Mapper() {

            public String getDBFieldName() {
                return "field23";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setStreetBusiness(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsStreetBusiness();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getStreetBusiness());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getStreetBusiness();
                final String y = original.getStreetBusiness();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getStreetBusiness();
            }

            public String getReadableTitle() {
                return "Street business";
            }
        };
        /** ************** * field24 * * ************ */
        mapping[Contact.POSTAL_CODE_BUSINESS] = new Mapper() {

            public String getDBFieldName() {
                return "field24";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setPostalCodeBusiness(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsPostalCodeBusiness();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getPostalCodeBusiness());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getPostalCodeBusiness();
                final String y = original.getPostalCodeBusiness();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getPostalCodeBusiness();
            }

            public String getReadableTitle() {
                return "Postal code business";
            }
        };
        /** ************** * field25 * * ************ */
        mapping[Contact.CITY_BUSINESS] = new Mapper() {

            public String getDBFieldName() {
                return "field25";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setCityBusiness(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsCityBusiness();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getCityBusiness());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getCityBusiness();
                final String y = original.getCityBusiness();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getCityBusiness();
            }

            public String getReadableTitle() {
                return "City business";
            }
        };
        /** ************** * field26 * * ************ */
        mapping[Contact.STATE_BUSINESS] = new Mapper() {

            public String getDBFieldName() {
                return "field26";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setStateBusiness(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsStateBusiness();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getStateBusiness());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getStateBusiness();
                final String y = original.getStateBusiness();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getStateBusiness();
            }

            public String getReadableTitle() {
                return "State business";
            }
        };
        /** ************** * field27 * * ************ */
        mapping[Contact.COUNTRY_BUSINESS] = new Mapper() {

            public String getDBFieldName() {
                return "field27";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setCountryBusiness(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsCountryBusiness();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getCountryBusiness());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getCountryBusiness();
                final String y = original.getCountryBusiness();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getCountryBusiness();
            }

            public String getReadableTitle() {
                return "Country business";
            }
        };
        /** ************** * field28 * * ************ */
        mapping[Contact.NUMBER_OF_EMPLOYEE] = new Mapper() {

            public String getDBFieldName() {
                return "field28";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setNumberOfEmployee(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsNumberOfEmployee();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getNumberOfEmployee());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getNumberOfEmployee();
                final String y = original.getNumberOfEmployee();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getNumberOfEmployee();
            }

            public String getReadableTitle() {
                return "Number of employee";
            }
        };
        /** ************** * field29 * * ************ */
        mapping[Contact.SALES_VOLUME] = new Mapper() {

            public String getDBFieldName() {
                return "field29";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setSalesVolume(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsSalesVolume();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getSalesVolume());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getSalesVolume();
                final String y = original.getSalesVolume();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getSalesVolume();
            }

            public String getReadableTitle() {
                return "Sales volume";
            }
        };
        /** ************** * field30 * * ************ */
        mapping[Contact.TAX_ID] = new Mapper() {

            public String getDBFieldName() {
                return "field30";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setTaxID(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsTaxID();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getTaxID());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getTaxID();
                final String y = original.getTaxID();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getTaxID();
            }

            public String getReadableTitle() {
                return "Tax id";
            }
        };
        /** ************** * field31 * * ************ */
        mapping[Contact.COMMERCIAL_REGISTER] = new Mapper() {

            public String getDBFieldName() {
                return "field31";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setCommercialRegister(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsCommercialRegister();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getCommercialRegister());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getCommercialRegister();
                final String y = original.getCommercialRegister();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getCommercialRegister();
            }

            public String getReadableTitle() {
                return "Commercial register";
            }
        };
        /** ************** * field32 * * ************ */
        mapping[Contact.BRANCHES] = new Mapper() {

            public String getDBFieldName() {
                return "field32";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setBranches(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsBranches();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getBranches());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getBranches();
                final String y = original.getBranches();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getBranches();
            }

            public String getReadableTitle() {
                return "Branches";
            }
        };
        /** ************** * field33 * * ************ */
        mapping[Contact.BUSINESS_CATEGORY] = new Mapper() {

            public String getDBFieldName() {
                return "field33";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setBusinessCategory(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsBusinessCategory();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getBusinessCategory());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getBusinessCategory();
                final String y = original.getBusinessCategory();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getBusinessCategory();
            }

            public String getReadableTitle() {
                return "Business category";
            }
        };
        /** ************** * field34 * * ************ */
        mapping[Contact.INFO] = new Mapper() {

            public String getDBFieldName() {
                return "field34";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setInfo(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsInfo();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getInfo());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getInfo();
                final String y = original.getInfo();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getInfo();
            }

            public String getReadableTitle() {
                return "Info";
            }
        };
        /** ************** * field35 * * ************ */
        mapping[Contact.MANAGER_NAME] = new Mapper() {

            public String getDBFieldName() {
                return "field35";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setManagerName(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsManagerName();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getManagerName());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getManagerName();
                final String y = original.getManagerName();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getManagerName();
            }

            public String getReadableTitle() {
                return "Manager's name";
            }
        };
        /** ************** * field36 * * ************ */
        mapping[Contact.ASSISTANT_NAME] = new Mapper() {

            public String getDBFieldName() {
                return "field36";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setAssistantName(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsAssistantName();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getAssistantName());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getAssistantName();
                final String y = original.getAssistantName();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getAssistantName();
            }

            public String getReadableTitle() {
                return "Assistant's name";
            }
        };
        /** ************** * field37 * * ************ */
        mapping[Contact.STREET_OTHER] = new Mapper() {

            public String getDBFieldName() {
                return "field37";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setStreetOther(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsStreetOther();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getStreetOther());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getStreetOther();
                final String y = original.getStreetOther();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getStreetOther();
            }

            public String getReadableTitle() {
                return "Street other";
            }
        };
        /** ************** * field38 * * ************ */
        mapping[Contact.POSTAL_CODE_OTHER] = new Mapper() {

            public String getDBFieldName() {
                return "field38";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setPostalCodeOther(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsPostalCodeOther();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getPostalCodeOther());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getPostalCodeOther();
                final String y = original.getPostalCodeOther();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getPostalCodeOther();
            }

            public String getReadableTitle() {
                return "Postal code other";
            }
        };
        /** ************** * field39 * * ************ */
        mapping[Contact.CITY_OTHER] = new Mapper() {

            public String getDBFieldName() {
                return "field39";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setCityOther(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsCityOther();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getCityOther());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getCityOther();
                final String y = original.getCityOther();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getCityOther();
            }

            public String getReadableTitle() {
                return "City other";
            }
        };
        /** ************** * field40 * * ************ */
        mapping[Contact.STATE_OTHER] = new Mapper() {

            public String getDBFieldName() {
                return "field40";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setStateOther(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsStateOther();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getStateOther());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getStateOther();
                final String y = original.getStateOther();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getStateOther();
            }

            public String getReadableTitle() {
                return "State other";
            }
        };
        /** ************** * field41 * * ************ */
        mapping[Contact.COUNTRY_OTHER] = new Mapper() {

            public String getDBFieldName() {
                return "field41";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setCountryOther(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsCountryOther();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getCountryOther());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getCountryOther();
                final String y = original.getCountryOther();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getCountryOther();
            }

            public String getReadableTitle() {
                return "Country other";
            }
        };
        /** ************** * field42 * * ************ */
        mapping[Contact.TELEPHONE_ASSISTANT] = new Mapper() {

            public String getDBFieldName() {
                return "field42";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setTelephoneAssistant(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsTelephoneAssistant();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getTelephoneAssistant());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getTelephoneAssistant();
                final String y = original.getTelephoneAssistant();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getTelephoneAssistant();
            }

            public String getReadableTitle() {
                return "Telephone assistant";
            }
        };
        /** ************** * field43 * * ************ */
        mapping[Contact.TELEPHONE_BUSINESS1] = new Mapper() {

            public String getDBFieldName() {
                return "field43";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setTelephoneBusiness1(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsTelephoneBusiness1();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getTelephoneBusiness1());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getTelephoneBusiness1();
                final String y = original.getTelephoneBusiness1();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getTelephoneBusiness1();
            }

            public String getReadableTitle() {
                return "Telephone business 1";
            }
        };
        /** ************** * field44 * * ************ */
        mapping[Contact.TELEPHONE_BUSINESS2] = new Mapper() {

            public String getDBFieldName() {
                return "field44";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setTelephoneBusiness2(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsTelephoneBusiness2();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getTelephoneBusiness2());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getTelephoneBusiness2();
                final String y = original.getTelephoneBusiness2();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getTelephoneBusiness2();
            }

            public String getReadableTitle() {
                return "Telephone business 2";
            }
        };
        /** ************** * field45 * * ************ */
        mapping[Contact.FAX_BUSINESS] = new Mapper() {

            public String getDBFieldName() {
                return "field45";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setFaxBusiness(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsFaxBusiness();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getFaxBusiness());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getFaxBusiness();
                final String y = original.getFaxBusiness();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getFaxBusiness();
            }

            public String getReadableTitle() {
                return "FAX business";
            }
        };
        /** ************** * field46 * * ************ */
        mapping[Contact.TELEPHONE_CALLBACK] = new Mapper() {

            public String getDBFieldName() {
                return "field46";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setTelephoneCallback(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsTelephoneCallback();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getTelephoneCallback());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getTelephoneCallback();
                final String y = original.getTelephoneCallback();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getTelephoneCallback();
            }

            public String getReadableTitle() {
                return "Telephone callback";
            }
        };
        /** ************** * field47 * * ************ */
        mapping[Contact.TELEPHONE_CAR] = new Mapper() {

            public String getDBFieldName() {
                return "field47";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setTelephoneCar(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsTelephoneCar();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getTelephoneCar());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getTelephoneCar();
                final String y = original.getTelephoneCar();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getTelephoneCar();
            }

            public String getReadableTitle() {
                return "Telephone car";
            }
        };
        /** ************** * field48 * * ************ */
        mapping[Contact.TELEPHONE_COMPANY] = new Mapper() {

            public String getDBFieldName() {
                return "field48";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setTelephoneCompany(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsTelephoneCompany();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getTelephoneCompany());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getTelephoneCompany();
                final String y = original.getTelephoneCompany();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getTelephoneCompany();
            }

            public String getReadableTitle() {
                return "Telephone company";
            }
        };
        /** ************** * field49 * * ************ */
        mapping[Contact.TELEPHONE_HOME1] = new Mapper() {

            public String getDBFieldName() {
                return "field49";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setTelephoneHome1(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsTelephoneHome1();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getTelephoneHome1());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getTelephoneHome1();
                final String y = original.getTelephoneHome1();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getTelephoneHome1();
            }

            public String getReadableTitle() {
                return "Telephone home 1";
            }
        };
        /** ************** * field50 * * ************ */
        mapping[Contact.TELEPHONE_HOME2] = new Mapper() {

            public String getDBFieldName() {
                return "field50";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setTelephoneHome2(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsTelephoneHome2();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getTelephoneHome2());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getTelephoneHome2();
                final String y = original.getTelephoneHome2();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getTelephoneHome2();
            }

            public String getReadableTitle() {
                return "Telephone home 2";
            }
        };
        /** ************** * field51 * * ************ */
        mapping[Contact.FAX_HOME] = new Mapper() {

            public String getDBFieldName() {
                return "field51";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setFaxHome(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsFaxHome();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getFaxHome());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getFaxHome();
                final String y = original.getFaxHome();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getFaxHome();
            }

            public String getReadableTitle() {
                return "FAX home";
            }
        };
        /** ************** * field52 * * ************ */
        mapping[Contact.TELEPHONE_ISDN] = new Mapper() {

            public String getDBFieldName() {
                return "field52";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setTelephoneISDN(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsTelephoneISDN();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getTelephoneISDN());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getTelephoneISDN();
                final String y = original.getTelephoneISDN();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getTelephoneISDN();
            }

            public String getReadableTitle() {
                return "Telephone ISDN";
            }
        };
        /** ************** * field53 * * ************ */
        mapping[Contact.CELLULAR_TELEPHONE1] = new Mapper() {

            public String getDBFieldName() {
                return "field53";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setCellularTelephone1(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsCellularTelephone1();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getCellularTelephone1());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getCellularTelephone1();
                final String y = original.getCellularTelephone1();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getCellularTelephone1();
            }

            public String getReadableTitle() {
                return "Cellular telephone 1";
            }
        };
        /** ************** * field54 * * ************ */
        mapping[Contact.CELLULAR_TELEPHONE2] = new Mapper() {

            public String getDBFieldName() {
                return "field54";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setCellularTelephone2(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsCellularTelephone2();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getCellularTelephone2());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getCellularTelephone2();
                final String y = original.getCellularTelephone2();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getCellularTelephone2();
            }

            public String getReadableTitle() {
                return "Cellular telephone 2";
            }
        };
        /** ************** * field55 * * ************ */
        mapping[Contact.TELEPHONE_OTHER] = new Mapper() {

            public String getDBFieldName() {
                return "field55";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setTelephoneOther(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsTelephoneOther();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getTelephoneOther());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getTelephoneOther();
                final String y = original.getTelephoneOther();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getTelephoneOther();
            }

            public String getReadableTitle() {
                return "Telephone other";
            }
        };
        /** ************** * field56 * * ************ */
        mapping[Contact.FAX_OTHER] = new Mapper() {

            public String getDBFieldName() {
                return "field56";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setFaxOther(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsFaxOther();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getFaxOther());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getFaxOther();
                final String y = original.getFaxOther();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getFaxOther();
            }

            public String getReadableTitle() {
                return "FAX other";
            }
        };
        /** ************** * field57 * * ************ */
        mapping[Contact.TELEPHONE_PAGER] = new Mapper() {

            public String getDBFieldName() {
                return "field57";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setTelephonePager(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsTelephonePager();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getTelephonePager());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getTelephonePager();
                final String y = original.getTelephonePager();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getTelephonePager();
            }

            public String getReadableTitle() {
                return "Telephone pager";
            }
        };
        /** ************** * field58 * * ************ */
        mapping[Contact.TELEPHONE_PRIMARY] = new Mapper() {

            public String getDBFieldName() {
                return "field58";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setTelephonePrimary(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsTelephonePrimary();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getTelephonePrimary());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getTelephonePrimary();
                final String y = original.getTelephonePrimary();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getTelephonePrimary();
            }

            public String getReadableTitle() {
                return "Telephone primary";
            }
        };
        /** ************** * field59 * * ************ */
        mapping[Contact.TELEPHONE_RADIO] = new Mapper() {

            public String getDBFieldName() {
                return "field59";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setTelephoneRadio(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsTelephoneRadio();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getTelephoneRadio());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getTelephoneRadio();
                final String y = original.getTelephoneRadio();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getTelephoneRadio();
            }

            public String getReadableTitle() {
                return "Telephone radio";
            }
        };
        /** ************** * field60 * * ************ */
        mapping[Contact.TELEPHONE_TELEX] = new Mapper() {

            public String getDBFieldName() {
                return "field60";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setTelephoneTelex(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsTelephoneTelex();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getTelephoneTelex());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getTelephoneTelex();
                final String y = original.getTelephoneTelex();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getTelephoneTelex();
            }

            public String getReadableTitle() {
                return "Telephone telex";
            }
        };
        /** ************** * field61 * * ************ */
        mapping[Contact.TELEPHONE_TTYTDD] = new Mapper() {

            public String getDBFieldName() {
                return "field61";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setTelephoneTTYTTD(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsTelephoneTTYTTD();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getTelephoneTTYTTD());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getTelephoneTTYTTD();
                final String y = original.getTelephoneTTYTTD();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getTelephoneTTYTTD();
            }

            public String getReadableTitle() {
                return "Telephone TTY/TDD";
            }
        };
        /** ************** * field62 * * ************ */
        mapping[Contact.INSTANT_MESSENGER1] = new Mapper() {

            public String getDBFieldName() {
                return "field62";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setInstantMessenger1(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsInstantMessenger1();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getInstantMessenger1());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getInstantMessenger1();
                final String y = original.getInstantMessenger1();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getInstantMessenger1();
            }

            public String getReadableTitle() {
                return "Instantmessenger 1";
            }
        };

        /** ************** * field63 * * ************ */
        mapping[Contact.INSTANT_MESSENGER2] = new Mapper() {

            public String getDBFieldName() {
                return "field63";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setInstantMessenger2(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsInstantMessenger2();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getInstantMessenger2());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getInstantMessenger2();
                final String y = original.getInstantMessenger2();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getInstantMessenger2();
            }

            public String getReadableTitle() {
                return "Instantmessenger 2";
            }
        };

        /** ************** * field64 * * ************ */
        mapping[Contact.TELEPHONE_IP] = new Mapper() {

            public String getDBFieldName() {
                return "field64";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setTelephoneIP(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsTelephoneIP();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getTelephoneIP());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getTelephoneIP();
                final String y = original.getTelephoneIP();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getTelephoneIP();
            }

            public String getReadableTitle() {
                return "Telephone IP";
            }
        };
        /** ************** * field65 * * ************ */
        mapping[Contact.EMAIL1] = new Mapper() {

            public String getDBFieldName() {
                return "field65";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setEmail1(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsEmail1();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getEmail1());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getEmail1();
                final String y = original.getEmail1();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getEmail1();
            }

            public String getReadableTitle() {
                return "Email 1";
            }
        };
        /** ************** * field66 * * ************ */
        mapping[Contact.EMAIL2] = new Mapper() {

            public String getDBFieldName() {
                return "field66";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setEmail2(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsEmail2();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getEmail2());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getEmail2();
                final String y = original.getEmail2();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getEmail2();
            }

            public String getReadableTitle() {
                return "Email 2";
            }
        };
        /** ************** * field67 * * ************ */
        mapping[Contact.EMAIL3] = new Mapper() {

            public String getDBFieldName() {
                return "field67";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setEmail3(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsEmail3();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getEmail3());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getEmail3();
                final String y = original.getEmail3();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getEmail3();
            }

            public String getReadableTitle() {
                return "Email 3";
            }
        };
        /** ************** * field68 * * ************ */
        mapping[Contact.URL] = new Mapper() {

            public String getDBFieldName() {
                return "field68";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setURL(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsURL();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getURL());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getURL();
                final String y = original.getURL();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getURL();
            }

            public String getReadableTitle() {
                return "URL";
            }
        };
        /** ************** * field69 * * ************ */
        mapping[Contact.CATEGORIES] = new Mapper() {

            public String getDBFieldName() {
                return "field69";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setCategories(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsCategories();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getCategories());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getCategories();
                final String y = original.getCategories();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getCategories();
            }

            public String getReadableTitle() {
                return "Categories";
            }
        };
        /** ************** * field70 * * ************ */
        mapping[Contact.USERFIELD01] = new Mapper() {

            public String getDBFieldName() {
                return "field70";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setUserField01(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsUserField01();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getUserField01());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getUserField01();
                final String y = original.getUserField01();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getUserField01();
            }

            public String getReadableTitle() {
                return "Dynamic Field 1";
            }
        };
        /** ************** * field71 * * ************ */
        mapping[Contact.USERFIELD02] = new Mapper() {

            public String getDBFieldName() {
                return "field71";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setUserField02(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsUserField02();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getUserField02());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getUserField02();
                final String y = original.getUserField02();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getUserField02();
            }

            public String getReadableTitle() {
                return "Dynamic Field 2";
            }
        };
        /** ************** * field72 * * ************ */
        mapping[Contact.USERFIELD03] = new Mapper() {

            public String getDBFieldName() {
                return "field72";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setUserField03(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsUserField03();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getUserField03());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getUserField03();
                final String y = original.getUserField03();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getUserField03();
            }

            public String getReadableTitle() {
                return "Dynamic Field 3";
            }
        };
        /** ************** * field73 * * ************ */
        mapping[Contact.USERFIELD04] = new Mapper() {

            public String getDBFieldName() {
                return "field73";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setUserField04(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsUserField04();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getUserField04());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getUserField04();
                final String y = original.getUserField04();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getUserField04();
            }

            public String getReadableTitle() {
                return "Dynamic Field 4";
            }
        };
        /** ************** * field74 * * ************ */
        mapping[Contact.USERFIELD05] = new Mapper() {

            public String getDBFieldName() {
                return "field74";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setUserField05(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsUserField05();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getUserField05());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getUserField05();
                final String y = original.getUserField05();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getUserField05();
            }

            public String getReadableTitle() {
                return "Dynamic Field 5";
            }
        };
        /** ************** * field75 * * ************ */
        mapping[Contact.USERFIELD06] = new Mapper() {

            public String getDBFieldName() {
                return "field75";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setUserField06(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsUserField06();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getUserField06());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getUserField06();
                final String y = original.getUserField06();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getUserField06();
            }

            public String getReadableTitle() {
                return "Dynamic Field 6";
            }
        };
        /** ************** * field76 * * ************ */
        mapping[Contact.USERFIELD07] = new Mapper() {

            public String getDBFieldName() {
                return "field76";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setUserField07(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsUserField07();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getUserField07());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getUserField07();
                final String y = original.getUserField07();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getUserField07();
            }

            public String getReadableTitle() {
                return "Dynamic Field 7";
            }
        };
        /** ************** * field77 * * ************ */
        mapping[Contact.USERFIELD08] = new Mapper() {

            public String getDBFieldName() {
                return "field77";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setUserField08(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsUserField08();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getUserField08());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getUserField08();
                final String y = original.getUserField08();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getUserField08();
            }

            public String getReadableTitle() {
                return "Dynamic Field 8";
            }
        };
        /** ************** * field78 * * ************ */
        mapping[Contact.USERFIELD09] = new Mapper() {

            public String getDBFieldName() {
                return "field78";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setUserField09(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsUserField09();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getUserField09());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getUserField09();
                final String y = original.getUserField09();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getUserField09();
            }

            public String getReadableTitle() {
                return "Dynamic Field 9";
            }
        };
        /** ************** * field79 * * ************ */
        mapping[Contact.USERFIELD10] = new Mapper() {

            public String getDBFieldName() {
                return "field79";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setUserField10(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsUserField10();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getUserField10());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getUserField10();
                final String y = original.getUserField10();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getUserField10();
            }

            public String getReadableTitle() {
                return "Dynamic Field 10";
            }
        };
        /** ************** * field80 * * ************ */
        mapping[Contact.USERFIELD11] = new Mapper() {

            public String getDBFieldName() {
                return "field80";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setUserField11(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsUserField11();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getUserField11());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getUserField11();
                final String y = original.getUserField11();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getUserField11();
            }

            public String getReadableTitle() {
                return "Dynamic Field 11";
            }
        };
        /** ************** * field81 * * ************ */
        mapping[Contact.USERFIELD12] = new Mapper() {

            public String getDBFieldName() {
                return "field81";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setUserField12(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsUserField12();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getUserField12());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getUserField12();
                final String y = original.getUserField12();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getUserField12();
            }

            public String getReadableTitle() {
                return "Dynamic Field 12";
            }
        };
        /** ************** * field82 * * ************ */
        mapping[Contact.USERFIELD13] = new Mapper() {

            public String getDBFieldName() {
                return "field82";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setUserField13(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsUserField13();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getUserField13());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getUserField13();
                final String y = original.getUserField13();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getUserField13();
            }

            public String getReadableTitle() {
                return "Dynamic Field 13";
            }
        };
        /** ************** * field83 * * ************ */
        mapping[Contact.USERFIELD14] = new Mapper() {

            public String getDBFieldName() {
                return "field83";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setUserField14(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsUserField14();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getUserField14());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getUserField14();
                final String y = original.getUserField14();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getUserField14();
            }

            public String getReadableTitle() {
                return "Dynamic Field 14";
            }
        };
        /** ************** * field84 * * ************ */
        mapping[Contact.USERFIELD15] = new Mapper() {

            public String getDBFieldName() {
                return "field84";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setUserField15(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsUserField15();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getUserField15());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getUserField15();
                final String y = original.getUserField15();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getUserField15();
            }

            public String getReadableTitle() {
                return "Dynamic Field 15";
            }
        };
        /** ************** * field85 * * ************ */
        mapping[Contact.USERFIELD16] = new Mapper() {

            public String getDBFieldName() {
                return "field85";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setUserField16(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsUserField16();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getUserField16());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getUserField16();
                final String y = original.getUserField16();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getUserField16();
            }

            public String getReadableTitle() {
                return "Dynamic Field 16";
            }
        };
        /** ************** * field86 * * ************ */
        mapping[Contact.USERFIELD17] = new Mapper() {

            public String getDBFieldName() {
                return "field86";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setUserField17(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsUserField17();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getUserField17());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getUserField17();
                final String y = original.getUserField17();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getUserField17();
            }

            public String getReadableTitle() {
                return "Dynamic Field 17";
            }
        };
        /** ************** * field87 * * ************ */
        mapping[Contact.USERFIELD18] = new Mapper() {

            public String getDBFieldName() {
                return "field87";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setUserField18(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsUserField18();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getUserField18());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getUserField18();
                final String y = original.getUserField18();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getUserField18();
            }

            public String getReadableTitle() {
                return "Dynamic Field 18";
            }
        };
        /** ************** * field88 * * ************ */
        mapping[Contact.USERFIELD19] = new Mapper() {

            public String getDBFieldName() {
                return "field88";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setUserField19(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsUserField19();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getUserField19());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getUserField19();
                final String y = original.getUserField19();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getUserField19();
            }

            public String getReadableTitle() {
                return "Dynamic Field 19";
            }
        };
        /** ************** * field89 * * ************ */
        mapping[Contact.USERFIELD20] = new Mapper() {

            public String getDBFieldName() {
                return "field89";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setUserField20(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsUserField20();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getUserField20());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getUserField20();
                final String y = original.getUserField20();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getUserField20();
            }

            public String getReadableTitle() {
                return "Dynamic Field 20";
            }
        };
        /** ************** * intfield01 * * ************ */
        mapping[Contact.OBJECT_ID] = new Mapper() {

            public String getDBFieldName() {
                return "intfield01";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final int t = rs.getInt(pos);
                if (!rs.wasNull()) {
                    co.setObjectID(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsObjectID();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setInt(pos, co.getObjectID());
            }

            public boolean compare(final Contact co, final Contact original) {
                return (original.getObjectID() == co.getObjectID());
            }

            public String getValueAsString(final Contact co) {
                return String.valueOf(co.getObjectID());
            }

            public String getReadableTitle() {
                return "Object id";
            }
        };
        /** ************** * intfield02 * * ************ */
        mapping[Contact.NUMBER_OF_DISTRIBUTIONLIST] = new Mapper() {

            public String getDBFieldName() {
                return "intfield02";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final int t = rs.getInt(pos);
                if (!rs.wasNull() && (t > 0)) {
                    co.setNumberOfDistributionLists(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsNumberOfDistributionLists();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setInt(pos, co.getNumberOfDistributionLists());
            }

            public boolean compare(final Contact co, final Contact original) {
                return (original.getNumberOfDistributionLists() == co.getNumberOfDistributionLists());
            }

            public String getValueAsString(final Contact co) {
                return String.valueOf(co.getNumberOfDistributionLists());
            }

            public String getReadableTitle() {
                return "Number of distributionlists";
            }
        };
        /** ************** * intfield03 * * ************ */
        mapping[Contact.NUMBER_OF_LINKS] = new Mapper() {

            public String getDBFieldName() {
                return "intfield03";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final int t = rs.getInt(pos);
                if (!rs.wasNull() && (t > 0)) {
                    co.setNumberOfLinks(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsNumberOfLinks();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setInt(pos, co.getNumberOfLinks());
            }

            public boolean compare(final Contact co, final Contact original) {
                return (original.getNumberOfLinks() == co.getNumberOfLinks());
            }

            public String getValueAsString(final Contact co) {
                return String.valueOf(co.getNumberOfLinks());
            }

            public String getReadableTitle() {
                return "Number of links";
            }
        };
        /** ************** * intfield02 Part 2 * * ************ */
        mapping[Contact.DISTRIBUTIONLIST] = new Mapper() {

            public String getDBFieldName() {
                return "intfield02";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) {
                try {
                    final int t = rs.getInt(pos);
                    if (!rs.wasNull() && (t > 0)) {
                        co.setDistributionList(fillDistributionListArray(co.getObjectID(), user, ctx, readcon));
                    }
                } catch (final Exception e) {
                    LOG.error("Unable to load Distributionlist", e);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsDistributionLists();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) {
                // nix
            }

            public boolean compare(final Contact co, final Contact original) {
                return false;
            }

            public String getValueAsString(final Contact co) {
                return null;
            }

            public String getReadableTitle() {
                return null;
            }
        };
        /** ************** * intfield03 Part 2 * * ************ */
        mapping[Contact.LINKS] = new Mapper() {

            public String getDBFieldName() {
                return "intfield03";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) {
                try {
                    final int t = rs.getInt(pos);
                    if (!rs.wasNull() && (t > 0)) {
                        co.setLinks(fillLinkArray(co, user, ctx, readcon));
                    }
                } catch (final Exception e) {
                    LOG.error("Unable to load Links", e);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsLinks();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) {
                // nix
            }

            public boolean compare(final Contact co, final Contact original) {
                return false;
            }

            public String getValueAsString(final Contact co) {
                return null;
            }

            public String getReadableTitle() {
                return null;
            }
        };
        /** ************** * fid * * ************ */
        mapping[Contact.FOLDER_ID] = new Mapper() {

            public String getDBFieldName() {
                return "fid";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final int t = rs.getInt(pos);
                if (!rs.wasNull()) {
                    co.setParentFolderID(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsParentFolderID();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setInt(pos, co.getParentFolderID());
            }

            public boolean compare(final Contact co, final Contact original) {
                return false;
            }

            public String getValueAsString(final Contact co) {
                return String.valueOf(co.getParentFolderID());
            }

            public String getReadableTitle() {
                return "Folder id";
            }
        };
        /** ************** * cid * * ************ */
        mapping[Contact.CONTEXTID] = new Mapper() {

            public String getDBFieldName() {
                return "cid";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final int t = rs.getInt(pos);
                if (!rs.wasNull()) {
                    co.setContextId(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsContextId();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setInt(pos, co.getContextId());
            }

            public boolean compare(final Contact co, final Contact original) {
                return (original.getContextId() == co.getContextId());
            }

            public String getValueAsString(final Contact co) {
                return String.valueOf(co.getContextId());
            }

            public String getReadableTitle() {
                return "Context id";
            }
        };
        mapping[Contact.PRIVATE_FLAG] = new PrivateFlag();
        /** ************** * created_from * * ************ */
        mapping[Contact.CREATED_BY] = new Mapper() {

            public String getDBFieldName() {
                return "created_from";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final int t = rs.getInt(pos);
                if (!rs.wasNull()) {
                    co.setCreatedBy(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsCreatedBy();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setInt(pos, co.getCreatedBy());
            }

            public boolean compare(final Contact co, final Contact original) {
                return false;
            }

            public String getValueAsString(final Contact co) {
                return String.valueOf(co.getCreatedBy());
            }

            public String getReadableTitle() {
                return "Created by";
            }
        };
        /** ************** * changed_from * * ************ */
        mapping[Contact.MODIFIED_BY] = new Mapper() {

            public String getDBFieldName() {
                return "changed_from";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final int t = rs.getInt(pos);
                if (!rs.wasNull()) {
                    co.setModifiedBy(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsModifiedBy();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setInt(pos, co.getModifiedBy());
            }

            public boolean compare(final Contact co, final Contact original) {
                return (co.getModifiedBy() == original.getModifiedBy());
            }

            public String getValueAsString(final Contact co) {
                return String.valueOf(co.getModifiedBy());
            }

            public String getReadableTitle() {
                return "Modified by";
            }
        };
        /** ************** * creating_date * * ************ */
        mapping[Contact.CREATION_DATE] = new Mapper() {

            public String getDBFieldName() {
                return "creating_date";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final long dong = rs.getLong(pos);
                if (!rs.wasNull()) {
                    final java.util.Date d = new java.util.Date(dong);
                    co.setCreationDate(d);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsCreationDate();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                final java.util.Date d = co.getCreationDate();
                ps.setLong(pos, d.getTime());
            }

            public boolean compare(final Contact co, final Contact original) {
                return false;
            }

            public String getValueAsString(final Contact co) {
                return co.getCreationDate() == null ? null : co.getCreationDate().toString();
            }

            public String getReadableTitle() {
                return "Creation date";
            }
        };
        /** ************** * changing_date * * ************ */
        mapping[Contact.LAST_MODIFIED] = new Mapper() {

            public String getDBFieldName() {
                return "changing_date";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final long dong = rs.getLong(pos);
                if (!rs.wasNull()) {
                    final java.util.Date d = new java.util.Date(dong);
                    co.setLastModified(d);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsLastModified();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                final java.util.Date d = co.getLastModified();
                ps.setLong(pos, d.getTime());
            }

            public boolean compare(final Contact co, final Contact original) {
                return false;
            }

            public String getValueAsString(final Contact co) {
                return co.getLastModified() == null ? null : co.getLastModified().toString();
            }

            public String getReadableTitle() {
                return "Changing date";
            }
        };
        /** ************** * timestampfield01 * * ************ */
        mapping[Contact.BIRTHDAY] = new Mapper() {

            public String getDBFieldName() {
                return "timestampfield01";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final Timestamp t = rs.getTimestamp(pos);
                if (!rs.wasNull()) {
                    co.setBirthday(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsBirthday();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                if (co.getBirthday() == null) {
                    ps.setTimestamp(pos, null);
                } else {
                    ps.setTimestamp(pos, new java.sql.Timestamp(co.getBirthday().getTime()));
                }
            }

            public boolean compare(final Contact co, final Contact original) {
                final java.util.Date x = co.getBirthday();
                final java.util.Date y = original.getBirthday();

                if (null == x) {
                    if (null == y) {
                        return true;
                    }
                    return false;
                } else if (null == y) {
                    return false;
                }
                return (x.getTime() == y.getTime());
            }

            public String getValueAsString(final Contact co) {
                return co.getBirthday() == null ? null : co.getBirthday().toString();
            }

            public String getReadableTitle() {
                return "Birthday";
            }
        };
        /** ************** * timestampfield02 * * ************ */
        mapping[Contact.ANNIVERSARY] = new Mapper() {

            public String getDBFieldName() {
                return "timestampfield02";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final Timestamp t = rs.getTimestamp(pos);
                if (!rs.wasNull()) {
                    co.setAnniversary(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsAnniversary();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                if (co.getAnniversary() == null) {
                    ps.setTimestamp(pos, null);
                } else {
                    ps.setTimestamp(pos, new java.sql.Timestamp(co.getAnniversary().getTime()));
                }
            }

            public boolean compare(final Contact co, final Contact original) {
                final java.util.Date x = co.getAnniversary();
                final java.util.Date y = original.getAnniversary();

                if (null == x) {
                    if (null == y) {
                        return true;
                    }
                    return false;
                } else if (null == y) {
                    return false;
                }
                return (x.getTime() == y.getTime());
            }

            public String getValueAsString(final Contact co) {
                return co.getAnniversary() == null ? null : co.getAnniversary().toString();
            }

            public String getReadableTitle() {
                return "Anniversary";
            }
        };

        /** ************** * image01 * * ************ */
        mapping[Contact.IMAGE1] = new Mapper() {

            public String getDBFieldName() {
                return "intfield04";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) {
                try {
                    final int t = rs.getInt(pos);
                    if (!rs.wasNull() && (t > 0)) {
                        getContactImage(co.getObjectID(), co, ctx.getContextId(), readcon);
                    }
                } catch (final Exception e) {
                    LOG.error("Image not found", e);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsImage1();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                if (co.containsImage1()) {
                    ps.setInt(pos, 1);
                } else {
                    ps.setInt(pos, 0);
                }
            }

            public boolean compare(final Contact co, final Contact original) {

                if ((co.getImage1() != null) && (original.getImage1() != null)) {
                    final String x = new String(co.getImage1());
                    final String y = new String(original.getImage1());

                    return (x.equals(y));
                }
                if (((co.getImage1() == null) && (original.getImage1() != null)) || ((co.getImage1() != null) && (original.getImage1() == null))) {
                    return false;
                }
                return true;
            }

            public String getValueAsString(final Contact co) {
                return null;
            }

            public String getReadableTitle() {
                return null;
            }
        };
        /** ************** * intfield04 * * ************ */
        mapping[Contact.IMAGE_LAST_MODIFIED] = new Mapper() {

            public String getDBFieldName() {
                return "intfield04";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) {
                try {
                    final int t = rs.getInt(pos);
                    if (!rs.wasNull() && (t > 0)) {
                        final Date dd = getContactImageLastModified(co.getObjectID(), ctx.getContextId(), readcon);
                        if (dd != null) {
                            co.setImageLastModified(dd);
                        }
                    }
                } catch (final Exception e) {
                    LOG.error("Unable to load Image", e);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsImageLastModified();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                if (co.containsImage1()) {
                    ps.setInt(pos, 1);
                } else {
                    ps.setInt(pos, 0);
                }
            }

            public boolean compare(final Contact co, final Contact original) {
                return false;
            }

            public String getValueAsString(final Contact co) {
                return null;
            }

            public String getReadableTitle() {
                return null;
            }
        };
        /** ************** * intfield04 * * ************ */
        mapping[Contact.IMAGE1_CONTENT_TYPE] = new Mapper() {

            public String getDBFieldName() {
                return "intfield04";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) {
                try {
                    final int t = rs.getInt(pos);
                    if (!rs.wasNull() && (t > 0)) {
                        final String ct = getContactImageContentType(co.getObjectID(), ctx.getContextId(), readcon);
                        if (ct != null) {
                            co.setImageContentType(ct);
                        }
                    }
                } catch (final Exception e) {
                    LOG.error("Image not found", e);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsImageContentType();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                if (co.containsImage1()) {
                    ps.setInt(pos, 1);
                } else {
                    ps.setInt(pos, 0);
                }
            }

            public boolean compare(final Contact co, final Contact original) {
                return false;
            }

            public String getValueAsString(final Contact co) {
                return null;
            }

            public String getReadableTitle() {
                return null;
            }
        };
        /** ************** * intfield04 * * ************ */
        mapping[Contact.NUMBER_OF_IMAGES] = new Mapper() {

            public String getDBFieldName() {
                return "intfield04";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) {
                //
            }

            public boolean containsElement(final Contact co) {
                return false;
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) {
                // false
            }

            public boolean compare(final Contact co, final Contact original) {
                return false;
            }

            public String getValueAsString(final Contact co) {
                return null;
            }

            public String getReadableTitle() {
                return null;
            }
        };
        /** ************** * userid * * ************ */
        mapping[Contact.INTERNAL_USERID] = new Mapper() {

            public String getDBFieldName() {
                return "userid";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final int i = rs.getInt(pos);
                if (!rs.wasNull() && (i > 0)) {
                    co.setInternalUserId(i);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsInternalUserId();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                if (co.containsInternalUserId()) {
                    ps.setInt(pos, co.getInternalUserId());
                } else {
                    ps.setInt(pos, 0);
                }
            }

            public boolean compare(final Contact co, final Contact original) {
                return false;
            }

            public String getValueAsString(final Contact co) {
                return null;
            }

            public String getReadableTitle() {
                return null;
            }
        };
        /** ************** * intfield05 * * ************ */
        mapping[Contact.COLOR_LABEL] = new Mapper() {

            public String getDBFieldName() {
                return "intfield05";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final int i = rs.getInt(pos);
                if (!rs.wasNull() && (i > 0)) {
                    co.setLabel(i);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsLabel();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                if (co.containsLabel()) {
                    ps.setInt(pos, co.getLabel());
                } else {
                    ps.setInt(pos, 0);
                }
            }

            public boolean compare(final Contact co, final Contact original) {
                return (co.getLabel() == original.getLabel());
            }

            public String getValueAsString(final Contact co) {
                return null;
            }

            public String getReadableTitle() {
                return null;
            }
        };
        /** ************** * field90 * * ************ */
        mapping[Contact.FILE_AS] = new Mapper() {

            public String getDBFieldName() {
                return "field90";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setFileAs(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsFileAs();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getFileAs());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getFileAs();
                final String y = original.getFileAs();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return null;
            }

            public String getReadableTitle() {
                return null;
            }
        };
        /** ************** * intfield06 * * ************ */
        mapping[Contact.DEFAULT_ADDRESS] = new Mapper() {

            public String getDBFieldName() {
                return "intfield06";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final int i = rs.getInt(pos);
                if (!rs.wasNull() && (i > 0)) {
                    co.setDefaultAddress(i);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsDefaultAddress();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                if (co.containsDefaultAddress()) {
                    ps.setInt(pos, co.getDefaultAddress());
                } else {
                    ps.setInt(pos, 0);
                }
            }

            public boolean compare(final Contact co, final Contact original) {
                return (co.getDefaultAddress() == original.getDefaultAddress());
            }

            public String getValueAsString(final Contact co) {
                return String.valueOf(co.getDefaultAddress());
            }

            public String getReadableTitle() {
                return "Default address";
            }
        };
        /** ************** * intfield07 * * ************ */
        mapping[Contact.MARK_AS_DISTRIBUTIONLIST] = new Mapper() {

            public String getDBFieldName() {
                return "intfield07";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final int i = rs.getInt(pos);
                if (!rs.wasNull() && (i > 0)) {
                    co.markAsDistributionlist();
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsMarkAsDistributionlist();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                if (co.containsMarkAsDistributionlist()) {
                    if (co.getMarkAsDistribtuionlist()) {
                        ps.setInt(pos, 1);
                    } else {
                        ps.setInt(pos, 0);
                    }
                } else {
                    ps.setInt(pos, 0);
                }
            }

            public boolean compare(final Contact co, final Contact original) {
                if (co.getMarkAsDistribtuionlist() && (!original.getMarkAsDistribtuionlist())) {
                    return false;
                } else if ((!co.getMarkAsDistribtuionlist()) && (original.getMarkAsDistribtuionlist())) {
                    return true;
                } else if (co.getMarkAsDistribtuionlist() == original.getMarkAsDistribtuionlist()) {
                    return true;
                } else {
                    return false;
                }
            }

            public String getValueAsString(final Contact co) {
                return null;
            }

            public String getReadableTitle() {
                return null;
            }
        };
        /** ************** * intfield08 * * ************ */
        mapping[Contact.NUMBER_OF_ATTACHMENTS] = new Mapper() {

            public String getDBFieldName() {
                return "intfield08";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final int i = rs.getInt(pos);
                if (!rs.wasNull()) {
                    co.setNumberOfAttachments(i);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsNumberOfAttachments();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                if (co.containsNumberOfAttachments()) {
                    ps.setInt(pos, co.getNumberOfAttachments());
                } else {
                    ps.setInt(pos, 0);
                }
            }

            public boolean compare(final Contact co, final Contact original) {
                return (co.getNumberOfAttachments() == original.getNumberOfAttachments());
            }

            public String getValueAsString(final Contact co) {
                return null;
            }

            public String getReadableTitle() {
                return null;
            }
        };
        /** ************** * useCount * * ************ */
        mapping[Contact.USE_COUNT] = new Mapper() {

            public String getDBFieldName() {
                return "useCount";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final int i = rs.getInt(pos);
                if (!rs.wasNull() && (i > 0)) {
                    co.setUseCount(i);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsUseCount();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                if (co.containsUseCount()) {
                    ps.setInt(pos, co.getUseCount());
                } else {
                    ps.setInt(pos, 0);
                }
            }

            public boolean compare(final Contact co, final Contact original) {
                return (co.getUseCount() == original.getUseCount());
            }

            public String getValueAsString(final Contact co) {
                return null;
            }

            public String getReadableTitle() {
                return null;
            }
        };
        /** ************** * yomiFirstName * * ************ */
        mapping[Contact.YOMI_FIRST_NAME] = new Mapper() {

            public String getDBFieldName() {
                return "yomiFirstName";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setYomiFirstName(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsYomiFirstName();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getYomiFirstName());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getYomiFirstName();
                final String y = original.getYomiFirstName();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getYomiFirstName();
            }

            public String getReadableTitle() {
                return "Yomi First Name";
            }
        };
        /** ************** * yomiLastName * * ************ */
        mapping[Contact.YOMI_LAST_NAME] = new Mapper() {

            public String getDBFieldName() {
                return "yomiLastName";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setYomiLastName(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsYomiLastName();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getYomiLastName());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getYomiLastName();
                final String y = original.getYomiLastName();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getYomiLastName();
            }

            public String getReadableTitle() {
                return "Yomi Last Name";
            }
        };
        /** ************** * Yomi Company * * ************ */
        mapping[Contact.YOMI_COMPANY] = new Mapper() {

            public String getDBFieldName() {
                return "yomiCompany";
            }

            public void addToContactObject(final ResultSet rs, final int pos, final Contact co, final Connection readcon, final int user, final int[] group, final Context ctx, final UserConfiguration uc) throws SQLException {
                final String t = rs.getString(pos);
                if (!rs.wasNull()) {
                    co.setYomiCompany(t);
                }
            }

            public boolean containsElement(final Contact co) {
                return co.containsYomiCompany();
            }

            public void fillPreparedStatement(final PreparedStatement ps, final int pos, final Contact co) throws SQLException {
                ps.setString(pos, co.getYomiCompany());
            }

            public boolean compare(final Contact co, final Contact original) {
                final String x = co.getYomiCompany();
                final String y = original.getYomiCompany();

                return areEqual(x, y);
            }

            public String getValueAsString(final Contact co) {
                return co.getYomiCompany();
            }

            public String getReadableTitle() {
                return "Yomi Company";
            }
        };
    }
}

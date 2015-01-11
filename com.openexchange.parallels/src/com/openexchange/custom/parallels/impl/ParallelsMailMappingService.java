package com.openexchange.custom.parallels.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Strings;
import com.openexchange.mailmapping.MailResolver;
import com.openexchange.mailmapping.ResolvedMail;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.UserService;


public class ParallelsMailMappingService implements MailResolver {

    /** The service look-up */
    private final ServiceLookup services;

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ParallelsMailMappingService.class);

    public ParallelsMailMappingService(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public ResolvedMail resolve(String mail) throws OXException {

        LOG.debug("resolving {} in {}", mail, this.getClass());

        if (Strings.isEmpty(mail)) {
            return null;
        }

        int atSign = mail.lastIndexOf('@');
        if (atSign <= 0 || atSign >= mail.length()) {
            // Does not seem to be a valid E-Mail address
            return null;
        }

        DatabaseService dbservice = services.getService(DatabaseService.class);
        if (null == dbservice) {
            throw ServiceExceptionCode.absentService(DatabaseService.class);
        }
        ContextService cservice = services.getService(ContextService.class);
        if (null == cservice) {
            throw ServiceExceptionCode.absentService(ContextService.class);
        }
        UserService uservice = services.getService(UserService.class);
        if (null == uservice) {
            throw ServiceExceptionCode.absentService(UserService.class);
        }

        Connection con = dbservice.getReadOnly();
        PreparedStatement prep = null;
        ResultSet rs = null;
        try {
            prep = con.prepareStatement("SELECT cid,login_info FROM login2context WHERE login_info LIKE ?");
            prep.setString(1, mail + "||%");
            rs = prep.executeQuery();

            if (!rs.next()) {
                return ResolvedMail.DENY();
            }
            final int ctxId = rs.getInt("cid");
            final String linfo = rs.getString("login_info");

            final Context ctx = cservice.getContext(ctxId);
            final String[] udata = linfo.split("\\|\\|");
            if (udata[0].equals(mail)) {
                final int uid = uservice.getUserId(udata[1], ctx);
                return new ResolvedMail(uid, ctx.getContextId());
            }
        } catch (SQLException e) {
            LOG.error("unable to find context containing mail address", e);
            throw new OXException(e);
        } finally {
            DBUtils.closeSQLStuff(rs, prep);
            dbservice.backReadOnly(con);
        }

        return ResolvedMail.DENY();
    }

}

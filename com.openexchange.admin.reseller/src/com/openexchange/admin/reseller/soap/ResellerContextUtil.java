package com.openexchange.admin.reseller.soap;

import com.openexchange.admin.reseller.rmi.extensions.OXContextExtensionImpl;
import com.openexchange.admin.reseller.soap.dataobjects.ResellerContext;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;


public final class ResellerContextUtil {

    /**
     * @param ctx
     * @return
     * @throws DuplicateExtensionException
     */
    public static Context resellerContext2Context(ResellerContext ctx) throws DuplicateExtensionException {
        Context ret = new Context();
        ret.setAverage_size(ctx.getAverage_size());
        ret.setEnabled(ctx.getEnabled());
        ret.setFilestore_name(ctx.getFilestore_name());
        ret.setFilestoreId(ctx.getFilestoreId());
        ret.setLoginMappings(ctx.getLoginMappings());
        ret.setMaintenanceReason(ctx.getMaintenanceReason());
        ret.setMaxQuota(ctx.getMaxQuota());
        ret.setName(ctx.getName());
        ret.setReadDatabase(ctx.getReadDatabase());
        ret.setUsedQuota(ctx.getUsedQuota());
        ret.setWriteDatabase(ctx.getWriteDatabase());
        OXContextExtensionImpl ctxext = new OXContextExtensionImpl();
        ctxext.setCustomid(ctx.getCustomid());
        ctxext.setOwner(ctx.getOwner());
        ctxext.setRestriction(ctx.getRestriction());
        ctxext.setSid(ctx.getSid());
        ret.addExtension(ctxext);
        return ret;
    }


}

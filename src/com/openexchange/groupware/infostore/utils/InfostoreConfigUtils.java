package com.openexchange.groupware.infostore.utils;

import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.infostore.InfostoreConfig;

public class InfostoreConfigUtils {
	private static long maxUploadSize = -1;
	
	
	public static long determineRelevantUploadSize(UserConfiguration userConfig) {
		if(maxUploadSize == -1) {
			maxUploadSize = InfostoreConfig.getMaxUploadSize();
		}
		
		long maxSize = 0;
		maxSize = userConfig.getUserSettingMail().getUploadQuota();
		
		return maxSize < 0 ? maxUploadSize : maxSize;
		
	}
	
	public static long determineRelevantUploadSizePerFile(UserConfiguration userConfig) {
		long perFileSize = userConfig.getUserSettingMail().getUploadQuotaPerFile();
		if(perFileSize == 0) {
			return 0;
		}
		
		long size = determineRelevantUploadSize(userConfig);
		
		if(perFileSize == -1 || (size > 1 && perFileSize > size)) {
			perFileSize = size;
		}
		
		return perFileSize;
	}
}

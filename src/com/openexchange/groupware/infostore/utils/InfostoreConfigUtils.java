package com.openexchange.groupware.infostore.utils;

import com.openexchange.groupware.infostore.InfostoreConfig;
import com.openexchange.imap.UserSettingMail;

public class InfostoreConfigUtils {
	private static long maxUploadSize = -1;

	public static long determineRelevantUploadSize(final UserSettingMail userSettingMail) {
		if (maxUploadSize == -1) {
			maxUploadSize = InfostoreConfig.getMaxUploadSize();
		}

		long maxSize = 0;
		maxSize = userSettingMail.getUploadQuota();

		return maxSize < 0 ? maxUploadSize : maxSize;

	}

	public static long determineRelevantUploadSizePerFile(final UserSettingMail userSettingMail) {
		long perFileSize = userSettingMail.getUploadQuotaPerFile();
		if (perFileSize == 0) {
			return 0;
		}

		final long size = determineRelevantUploadSize(userSettingMail);

		if (perFileSize == -1 || (size > 1 && perFileSize > size)) {
			perFileSize = size;
		}

		return perFileSize;
	}
}

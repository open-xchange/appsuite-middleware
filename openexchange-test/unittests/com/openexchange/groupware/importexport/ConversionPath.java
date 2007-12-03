package com.openexchange.groupware.importexport;


public class ConversionPath {
	private String folder;
	private Format format;

	ConversionPath(String folder, Format format){
		this.folder = folder;
		this.format = format;
	}

	@Override
	public int hashCode() {
		final int PRIME = 23;
		int result = 1;
		result = PRIME * result + ((folder == null) ? 0 : folder.hashCode());
		result = PRIME * result + ((format == null) ? 0 : format.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ConversionPath other = (ConversionPath) obj;
		if (folder == null) {
			if (other.folder != null)
				return false;
		} else if (!folder.equals(other.folder))
			return false;
		if (format == null) {
			if (other.format != null)
				return false;
		} else if (!format.equals(other.format))
			return false;
		return true;
	}
}


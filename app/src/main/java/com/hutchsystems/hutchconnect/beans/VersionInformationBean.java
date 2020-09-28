package com.hutchsystems.hutchconnect.beans;


public class VersionInformationBean  {

	private boolean AutoDownloadFg;
	private boolean AutoUpdateFg;
	private String CurrentVersion;
	private String DownloadDate;
	private boolean DownloadFg;
	private boolean LiveFg;
	private String PreviousVersion;
	private String SerialNo;
	private String UpdateArchiveName;
	private String UpdateDate;
	private String UpdateUrl;
	private boolean UpdatedFg;
	private String VersionDate;
	private int id;
	// File Length
	private int fileContentLength;
	int SyncFg;
	// Application is Downloaded from playstore or link
    private boolean AppDownloadFromPlayStore;
    private String SyncDate;

	public int getSyncFg() {
		return SyncFg;
	}

	public void setSyncFg(int syncFg) {
		SyncFg = syncFg;
	}

	public int getFileContentLength() {
		return fileContentLength;
	}

	public void setFileContentLength(int fileContentLength) {
		this.fileContentLength = fileContentLength;
	}

	public String getSyncDate() {
		return SyncDate;
	}

	public void setSyncDate(String syncDate) {
		SyncDate = syncDate;
	}

	public boolean isAppDownloadFromPlayStore() {
		return AppDownloadFromPlayStore;
	}

	public void setAppDownloadFromPlayStore(boolean appDownloadFromPlayStore) {
		AppDownloadFromPlayStore = appDownloadFromPlayStore;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean getAutoDownloadFg() {
		return AutoDownloadFg;
	}

	public void setAutoDownloadFg(boolean value) {
		AutoDownloadFg = value;
	}

	public boolean getAutoUpdateFg() {
		return AutoUpdateFg;
	}

	public void setAutoUpdateFg(boolean value) {
		AutoUpdateFg = value;
	}

	public String getCurrentVersion() {
		return CurrentVersion;
	}

	public void setCurrentVersion(String value) {
		CurrentVersion = value;
	}

	public String getDownloadDate() {
		return DownloadDate;
	}

	public void setDownloadDate(String value) {
		DownloadDate = value;
	}

	public boolean getDownloadFg() {
		return DownloadFg;
	}

	public void setDownloadFg(boolean value) {
		DownloadFg = value;
	}

	public boolean getLiveFg() {
		return LiveFg;
	}

	public void setLiveFg(boolean value) {
		LiveFg = value;
	}

	public String getPreviousVersion() {
		return PreviousVersion;
	}

	public void setPreviousVersion(String value) {
		PreviousVersion = value;
	}

	public String getSerialNo() {
		return SerialNo;
	}

	public void setSerialNo(String value) {
		SerialNo = value;
	}

	public String getUpdateArchiveName() {
		return UpdateArchiveName;
	}

	public void setUpdateArchiveName(String value)
	{
		UpdateArchiveName = value;
	}

	public String getUpdateDate() {
		return UpdateDate;
	}

	public void setUpdateDate(String value) {
		UpdateDate = value;
	}

	public String getUpdateUrl() {
		return UpdateUrl;
	}

	public void setUpdateUrl(String value) {
		UpdateUrl = value;
	}

	public boolean getUpdatedFg() {
		return UpdatedFg;
	}

	public void setUpdatedFg(boolean value) {
		UpdatedFg = value;
	}

	public String getVersionDate() {
		return VersionDate;
	}

	public void setVersionDate(String value) {
		VersionDate = value;
	}

}

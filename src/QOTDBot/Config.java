package QOTDBot;

public class Config {
	private String botToken;
	private String serverID;
	private String channelID;
	private String prefix;

	private int interval;
	private int hour;
	private int minute;

	private String permRoleID;
	private String managerRoleID;

	private boolean dynamicConfig = false;

	private boolean managerReview = false;
	private String reviewChannel;
	
	private boolean initialized;

	public Config() {}

	public String getBotToken() {
		return botToken;
	}
	public void setBotToken(String botToken) {
		this.botToken = botToken;
	}
	public String getServerID() {
		return serverID;
	}
	public void setServerID(String serverID) {
		this.serverID = serverID;
	}
	public String getChannelID() {
		return channelID;
	}
	public void setChannelID(String channelID) {
		this.channelID = channelID;
		writeYML(false);
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
		writeYML(false);
	}
	public int getInterval() {
		return interval;
	}
	public void setInterval(int interval) {
		this.interval = interval;
	}
	public int getHour() {
		return hour;
	}
	public void setHour(int hour) {
		this.hour = hour;
	}
	public int getMinute() {
		return minute;
	}
	public void setMinute(int minute) {
		this.minute = minute;
	}
	public String getPermRoleID() {
		return permRoleID;
	}
	public void setPermRoleID(String permRoleID) {
		this.permRoleID = permRoleID;
		writeYML(false);
	}
	public String getManagerRoleID() {
		return managerRoleID;
	}
	public void setManagerRoleID(String managerRoleID) {
		this.managerRoleID = managerRoleID;
		writeYML(false);
	}
	public boolean getDynamicConfig() {
		return dynamicConfig;
	}
	public void setDynamicConfig(boolean dynamicConfig) {
		this.dynamicConfig = dynamicConfig;
		writeYML(true);
	}
	public boolean getManagerReview() {
		return managerReview;
	}
	public void setManagerReview(boolean managerReview) {
		this.managerReview = managerReview;
		writeYML(false);
	}
	public String getReviewChannel() {
		return reviewChannel;
	}
	public void setReviewChannel(String reviewChannel) {
		this.reviewChannel = reviewChannel;
		writeYML(false);
	}

	
	public boolean getInitialized() {
		return initialized;
	}
	public void setInitializedY(boolean initialized) {
		this.initialized = initialized;
	}
	

	public boolean isValid() {
		if(botToken.isBlank())
			return false;

		if(serverID.isBlank())
			return false;

		if(channelID.isBlank())
			return false;

		if(prefix.isBlank())
			return false;

		if(interval < 1 || interval > 1440)
			return false;

		if(hour < 0 || hour > 24 || minute < 0 || minute > 59)
			return false;

		if(permRoleID.isBlank())
			return false;
		
		if(managerRoleID.isBlank())
			return false;
		
		return true;
	}
	
	private void writeYML(boolean exception) {
		if((dynamicConfig || exception) && getInitialized()) {
			QOTDBot.writeConfigYML();
		}
	}
}
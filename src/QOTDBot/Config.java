package QOTDBot;

public class Config {
	private String botToken;
	private String serverID;
	private String channelID;
	private String prefix;
	
	private String permRoleID;
	private String managerRoleID;
	private int interval;
	
	
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
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public String getPermRoleID() {
		return permRoleID;
	}
	public void setPermRoleID(String permRoleID) {
		this.permRoleID = permRoleID;
	}
	public String getManagerRoleID() {
		return managerRoleID;
	}
	public void setManagerRoleID(String managerRoleID) {
		this.managerRoleID = managerRoleID;
	}
	public int getInterval() {
		return interval;
	}
	public void setInterval(int interval) {
		this.interval = interval;
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
		
		return true;
	}
}
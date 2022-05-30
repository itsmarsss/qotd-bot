package QOTDBot;

import java.util.Scanner;

import javax.security.auth.login.LoginException;

public class Main {
	public static QOTD qotd;
	public static String prefix = "qotd";
	public static String permRoleID = "";
	public static String managerRoleID = "";
	public static String channelID = "";
	public static int interval = 24;
	public static void main(String[] args) throws LoginException, InterruptedException {
		Scanner sc = new Scanner(System.in);
		System.out.println("Token: ");
		String token = sc.nextLine();
		sc.close();
		qotd = new QOTD(token);
	}
}

package de.ancash.bazaar.utils;

public class Chat {

	private static String prefix = "[Bazaar]";
	
	public static void sendMessage(String msg, ChatLevel level) {
		switch (level) {
		case INFO:
			System.out.println(prefix + " " + msg);
			break;
		case WARN:
			System.err.println(prefix + " " + msg);
			break;
		case FATAL:
			System.err.println(prefix + " " + msg);
			break;
		default:
			break;
		}
	}
	
	public enum ChatLevel{
		
		INFO("[Info]"),
		WARN("[§6Warn§r]"),
		FATAL("[§cFatal§r]");
		
		private final String level;
		
		ChatLevel(String level) {
			this.level = level;
		}
		
		public String getLevel() {
			return level;
		}
	}
}

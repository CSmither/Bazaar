package de.ancash.bazaar.utils;

import org.bukkit.Bukkit;

public class Chat {

	private static String prefix = "[Bazaar]";
	
	public static void sendMessage(String msg) {
		sendMessage(msg, ChatLevel.INFO);
	}
	
	public static void sendMessage(String msg, ChatLevel level) {
		switch (level) {
		case INFO:
			System.out.println(prefix + " " + msg);
			break;
		case WARN:
			Bukkit.getLogger().warning(prefix + " " + msg);
			break;
		case FATAL:
			Bukkit.getLogger().severe(prefix + " " + msg);
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

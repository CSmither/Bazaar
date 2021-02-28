package de.ancash.bazaar.utils;

import org.bukkit.Bukkit;

public class Chat {

	private static String prefix = "[Bazaar]";
	
	public static void sendMessage(String msg, ChatLevel level) {
		Bukkit.getConsoleSender().sendMessage(prefix + " " + level.getLevel() + " " + msg);;
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

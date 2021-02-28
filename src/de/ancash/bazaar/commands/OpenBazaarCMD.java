package de.ancash.bazaar.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.ancash.bazaar.listeners.BazaarInvClick;
import de.ancash.bazaar.management.Category;
import de.ancash.bazaar.management.PlayerManager;

public class OpenBazaarCMD implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		if(args.length == 0) return false;
		if(sender instanceof Player) {
			Player p = (Player) sender;
			//p.hasPermission("bazaar.open")
			if(true) {
				if(args[0].toLowerCase().equals("open")) {
					PlayerManager pm = PlayerManager.get(p.getUniqueId());
					pm.getBazaarMain().setContents(Category.getCategory(1).getShowcase().clone());
					p.openInventory(BazaarInvClick.prepareMain(pm.getBazaarMain(), 1, 0, (Player) sender));
				}
			}
		}
		return true;
	}

}

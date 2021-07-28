package de.ancash.bazaar.commands;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.ancash.bazaar.Bazaar;
import de.ancash.bazaar.gui.BazaarIGUI;
import de.ancash.minecraft.ItemStackUtils;
import de.ancash.minecraft.XMaterial;

public class BazaarCMD implements CommandExecutor{

	private final Bazaar pl;
	
	public BazaarCMD(Bazaar pl) {
		this.pl = pl;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		if(args.length == 0) return false;
		if(args.length > 0) {
			
			Player player = sender instanceof Player ? (Player) sender : null;
			switch (args[0].toLowerCase()) {
			case "open":
				if(args.length == 1) {
					if(!sender.hasPermission("bazaar.open") || !(sender instanceof Player)) {
						sender.sendMessage("§cYou don't have permission to do that!");
						return true;
					} else {
						BazaarIGUI gui = new BazaarIGUI(pl, player.getUniqueId(), 45, "Bazaar");
						player.openInventory(gui.getInventory());
						return true;
					}
				}
				if(args.length == 2) {
					Player target = Bukkit.getPlayer(args[1]);
					if(!sender.hasPermission("bazaar.openo")) {
						sender.sendMessage("§cYou don't have permission to do that!");
						return true;
					}
					if(target == null || !target.isOnline()) {
						sender.sendMessage("§cThat player is not online!");
						return true;
					}
					BazaarIGUI gui = new BazaarIGUI(pl, target.getUniqueId(), 45, "Bazaar");
					target.openInventory(gui.getInventory());
					return true;
				}
				return false;
			case "setitem":
				//XXX rework
				if(!(sender instanceof Player)) {
					return true;
				}
				if(!player.hasPermission("bazaar.setitem")) {
					player.sendMessage("§cYou don't have permission to do that!");
					return true;
				}
				ItemStack toSave = player.getItemInHand().clone();
				if(toSave == null || toSave.getType().equals(XMaterial.AIR.parseMaterial())) {
					player.sendMessage("§cHold a valid item!");
					return true;
				}
				toSave.setAmount(1);
				if(args.length != 4) {
					sender.sendMessage("§7Use /bz setitem <category, 1-5> <subcategory, 1-18> <subsubcategory, 1-9>");
					return true;
				}
				try {
					int cat = Integer.valueOf(args[1]);
					int subCat = Integer.valueOf(args[2]);
					int subsub = Integer.valueOf(args[3]);
					if(cat < 1 || cat > 5 || subCat < 1 || subCat > 18 || subsub < 1 || subsub > 9) {
						sender.sendMessage("§7Use /bz setitem <category, 1-5> <subcategory, 1-18> <subsubcategory, 1-9>");
						return true;
					}
					File file = new File(pl.getInvConfig().getString("inventory.categories." + cat + ".file"));
					if(!file.exists()) {
						sender.sendMessage("§cThat file doesn't exist! (" + file.getPath() + ")");
						return true;
					}
					
					FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
					fc.load(file);
					fc.set(subCat + ".subsub." + subsub + ".original", toSave);
					if(ItemStackUtils.get(fc, subCat + ".subsub." + subsub + ".show") == null) {
						sender.sendMessage("§cSubsub Show item is null trying to copy from previous item!");
						if(fc.getDouble(subCat + ".subsub." + subsub + ".default.price") == 0) fc.set(subCat + ".subsub." + subsub + ".default-price", 2);
						if(ItemStackUtils.get(fc, subCat + ".subsub." + (subsub - 1) + ".show") == null) {
							sender.sendMessage("§cDidn't find previous item! Set it manually else it won't work!");
						} else {
							ItemStack show = ItemStackUtils.get(fc, subCat + ".subsub." + (subsub - 1) + ".show").clone();
							show.setType(toSave.getType());
							show.getData().setData(toSave.getData().getData());
							fc.set(subCat + ".subsub." + subsub + ".show", show);
							sender.sendMessage("§aSet show item.");
						}
					}
					if(ItemStackUtils.get(fc, subCat + ".sub") == null) {
						sender.sendMessage("§cSub item is null trying to copy from previous item!");
						if(ItemStackUtils.get(fc, subCat - 1 + ".sub") == null) {
							sender.sendMessage("§cDidn't find previous item! Set it manually else it won't work!");
						} else {
							ItemStack sub = ItemStackUtils.get(fc, subCat - 1 + ".sub").clone();
							sub.setType(toSave.getType());
							sub.getData().setData(toSave.getData().getData());
							fc.set(subCat + ".sub", sub);
						}
					}
					fc.save(file);
					sender.sendMessage("§aItem set.");
				}catch(Exception ex) {
					sender.sendMessage("§7Use /bz setitem <category, 1-5> <subcategory, 1-18> <subsubcategory, 1-9>");
					return true;
				}
				return true;
			case "speedtest":
				if(7 == Math.sqrt(49)) return false;
				if(sender.hasPermission("bazaar.speedtest")) {
					sender.sendMessage("Sending packet");
					//ILibrary.getInstance().send(PacketBuilder.newPacketBuilder("speedtesttrue").getPacket().getString());
					sender.sendMessage("Sent message! Look in the console of the other servers to check the speed.");
				}
				break;
			default:
				break;
			}
		}
		return false;
	}

}

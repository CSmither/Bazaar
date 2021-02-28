package de.ancash.bazaar.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

public class ItemStackUtils {

	@SuppressWarnings("deprecation")
	public static String itemStackToString(ItemStack is) {
		//converting to string
		StringBuilder test = new StringBuilder();
		
		if(is == null || is.getType().equals(Material.AIR)) return "";
		
		if(is.hasItemMeta()) {
			test.append("[t=" + is.getType().toString());
			test.append(",d=" + is.getData().getData());
			ItemMeta im = is.getItemMeta();
			if(im.hasDisplayName()) test.append(",dm=" + im.getDisplayName());
			if(im.hasLore()) {
				test.append(",l=[");
				im.getLore().forEach(lore -> test.append(lore + ";"));
				test.append("]");
			}
			if(is.getType().equals(Material.valueOf("SKULL_ITEM"))) {
				String tex = null;
				try {
					tex = getTexure(is);
				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException
						| IllegalAccessException e1) {
					System.out.println("Error while converting item to string");
					e1.printStackTrace();
				}
				if(tex != null) test.append(",tx=" + tex);
			}
			test.append("]");
		} else {
			test.append("[t=" + is.getType().toString() + ",d=" + is.getData().getData() + "]");
		}
		String t = test.toString();
		t = t.replace(";]", "]");
		return t;
	}
	
	public static String getTexure(ItemStack pet) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		String texture = null;
		if(!pet.getType().name().toLowerCase().contains("skull_item")) return null;
		SkullMeta sm = (SkullMeta) pet.getItemMeta();
		Field profileField = sm.getClass().getDeclaredField("profile");
		profileField.setAccessible(true);
		GameProfile profile = (GameProfile) profileField.get(sm);
		Collection<Property> textures = profile.getProperties().get("textures");
		for(Property p : textures) {
			texture = p.getValue();
		}
		return texture;
	}
	
	public static ItemStack replacePlaceholder(ItemStack is, HashMap<String, String> placeholder) {
		ItemMeta im = is.getItemMeta();
		List<String> lore = new ArrayList<String>();
		for(String str : im.getLore()) {
			for(String place : placeholder.keySet()) {
				if(str.contains(place)) {
					str = str.replace(place, placeholder.get(place));
				}
			}
			lore.add(str);
		}
		im.setLore(lore);
		is.setItemMeta(im);
		return is;
	}
	
	public static ItemStack setLore(ItemStack is, String...str) {
		return setLore(Arrays.asList(str), is);
	}
	
	public static ItemStack setLore(List<String> lore, ItemStack is) {
		ItemMeta im = is.getItemMeta();
		im.setLore(lore);
		is.setItemMeta(im);
		return is;
	}
	
	public static ItemStack removeLine(String hasToContain, ItemStack is) {
		ItemMeta im = is.getItemMeta();
		List<String> newLore = new ArrayList<String>();
		for(String str : im.getLore()) {
			if(!str.contains(hasToContain)) newLore.add(str);
		}
		im.setLore(newLore);
		is.setItemMeta(im);
		return is;
	}
}

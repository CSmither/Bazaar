package de.ancash.bazaar.gui.base;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import de.ancash.bazaar.utils.Chat;

public final class BazaarInventoryClassManager {

	private final Map<Class<?>, Object> instances = new HashMap<>();
	private final Map<Class<?>, Constructor<BazaarInventoryClassManager>> constructors = new HashMap<>();
	
	public final void register(Class<?> clazz) {
		instances.put(clazz.getSuperclass(), newInstance(clazz));
		Chat.sendMessage("Registered " + clazz.getSimpleName() + " for " + clazz.getSuperclass().getSimpleName());
	}
	
	@SuppressWarnings("unchecked")
	public final <T> T get(Class<T> clazz) {
		return (T) instances.get(clazz);
	}
	
	@SuppressWarnings("unchecked")
	private final <T> T newInstance(Class<?> clazz) {
		try {
			if(!constructors.containsKey(clazz)) constructors.put(clazz, (Constructor<BazaarInventoryClassManager>) clazz.getConstructor(this.getClass()));
			return (T) constructors.get(clazz).newInstance(this);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}
}
package de.ancash.bazaar.gui.inventory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public final class BazaarInventoryClassManager {

	private final Map<Class<?>, Object> instances = new HashMap<>();
	
	public final void register(Class<?> clazz) {
		instances.put(clazz.getSuperclass(), newInstance(clazz));
	}
	
	@SuppressWarnings("unchecked")
	public final <T> T get(Class<T> clazz) {
		return (T) instances.get(clazz);
	}
	
	@SuppressWarnings("unchecked")
	private final <T> T newInstance(Class<?> clazz) {
		try {
			return (T) clazz.getConstructor(this.getClass()).newInstance(this);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}
}
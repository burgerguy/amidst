package amidst.clazz.fabric;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.transformer.accesswidener.AccessWidener;
import net.fabricmc.mappings.EntryTriple;
import org.objectweb.asm.commons.Remapper;

import amidst.logging.AmidstLogger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class FixedAWRemapper {
	private final AccessWidener input;
	private final String to;
	private final Remapper remapper;
	
	private static final Method addOrMergeMethod;
	
	static {
		Method m1 = null;
		try {
			m1 = AccessWidener.class.getDeclaredMethod("addOrMerge", Map.class, EntryTriple.class, AccessWidener.Access.class);
			m1.setAccessible(true);
		} catch (NoSuchMethodException | SecurityException e) {
			AmidstLogger.crash(e);
		}
		
		addOrMergeMethod = m1;
	}

	public FixedAWRemapper(AccessWidener input, Remapper remapper, String to) {
		this.input = input;
		this.to = to;
		this.remapper = remapper;
	}

	public AccessWidener remap() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		//Dont remap if we dont need to
		if (input.namespace.equals(to)) {
			return input;
		}

		@SuppressWarnings("deprecation")
		AccessWidener remapped = new AccessWidener(FabricLoader.INSTANCE);
		remapped.namespace = to;

		for (Map.Entry<String, AccessWidener.Access> entry : input.classAccess.entrySet()) {
			remapped.classAccess.put(remapper.map(entry.getKey()), entry.getValue());
		}

		for (Map.Entry<EntryTriple, AccessWidener.Access> entry : input.methodAccess.entrySet()) {
			addOrMergeMethod.invoke(remapped, remapped.methodAccess, remapMethod(entry.getKey()), entry.getValue());
		}

		for (Map.Entry<EntryTriple, AccessWidener.Access> entry : input.fieldAccess.entrySet()) {
			addOrMergeMethod.invoke(remapped, remapped.fieldAccess, remapField(entry.getKey()), entry.getValue());
		}

		return remapped;
	}

	private EntryTriple remapMethod(EntryTriple entryTriple) {
		return new EntryTriple(
					remapper.map(entryTriple.getOwner()),
					remapper.mapMethodName(entryTriple.getOwner(), entryTriple.getName(), entryTriple.getDesc()),
					remapper.mapDesc(entryTriple.getDesc())
				);
	}

	private EntryTriple remapField(EntryTriple entryTriple) {
		return new EntryTriple(
				remapper.map(entryTriple.getOwner()),
				remapper.mapFieldName(entryTriple.getOwner(), entryTriple.getName(), entryTriple.getDesc()),
				remapper.mapDesc(entryTriple.getDesc())
		);
	}
}

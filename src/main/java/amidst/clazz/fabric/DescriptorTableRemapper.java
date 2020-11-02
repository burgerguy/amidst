package amidst.clazz.fabric;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import amidst.logging.AmidstLogger;
import net.fabricmc.loader.util.mappings.MixinIntermediaryDevRemapper;
import net.fabricmc.mapping.tree.TinyTree;
import net.fabricmc.mapping.util.MixinRemapper;
import net.fabricmc.mappings.EntryTriple;

public class DescriptorTableRemapper extends MixinIntermediaryDevRemapper {
	public final Map<String, String> methodNameDescMap = new HashMap<String, String>();

	@SuppressWarnings("unchecked")
	public DescriptorTableRemapper(TinyTree mappings, String from, String to) {
		super(mappings, from, to);

		try {
			Field f1 = MixinRemapper.class.getDeclaredField("methodNames");
			f1.setAccessible(true);
			Map<EntryTriple, String> methodsMap = (Map<EntryTriple, String>) f1.get(this);
			for (Entry<EntryTriple, String> mapEntry : methodsMap.entrySet()) {
				EntryTriple triple = mapEntry.getKey();
				methodNameDescMap.put(triple.getName(), triple.getName() + triple.getDesc());
			}
		} catch (Throwable e) {
			AmidstLogger.crash(e);
		}
	}
}

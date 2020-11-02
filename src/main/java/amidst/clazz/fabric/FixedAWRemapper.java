/*
 * This file is part of fabric-loom, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016, 2017, 2018 FabricMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package amidst.clazz.fabric;

import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.transformer.accesswidener.AccessWidener;
import net.fabricmc.mappings.EntryTriple;
import org.objectweb.asm.commons.Remapper;

import amidst.logging.AmidstLogger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * This class has been modified to work with amidst. For some reason,
 * the original AccessWidenerRemapper puts the name in the owner slot,
 * rather than the actual owner.
 */
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

package org.spongepowered.asm.mixin.refmap;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IRemapper;
import org.spongepowered.asm.mixin.injection.struct.MemberInfo;
import org.spongepowered.asm.obfuscation.RemapperChain;

import amidst.clazz.fabric.NameDescriptorRemapper;
import amidst.logging.AmidstLogger;

/**
* This adapter is designed to apply the same remapping used elsewhere in
* the development chain (RemapperChain) to reference maps.
*/
public final class RemappingReferenceMapper implements IClassReferenceMapper, IReferenceMapper {
   /**
    * Logger
    */
   private static final Logger logger = LogManager.getLogger("mixin");
   
   /**
    * The "inner" refmap, this is the original refmap specified in the config
    */
   private final IReferenceMapper refMap;
   
   /**
    * The remapper in use.
    */
   private final IRemapper remapper;

   /**
    * A map between reference mapper values and their remapped equivalents.
    */
   private final Map<String, String> mappedReferenceCache = new HashMap<String, String>();
   
   private NameDescriptorRemapper descRemapper;

   private RemappingReferenceMapper(MixinEnvironment env, IReferenceMapper refMap) {
       this.refMap = refMap;
       this.remapper = env.getRemappers();
       
		try {
			Field f1 = RemapperChain.class.getDeclaredField("remappers");
			f1.setAccessible(true);
			@SuppressWarnings("unchecked")
			List<IRemapper> remappers = (List<IRemapper>) f1.get(remapper);
			descRemapper = (NameDescriptorRemapper) remappers.get(0);
		} catch (Throwable e) {
			AmidstLogger.crash(e);
		}
       
       RemappingReferenceMapper.logger.info("Remapping refMap {} using remapper chain", refMap.getResourceName());
   }

   /* (non-Javadoc)
    * @see org.spongepowered.asm.mixin.refmap.IReferenceMapper#isDefault()
    */
   @Override
   public boolean isDefault() {
       return this.refMap.isDefault();
   }

   /* (non-Javadoc)
    * @see org.spongepowered.asm.mixin.refmap.IReferenceMapper
    *      #getResourceName()
    */
   @Override
   public String getResourceName() {
       return this.refMap.getResourceName();
   }

   /* (non-Javadoc)
    * @see org.spongepowered.asm.mixin.refmap.IReferenceMapper#getStatus()
    */
   @Override
   public String getStatus() {
       return this.refMap.getStatus();
   }

   /* (non-Javadoc)
    * @see org.spongepowered.asm.mixin.refmap.IReferenceMapper#getContext()
    */
   @Override
   public String getContext() {
       return this.refMap.getContext();
   }

   /* (non-Javadoc)
    * @see org.spongepowered.asm.mixin.refmap.IReferenceMapper#setContext(
    *      java.lang.String)
    */
   @Override
   public void setContext(String context) {
       this.refMap.setContext(context);
   }

   /* (non-Javadoc)
    * @see org.spongepowered.asm.mixin.refmap.IReferenceMapper#remap(
    *      java.lang.String, java.lang.String)
    */
   @Override
   public String remap(String className, String reference) {
       return this.remapWithContext(getContext(), className, reference);
   }

   private static String remapMethodDescriptor(IRemapper remapper, String desc) {
       StringBuilder newDesc = new StringBuilder();
       newDesc.append('(');
       for (Type arg : Type.getArgumentTypes(desc)) {
           newDesc.append(remapper.mapDesc(arg.getDescriptor()));
       }
       return newDesc.append(')').append(remapper.mapDesc(Type.getReturnType(desc).getDescriptor())).toString();
   }

   /* (non-Javadoc)
    * @see org.spongepowered.asm.mixin.refmap.IReferenceMapper
    *      #remapWithContext(java.lang.String, java.lang.String,
    *      java.lang.String)
    */
   @Override
   public String remapWithContext(String context, String className, String reference) {
       if (reference.isEmpty()) {
           return reference;
       }

       String origInfoString = this.refMap.remapWithContext(context, className, reference);
       String remappedCached = mappedReferenceCache.get(origInfoString);
       if (remappedCached != null) {
           return remappedCached;
       } else {
           String remapped = origInfoString;

           // To handle propagation, find super/itf-class (for IRemapper)
           // but pass the requested class in the MemberInfo
           MemberInfo info = MemberInfo.parse(remapped, null, null);
           if (info.getName() == null && info.getDesc() == null) {
               return info.getOwner() != null ? new MemberInfo(remapper.map(info.getOwner()), null, null).toString() : info.toString();
           } else if (info.isField()) {
               remapped = new MemberInfo(
                       remapper.mapFieldName(info.getOwner(), info.getName(), info.getDesc()),
                       info.getOwner() == null ? null : remapper.map(info.getOwner()),
                       info.getDesc() == null ? null : remapper.mapDesc(info.getDesc())
               ).toString();
           } else {
        	   if (info.getDesc() == null && info.getName() != null && !remapped.equals("<init>") && !remapped.equals("<clinit>")) {
        		   String withDesc = descRemapper.methodNameDescMap.get(remapped);
        		   if (withDesc != null && !withDesc.equals(remapped)) {
	        		   MemberInfo newInfo = MemberInfo.parse(withDesc, null, null);
	        		   remapped = new MemberInfo(
		                       remapper.mapMethodName(newInfo.getOwner(), newInfo.getName(), newInfo.getDesc()),
		                       newInfo.getOwner() == null ? null : remapper.map(newInfo.getOwner()),
		                       newInfo.getDesc() == null ? null : remapMethodDescriptor(remapper, newInfo.getDesc())
		               ).toString();
	        		   
	                   mappedReferenceCache.put(origInfoString, remapped);
	                   return remapped;
        		   }
        	   }
               remapped = new MemberInfo(
                       remapper.mapMethodName(info.getOwner(), info.getName(), info.getDesc()),
                       info.getOwner() == null ? null : remapper.map(info.getOwner()),
                       info.getDesc() == null ? null : remapMethodDescriptor(remapper, info.getDesc())
               ).toString();
           }

           mappedReferenceCache.put(origInfoString, remapped);
           return remapped;
       }
   }
   
   /**
    * Wrap the specified refmap in a remapping adapter using settings in the
    * supplied environment
    * 
    * @param env environment to read configuration from
    * @param refMap refmap to wrap
    * @return wrapped refmap or original refmap is srg data is not available
    */
   public static IReferenceMapper of(MixinEnvironment env, IReferenceMapper refMap) {
       if (!refMap.isDefault()) {
           return new RemappingReferenceMapper(env, refMap);
       }
       return refMap;
   }

   @Override
   public String remapClassName(String className, String inputClassName) {
       return remapClassNameWithContext(getContext(), className, inputClassName);
   }

   @Override
   public String remapClassNameWithContext(String context, String className, String remapped) {
       String origInfoString;
       if (this.refMap instanceof IClassReferenceMapper) {
           origInfoString = ((IClassReferenceMapper) this.refMap).remapClassNameWithContext(context, className, remapped);
       } else {
           origInfoString = this.refMap.remapWithContext(context, className, remapped);
       }
       return remapper.map(origInfoString.replace('.', '/'));
   }
}

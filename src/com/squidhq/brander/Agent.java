package com.squidhq.brander;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class Agent implements ClassFileTransformer {

	public static void premain(String agentArgs, Instrumentation instrumentation) {
		instrumentation.addTransformer(new Agent());
	}

	public byte[] transform(ClassLoader classLoader, String className, Class<?> classBeingTransformed, ProtectionDomain protectionDomain, byte[] bytes) throws IllegalClassFormatException {
		if (className == null) {
			return null;
		}
		className = className.replace("/", ".");
		if (!className.equals("net.minecraft.client.ClientBrandRetriever")) {
			return null;	
		}
		try {
			ClassPool pool = ClassPool.getDefault();
			pool.appendClassPath(new ByteArrayClassPath(className, bytes));

			CtClass ctClass = pool.get(className);
			CtMethod getClientModName = ctClass.getMethod("getClientModName", "()Ljava/lang/String;");
			CtMethod getClientModNameZ = CtNewMethod.copy(getClientModName, ctClass, null);
			getClientModNameZ.setName("getClientModNameZ");
			ctClass.addMethod(getClientModNameZ);
			getClientModName.setBody("{ return getClientModNameZ() + \",squidhq\"; }");

			System.out.println("SquidHQ: Branded");
			return ctClass.toBytecode();
		} catch(Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}

}

package org.unique.commons.io.read.impl;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unique.commons.io.read.AbstractClassReader;
import org.unique.commons.io.read.ClassReader;
import org.unique.commons.io.read.ClassReaderException;
import org.unique.commons.utils.CollectionUtil;
import org.unique.commons.utils.Validate;

public class JarReaderImpl extends AbstractClassReader implements ClassReader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JarReaderImpl.class);
	
	@Override
	public Set<Class<?>> getClass(String packageName, boolean recursive) {
		return this.getClassByAnnotation(packageName, null, null, recursive);
	}

	@Override
	public Set<Class<?>> getClass(String packageName, Class<?> parent, boolean recursive) {
		return this.getClassByAnnotation(packageName, parent, null, recursive);
	}

	@Override
	public Set<Class<?>> getClassByAnnotation(String packageName, Class<? extends Annotation> annotation, boolean recursive) {
		return this.getClassByAnnotation(packageName, null, annotation, recursive);
	}

	@Override
	public Set<Class<?>> getClassByAnnotation(String packageName, Class<?> parent, Class<? extends Annotation> annotation, boolean recursive) {
		Validate.notBlank(packageName);
		Set<Class<?>> classes = CollectionUtil.newHashSet();
        // 获取包的名字 并进行替换
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的URL
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
				Set<Class<?>> subClasses = this.getClasses(url, packageDirName, packageName, parent, annotation, recursive, classes);
				if(subClasses.size() > 0){
					classes.addAll(subClasses);
				}
            }
        } catch (IOException e) {
        	LOGGER.error(e.getMessage());
        }
        return classes;
	}
	
	private Set<Class<?>> getClasses(final URL url, final String packageDirName, String packageName, final Class<?> parent, 
			final Class<? extends Annotation> annotation, final boolean recursive, Set<Class<?>> classes){
		JarFile jar = null;
		try {
			// 获取jar
			jar = ((JarURLConnection) url.openConnection()).getJarFile();
			// 从此jar包 得到一个枚举类
			Enumeration<JarEntry> entries = jar.entries();
			// 同样的进行循环迭代
			while (entries.hasMoreElements()) {
				// 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
				JarEntry entry = entries.nextElement();
				String name = entry.getName();
				// 如果是以/开头的
				if (name.charAt(0) == '/') {
					// 获取后面的字符串
					name = name.substring(1);
				}
				// 如果前半部分和定义的包名相同
				if (name.startsWith(packageDirName)) {
					int idx = name.lastIndexOf('/');
					// 如果以"/"结尾 是一个包
					if (idx != -1) {
						// 获取包名 把"/"替换成"."
						packageName = name.substring(0, idx).replace('/', '.');
					}
					// 如果可以迭代下去 并且是一个包
					if ((idx != -1) || recursive) {
						// 如果是一个.class文件 而且不是目录
						if (name.endsWith(".class") && !entry.isDirectory()) {
							// 去掉后面的".class" 获取真正的类名
							String className = name.substring(packageName.length() + 1, name.length() - 6);
							try {
								// 添加到classes
								Class<?> clazz = Class.forName(packageName + '.' + className);
								if(null != parent && null != annotation){
		                    		if(null != clazz.getSuperclass() && 
		                    			clazz.getSuperclass().equals(parent) && null != clazz.getAnnotation(annotation)){
		                    			classes.add(clazz);
		                    		}
		                    		continue;
		                    	}
		                    	if(null != parent){
		                    		if(null != clazz.getSuperclass() && clazz.getSuperclass().equals(parent)){
		                    			classes.add(clazz);
		                    		}
		                    		continue;
		                    	}
		                    	if(null != annotation){
		                    		if(null != clazz.getAnnotation(annotation)){
		                    			classes.add(clazz);
		                    		}
		                    		continue;
		                    	}
		                        classes.add(clazz);
							} catch (ClassNotFoundException e) {
								LOGGER.error("添加用户自定义视图类错误 找不到此类的.class文件");
								throw new ClassReaderException(e);
							}
						}
					}
				}
			}
		} catch (IOException e) {
			LOGGER.error("在扫描用户定义视图时从jar包获取文件出错：{}", e.getMessage());
		}
		return classes;
	}
}

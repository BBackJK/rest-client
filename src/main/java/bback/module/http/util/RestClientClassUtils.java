package bback.module.http.util;

import org.springframework.util.ClassUtils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.*;

public final class RestClientClassUtils {

    private RestClientClassUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static final Map<Class<?>, Object> PRIMITIVE_INIT_VALUE_MAP = new IdentityHashMap<>(9);

    private static final char PACKAGE_SEPARATOR = '.';
    private static final char FILE_SEPARATOR = '/';
    private static final String FILE_CLASS = ".class";

    static {
        PRIMITIVE_INIT_VALUE_MAP.put(boolean.class, false);
        PRIMITIVE_INIT_VALUE_MAP.put(byte.class, (byte)0);
        PRIMITIVE_INIT_VALUE_MAP.put(char.class, '\u0000');
        PRIMITIVE_INIT_VALUE_MAP.put(double.class, 0.0d);
        PRIMITIVE_INIT_VALUE_MAP.put(float.class, 0.0);
        PRIMITIVE_INIT_VALUE_MAP.put(int.class, 0);
        PRIMITIVE_INIT_VALUE_MAP.put(long.class, 0L);
        PRIMITIVE_INIT_VALUE_MAP.put(short.class, (short)0);
        PRIMITIVE_INIT_VALUE_MAP.put(void.class, null);
    }

    public static Set<Class<?>> scanningClassByAnnotation(String packageName, Class<? extends Annotation> annotationClazz) throws IOException, ClassNotFoundException {
        String resourcePath = packageName.replace(PACKAGE_SEPARATOR, FILE_SEPARATOR);
        List<File> files = getAllResourceFile(resourcePath);

        Set<Class<?>> classes = new LinkedHashSet<>();
        int fileCount = files.size();
        for (int i=0; i<fileCount; i++) {
            File file = files.get(i);
            if ( file.isDirectory() ) {
                classes.addAll(findClassesByFile(file, packageName, annotationClazz));
            }
        }

        return classes;
    }

    public static ClassLoader[] getClassLoaders() {
        return new ClassLoader[] {
                Thread.currentThread().getContextClassLoader()
                , ClassLoader.getSystemClassLoader()
        };
    }

    public static Object getTypeInitValue(Class<?> clazz) {
        if (!clazz.isPrimitive()) return null;
        return PRIMITIVE_INIT_VALUE_MAP.get(clazz);
    }

    public static boolean isPrimitiveOrString(Class<?> clazz) {
        return clazz != null && (ClassUtils.isPrimitiveOrWrapper(clazz) || String.class.equals(clazz));
    }

    public static Class<?> classForName(String name, ClassLoader[] classLoaders) throws ClassNotFoundException {
        for (ClassLoader cl : classLoaders) {
            if (null != cl) {
                try {
                    return Class.forName(name, true, cl);
                } catch (ClassNotFoundException e) {
                    // ignore
                }
            }
        }
        throw new ClassNotFoundException("Cannot find class: " + name);
    }

    private static List<File> getAllResourceFile(String resourcePath) throws IOException {
        List<File> files = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(resourcePath);
        while ( resources.hasMoreElements() ) {
            files.add(new File(resources.nextElement().getFile()));
        }
        return files;
    }

    private static Set<Class<?>> findClassesByFile(File dir, String packageName, Class<? extends Annotation> annotationClazz) throws ClassNotFoundException {
        Set<Class<?>> classes = new LinkedHashSet<>();
        if (!dir.exists()) {
            return classes;
        }

        File[] files = dir.listFiles();
        if ( files == null ) {
            return classes;
        }

        int fileCount = files.length;
        for (int i=0; i<fileCount; i++) {
            File file = files[i];
            String fileName = file.getName();
            if ( file.isDirectory() ) {
                classes.addAll(findClassesByFile(file, packageName + PACKAGE_SEPARATOR + fileName, annotationClazz));
            } else if ( isClassByFileName(fileName) ) {
                String className = packageName + PACKAGE_SEPARATOR + fileName.substring(0, fileName.length() - 6);
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                Class<?> clazz = Class.forName(className, false, classLoader);
                Annotation annotation = clazz.getAnnotation(annotationClazz);
                if ( annotation != null ) {
                    classes.add(clazz);
                }
            }
        }

        return classes;
    }


    private static boolean isClassByFileName(String fileName) {
        return fileName != null && fileName.endsWith(FILE_CLASS);
    }
}

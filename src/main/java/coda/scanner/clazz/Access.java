package coda.scanner.clazz;

import org.objectweb.asm.Opcodes;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class Access {
    public enum Visibility {
        Default,
        Public,
        Private,
        Protected;

        public String id() {
            return name().toLowerCase();
        }
    }

    public static Visibility visibility(int access) {
        if (isPublic(access))
            return Visibility.Public;
        else if (isPrivate(access))
            return Visibility.Private;
        else if (isProtected(access))
            return Visibility.Protected;
        else
            return Visibility.Default;
    }

    public static boolean isPublic(int access) {
        return (access & Opcodes.ACC_PUBLIC) != 0;
    }

    public static boolean isPrivate(int access) {
        return (access & Opcodes.ACC_PRIVATE) != 0;
    }

    public static boolean isProtected(int access) {
        return (access & Opcodes.ACC_PROTECTED) != 0;
    }

    public static boolean isFinal(int access) {
        return (access & Opcodes.ACC_FINAL) != 0;
    }

    public static boolean isAbstract(int access) {
        return (access & Opcodes.ACC_ABSTRACT) != 0;
    }

    public static boolean isStatic(int access) {
        return (access & Opcodes.ACC_STATIC) != 0;
    }
}

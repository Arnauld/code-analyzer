package coda.scanner.clazz;

import coda.util.Files;
import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class ClazzScanner {

    private final Files files = new Files(ClazzScanner::isClassFile);
    private final DeclarationFactory declarationFactory;

    public ClazzScanner(DeclarationFactory declarationFactory) {
        this.declarationFactory = declarationFactory;
    }


    private static boolean isClassFile(File file) {
        return file.getName().endsWith(".class");
    }

    public ClazzScanner appendSourceFile(File sourceFile) {
        files.addSource(sourceFile);
        return this;
    }

    public ClazzScanner appendSourceTree(File sourceTree) {
        files.addSourceTree(sourceTree);
        return this;
    }

    public void process() {
        files.traverse(this::scan);
    }

    private void scan(File file) {
        try {
            try (InputStream stream = new FileInputStream(file)) {
                ClassReader reader = new ClassReader(stream);
                reader.accept(new ClassScanner(declarationFactory), Opcodes.ASM5);
            }
        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    public static class ClassScanner extends ClassVisitor {
        private final DeclarationFactory declarationFactory;
        // current
        private ClassDeclaration classDeclaration;

        public ClassScanner(DeclarationFactory declarationFactory) {
            super(Opcodes.ASM5);
            this.declarationFactory = declarationFactory;
        }

        @Override
        public void visit(int version, int access, String name, String signature,
                          String superName, String[] interfaces) {
            if (classDeclaration == null) {
                Type objectType = Type.getObjectType(name);
                classDeclaration = declarationFactory.createClassInfo(objectType.getClassName(), access);
            }

            if (superName != null) {
                Type objectType = Type.getObjectType(superName);
                classDeclaration.setSuperClass(objectType.getClassName());
            }

            if (interfaces != null) {
                for (String interfaceName : interfaces) {
                    Type objectType = Type.getObjectType(interfaceName);
                    classDeclaration.addInterface(objectType.getClassName());
                }
            }
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                                         String signature, String[] exceptions) {
            Collection<String> params = getParamTypes(desc);
            String returnType = getReturnType(desc);

            List<String> exceptionList = notNull(exceptions)
                    .map(Type::getObjectType)
                    .map(Type::getClassName)
                    .collect(Collectors.toList());

            MethodDeclaration methodDeclaration = declarationFactory.createMethodInfo(access, name, params, returnType, exceptionList);
            classDeclaration.addMethod(methodDeclaration);
            return new MethodScanner(classDeclaration, methodDeclaration);
        }

        private static <T> Stream<T> notNull(T[] values) {
            if (values == null)
                return Stream.empty();
            else
                return Stream.of(values);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            Set<String> types = new HashSet<>();
            if (signature != null) {
                SignatureScanner sig = new SignatureScanner();
                new SignatureReader(signature).accept(sig);
                types.addAll(sig.collectedTypeNames());
            }

            String typeName = Type.getType(desc).getClassName();
            FieldDeclaration fieldDeclaration = declarationFactory.createFieldInfo(access, name, typeName);
            fieldDeclaration.declareTypes(types);

            classDeclaration.addField(fieldDeclaration);
            return new FieldScanner(fieldDeclaration, declarationFactory);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visibleAtRuntime) {
            String typeName = Type.getType(desc).getClassName();
            AnnotationDeclaration annotationDeclaration = declarationFactory.createAnnotationInfo(typeName, visibleAtRuntime);
            classDeclaration.addAnnotation(annotationDeclaration);
            return new AnnotationScanner(annotationDeclaration, declarationFactory);
        }

        private Collection<String> getParamTypes(final String desc) {
            Collection<String> params = new ArrayList<>();
            for (Type paramType : Type.getArgumentTypes(desc)) {
                params.add(paramType.getClassName());
            }
            return params;
        }

        private String getReturnType(final String desc) {
            final Type returnType = Type.getReturnType(desc);
            return returnType.getClassName();
        }
    }

    public static class SignatureScanner extends SignatureVisitor {
        private List<String> typeNames = new ArrayList<>();

        public SignatureScanner() {
            super(Opcodes.ASM5);
        }

        @Override
        public void visitFormalTypeParameter(String name) {
            String className = Type.getObjectType(name).getClassName();
            typeNames.add(className);
        }

        @Override
        public void visitTypeVariable(String name) {
            String className = Type.getObjectType(name).getClassName();
            typeNames.add(className);
        }

        @Override
        public void visitClassType(String name) {
            String className = Type.getObjectType(name).getClassName();
            typeNames.add(className);
        }

        @Override
        public void visitInnerClassType(String name) {
            String className = Type.getObjectType(name).getClassName();
            typeNames.add(className);
        }

        public Collection<? extends String> collectedTypeNames() {
            return typeNames;
        }
    }

    public static class MethodScanner extends MethodVisitor {

        private final ClassDeclaration owningClass;
        private final MethodDeclaration methodDeclaration;

        public MethodScanner(ClassDeclaration owningClass, MethodDeclaration methodDeclaration) {
            super(Opcodes.ASM5);
            this.owningClass = owningClass;
            this.methodDeclaration = methodDeclaration;
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            Type objectType = Type.getObjectType(type);
            methodDeclaration.declareBodyDependency(classNameOf(objectType));
            super.visitTypeInsn(opcode, type);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            Type objectType = Type.getObjectType(owner);
            String typeName = classNameOf(objectType);
            if (!owningClass.getName().equals(typeName))
                methodDeclaration.declareBodyDependency(typeName);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            Type objectType = Type.getObjectType(owner);
            methodDeclaration.declareBodyDependency(classNameOf(objectType));
        }

        private String classNameOf(Type type) {
            if (type.getSort() == Type.ARRAY)
                return type.getElementType().getClassName();
            else
                return type.getClassName();
        }
    }

    public static class AnnotationScanner extends AnnotationVisitor {
        private final AnnotationDeclaration annotationDeclaration;
        private final DeclarationFactory declarationFactory;

        public AnnotationScanner(AnnotationDeclaration annotationDeclaration, DeclarationFactory declarationFactory) {
            super(Opcodes.ASM5);
            this.annotationDeclaration = annotationDeclaration;
            this.declarationFactory = declarationFactory;
        }

        @Override
        public void visit(String name, Object value) {
            if (value instanceof String)
                annotationDeclaration.declareDependency(String.class.getName());
            else if (value instanceof Type)
                annotationDeclaration.declareDependency(((Type) value).getClassName());
        }

        @Override
        public void visitEnum(String name, String desc, String value) {
            String typeName = Type.getType(desc).getClassName();
            annotationDeclaration.declareDependency(typeName);
        }

        @Override
        public AnnotationVisitor visitArray(String name) {
            return new AnnotationScanner(annotationDeclaration, declarationFactory);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, String desc) {
            String typeName = Type.getType(desc).getClassName();
            AnnotationDeclaration nested = declarationFactory.createAnnotationInfo(typeName, annotationDeclaration.visibleAtRuntime());

            annotationDeclaration.declareDependency(typeName);
            annotationDeclaration.addAnnotation(nested);
            return new AnnotationScanner(nested, declarationFactory);
        }
    }

    public static class FieldScanner extends FieldVisitor {
        private final FieldDeclaration fieldDeclaration;
        private final DeclarationFactory declarationFactory;

        public FieldScanner(FieldDeclaration fieldDeclaration, DeclarationFactory declarationFactory) {
            super(Opcodes.ASM5);
            this.fieldDeclaration = fieldDeclaration;
            this.declarationFactory = declarationFactory;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visibleAtRuntime) {
            String typeName = Type.getType(desc).getClassName();
            AnnotationDeclaration annotationDeclaration = declarationFactory.createAnnotationInfo(typeName, visibleAtRuntime);
            fieldDeclaration.addAnnotation(annotationDeclaration);
            return new AnnotationScanner(annotationDeclaration, declarationFactory);
        }
    }

}

package com.github.fluorumlabs.antipatterns;

import com.github.fluorumlabs.antipatterns.annotations.Constructor;
import com.github.fluorumlabs.antipatterns.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper methods for dealing with classes and instances
 */
public final class Classes {
    private Classes() {
    } // not instantiable

    /**
     * Perform safe type casting
     * <p>
     * Usage example:
     * {@code safeCast(x, String.class).ifPresent(...);}
     *
     * @param object object to cast
     * @param clazz  class
     * @param <T>    expected type
     * @return Optional of casted object if it is instance of clazz, empty otherwise
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> safeCast(@Nonnull Object object, @Nonnull Class<T> clazz) {
        Validate.notNull(object, "object must not be null");
        Validate.notNull(clazz, "clazz must not be null");
        if (clazz.isAssignableFrom(object.getClass())) {
            return Optional.of((T) object);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Safe type casting for Optional/Stream
     * <p>
     * Usage example:
     * {@code Optional.of(x).map(AntiPatterns.safeCast(String.class)).ifPresent(...);}
     * <p>
     * Anti-antipattern hint: for unsafe cast use `Class.class::cast`:
     * {@code Optional.of(x).map(String.class::cast).ifPresent(...);}
     *
     * @param clazz class
     * @param <O>   object type
     * @param <T>   expected type
     * @return function that safely maps from O to T
     */
    public static <O, T> Function<O, T> safeCast(@Nonnull Class<T> clazz) {
        Validate.notNull(clazz, "clazz must not be null");
        return new SafeCaster<>(clazz);
    }

    /*
     * DISCLAIMER
     *
     * USE THE HELPER METHODS BELOW AT YOUR OWN RISK
     *
     * THEY CAN BE EASILY USED TO FUCK UP THE WHOLE OOP APPROACH
     *
     * OR TO SHOOT OWN LEG
     *
     * LIKE, REDEFINE Boolean.FALSE TO BE TRUE
     *
     * SHOULD YOU ENCOUNTER ANY PROBLEMS USING IT, SEE FIGURE 1 BELOW
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     * 		---------------------------
     *		!            -            !
     *		!           { }           !
     *		!           | |           !
     *		!           | |           !
     *		!        .-.! !.-.        !
     *		!      .-!  ! !  !.-.     !
     *		!      ! !       !  ;     !
     *		!      \           ;      !
     *		!       \         ;       !
     *		!        !       :        !
     *		!        !       |        !
     *		!        |       |        !
     *		!                         !
     *		---------------------------
     *			     Figure 1.
     *
     */

    /**
     * Create a new proxy instance, mirroring static/virtual members of class, specified in `@TargetClass` annotation.
     *
     * @param mirrorInterface Interface having `@TargetClass` annotation
     * @param instance        Instance of target class to attach to
     * @param <T>
     * @return Proxy instance
     */
    public static <T> T attach(@Nonnull Class<?> mirrorInterface, @Nonnull Object instance) {
        Validate.notNull(mirrorInterface, "mirrorInterface must not be null");
        Validate.notNull(instance, "instance must not be null");

        return attachMirror(mirrorInterface, instance);
    }

    /**
     * Create a new proxy instance, mirroring static members of class, specified in `@TargetClass` annotation.
     *
     * @param mirrorInterface Interface having `@TargetClass` annotation
     * @param <T>
     * @return Proxy instance
     */
    public static <T> T attachStatic(@Nonnull Class<T> mirrorInterface) {
        Validate.notNull(mirrorInterface, "mirrorInterface must not be null");

        return attachMirror(mirrorInterface, null);
    }

    /**
     * Create a shallow clone of instance. That is -- a new instance with exact same values in its fields
     *
     * @param instance object to clone
     * @param <T>
     * @return new instance of object
     */
    @SuppressWarnings("unchecked")
    public static <T> T shallowClone(@Nonnull T instance) {
        Validate.notNull(instance, "instance must not be null");
        return (T) upgrade(instance, instance.getClass());
    }

    /**
     * Upgrade instance to a subclass of it. The instance of target class is created,
     * holding the same values as the original instance.
     * <p>
     * No constructors are called.
     * <p>
     * If instance.getClass() and target are the same class, the method will produce
     * a shallow clone of instance.
     * <p>
     * This can be used to extend some arbitrary instance with additional or overriden methods
     *
     * @param instance instance to upgrade
     * @param target   target class. Instance must be super class for target.
     * @param <T>
     * @return new instance
     */
    public static <T> T upgrade(@Nonnull Object instance, @Nonnull Class<T> target) {
        Validate.notNull(instance, "instance must not be null");
        Validate.notNull(target, "target must not be null");
        Validate.isAssignableFrom(instance.getClass(), target, "target must extend instance.getClass()");
        Validate.isTrue(target.getDeclaredConstructors().length > 0, "target must be a concrete class");

        return upgradeIndirect(instance, target);
    }

    /**
     * Upgrade instance to an arbitrary class. The instance of target class is created,
     * holding the field values of the original instance, where the field names are equal
     * and types are assignable.
     * <p>
     * No constructors are called.
     *
     * @param instance instance to upgrade
     * @param target   target class
     * @param <T>
     * @return new instance
     */
    public static <T> T upgradeIndirect(@Nonnull Object instance, @Nonnull Class<T> target) {
        Validate.notNull(instance, "instance must not be null");
        Validate.notNull(target, "target must not be null");
        Validate.isTrue(target.getDeclaredConstructors().length > 0, "target must be a concrete class");

        return upgradeIndirect(instance, target, Collections.emptyMap());
    }

    /**
     * Upgrade instance to an arbitrary class. The instance of target class is created,
     * holding the field values of the original instance, where the remapped field names are equal
     * and types are assignable.
     * <p>
     * No constructors are called.
     *
     * @param instance     instance to upgrade
     * @param target       target class
     * @param fieldMapping field remapping
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T upgradeIndirect(@Nonnull Object instance, @Nonnull Class<T> target, @Nonnull Map<String, String> fieldMapping) {
        Validate.notNull(instance, "instance must not be null");
        Validate.notNull(target, "target must not be null");
        Validate.isTrue(target.getDeclaredConstructors().length > 0, "target must be a concrete class");

        T newInstance;

        // Get first constructor (don't care which one).
        // This is needed to get otherwise not accessible DirectMethodHandle.Constructor class,
        // and, therefore, call allocate instances without direct calls to sun.misc.Unsafe
        try {
            MethodHandle anyConstructor = lookupAll().unreflectConstructor(Optionals.getFirst(target.getDeclaredConstructors()).orElseThrow(IllegalStateException::new));

            // Create a new empty instance of target class
            newInstance = (T) lookupAll().findStatic(anyConstructor.getClass(), "allocateInstance", MethodType.methodType(Object.class, Object.class)).invoke(anyConstructor);
        } catch (Throwable e) {
            throw new IllegalStateException(String.format("Cannot upgrade instance of %s to %s", instance.getClass().getSimpleName(), target.getSimpleName()), e);
        }

        // All non-static fields
        Map<String, Class<?>> fields = getFields(instance.getClass());
        Map<String, Class<?>> targetFields = getFields(target);

        for (Map.Entry<String, Class<?>> member : fields.entrySet()) {
            String newFieldName = fieldMapping.getOrDefault(member.getKey(), member.getKey());

            Class<?> sourceType = member.getValue();
            Class<?> targetType = targetFields.get(newFieldName);

            if (targetType != null) {
                // Check if types are assignable
                if (!targetType.isAssignableFrom(sourceType)) {
                    throw new IllegalArgumentException(String.format("Cannot upgrade instance of %s to %s: field %s: %s is not assignable to %s", instance.getClass().getSimpleName(), target.getSimpleName(), member.getKey(), sourceType.getSimpleName(), targetType.getSimpleName()));
                }

                // Copy value using MethodHandlers
                try {
                    Object value = lookupAll().findGetter(instance.getClass(), member.getKey(), member.getValue()).bindTo(instance).invoke();
                    lookupAll().findSetter(newInstance.getClass(), newFieldName, targetType).bindTo(newInstance).invoke(value);
                } catch (Throwable e) {
                    throw new IllegalStateException(String.format("Cannot upgrade instance of %s to %s", instance.getClass().getSimpleName(), target.getSimpleName()), e);
                }
            }
        }
        return newInstance;
    }

    /**
     * Functional interface capabale of getting argument name
     *
     * @param <T>
     */
    public interface NamedValue<T> extends Serializable, MethodFinder, Function<String, T> {
        default String name() {
            String name = lastParameter().getName();
            if (name.startsWith("arg")) {
                throw new UnsupportedOperationException("You need to compile with javac -parameters for parameter reflection to work; You also need java 8u60 or newer to use it with lambdas");
            }
            return name;
        }

        default T value(String name) {
            return apply(name);
        }
    }

    /**
     * Interface providing target instance for use with {@link Classes#attach(Class, Object)}.
     * <p>
     * When this interface is used as a super, {@link TargetClass} annotation is not needed.
     *
     * @param <T> target class
     */
    public interface Attachable<T> {
        /**
         * Access instance of underlying target class
         *
         * @return target instance
         */
        T instance();
    }

    /**
     * Create a mirror proxy
     *
     * @param iface    Mirror interface
     * @param instance Object instance. Can be null when only static methods are needed.
     * @param <T>
     * @return Mirror instance
     */
    @SuppressWarnings("unchecked")
    private static <T> T attachMirror(@Nonnull Class<?> iface, @Nullable Object instance) {
        Class<?> targetClass = Optionals.trySequentially(
                () -> Optional.ofNullable(iface.getAnnotation(TargetClass.class)).map(TargetClass::value),
                () -> getTargetClass(iface))
                .orElse(null);

        if (targetClass == null && iface.getAnnotation(TargetClass.Instance.class) != null) {
            Validate.notNull(instance, "instance must not be null");
            targetClass = instance.getClass();
        }

        Validate.notNull(targetClass, "Interface must be annotated with @TargetClass, @TargetClass.Instance or extend Attachable");

        if (instance != null) {
            Validate.isInstanceOf(targetClass, instance, "Object must be instance of %s specified in @TargetClass", targetClass.getName());
        }

        InvocationHandler mirrorInvocationHandler = new MirrorInvocationHandler(iface, targetClass, instance);

        return (T) Proxy.newProxyInstance(iface.getClassLoader(),
                new Class[]{iface},
                mirrorInvocationHandler);
    }

    /**
     * Extract target class from Attachable super interface
     *
     * @param iface interface extending Attachable
     * @return Type parameter of Attachable
     */
    private static Optional<Class<?>> getTargetClass(@Nonnull Class<?> iface) {
        return Stream.of(iface.getGenericInterfaces())
                .filter(type -> type instanceof ParameterizedType)
                .filter(type -> ((ParameterizedType) type).getRawType() == Attachable.class)
                .findAny()
                .map(safeCast(ParameterizedType.class))
                .flatMap(type -> Optionals.getFirst(type.getActualTypeArguments()))
                .map(type -> {
                    if (type instanceof Class) {
                        return (Class<?>) type;
                    } else if (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType() instanceof Class) {
                        return (Class<?>) ((ParameterizedType) type).getRawType();
                    } else {
                        return Object.class;
                    }
                });
    }

    /**
     * Invocation handler for mirror proxy classes
     */
    private static final class MirrorInvocationHandler implements InvocationHandler {
        private final Map<Method, MethodHandle> invocationMap;
        private final Class<?> targetClass;
        private final Class<?> interfaceClass;
        private final Object instance;

        private MirrorInvocationHandler(@Nonnull Class<?> mirrorInterface, @Nonnull Class<?> target, @Nullable Object targetInstance) {
            Validate.notNull(mirrorInterface, "mirrorInterface must not be null");
            Validate.notNull(target, "target must not be null");

            targetClass = target;
            instance = targetInstance;
            interfaceClass = mirrorInterface;

            invocationMap = new HashMap<>(mirrorInterface.getDeclaredMethods().length);

            for (Method method : mirrorInterface.getMethods()) {
                if (method.isDefault()) {
                    // Special handling for default methods
                    try {
                        invocationMap.put(method, lookupAll().unreflectSpecial(method, method.getDeclaringClass()));
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("Unable to get MethodHandle for default method", e);
                    }
                } else if (Attachable.class.isAssignableFrom(interfaceClass) && "instance".equals(method.getName())) {
                    // Skip special `instance` method of Attachable
                } else if (!Modifier.isStatic(method.getModifiers())) { // <- Skip static methods
                    boolean isDirectField = method.getAnnotation(DirectField.class) != null;
                    boolean isStatic = method.getAnnotation(Static.class) != null;
                    boolean isConstructor = method.getAnnotation(Constructor.class) != null;
                    ReturnType returnTypeAnnotation = method.getAnnotation(ReturnType.class);
                    Super superAnnotation = method.getAnnotation(Super.class);

                    MethodHandle handle;
                    MethodType signature;

                    if (!isStatic && !isConstructor && targetInstance == null) {
                        throw new IllegalArgumentException(String.format("Cannot mirror %s: Method is not @Static, but no instance is provided", printMethod(method)));
                    }

                    try {
                        // Remove first parameter from Interface method signature (it is used to pass `this`)
                        signature = lookupAll().unreflect(method).type().dropParameterTypes(0, 1);

                        // Apply @ArgumentTypes for fluency
                        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                        for (int ix = 0; ix < parameterAnnotations.length; ix++) {
                            Annotation[] annotations = parameterAnnotations[ix];
                            for (Annotation annotation : annotations) {
                                if (annotation instanceof ArgumentType) {
                                    signature = signature.changeParameterType(ix, ((ArgumentType) annotation).value());
                                }
                            }
                        }

                        // Method name from Interface
                        String name = method.getName();

                        if (isDirectField) {
                            // Direct access to field
                            handle = getDirectField(name, signature, isStatic, returnTypeAnnotation);
                        } else if (isConstructor) {
                            // Constructors
                            handle = getConstructor(signature);
                        } else {
                            // Normal and super methods
                            handle = getMethod(name, signature, isStatic, returnTypeAnnotation, superAnnotation);
                        }

                        // Check if Optional.ofNullable wrapper needed
                        if (handle.type().returnType() != signature.returnType() && signature.returnType() == Optional.class) {
                            // Prepare explicit cast for Optional.ofNullable argument
                            MethodType filterType = MethodType.methodType(Optional.class, handle.type().returnType());
                            // Filter (applied to the result of original handle)
                            MethodHandle filter = MethodHandles.explicitCastArguments(OPTIONAL_OFNULLABLE, filterType);
                            handle = MethodHandles.filterReturnValue(handle, filter);
                        }
                    } catch (Exception e) {
                        throw new IllegalArgumentException(String.format("Cannot mirror %s", printMethod(method)), e);
                    }

                    invocationMap.put(method, handle);
                }
            }
        }

        private MethodHandle getDirectField(@Nonnull String name, @Nonnull MethodType signature, boolean isStatic, @Nullable ReturnType returnTypeAnnotation) throws IllegalAccessException, NoSuchFieldException {
            // Remove `set`/`get`/`is` from method name to get field name
            String fieldName = getFieldName(name);

            boolean isGetter = signature.parameterCount() == 0;
            boolean isSetter = signature.parameterCount() == 1;

            if (isGetter) {
                if (signature.returnType() == void.class) {
                    throw new IllegalArgumentException("Invalid return value for getter method marked with @DirectField");
                }
                Class<?> targetReturnType = signature.returnType();
                if (returnTypeAnnotation != null) {
                    // @OriginalReturnType is specified
                    if (signature.returnType() != Optional.class) {
                        throw new IllegalArgumentException("Only Optional is supported for direct field getters");
                    }
                    targetReturnType = returnTypeAnnotation.value();
                }
                return isStatic
                        ? lookupAll().findStaticGetter(targetClass, fieldName, targetReturnType)
                        : lookupAll().findGetter(targetClass, fieldName, targetReturnType).bindTo(instance);
            } else if (isSetter) {
                if (signature.returnType() != void.class && signature.returnType() != interfaceClass) {
                    // Setters in Interface must return either void or Interface instance (for chaining)
                    throw new IllegalArgumentException("Invalid return value for setter method marked with @DirectField");
                }
                return isStatic
                        ? lookupAll().findStaticSetter(targetClass, fieldName, signature.parameterType(0))
                        : lookupAll().findSetter(targetClass, fieldName, signature.parameterType(0)).bindTo(instance);
            }

            throw new IllegalArgumentException("Invalid argument count for a method marked with @DirectField");
        }

        private MethodHandle getConstructor(@Nonnull MethodType signature) throws IllegalAccessException, NoSuchMethodException {
            return lookupAll().findConstructor(targetClass, signature.changeReturnType(void.class));
        }

        private MethodHandle getMethod(@Nonnull String methodName, @Nonnull MethodType signature, boolean isStatic, @Nullable ReturnType returnTypeAnnotation, @Nullable Super superAnnotation) throws NoSuchMethodException, IllegalAccessException {
            Class<?> targetReturnType = signature.returnType();
            if (returnTypeAnnotation != null) {
                // @OriginalReturnType is specified
                targetReturnType = returnTypeAnnotation.value();
            }

            // Result casting is performed in MirrorInvocationHandler.invoke
            MethodType newSignature = signature.changeReturnType(targetReturnType);

            if (superAnnotation == null) {
                return isStatic
                        ? lookupAll().findStatic(targetClass, methodName, newSignature)
                        : lookupAll().findVirtual(targetClass, methodName, newSignature).bindTo(instance);
            } else {
                return lookupAll().findSpecial(targetClass, methodName, newSignature, superAnnotation.value()).bindTo(instance);
            }
        }

        // Compute field name from setter/getter method name
        private static String getFieldName(@Nonnull String methodName) {
            if (methodName.startsWith("get")) {
                return StringUtils.uncapitalize(StringUtils.removeStart(methodName, "get"));
            } else if (methodName.startsWith("set")) {
                return StringUtils.uncapitalize(StringUtils.removeStart(methodName, "set"));
            } else if (methodName.startsWith("is")) {
                return StringUtils.uncapitalize(StringUtils.removeStart(methodName, "is"));
            } else {
                return methodName;
            }
        }

        // Pretty print Method
        private static String printMethod(@Nonnull Method method) {
            return method.getReturnType().getSimpleName() + ' ' + method.getName() +
                    Stream.of(method.getParameters())
                            .map(parameter -> parameter.getType().getSimpleName())
                            .collect(Collectors.joining(", ", "(", ")"));
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("instance".equals(method.getName()) && proxy instanceof Attachable) {
                return instance;
            }

            MethodHandle handle = invocationMap.get(method);
            Validate.notNull(handle, "Requested method is not mapped: %s", printMethod(method));

            if (method.isDefault()) {
                // Default methods require different invocation
                return handle.bindTo(proxy).invokeWithArguments(args);
            }

            if (args != null) {
                // Unfold mirrors to instances (for fluency)
                for (int ix = 0; ix < args.length; ix++) {
                    if (args[ix] instanceof Attachable) {
                        args[ix] = ((Attachable<?>) args[ix]).instance();
                    }
                }
            }

            Object result = handle.invokeWithArguments(args);

            if (method.getReturnType() == interfaceClass) {
                return proxy;
            } else {
                return result;
            }
        }
    }

    /**
     * Helper method to collect all non-static fields from class and all of it's superclasses
     *
     * @param clazz class
     * @return Map of field names to field types
     */
    private static Map<String, Class<?>> getFields(@Nonnull Class<?> clazz) {
        Class<?> visitingClass = clazz;
        Map<String, Class<?>> result = new HashMap<>();
        while (visitingClass != Object.class) {
            for (Field declaredField : visitingClass.getDeclaredFields()) {
                if (!Modifier.isStatic(declaredField.getModifiers())) {
                    result.put(declaredField.getName(), declaredField.getType());
                }
            }
            visitingClass = visitingClass.getSuperclass();
        }

        return result;
    }


    /**
     * Helper interface that allows accessing underlying method of lambda
     */
    private interface MethodFinder {
        /**
         * Get underlying method of current lambda
         *
         * @return method
         * @throws IllegalStateException if no method is found
         */
        default Method method() {
            return getUnderlyingLambdaMethod(this);
        }

        /**
         * Get last parameter of the underlying method
         *
         * @return parameter
         */
        default Parameter lastParameter() {
            Parameter[] parameters = method().getParameters();
            Validate.validState(parameters.length > 0, "parameters.length > 0 must be true");

            return method().getParameters()[parameters.length - 1];
        }

        /**
         * Get underlying member for specified lambda class
         * <p>
         * This is replacing SerializableLambda hack with the purpose to get rid of serialization: SerializedLambda
         * clones all bound arguments, which will pollute memory. Instead, we'll inspect Class.getConstantPool() contents.
         * <p>
         * Internally functional interfaces do not have implementation directly, it is called via
         * `invokedynamic`; it is not possible to access original method directly either. Therefore we are
         * walking through constant pool. The last found method should be the original method in the caller class; this
         * is what we want. Actual position in constant pool depends on implementation/JVM/class redefinition, so we cannot
         * rely on that.
         *
         * @param instance instance of MethodFinder
         * @return Member (should be instance of Method, but it's up to caller to verify that)
         */
        static Method getUnderlyingLambdaMethod(MethodFinder instance) {
            SerializedLambdaAccessor accessor = attach(SerializedLambdaAccessor.class, instance);
            SerializedLambda serializedLambda = accessor.writeReplace();

            try {
                Class<?> containingClass = Class.forName(serializedLambda.getImplClass().replace('/', '.'));

                return Stream.of(containingClass.getDeclaredMethods())
                        .filter(method -> Objects.equals(method.getName(), serializedLambda.getImplMethodName()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(String.format("Can't find implementing method in %s", instance.getClass())));

            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(String.format("Can't find implementing method in %s", instance.getClass()), e);
            }
        }

    }

    /**
     * Safe caster for use in `Stream` and `Optional`
     *
     * @param <O> original type
     * @param <T> target type
     */
    private static final class SafeCaster<O, T> implements Function<O, T> {
        private final Class<T> clazz;

        private SafeCaster(Class<T> clazz) {
            this.clazz = clazz;
        }

        /**
         * Try to cast O to T
         *
         * @param t original object
         * @return casted object or null if types are not compatible
         */
        @Nullable
        @SuppressWarnings("unchecked")
        @Override
        public T apply(O t) {
            if (t != null && clazz.isAssignableFrom(t.getClass())) {
                return (T) t;
            } else {
                return null;
            }
        }
    }

    /**
     * Mirror for lambda's writeReplace method
     */
    @TargetClass.Instance
    private interface SerializedLambdaAccessor {
        @ReturnType(Object.class)
        SerializedLambda writeReplace();
    }

    /**
     * This holds MethodHandle for `Optional.ofNullable`
     */
    private static final MethodHandle OPTIONAL_OFNULLABLE;

    /**
     * This holds MethodHandles.Lookup that can see all public, protected, private and static methods.
     */
    private static final MethodHandles.Lookup LOOKUP_TRUSTED;

    /**
     * Accessor for LOOKUP_TRUSTED. This can be used instead of MethodHandles.lookup() / MethodHandles.publicLookup(),
     * if unrestricted access is required
     * <p>
     * Note: all security checks are bypassed.
     *
     * @return instance of lookup
     */
    public static MethodHandles.Lookup lookupAll() {
        return LOOKUP_TRUSTED;
    }

    static {
        try {
            // Get the protected static field IMPL_LOOKUP of MethodHandles.Lookup
            Field lookup = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            lookup.setAccessible(true);
            // Expose a static IMPL_LOOKUP
            LOOKUP_TRUSTED = (MethodHandles.Lookup) lookup.get(null);

            OPTIONAL_OFNULLABLE = LOOKUP_TRUSTED.findStatic(Optional.class, "ofNullable", MethodType.methodType(Optional.class, Object.class));
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }
}

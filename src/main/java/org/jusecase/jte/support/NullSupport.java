package org.jusecase.jte.support;

import net.jodah.typetools.TypeResolver;
import org.jusecase.jte.internal.TemplateCompiler;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.*;

@SuppressWarnings("unused") // Used by generated template code
public final class NullSupport {
    private static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final short[] EMPTY_SHORT_ARRAY = new short[0];
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final long[] EMPTY_LONG_ARRAY = new long[0];
    private static final float[] EMPTY_FLOAT_ARRAY = new float[0];
    private static final double[] EMPTY_DOUBLE_ARRAY = new double[0];

    public static boolean evaluate(BooleanSupplier expression) {
        try {
            return expression.getAsBoolean();
        } catch (NullPointerException e) {
            if (isTemplateOrigin(e)) {
                return false;
            }
            throw e;
        }
    }

    public static <T> T evaluate(Supplier<T> expression) {
        try {
            return expression.get();
        } catch (NullPointerException e) {
            if (isTemplateOrigin(e)) {
                return null;
            }
            throw e;
        }
    }

    public static String evaluate(IntSupplier expression) {
        try {
            return String.valueOf(expression.getAsInt());
        } catch (NullPointerException e) {
            if (isTemplateOrigin(e)) {
                return "";
            }
            throw e;
        }
    }

    public static String evaluate(LongSupplier expression) {
        try {
            return String.valueOf(expression.getAsLong());
        } catch (NullPointerException e) {
            if (isTemplateOrigin(e)) {
                return "";
            }
            throw e;
        }
    }

    public static String evaluate(DoubleSupplier expression) {
        try {
            return String.valueOf(expression.getAsDouble());
        } catch (NullPointerException e) {
            if (isTemplateOrigin(e)) {
                return "";
            }
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T evaluateIterable(Supplier<T> expression) {
        try {
            return expression.get();
        } catch (NullPointerException e) {
            if (isTemplateOrigin(e)) {
                Class<?>[] classes = TypeResolver.resolveRawArguments(Supplier.class, expression.getClass());
                Class<?> clazz = classes[0];

                if (clazz == List.class || clazz == Collection.class || clazz == Iterable.class) {
                    return (T)Collections.emptyList();
                } else if (clazz == Set.class) {
                    return (T)Collections.emptySet();
                } else if (clazz == boolean[].class) {
                    return (T)EMPTY_BOOLEAN_ARRAY;
                } else if (clazz == byte[].class) {
                    return (T)EMPTY_BYTE_ARRAY;
                } else if (clazz == short[].class) {
                    return (T)EMPTY_SHORT_ARRAY;
                } else if (clazz == int[].class) {
                    return (T)EMPTY_INT_ARRAY;
                } else if (clazz == long[].class) {
                    return (T)EMPTY_LONG_ARRAY;
                } else if (clazz == float[].class) {
                    return (T)EMPTY_FLOAT_ARRAY;
                } else if (clazz == double[].class) {
                    return (T)EMPTY_DOUBLE_ARRAY;
                } else {
                    try {
                        return (T)clazz.getConstructor().newInstance();
                    } catch (Exception instantiationException) {
                        throw new UnsupportedOperationException("NullSupport does not yet handle iterable class " + clazz, instantiationException);
                    }
                }
            }
            throw e;
        }
    }

    private static boolean isTemplateOrigin(NullPointerException e) {
        StackTraceElement[] stackTrace = e.getStackTrace();
        if (stackTrace.length == 0) {
            return true; // Not sure, but since the stacktrace was omitted by Java, this is most likely template code
        }

        return stackTrace[0].getClassName().startsWith(TemplateCompiler.PACKAGE_NAME);
    }
}

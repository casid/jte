package gg.jte.internal;

import java.util.function.*;

@SuppressWarnings("unused") // Used by generated template code
public final class NullCheck {
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

    public static void handleNullOutput(NullPointerException e) {
        if (isTemplateOrigin(e)) {
            return;
        }
        throw e;
    }

    private static boolean isTemplateOrigin(NullPointerException e) {
        StackTraceElement[] stackTrace = e.getStackTrace();
        if (stackTrace.length == 0) {
            return true; // Not sure, but since the stacktrace was omitted by Java, this is most likely template code
        }

        return stackTrace[0].getClassName().startsWith(Constants.PACKAGE_NAME);
    }
}

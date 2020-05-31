package org.jusecase.jte.support;

import org.jusecase.jte.internal.TemplateCompiler;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

@SuppressWarnings("unused") // Used by generated template code
public final class NullSupport {
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

    public static String evaluate(Supplier<String> expression) {
        try {
            return expression.get();
        } catch (NullPointerException e) {
            if (isTemplateOrigin(e)) {
                return null;
            }
            throw e;
        }
    }

    public static int evaluate(IntSupplier expression) {
        try {
            return expression.getAsInt();
        } catch (NullPointerException e) {
            if (isTemplateOrigin(e)) {
                return 0;
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

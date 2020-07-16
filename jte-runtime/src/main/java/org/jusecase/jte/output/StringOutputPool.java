package org.jusecase.jte.output;

public class StringOutputPool {
    private final ThreadLocal<StringOutput> pool = ThreadLocal.withInitial(StringOutput::new);

    public StringOutput get() {
        StringOutput stringOutput = pool.get();
        stringOutput.reset();
        return stringOutput;
    }
}

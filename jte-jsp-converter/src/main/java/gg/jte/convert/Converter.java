package gg.jte.convert;

public interface Converter {
    boolean canConvert(Parser parser);
    boolean advance(Parser parser);
    Converter newInstance();

    default void onPushed(Parser parser) {

    }

    default void onPopped(Parser parser) {

    }
}

package gg.jte.gradle;

/**
 * which kind of jte generation should the plugin be doing.
 *
 * @author edward3h
 * @since 2021-05-03
 */
public enum JteStage
{
    /**
     * "precompile" means that jte files will be turned into java classes _after_ the main application code is built,
     * and will be separate resources to be loaded by the jte runtime.
     */
    PRECOMPILE,

    /**
     * "generate" means that jte files will be converted into Java source _before_ the main java code of your project
     * is compiled, and will become part of your application jar during project build.
     */
    GENERATE
}

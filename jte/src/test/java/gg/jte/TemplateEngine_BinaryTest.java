package gg.jte;

import org.junit.jupiter.api.BeforeEach;

/**
 * Ensure the template engine test works in binary mode, too
 */
public class TemplateEngine_BinaryTest extends TemplateEngineTest {
    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        templateEngine.setBinaryStaticContent(true);
    }
}

package gg.jte.kotlin;

import org.junit.jupiter.api.BeforeEach;

public class TemplateEngine_BinaryTest extends TemplateEngineTest {
    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        templateEngine.setBinaryStaticContent(true);
    }
}

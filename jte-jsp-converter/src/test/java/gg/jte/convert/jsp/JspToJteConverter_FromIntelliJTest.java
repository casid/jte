package gg.jte.convert.jsp;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class JspToJteConverter_FromIntelliJTest {

    MyConverter converter = new MyConverter();

    @Test
    void missingArgument() {
        Throwable throwable = catchThrowable(() -> MyConverter.convertFromIntelliJPlugin(new String[]{}, converter, null));
        assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void simpleTag() {
        whenJspFileIsConverted("jsp/dummy.tag");
        assertThat(converter.getJteTag()).isEqualTo("tag/dummy.jte");
    }

    @Test
    void nestedTag() {
        whenJspFileIsConverted("jsp/tag/dummy.tag");
        assertThat(converter.getJteTag()).isEqualTo("tag/dummy.jte");
    }

    @Test
    void nestedTag2() {
        whenJspFileIsConverted("jsp/tags/dummy.tag");
        assertThat(converter.getJteTag()).isEqualTo("tag/dummy.jte");
    }

    @Test
    void nestedLayout() {
        whenJspFileIsConverted("jsp/layout/dummy.tag");
        assertThat(converter.getJteTag()).isEqualTo("layout/dummy.jte");
    }

    @Test
    void nestedLayout2() {
        whenJspFileIsConverted("jsp/layouts/dummy.tag");
        assertThat(converter.getJteTag()).isEqualTo("layout/dummy.jte");
    }

    @Test
    void tagWithKebabCase() {
        whenJspFileIsConverted("jsp/tags/some-tag.tag");
        assertThat(converter.getJteTag()).isEqualTo("tag/someTag.jte");
    }

    private void whenJspFileIsConverted(String file) {
        MyConverter.convertFromIntelliJPlugin(new String[]{Paths.get("").toAbsolutePath().resolve(file).toString()}, converter, null);
    }

    static class MyConverter extends JspToJteConverter {

        String jteTag;

        public MyConverter() {
            super(Paths.get("jsp"), Paths.get("jte"), "my:jte");
        }

        @Override
        public void convertTag(String jspTag, String jteTag, Consumer<Converter> parserSetup) {
            checkJteName(jteTag);
            this.jteTag = jteTag;
        }

        public String getJteTag() {
            return jteTag;
        }
    }
}
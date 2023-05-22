package gg.jte.output;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class Utf8ByteArrayOutputTest extends AbstractTemplateOutputTest<Utf8ByteArrayOutput> {

    @Override
    Utf8ByteArrayOutput createTemplateOutput() {
        return new Utf8ByteArrayOutput(16); // Small initial capacity size for tests;
    }

    @Test
    void empty() {
        thenOutputIs("");
    }

    @Test
    void string() {
        output.writeContent("Hello");
        thenOutputIs("Hello");
    }

    @Test
    void substring() {
        output.writeContent("Hello", 1, 3);
        thenOutputIs("el");
    }

    @Test
    void longString() {
        output.writeContent("The quick brown fox jumps over the lazy dog");
        thenOutputIs("The quick brown fox jumps over the lazy dog");
    }

    @Test
    void longStringSpecialChars() {
        output.writeContent("\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9");
        thenOutputIs("\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9");
    }

    @Test
    void outputs() {
        output.writeContent("\uD83D\uDCA9");
        output.writeContent(" says ");
        output.writeUserContent(42);
        output.writeContent("x ");
        output.writeContent("\uD83D\uDCA9!!!");

        thenOutputIs("\uD83D\uDCA9 says 42x \uD83D\uDCA9!!!");
    }

    @Test
    void binary_string() {
        output.writeBinaryContent("Hello".getBytes(StandardCharsets.UTF_8));
        thenOutputIs("Hello");
    }

    @Test
    void binary_longString() {
        output.writeBinaryContent("The quick brown fox jumps over the lazy dog".getBytes(StandardCharsets.UTF_8));
        thenOutputIs("The quick brown fox jumps over the lazy dog");
    }

    @Test
    void binary_longStringSpecialChars() {
        output.writeBinaryContent("\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9".getBytes(StandardCharsets.UTF_8));
        thenOutputIs("\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9\uD83D\uDCA9");
    }

    @Test
    void mixed() {
        output.writeContent("\uD83D\uDCA9");
        output.writeBinaryContent(" says ".getBytes(StandardCharsets.UTF_8));
        output.writeUserContent(42);
        output.writeContent("x ");
        output.writeBinaryContent("\uD83D\uDCA9!!!".getBytes(StandardCharsets.UTF_8));

        thenOutputIs("\uD83D\uDCA9 says 42x \uD83D\uDCA9!!!");
    }

    @Test
    void utf8_0x00() {
        output.writeUserContent((char)0);
        thenOutputIs("\u0000");
    }

    @Test
    void utf8_greek() {
        output.writeContent("Γαζέες καὶ μυρτιὲς δὲν θὰ βρῶ πιὰ στὸ χρυσαφὶ ξέφωτο");
        thenOutputIs("Γαζέες καὶ μυρτιὲς δὲν θὰ βρῶ πιὰ στὸ χρυσαφὶ ξέφωτο");
    }

    @Test
    void utf8_thai() {
        String str = "๏ เป็นมนุษย์สุดประเสริฐเลิศคุณค่า  กว่าบรรดาฝูงสัตว์เดรัจฉาน\n" +
                "  จงฝ่าฟันพัฒนาวิชาการ           อย่าล้างผลาญฤๅเข่นฆ่าบีฑาใคร\n" +
                "  ไม่ถือโทษโกรธแช่งซัดฮึดฮัดด่า     หัดอภัยเหมือนกีฬาอัชฌาสัย\n" +
                "  ปฏิบัติประพฤติกฎกำหนดใจ        พูดจาให้จ๊ะๆ จ๋าๆ น่าฟังเอย ฯ";
        output.writeContent(str);
        thenOutputIs(str);
    }

    @Test
    void utf8_japanese_hiragana() {
        String str = "いろはにほへとちりぬるを\n" +
                "  わかよたれそつねならむ\n" +
                "  うゐのおくやまけふこえて\n" +
                "  あさきゆめみしゑひもせす";

        output.writeContent(str);
        thenOutputIs(str);
    }

    @Test
    void utf8_japanese_katakana() {
        String str = "イロハニホヘト チリヌルヲ ワカヨタレソ ツネナラム\n" +
                "  ウヰノオクヤマ ケフコエテ アサキユメミシ ヱヒモセスン";

        output.writeContent(str);
        thenOutputIs(str);
    }

    protected void thenOutputIs(String expected) {
        byte[] bytes = output.toByteArray();

        String actual = new String(bytes, StandardCharsets.UTF_8);
        assertThat(actual).isEqualTo(expected);
    }
}
package org.delcom.ifs25007springbootcashflow;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DotEnvTest {
    @TempDir
    Path dir;

    @AfterEach
    void clean() {
        for (String k : new String[]{"CF_A", "CF_B", "CF_C", "CF_D", "CF_KEEP"}) {
            System.clearProperty(k);
        }
    }

    @Test
    void loadsValuesAndSkipsInvalidLines() throws Exception {
        System.setProperty("CF_KEEP", "old");
        Path f = dir.resolve(".env");
        Files.writeString(f, "# comment\n\nCF_A=1\nCF_B = \"two\"\nCF_C='three'\nNOEQUALS\n=novalue\n"
                + "CF_KEEP=new\nPATH=ignored\nCF_D=a=b\n");
        DotEnv.load(f);
        assertEquals("1", System.getProperty("CF_A"));
        assertEquals("two", System.getProperty("CF_B"));
        assertEquals("three", System.getProperty("CF_C"));
        assertEquals("a=b", System.getProperty("CF_D"));
        assertEquals("old", System.getProperty("CF_KEEP"));
        assertNull(System.getProperty("PATH"));
    }

    @Test
    void missingFileIsIgnored() {
        assertDoesNotThrow(() -> DotEnv.load(dir.resolve("none.env")));
    }

    @Test
    void unreadableFileIsIgnored() throws Exception {
        Path f = dir.resolve("bad.env");
        Files.write(f, new byte[]{(byte) 0xC3, 0x28, (byte) 0xFF});
        assertDoesNotThrow(() -> DotEnv.load(f));
    }

    @Test
    void defaultLoadDoesNotFail() {
        assertDoesNotThrow(() -> DotEnv.load());
    }
}

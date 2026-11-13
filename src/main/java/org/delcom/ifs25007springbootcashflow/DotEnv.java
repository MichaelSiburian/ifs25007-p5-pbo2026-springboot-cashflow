package org.delcom.ifs25007springbootcashflow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Memuat berkas .env ke system properties sebelum Spring Boot start. */
public final class DotEnv {
    private DotEnv() {
    }

    public static void load() {
        load(Path.of(".env"));
    }

    static void load(Path file) {
        if (!Files.isRegularFile(file)) {
            return;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (IOException e) {
            return;
        }
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int idx = line.indexOf('=');
            if (idx <= 0) {
                continue;
            }
            String key = line.substring(0, idx).trim();
            String value = line.substring(idx + 1).trim().replaceAll("^([\"'])(.*)\\1$", "$2");
            if (System.getProperty(key) == null && System.getenv(key) == null) {
                System.setProperty(key, value);
            }
        }
    }
}

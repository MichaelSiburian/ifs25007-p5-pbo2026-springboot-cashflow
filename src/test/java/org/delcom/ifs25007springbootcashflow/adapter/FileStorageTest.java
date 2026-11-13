package org.delcom.ifs25007springbootcashflow.adapter;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.ifs25007springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25007springbootcashflow.domain.exception.ValidationException;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileStorageTest {
    @TempDir
    Path dir;

    private static ByteArrayInputStream data() {
        return new ByteArrayInputStream(new byte[]{1, 2, 3});
    }

    @Test
    void savesAllowedImage() throws Exception {
        FileStorage storage = new FileStorage(dir.resolve("uploads").toString());
        String name = storage.save("Foto.PNG", data());
        assertTrue(name.endsWith(".png"));
        assertTrue(Files.exists(storage.getRoot().resolve(name)));
    }

    @Test
    void rejectsMissingExtension() {
        FileStorage storage = new FileStorage(dir.toString());
        assertThrows(ValidationException.class, () -> storage.save(null, data()));
        assertThrows(ValidationException.class, () -> storage.save("tanpaekstensi", data()));
    }

    @Test
    void rejectsUnsupportedFormat() {
        FileStorage storage = new FileStorage(dir.toString());
        assertThrows(ValidationException.class, () -> storage.save("virus.exe", data()));
    }

    @Test
    void reportsWriteFailure() throws Exception {
        Path file = Files.createFile(dir.resolve("not-a-directory"));
        FileStorage storage = new FileStorage(file.toString());
        assertThrows(ValidationException.class, () -> storage.save("a.png", data()));
    }
}

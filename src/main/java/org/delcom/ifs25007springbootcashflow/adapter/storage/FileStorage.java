package org.delcom.ifs25007springbootcashflow.adapter.storage;

import org.delcom.ifs25007springbootcashflow.domain.exception.ValidationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Menyimpan file unggahan (foto profil dan bukti transaksi) ke UPLOAD_DIR. */
public class FileStorage {
    private static final Set<String> ALLOWED = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final Path root;

    public FileStorage(String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public Path getRoot() {
        return root;
    }

    /** Menyimpan file dengan nama UUID + ekstensi; mengembalikan nama file. */
    public String save(String originalName, InputStream content) throws ValidationException {
        if (originalName == null || originalName.lastIndexOf('.') < 0) {
            throw new ValidationException("File harus memiliki ekstensi gambar.");
        }
        String extension = originalName.substring(originalName.lastIndexOf('.') + 1)
                .toLowerCase(Locale.ROOT);
        if (!ALLOWED.contains(extension)) {
            throw new ValidationException("Format file tidak didukung (jpg, jpeg, png, gif, webp).");
        }
        String name = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(root);
            Files.copy(content, root.resolve(name));
        } catch (IOException e) {
            throw new ValidationException("Gagal menyimpan file.");
        }
        return name;
    }
}

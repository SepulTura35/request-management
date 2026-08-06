package com.enoca.requestmanagement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Migration guvenligi")
class MigrationSafetyTest {

    private static final Path SHARED = Path.of("src/main/resources/db/migration");
    private static final Path DEV_ONLY = Path.of("src/main/resources/db/migration-dev");
    private static final Path PROD_ONLY = Path.of("src/main/resources/db/migration-prod");

    private List<Path> sqlFilesIn(Path directory) throws IOException {
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(path -> path.toString().endsWith(".sql")).toList();
        }
    }

    private String contentOf(Path file) throws IOException {
        return Files.readString(file).toLowerCase(Locale.ROOT);
    }

    @Test
    @DisplayName("Ortak migration klasoru users tablosuna veri eklemez")
    void sharedMigrationsNeverSeedUsers() throws IOException {
        for (Path file : sqlFilesIn(SHARED)) {
            assertThat(contentOf(file))
                    .as("%s ortak klasorde ve her ortamda calisir; kullanici eklememeli", file.getFileName())
                    .doesNotContain("insert into users");
        }
    }

    @Test
    @DisplayName("Ortak migration klasoru parola hash'i icermez")
    void sharedMigrationsCarryNoPasswordHash() throws IOException {
        for (Path file : sqlFilesIn(SHARED)) {
            assertThat(contentOf(file))
                    .as("%s bilinen bir parola hash'i tasimamali", file.getFileName())
                    .doesNotContain("$2a$");
        }
    }

    @Test
    @DisplayName("Demo veri yalnizca dev klasorunde")
    void demoDataLivesOnlyInTheDevLocation() throws IOException {
        List<Path> devFiles = sqlFilesIn(DEV_ONLY);

        assertThat(devFiles).isNotEmpty();
        assertThat(devFiles.stream().anyMatch(file -> {
            try {
                return contentOf(file).contains("insert into users");
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        })).as("demo kullanicilar dev klasorunde bulunmali").isTrue();
    }

    @Test
    @DisplayName("Profil klasorleri ortak klasorun alti degil")
    void profileFoldersAreSiblingsOfTheSharedFolder() {
        assertThat(DEV_ONLY.startsWith(SHARED))
                .as("Flyway bir konumu alt klasorleriyle tarar; dev klasoru ortak klasorun altinda olmamali")
                .isFalse();
        assertThat(PROD_ONLY.startsWith(SHARED))
                .as("prod klasoru ortak klasorun altinda olmamali")
                .isFalse();
    }
}

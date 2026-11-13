package org.delcom.ifs25007springbootcashflow.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.delcom.ifs25007springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25007springbootcashflow.domain.entity.Role;
import org.delcom.ifs25007springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25007springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25007springbootcashflow.usecase.AuthUseCase;
import java.nio.file.Path;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

class AppConfigTest {
    @TempDir
    Path dir;

    @Test
    void beansAreWiredAndRootAdminIsSeeded() throws Exception {
        DataSource ds = new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1", "sa", "");
        AppConfig config = new AppConfig();
        IUserRepository users = config.userRepository(ds);
        ITransactionRepository transactions = config.transactionRepository(ds, users);
        AuthUseCase auth = config.authUseCase(users);
        assertNotNull(config.userUseCase(users));
        assertNotNull(config.financeUseCase(transactions));
        assertNotNull(config.financePresenter());

        config.seedRootAdmin(auth, "root", "rahasia").run(null);
        assertEquals(Role.ADMIN, auth.login("root", "rahasia").getRole());
    }

    @Test
    void uploadsAreServedFromUploadDir() {
        AppConfig config = new AppConfig();
        FileStorage storage = config.fileStorage(dir.toString());
        WebMvcConfigurer configurer = config.uploadsResourceConfigurer(storage);
        ResourceHandlerRegistry registry =
                mock(ResourceHandlerRegistry.class, Mockito.RETURNS_DEEP_STUBS);
        configurer.addResourceHandlers(registry);
        verify(registry).addResourceHandler("/uploads/**");
    }
}

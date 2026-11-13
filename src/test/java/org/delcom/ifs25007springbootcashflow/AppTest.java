package org.delcom.ifs25007springbootcashflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import org.delcom.ifs25007springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25007springbootcashflow.domain.entity.Role;
import org.delcom.ifs25007springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25007springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25007springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25007springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25007springbootcashflow.usecase.UserUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = App.class)
@ActiveProfiles("test")
class AppTest {
    @Autowired
    private ApplicationContext context;

    @Test
    void contextWiresCleanArchitectureBeans() {
        assertNotNull(context.getBean(IUserRepository.class));
        assertNotNull(context.getBean(ITransactionRepository.class));
        assertNotNull(context.getBean(AuthUseCase.class));
        assertNotNull(context.getBean(UserUseCase.class));
        assertNotNull(context.getBean(FinanceUseCase.class));
        assertNotNull(context.getBean(FinancePresenter.class));
    }

    @Test
    void rootAdminIsSeededFromConfiguration() throws Exception {
        assertEquals(Role.ADMIN, context.getBean(AuthUseCase.class).login("admin", "admin").getRole());
    }

    @Test
    void mainDelegatesToSpringApplicationRun() throws Exception {
        try (MockedStatic<SpringApplication> spring = Mockito.mockStatic(SpringApplication.class)) {
            App.main(new String[0]);
            spring.verify(() -> SpringApplication.run(eq(App.class), any(String[].class)));
        }
    }
}

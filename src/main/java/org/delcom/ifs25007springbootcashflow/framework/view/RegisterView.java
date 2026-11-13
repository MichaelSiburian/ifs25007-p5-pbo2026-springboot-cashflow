package org.delcom.ifs25007springbootcashflow.framework.view;

import org.delcom.ifs25007springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25007springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25007springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25007springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25007springbootcashflow.usecase.AuthUseCase;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

/** Halaman registrasi (publik). */
@Route("register")
@PageTitle("Daftar - Cashflow")
public class RegisterView extends VerticalLayout implements BeforeEnterObserver {
    private final transient AuthUseCase authUseCase;
    private final transient IUserRepository users;
    private final transient FinancePresenter presenter;

    private final TextField fullName = new TextField("Nama lengkap");
    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final PasswordField confirm = new PasswordField("Ulangi password");
    private final ProgressBar progress = new ProgressBar();

    public RegisterView(AuthUseCase authUseCase, IUserRepository users,
                        FinancePresenter presenter) {
        this.authUseCase = authUseCase;
        this.users = users;
        this.presenter = presenter;

        getElement().setAttribute("role", "main");
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        H1 title = new H1("Daftar akun Cashflow");
        title.getStyle().set("font-size", "1.5rem").set("margin", "0 0 0.5rem 0");
        fullName.setWidthFull();
        username.setWidthFull();
        password.setWidthFull();
        confirm.setWidthFull();
        progress.setIndeterminate(true);
        progress.setVisible(false);

        Button register = new Button("Daftar", e -> submit());
        register.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        register.setWidthFull();

        PageCard card = new PageCard("Buat akun baru");
        card.setWidth("min(24rem, 92vw)");
        card.add(title, fullName, username, password, confirm, progress, register,
                new RouterLink("Sudah punya akun? Masuk", LoginView.class));
        add(card);
    }

    private void submit() {
        if (!password.getValue().equals(confirm.getValue())) {
            Notifier.error("Gagal: konfirmasi password tidak sama.");
            return;
        }
        progress.setVisible(true);
        try {
            authUseCase.register(username.getValue(), password.getValue(), fullName.getValue());
            Notifier.success("Registrasi berhasil. Silakan masuk.");
            UI.getCurrent().navigate(LoginView.class);
        } catch (DomainException ex) {
            progress.setVisible(false);
            Notifier.error(presenter.error(ex));
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(users) != null) {
            event.forwardTo(FinanceView.class);
        }
    }
}

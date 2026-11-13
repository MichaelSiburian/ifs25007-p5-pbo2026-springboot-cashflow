package org.delcom.ifs25007springbootcashflow.framework.view;

import org.delcom.ifs25007springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25007springbootcashflow.domain.entity.User;
import org.delcom.ifs25007springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25007springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25007springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25007springbootcashflow.usecase.AuthUseCase;
import com.vaadin.flow.component.Key;
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

/** Halaman login (publik). */
@Route("login")
@PageTitle("Masuk - Cashflow")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {
    private final transient AuthUseCase authUseCase;
    private final transient IUserRepository users;
    private final transient FinancePresenter presenter;

    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final ProgressBar progress = new ProgressBar();

    public LoginView(AuthUseCase authUseCase, IUserRepository users, FinancePresenter presenter) {
        this.authUseCase = authUseCase;
        this.users = users;
        this.presenter = presenter;

        getElement().setAttribute("role", "main");
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        H1 title = new H1("Masuk ke Cashflow");
        title.getStyle().set("font-size", "1.5rem").set("margin", "0 0 0.5rem 0");

        username.setId("login-username");
        username.setWidthFull();
        username.setAutofocus(true);
        password.setId("login-password");
        password.setWidthFull();

        progress.setIndeterminate(true);
        progress.setVisible(false);

        Button login = new Button("Masuk", e -> submit());
        login.setId("login-submit");
        login.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        login.setWidthFull();
        login.addClickShortcut(Key.ENTER);

        PageCard card = new PageCard("Silakan login");
        card.setWidth("min(24rem, 92vw)");
        card.add(title, username, password, progress, login,
                new RouterLink("Belum punya akun? Daftar", RegisterView.class));
        add(card);
    }

    private void submit() {
        progress.setVisible(true);
        try {
            User user = authUseCase.login(username.getValue(), password.getValue());
            AuthSession.login(user);
            UI.getCurrent().navigate(FinanceView.class);
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

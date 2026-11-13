package org.delcom.ifs25007springbootcashflow.framework.view;

import org.delcom.ifs25007springbootcashflow.domain.entity.User;
import org.delcom.ifs25007springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25007springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25007springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import java.util.Comparator;
import java.util.Map;

/** Daftar seluruh pengguna (ADMIN saja). */
@Route(value = "users", layout = MainLayout.class)
@PageTitle("Pengguna - Cashflow")
public class UsersView extends VerticalLayout implements BeforeEnterObserver {
    private final transient UserUseCase userUseCase;
    private final transient IUserRepository users;
    private final Grid<User> grid = new Grid<>(User.class, false);

    public UsersView(UserUseCase userUseCase, IUserRepository users) {
        this.userUseCase = userUseCase;
        this.users = users;

        getElement().setAttribute("role", "main");
        setSizeFull();
        H1 title = new H1("Daftar Pengguna");
        title.getStyle().set("font-size", "1.75rem").set("margin", "0");

        grid.addColumn(User::getId).setHeader("ID").setSortable(true)
                .setComparator(Comparator.comparing(User::getId)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(User::getUsername).setHeader("Username").setSortable(true)
                .setComparator(Comparator.comparing(User::getUsername));
        grid.addColumn(User::getFullName).setHeader("Nama").setSortable(true)
                .setComparator(Comparator.comparing(User::getFullName));
        grid.addColumn(u -> u.getRole().name()).setHeader("Role").setAutoWidth(true);
        grid.addComponentColumn(u -> {
            Button view = new Button("Lihat Transaksi", e -> UI.getCurrent().navigate(
                    "user-transactions",
                    QueryParameters.simple(Map.of("userId", String.valueOf(u.getId())))));
            view.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            view.setAriaLabel("Lihat transaksi " + u.getUsername());
            return view;
        }).setHeader("Tindakan").setAutoWidth(true).setFlexGrow(0);
        GridStyles.apply(grid, "Daftar pengguna");
        grid.setSizeFull();
        add(title, grid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User current = AuthSession.currentUser(users);
        if (current == null) {
            event.forwardTo(LoginView.class);
        } else if (!current.isAdmin()) {
            event.forwardTo(FinanceView.class);
        } else {
            grid.setItems(userUseCase.findAll());
        }
    }
}

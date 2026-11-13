package org.delcom.ifs25007springbootcashflow.framework.view;

import org.delcom.ifs25007springbootcashflow.domain.entity.User;
import org.delcom.ifs25007springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25007springbootcashflow.framework.auth.AuthSession;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLayout;

/** Layout utama: sidebar, menu avatar (Profil / Keluar), dan penjaga login global. */
public class MainLayout extends AppLayout implements RouterLayout, BeforeEnterObserver {
    private final transient IUserRepository users;
    private final SideNav nav = new SideNav();
    private final Div menuHolder = new Div();

    public MainLayout(IUserRepository users) {
        this.users = users;

        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Buka atau tutup menu navigasi");
        Span brand = new Span("Cashflow");
        brand.getStyle().set("font-size", "1.25rem").set("font-weight", "700");

        HorizontalLayout bar = new HorizontalLayout(toggle, brand);
        bar.setDefaultVerticalComponentAlignment(com.vaadin.flow.component.orderedlayout
                .FlexComponent.Alignment.CENTER);
        bar.setWidthFull();
        bar.expand(brand);
        bar.add(menuHolder);
        bar.getStyle().set("padding-right", "1rem");

        setPrimarySection(Section.DRAWER);
        addToNavbar(bar);
        addToDrawer(nav);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = AuthSession.currentUser(users);
        if (user == null) {
            event.forwardTo(LoginView.class);
            return;
        }
        nav.removeAll();
        nav.addItem(new SideNavItem("Transaksi", FinanceView.class));
        nav.addItem(new SideNavItem("Profil", ProfileView.class));
        if (user.isAdmin()) {
            nav.addItem(new SideNavItem("Pengguna", UsersView.class));
        }
        menuHolder.removeAll();
        menuHolder.add(buildMenu(user));
    }

    private MenuBar buildMenu(User user) {
        Avatar avatar = new Avatar(user.getFullName());
        if (user.getPhotoPath() != null) {
            avatar.setImage("/uploads/" + user.getPhotoPath());
        }
        MenuBar menu = new MenuBar();
        menu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        var item = menu.addItem(avatar);
        item.getElement().setAttribute("aria-label", "Menu akun " + user.getFullName());
        item.getSubMenu().addItem("Profil", e -> UI.getCurrent().navigate(ProfileView.class));
        item.getSubMenu().addItem("Keluar", e -> {
            AuthSession.logout();
            UI.getCurrent().navigate(LoginView.class);
        });
        return menu;
    }
}

package org.delcom.ifs25007springbootcashflow.framework.view;

import org.delcom.ifs25007springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25007springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25007springbootcashflow.domain.entity.User;
import org.delcom.ifs25007springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25007springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25007springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25007springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25007springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.Comparator;

/** Transaksi pengguna lain, read-only (ADMIN saja). */
@Route(value = "user-transactions", layout = MainLayout.class)
@PageTitle("Transaksi Pengguna - Cashflow")
public class UserTransactionsView extends VerticalLayout implements BeforeEnterObserver {
    private final transient FinanceUseCase financeUseCase;
    private final transient UserUseCase userUseCase;
    private final transient FinancePresenter presenter;
    private final transient IUserRepository users;

    private final H1 title = new H1("Transaksi Pengguna");
    private final Span balance = new Span();
    private final Span totalIncome = new Span();
    private final Span totalExpense = new Span();
    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);

    public UserTransactionsView(FinanceUseCase financeUseCase, UserUseCase userUseCase,
                                FinancePresenter presenter, IUserRepository users) {
        this.financeUseCase = financeUseCase;
        this.userUseCase = userUseCase;
        this.presenter = presenter;
        this.users = users;

        getElement().setAttribute("role", "main");
        setSizeFull();
        title.getStyle().set("font-size", "1.75rem").set("margin", "0");

        FlexLayout summary = new FlexLayout(card("Total Pemasukan", totalIncome),
                card("Total Pengeluaran", totalExpense), card("Saldo", balance));
        summary.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        summary.getStyle().set("gap", "1rem");

        grid.addColumn(Transaction::getId).setHeader("ID").setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getId))
                .setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Transaction::getDescription).setHeader("Keterangan").setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription,
                        String.CASE_INSENSITIVE_ORDER));
        grid.addColumn(t -> presenter.formatMoney(t.getAmount())).setHeader("Jumlah")
                .setSortable(true).setComparator(Comparator.comparing(Transaction::getAmount))
                .setAutoWidth(true);
        grid.addComponentColumn(GridStyles::typeBadge).setHeader("Tipe").setAutoWidth(true);
        grid.addComponentColumn(t -> {
            Div cell = new Div();
            if (t.hasProof()) {
                Image thumb = new Image("/uploads/" + t.getProofPath(),
                        "Bukti transaksi " + t.getId());
                thumb.setHeight("40px");
                thumb.getStyle().set("cursor", "pointer");
                thumb.addClickListener(e -> new ImagePreviewDialog(
                        "/uploads/" + t.getProofPath(), "Bukti transaksi " + t.getId()).open());
                cell.add(thumb);
            } else {
                cell.setText("-");
            }
            return cell;
        }).setHeader("Bukti").setAutoWidth(true);
        grid.addColumn(t -> presenter.formatTime(t.getCreatedAt())).setHeader("Waktu")
                .setSortable(true).setComparator(Comparator.comparing(Transaction::getCreatedAt))
                .setAutoWidth(true);
        GridStyles.apply(grid, "Daftar transaksi pengguna (read-only)");
        grid.setSizeFull();
        add(title, summary, grid);
    }

    private Div card(String label, Span value) {
        Span caption = new Span(label);
        caption.addClassName("caption");
        value.addClassName("value");
        Div card = new Div(caption, value);
        card.addClassName("summary-card");
        return card;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User current = AuthSession.currentUser(users);
        if (current == null) {
            event.forwardTo(LoginView.class);
            return;
        }
        if (!current.isAdmin()) {
            event.forwardTo(FinanceView.class);
            return;
        }
        try {
            int id = Integer.parseInt(event.getLocation().getQueryParameters()
                    .getSingleParameter("userId").orElse("-1"));
            User target = userUseCase.get(id);
            title.setText("Transaksi " + target.getFullName() + " (read-only)");
            grid.setItems(financeUseCase.findByUser(id));
            totalIncome.setText(presenter.formatMoney(financeUseCase.getTotalIncome(id)));
            totalExpense.setText(presenter.formatMoney(financeUseCase.getTotalExpense(id)));
            balance.setText(presenter.formatMoney(financeUseCase.getBalance(id)));
        } catch (NumberFormatException ex) {
            Notifier.error("Gagal: ID pengguna tidak valid.");
            event.forwardTo(UsersView.class);
        } catch (DomainException ex) {
            Notifier.error(presenter.error(ex));
            event.forwardTo(UsersView.class);
        }
    }
}

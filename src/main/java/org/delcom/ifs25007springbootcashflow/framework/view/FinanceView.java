package org.delcom.ifs25007springbootcashflow.framework.view;

import org.delcom.ifs25007springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25007springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25007springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25007springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25007springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25007springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25007springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25007springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25007springbootcashflow.usecase.FinanceUseCase;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import java.io.ByteArrayInputStream;
import java.util.Comparator;

/** Dashboard transaksi milik pengguna yang login. */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Transaksi - Cashflow")
public class FinanceView extends VerticalLayout {
    private final transient FinanceUseCase useCase;
    private final transient FinancePresenter presenter;
    private final transient IUserRepository users;
    private final transient FileStorage storage;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField("Cari keterangan");
    private final Select<SortOption> sortSelect = new Select<>();
    private final Span totalCount = new Span();
    private final Span totalIncome = new Span();
    private final Span totalExpense = new Span();
    private final Span balance = new Span();

    public FinanceView(FinanceUseCase useCase, FinancePresenter presenter,
                       IUserRepository users, FileStorage storage) {
        this.useCase = useCase;
        this.presenter = presenter;
        this.users = users;
        this.storage = storage;

        getElement().setAttribute("role", "main");
        setSizeFull();
        H1 title = new H1("Transaksi Saya");
        title.getStyle().set("font-size", "1.75rem").set("margin", "0");
        add(title, buildSummary(), buildToolbar(), buildGrid());
        refresh();
    }

    private int userId() {
        return AuthSession.currentUserId(users);
    }

    private FlexLayout buildSummary() {
        FlexLayout summary = new FlexLayout(
                card("Total Transaksi", totalCount),
                card("Total Pemasukan", totalIncome),
                card("Total Pengeluaran", totalExpense),
                card("Saldo", balance));
        summary.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        summary.getStyle().set("gap", "1rem");
        summary.setWidthFull();
        return summary;
    }

    private Div card(String label, Span value) {
        Span caption = new Span(label);
        caption.addClassName("caption");
        value.addClassName("value");
        Div card = new Div(caption, value);
        card.addClassName("summary-card");
        return card;
    }

    private HorizontalLayout buildToolbar() {
        searchField.setPlaceholder("Ketik keterangan...");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> refresh());

        sortSelect.setLabel("Urutkan");
        sortSelect.setItems(SortOption.values());
        sortSelect.setItemLabelGenerator(SortOption::getLabel);
        sortSelect.setPlaceholder("Urutan masuk");
        sortSelect.addValueChangeListener(e -> refresh());

        Button reload = new Button("Muat ulang", e -> refresh());
        Button addIncome = new Button("Tambah Pemasukan", e -> openAddDialog(TransactionType.INCOME));
        addIncome.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addIncome.getStyle().set("background-color", "#166534").set("color", "#ffffff");
        Button addExpense = new Button("Tambah Pengeluaran",
                e -> openAddDialog(TransactionType.EXPENSE));
        addExpense.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addExpense.getStyle().set("background-color", "#b91c1c").set("color", "#ffffff");

        HorizontalLayout toolbar = new HorizontalLayout(
                searchField, sortSelect, reload, addIncome, addExpense);
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        toolbar.setWidthFull();
        toolbar.getStyle().set("flex-wrap", "wrap");
        return toolbar;
    }

    private Grid<Transaction> buildGrid() {
        grid.addColumn(Transaction::getId).setHeader("ID")
                .setSortable(true).setComparator(Comparator.comparing(Transaction::getId))
                .setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Transaction::getDescription).setHeader("Keterangan")
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription,
                        String.CASE_INSENSITIVE_ORDER))
                .setFlexGrow(1);
        grid.addColumn(t -> presenter.formatMoney(t.getAmount())).setHeader("Jumlah")
                .setSortable(true).setComparator(Comparator.comparing(Transaction::getAmount))
                .setAutoWidth(true);
        grid.addComponentColumn(GridStyles::typeBadge).setHeader("Tipe")
                .setSortable(true).setComparator(Comparator.comparing(Transaction::getType))
                .setAutoWidth(true);
        grid.addComponentColumn(this::proofCell).setHeader("Bukti").setAutoWidth(true);
        grid.addColumn(t -> presenter.formatTime(t.getCreatedAt())).setHeader("Waktu")
                .setSortable(true).setComparator(Comparator.comparing(Transaction::getCreatedAt))
                .setAutoWidth(true);
        grid.addComponentColumn(this::actionCell).setHeader("Tindakan")
                .setAutoWidth(true).setFlexGrow(0);
        GridStyles.apply(grid, "Daftar transaksi");
        grid.setSizeFull();
        return grid;
    }

    private Div proofCell(Transaction t) {
        Div cell = new Div();
        if (t.hasProof()) {
            Image thumb = new Image("/uploads/" + t.getProofPath(),
                    "Bukti transaksi " + t.getId());
            thumb.setHeight("40px");
            thumb.getStyle().set("cursor", "pointer");
            thumb.addClickListener(e -> new ImagePreviewDialog("/uploads/" + t.getProofPath(),
                    "Bukti transaksi " + t.getId()).open());
            cell.add(thumb);
        } else {
            cell.setText("-");
        }
        return cell;
    }

    private HorizontalLayout actionCell(Transaction t) {
        Button edit = new Button("Ubah", e -> openEditDialog(t));
        edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        edit.setAriaLabel("Ubah transaksi " + t.getId());
        Button delete = new Button("Hapus", e -> openDeleteDialog(t));
        delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        delete.getStyle().set("color", "#b91c1c");
        delete.setAriaLabel("Hapus transaksi " + t.getId());
        return new HorizontalLayout(edit, delete);
    }

    private void refresh() {
        int uid = userId();
        var items = useCase.search(uid, searchField.getValue());
        SortOption option = sortSelect.getValue();
        if (option != null) {
            items = useCase.sort(items, option);
        }
        grid.setItems(items);
        totalCount.setText(String.valueOf(useCase.getTotalCount(uid)));
        totalIncome.setText(presenter.formatMoney(useCase.getTotalIncome(uid)));
        totalExpense.setText(presenter.formatMoney(useCase.getTotalExpense(uid)));
        balance.setText(presenter.formatMoney(useCase.getBalance(uid)));
        balance.setTitle(presenter.formatBalance(useCase.getBalance(uid)));
    }

    private void openAddDialog(TransactionType type) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Tambah " + type.getLabel());

        TextField description = new TextField("Keterangan");
        description.setWidthFull();
        TextField amount = new TextField("Jumlah (Rp)");
        amount.setWidthFull();
        amount.setPlaceholder("contoh: 1000000");
        PendingUpload pending = new PendingUpload();
        Upload upload = proofUpload(pending);
        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);
        dialog.add(new VerticalLayout(description, amount, new Span("Bukti gambar (opsional)"),
                upload, progress));

        Button save = new Button("Simpan", e -> {
            progress.setVisible(true);
            try {
                String proof = pending.isPresent()
                        ? storage.save(pending.getName(), new ByteArrayInputStream(pending.getData()))
                        : null;
                Transaction saved = type == TransactionType.INCOME
                        ? useCase.addIncome(userId(), description.getValue(), amount.getValue(), proof)
                        : useCase.addExpense(userId(), description.getValue(), amount.getValue(), proof);
                dialog.close();
                refresh();
                Notifier.success(presenter.addSuccess(saved));
            } catch (DomainException ex) {
                progress.setVisible(false);
                Notifier.error(presenter.error(ex));
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(new Button("Batal", e -> dialog.close()), save);
        dialog.open();
    }

    private Upload proofUpload(PendingUpload pending) {
        Upload upload = new Upload(UploadHandler.inMemory(
                (metadata, data) -> pending.set(metadata.fileName(), data)));
        upload.setMaxFiles(1);
        upload.setAcceptedFileTypes(".png", ".jpg", ".jpeg", ".gif", ".webp");
        upload.setMaxFileSize(2 * 1024 * 1024);
        return upload;
    }

    private void openEditDialog(Transaction t) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Ubah Transaksi " + t.getId());
        TextField description = new TextField("Keterangan");
        description.setValue(t.getDescription());
        description.setWidthFull();
        TextField amount = new TextField("Jumlah (Rp)");
        amount.setValue(String.valueOf(t.getAmount()));
        amount.setWidthFull();
        dialog.add(new VerticalLayout(description, amount));

        Button save = new Button("Simpan", e -> {
            try {
                Transaction updated = useCase.update(userId(), t.getId(),
                        description.getValue(), amount.getValue());
                dialog.close();
                refresh();
                Notifier.success(presenter.updateSuccess(updated));
            } catch (DomainException ex) {
                Notifier.error(presenter.error(ex));
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(new Button("Batal", e -> dialog.close()), save);
        dialog.open();
    }

    private void openDeleteDialog(Transaction t) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Hapus transaksi?");
        dialog.add(new Span(presenter.formatTransaction(t)));
        Button confirm = new Button("Hapus", e -> {
            try {
                Transaction deleted = useCase.delete(userId(), t.getId());
                dialog.close();
                refresh();
                Notifier.success(presenter.deleteSuccess(deleted));
            } catch (DomainException ex) {
                Notifier.error(presenter.error(ex));
            }
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirm.getStyle().set("background-color", "#b91c1c").set("color", "#ffffff");
        dialog.getFooter().add(new Button("Batal", e -> dialog.close()), confirm);
        dialog.open();
    }
}

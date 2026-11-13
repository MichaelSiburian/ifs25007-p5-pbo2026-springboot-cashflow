package org.delcom.ifs25007springbootcashflow.framework.view;

import org.delcom.ifs25007springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25007springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25007springbootcashflow.domain.entity.User;
import org.delcom.ifs25007springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25007springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25007springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25007springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import java.io.ByteArrayInputStream;

/** Pengaturan profil: nama, password, foto. */
@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil - Cashflow")
public class ProfileView extends VerticalLayout {
    private final transient UserUseCase userUseCase;
    private final transient FinancePresenter presenter;
    private final transient IUserRepository users;
    private final transient FileStorage storage;

    public ProfileView(UserUseCase userUseCase, FinancePresenter presenter,
                       IUserRepository users, FileStorage storage) {
        this.userUseCase = userUseCase;
        this.presenter = presenter;
        this.users = users;
        this.storage = storage;

        getElement().setAttribute("role", "main");
        User user = AuthSession.currentUser(users);
        H1 title = new H1("Profil");
        title.getStyle().set("font-size", "1.75rem").set("margin", "0");
        add(title);
        if (user == null) {
            return;
        }

        Avatar avatar = new Avatar(user.getFullName());
        if (user.getPhotoPath() != null) {
            avatar.setImage("/uploads/" + user.getPhotoPath());
        }
        Span username = new Span("Username: " + user.getUsername() + " (" + user.getRole() + ")");

        TextField fullName = new TextField("Nama lengkap");
        fullName.setValue(user.getFullName());
        fullName.setWidthFull();
        PasswordField newPassword = new PasswordField("Password baru (kosongkan bila tidak diubah)");
        newPassword.setWidthFull();
        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);

        Button save = new Button("Simpan profil", e -> {
            progress.setVisible(true);
            try {
                userUseCase.updateProfile(user.getId(), fullName.getValue(), newPassword.getValue());
                Notifier.success("Profil berhasil disimpan.");
                UI.getCurrent().getPage().reload();
            } catch (DomainException ex) {
                progress.setVisible(false);
                Notifier.error(presenter.error(ex));
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        PendingUpload pending = new PendingUpload();
        Upload upload = new Upload(UploadHandler.inMemory(
                (metadata, data) -> pending.set(metadata.fileName(), data)));
        upload.setMaxFiles(1);
        upload.setAcceptedFileTypes(".png", ".jpg", ".jpeg", ".gif", ".webp");
        upload.setMaxFileSize(2 * 1024 * 1024);
        Button savePhoto = new Button("Simpan foto", e -> {
            if (!pending.isPresent()) {
                Notifier.error("Gagal: pilih foto terlebih dahulu.");
                return;
            }
            progress.setVisible(true);
            try {
                String name = storage.save(pending.getName(),
                        new ByteArrayInputStream(pending.getData()));
                userUseCase.updatePhoto(user.getId(), name);
                Notifier.success("Foto profil berhasil disimpan.");
                UI.getCurrent().getPage().reload();
            } catch (DomainException ex) {
                progress.setVisible(false);
                Notifier.error(presenter.error(ex));
            }
        });
        savePhoto.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        PageCard card = new PageCard("Data akun");
        card.setWidth("min(32rem, 100%)");
        card.add(avatar, username, fullName, newPassword, save,
                new Span("Foto profil"), upload, savePhoto, progress);
        add(card);
    }
}

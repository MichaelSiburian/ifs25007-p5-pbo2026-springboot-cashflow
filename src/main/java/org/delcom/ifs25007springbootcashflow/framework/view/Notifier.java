package org.delcom.ifs25007springbootcashflow.framework.view;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

/** Notifikasi sukses / gagal. */
public final class Notifier {
    private Notifier() {
    }

    public static void success(String message) {
        Notification n = Notification.show(message, 3500, Notification.Position.TOP_CENTER);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    public static void error(String message) {
        Notification n = Notification.show(message, 4500, Notification.Position.TOP_CENTER);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}

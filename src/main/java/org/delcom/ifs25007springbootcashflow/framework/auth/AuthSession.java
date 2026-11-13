package org.delcom.ifs25007springbootcashflow.framework.auth;

import org.delcom.ifs25007springbootcashflow.domain.entity.User;
import org.delcom.ifs25007springbootcashflow.domain.repository.IUserRepository;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Sesi login: menyimpan user di VaadinSession + HTTP session + cookie bertanda tangan.
 * Flag LOGGED_OUT_KEY mencegah sesi dipulihkan dari cookie setelah logout.
 */
public final class AuthSession {
    public static final String LOGGED_OUT_KEY = "cashflow.loggedOut";
    private static final String USER_ID_KEY = "cashflow.userId";
    private static final String COOKIE_NAME = "cashflow_uid";
    private static final int COOKIE_AGE_SECONDS = 8 * 3600;
    private static final byte[] SECRET = secret();

    private AuthSession() {
    }

    public static void login(User user) {
        VaadinSession session = VaadinSession.getCurrent();
        session.setAttribute(USER_ID_KEY, user.getId());
        session.setAttribute(LOGGED_OUT_KEY, Boolean.FALSE);
        WrappedSession http = session.getSession();
        if (http != null) {
            http.setAttribute(USER_ID_KEY, user.getId());
            http.setAttribute(LOGGED_OUT_KEY, Boolean.FALSE);
        }
        writeCookie(user.getId() + "." + sign(String.valueOf(user.getId())), COOKIE_AGE_SECONDS);
    }

    public static void logout() {
        VaadinSession session = VaadinSession.getCurrent();
        session.setAttribute(USER_ID_KEY, null);
        session.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
        WrappedSession http = session.getSession();
        if (http != null) {
            http.removeAttribute(USER_ID_KEY);
            http.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
        }
        writeCookie("", 0);
    }

    /** Pengguna yang sedang login (memulihkan sesi setelah refresh), atau null. */
    public static User currentUser(IUserRepository users) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null || loggedOut(session)) {
            return null;
        }
        Integer id = (Integer) session.getAttribute(USER_ID_KEY);
        WrappedSession http = session.getSession();
        if (id == null && http != null) {
            id = (Integer) http.getAttribute(USER_ID_KEY);
        }
        if (id == null) {
            id = idFromCookie();
        }
        if (id == null) {
            return null;
        }
        Optional<User> user = users.findById(id);
        user.ifPresent(u -> session.setAttribute(USER_ID_KEY, u.getId()));
        return user.orElse(null);
    }

    /** ID pengguna yang sedang login, atau -1. */
    public static int currentUserId(IUserRepository users) {
        User user = currentUser(users);
        return user == null ? -1 : user.getId();
    }

    private static boolean loggedOut(VaadinSession session) {
        WrappedSession http = session.getSession();
        return Boolean.TRUE.equals(session.getAttribute(LOGGED_OUT_KEY))
                || (http != null && Boolean.TRUE.equals(http.getAttribute(LOGGED_OUT_KEY)));
    }

    private static Integer idFromCookie() {
        VaadinRequest request = VaadinService.getCurrentRequest();
        if (request == null || request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                String[] parts = cookie.getValue().split("\\.");
                if (parts.length == 2 && parts[0].matches("\\d{1,9}")
                        && MessageDigest.isEqual(sign(parts[0]).getBytes(StandardCharsets.UTF_8),
                        parts[1].getBytes(StandardCharsets.UTF_8))) {
                    return Integer.valueOf(parts[0]);
                }
            }
        }
        return null;
    }

    private static void writeCookie(String value, int maxAge) {
        VaadinResponse response = VaadinService.getCurrentResponse();
        if (response == null) {
            return;
        }
        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private static String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] secret() {
        String configured = System.getenv("SESSION_SECRET");
        if (configured != null && !configured.isBlank()) {
            return configured.getBytes(StandardCharsets.UTF_8);
        }
        byte[] random = new byte[32];
        new SecureRandom().nextBytes(random);
        return random;
    }
}

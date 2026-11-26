package com.qldtkhoa.qldtkhoa_app.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SessionInvalidationHelper {

    private final SessionRegistry sessionRegistry;

    public void expireUserSessions(String email) {
        expireOtherSessions(email, null);
    }

    public void expireOtherSessions(String email, String keepSessionId) {
        if (email == null || email.isBlank() || sessionRegistry == null) {
            return;
        }
        String normalized = email.trim();
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (principal instanceof AppUserPrincipal user && user.getEmail() != null
                    && user.getEmail().equalsIgnoreCase(normalized)) {
                List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
                for (SessionInformation session : sessions) {
                    if (keepSessionId == null || !keepSessionId.equals(session.getSessionId())) {
                        session.expireNow();
                    }
                }
            }
        }
    }
}

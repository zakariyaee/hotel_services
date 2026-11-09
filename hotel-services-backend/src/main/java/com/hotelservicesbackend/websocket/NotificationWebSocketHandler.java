package com.hotelservicesbackend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hotelservicesbackend.dto.notification.WebSocketNotificationEvent;
import com.hotelservicesbackend.entity.enums.Role;
import com.hotelservicesbackend.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(NotificationWebSocketHandler.class);

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    // Sessions indexées
    private final Set<WebSocketSession> allSessions = new CopyOnWriteArraySet<>();
    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    private final Map<Role, Set<WebSocketSession>> roleSessions = new ConcurrentHashMap<>();
    private final Map<Long, Set<WebSocketSession>> departmentSessions = new ConcurrentHashMap<>();

    // Stockage des métadonnées de session
    private final Map<String, SessionMetadata> sessionMetadata = new ConcurrentHashMap<>();

    public record SessionMetadata(Long userId, String email, Role role, Long departementId) {}

    public NotificationWebSocketHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        allSessions.add(session);
        log.info("🔌 WebSocket: Nouvelle session connectée -> id: {}", session.getId());

        // Extraction des paramètres de requête (ex: /ws/notifications?token=...)
        URI uri = session.getUri();
        if (uri != null && uri.getQuery() != null) {
            Map<String, String> params = parseQuery(uri.getQuery());
            String token = params.get("token");
            String deptParam = params.get("departementId");
            Long deptId = deptParam != null && !deptParam.isBlank() ? Long.parseLong(deptParam) : null;

            if (token != null && !token.isBlank()) {
                authenticateAndRegister(session, token, deptId);
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            if (payload == null || payload.isBlank()) return;

            Map<String, Object> map = objectMapper.readValue(payload, Map.class);
            String type = (String) map.get("type");

            if ("SUBSCRIBE".equalsIgnoreCase(type) || "AUTH".equalsIgnoreCase(type)) {
                String token = (String) map.get("token");
                Object deptObj = map.get("departementId");
                Long deptId = null;
                if (deptObj instanceof Number num) {
                    deptId = num.longValue();
                } else if (deptObj instanceof String s && !s.isBlank()) {
                    deptId = Long.parseLong(s);
                }

                if (token != null && !token.isBlank()) {
                    authenticateAndRegister(session, token, deptId);
                }
            } else if ("PING".equalsIgnoreCase(type)) {
                session.sendMessage(new TextMessage("{\"type\":\"PONG\",\"timestamp\":\"" + System.currentTimeMillis() + "\"}"));
            }
        } catch (Exception e) {
            log.warn("⚠️ WebSocket: Erreur lors du parsing du message entrant: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        allSessions.remove(session);
        SessionMetadata meta = sessionMetadata.remove(session.getId());

        if (meta != null) {
            if (meta.userId() != null) {
                Set<WebSocketSession> set = userSessions.get(meta.userId());
                if (set != null) {
                    set.remove(session);
                    if (set.isEmpty()) userSessions.remove(meta.userId());
                }
            }
            if (meta.role() != null) {
                Set<WebSocketSession> set = roleSessions.get(meta.role());
                if (set != null) {
                    set.remove(session);
                    if (set.isEmpty()) roleSessions.remove(meta.role());
                }
            }
            if (meta.departementId() != null) {
                Set<WebSocketSession> set = departmentSessions.get(meta.departementId());
                if (set != null) {
                    set.remove(session);
                    if (set.isEmpty()) departmentSessions.remove(meta.departementId());
                }
            }
        }

        log.info("🔌 WebSocket: Session fermée -> id: {}, code: {}", session.getId(), status.getCode());
    }

    private void authenticateAndRegister(WebSocketSession session, String token, Long departementId) {
        try {
            String email = jwtUtil.extractUsername(token);
            Long userId = jwtUtil.extractUserId(token);
            String roleStr = jwtUtil.extractRole(token);
            Role role = roleStr != null ? Role.valueOf(roleStr) : null;

            SessionMetadata meta = new SessionMetadata(userId, email, role, departementId);
            sessionMetadata.put(session.getId(), meta);

            if (userId != null) {
                userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
            }
            if (role != null) {
                roleSessions.computeIfAbsent(role, k -> new CopyOnWriteArraySet<>()).add(session);
            }
            if (departementId != null) {
                departmentSessions.computeIfAbsent(departementId, k -> new CopyOnWriteArraySet<>()).add(session);
            }

            log.info("✅ WebSocket: Session {} enregistrée avec succès (User: {}, Role: {}, Dpt: {})",
                    session.getId(), email, role, departementId);

            // Accusé de réception
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "CONNECTED",
                    "userId", userId != null ? userId : 0,
                    "role", role != null ? role.name() : "ANONYMOUS",
                    "message", "Connecté au service temps réel de l'hôtel"
            ))));

        } catch (Exception e) {
            log.warn("❌ WebSocket: Échec validation du token pour la session {}: {}", session.getId(), e.getMessage());
        }
    }

    // =========================================================================
    // MÉTHODES DE BROADCAST TEMPS RÉEL
    // =========================================================================

    public void sendToUser(Long userId, WebSocketNotificationEvent event) {
        if (userId == null) return;
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null) {
            sendJsonToSessions(sessions, event);
        }
    }

    public void sendToRole(Role role, WebSocketNotificationEvent event) {
        if (role == null) return;
        Set<WebSocketSession> sessions = roleSessions.get(role);
        if (sessions != null) {
            sendJsonToSessions(sessions, event);
        }
    }

    public void sendToDepartment(Long departementId, WebSocketNotificationEvent event) {
        if (departementId == null) return;
        Set<WebSocketSession> sessions = departmentSessions.get(departementId);
        if (sessions != null) {
            sendJsonToSessions(sessions, event);
        }
    }

    public void sendToAllStaff(WebSocketNotificationEvent event) {
        Set<WebSocketSession> targetSessions = new HashSet<>();
        addRoleSessions(targetSessions, Role.EMPLOYE);
        addRoleSessions(targetSessions, Role.STAFF);
        addRoleSessions(targetSessions, Role.PERSONNEL);
        addRoleSessions(targetSessions, Role.RECEPTIONNISTE);
        addRoleSessions(targetSessions, Role.ADMIN);
        sendJsonToSessions(targetSessions, event);
    }

    public void sendToReceptionAndAdmin(WebSocketNotificationEvent event) {
        Set<WebSocketSession> targetSessions = new HashSet<>();
        addRoleSessions(targetSessions, Role.RECEPTIONNISTE);
        addRoleSessions(targetSessions, Role.ADMIN);
        sendJsonToSessions(targetSessions, event);
    }

    public void broadcast(WebSocketNotificationEvent event) {
        sendJsonToSessions(allSessions, event);
    }

    private void addRoleSessions(Set<WebSocketSession> target, Role role) {
        Set<WebSocketSession> sessions = roleSessions.get(role);
        if (sessions != null) {
            target.addAll(sessions);
        }
    }

    private void sendJsonToSessions(Collection<WebSocketSession> sessions, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            TextMessage message = new TextMessage(json);

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        synchronized (session) {
                            session.sendMessage(message);
                        }
                    } catch (IOException e) {
                        log.warn("⚠️ WebSocket: Impossible d'envoyer le message à la session {}: {}", session.getId(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("❌ WebSocket: Erreur de sérialisation JSON: {}", e.getMessage());
        }
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null) return map;
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2) {
                map.put(pair[0], pair[1]);
            } else if (pair.length == 1) {
                map.put(pair[0], "");
            }
        }
        return map;
    }
}


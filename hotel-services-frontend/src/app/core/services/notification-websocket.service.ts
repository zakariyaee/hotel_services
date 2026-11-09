import { Injectable, signal, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { WsNotification, NotificationResponse } from '../models/notification.model';

const WS_URL = 'ws://localhost:8090/ws/notifications';
const API_URL = 'http://localhost:8090/api/notifications';

@Injectable({ providedIn: 'root' })
export class NotificationWebSocketService implements OnDestroy {
  // ── Signaux réactifs ───────────────────────────────────────────────────────
  readonly notifications = signal<WsNotification[]>([]);
  readonly unreadCount   = signal<number>(0);
  readonly connected     = signal<boolean>(false);

  private ws: WebSocket | null = null;
  private token: string | null = null;
  private departementId: number | null = null;
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null;
  private isExplicitlyClosed = false;

  constructor(private http: HttpClient) {}

  // ── Connexion ──────────────────────────────────────────────────────────────
  connect(token: string, departementId?: number | null): void {
    this.isExplicitlyClosed = false;
    this.token = token;
    this.departementId = departementId ?? null;

    // Charger immédiatement l'historique et le compteur depuis la BDD
    this.loadHistory();

    // Ouvrir le socket s'il n'est pas déjà actif
    if (!this.ws || this.ws.readyState === WebSocket.CLOSED || this.ws.readyState === WebSocket.CLOSING) {
      this.openSocket();
    }
  }

  loadHistory(): void {
    if (!this.token) return;

    this.fetchHistory().subscribe({
      next: (history) => {
        if (!Array.isArray(history)) return;
        const items: WsNotification[] = history.map(h => ({
          idNotification: h.idNotification,
          type: h.type || 'NOTIFICATION',
          titre: h.titre,
          message: h.message,
          idDemande: h.idDemande,
          referenceDemande: h.referenceDemande,
          timestamp: h.dateCreation,
          read: !!h.dateLecture,
        }));
        this.notifications.set(items);
      },
      error: (err) => console.warn('Notifications history fetch error:', err)
    });

    this.fetchUnreadCount().subscribe({
      next: (count) => this.unreadCount.set(count),
      error: () => {}
    });
  }

  private openSocket(): void {
    if (this.isExplicitlyClosed || !this.token) return;

    let url = `${WS_URL}?token=${encodeURIComponent(this.token)}`;
    if (this.departementId) url += `&departementId=${this.departementId}`;

    try {
      this.ws = new WebSocket(url);
    } catch {
      this.scheduleReconnect();
      return;
    }

    this.ws.onopen = () => {
      this.connected.set(true);
      this.clearReconnectTimer();
      // Envoyer un payload AUTH explicite
      this.send({ type: 'SUBSCRIBE', token: this.token, departementId: this.departementId });
    };

    this.ws.onmessage = (evt: MessageEvent<string>) => {
      try {
        const data: WsNotification = JSON.parse(evt.data);
        if (data.type === 'CONNECTED') {
          this.connected.set(true);
          return;
        }
        if (data.type === 'PONG') return;

        // Nouvelle notification reçue en temps réel
        this.notifications.update(list => [{ ...data, read: false }, ...list].slice(0, 100));
        this.unreadCount.update(n => n + 1);
      } catch {
        // Ignorer les trames mal formatées
      }
    };

    this.ws.onerror = () => {
      this.connected.set(false);
    };

    this.ws.onclose = () => {
      this.connected.set(false);
      if (!this.isExplicitlyClosed) {
        this.scheduleReconnect();
      }
    };
  }

  disconnect(): void {
    this.isExplicitlyClosed = true;
    this.clearReconnectTimer();
    if (this.ws) {
      try { this.ws.close(); } catch {}
      this.ws = null;
    }
    this.connected.set(false);
  }

  reset(): void {
    this.disconnect();
    this.isExplicitlyClosed = false;
    this.notifications.set([]);
    this.unreadCount.set(0);
    this.token = null;
    this.departementId = null;
  }

  markAllRead(): void {
    this.notifications.update(list => list.map(n => ({ ...n, read: true })));
    this.unreadCount.set(0);
    this.http.patch(`${API_URL}/mark-all-read`, {}).subscribe({
      error: (e) => console.warn('markAllRead error:', e)
    });
  }

  markOneRead(notif: WsNotification): void {
    if (notif.read) return;
    notif.read = true;
    this.unreadCount.update(n => Math.max(0, n - 1));
    if (notif.idNotification) {
      this.http.patch(`${API_URL}/${notif.idNotification}/read`, {}).subscribe({
        error: (e) => console.warn('markOneRead error:', e)
      });
    }
  }

  // ── API REST complémentaire ────────────────────────────────────────────────
  fetchHistory(): Observable<NotificationResponse[]> {
    return this.http.get<NotificationResponse[]>(API_URL);
  }

  fetchUnreadCount(): Observable<number> {
    return this.http.get<{ unreadCount: number }>(`${API_URL}/unread-count`).pipe(
      map(res => res?.unreadCount ?? 0)
    );
  }

  private send(payload: object): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(payload));
    }
  }

  private scheduleReconnect(): void {
    this.clearReconnectTimer();
    this.reconnectTimer = setTimeout(() => {
      if (!this.isExplicitlyClosed && this.token) {
        this.openSocket();
      }
    }, 4000);
  }

  private clearReconnectTimer(): void {
    if (this.reconnectTimer !== null) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
  }

  ngOnDestroy(): void {
    this.disconnect();
  }
}

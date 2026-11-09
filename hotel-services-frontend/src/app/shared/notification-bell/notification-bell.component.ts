import { Component, Input, signal, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationWebSocketService } from '../../core/services/notification-websocket.service';
import { WsNotification } from '../../core/models/notification.model';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="notif-bell-wrapper" [ngClass]="'variant-' + variant">
      <!-- Bouton cloche -->
      <button
        class="notif-bell-btn"
        (click)="toggle()"
        [class.active]="open()"
        aria-label="Centre de notifications"
      >
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none"
             stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
          <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
        </svg>
        @if (ws.unreadCount() > 0) {
          <span class="notif-badge">{{ ws.unreadCount() > 99 ? '99+' : ws.unreadCount() }}</span>
        }
      </button>

      <!-- Panneau déroulant -->
      @if (open()) {
        <div class="notif-panel" [class.align-left]="align === 'left'">
          <div class="notif-panel-header">
            <div class="header-title-box">
              <span class="notif-panel-title">Centre de Notifications</span>
              <span class="notif-panel-count">{{ ws.notifications().length }} message(s)</span>
            </div>
            @if (ws.unreadCount() > 0) {
              <button class="notif-mark-all" (click)="markAll()">Tout marquer comme lu</button>
            }
          </div>

          <div class="notif-list">
            @if (ws.notifications().length === 0) {
              <div class="notif-empty">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="1.5">
                  <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                  <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
                </svg>
                <p>Aucune notification pour le moment</p>
              </div>
            }
            @for (n of ws.notifications(); track n.idNotification ?? n.timestamp) {
              <div class="notif-item" [class.unread]="!n.read" (click)="read(n)">
                <div class="notif-icon" [ngClass]="iconClass(n.type)">
                  {{ iconEmoji(n.type) }}
                </div>
                <div class="notif-content">
                  <div class="notif-title">{{ n.titre }}</div>
                  <div class="notif-message">{{ n.message }}</div>
                  @if (n.timestamp) {
                    <div class="notif-time">{{ formatTime(n.timestamp) }}</div>
                  }
                </div>
                @if (!n.read) {
                  <div class="notif-dot" title="Non lue"></div>
                }
              </div>
            }
          </div>

          <!-- Indicateur de connexion -->
          <div class="notif-footer">
            <span class="conn-dot" [class.connected]="ws.connected()"></span>
            <span class="conn-text">{{ ws.connected() ? 'Connecté au temps réel' : 'Reconnexion en cours...' }}</span>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .notif-bell-wrapper {
      position: relative;
      display: inline-flex;
      align-items: center;
    }

    .notif-bell-btn {
      position: relative;
      width: 40px;
      height: 40px;
      border-radius: 12px;
      border: 1px solid rgba(0, 0, 0, 0.08);
      background: #ffffff;
      color: #1e293b;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s ease;
      box-shadow: 0 1px 3px rgba(0,0,0,0.06);
    }
    .notif-bell-btn:hover, .notif-bell-btn.active {
      background: #f8fafc;
      border-color: #cbd5e1;
      transform: translateY(-1px);
      box-shadow: 0 4px 10px rgba(0,0,0,0.08);
    }
    .notif-bell-btn svg { width: 20px; height: 20px; }

    /* Variant light (for dark headers) */
    .variant-light .notif-bell-btn {
      background: rgba(255, 255, 255, 0.12);
      border-color: rgba(255, 255, 255, 0.2);
      color: #ffffff;
      box-shadow: none;
    }
    .variant-light .notif-bell-btn:hover,
    .variant-light .notif-bell-btn.active {
      background: rgba(255, 255, 255, 0.22);
      border-color: rgba(255, 255, 255, 0.35);
    }

    .notif-badge {
      position: absolute;
      top: -3px;
      right: -3px;
      background: #ef4444;
      color: #ffffff;
      border-radius: 999px;
      font-size: 10px;
      font-weight: 800;
      min-width: 18px;
      height: 18px;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0 4px;
      border: 2px solid #ffffff;
      box-shadow: 0 2px 4px rgba(239, 68, 68, 0.3);
      animation: pulseBadge 2s infinite;
    }
    @keyframes pulseBadge {
      0%, 100% { transform: scale(1); }
      50% { transform: scale(1.1); }
    }

    .notif-panel {
      position: absolute;
      right: 0;
      top: calc(100% + 10px);
      width: 380px;
      max-width: calc(100vw - 32px);
      background: #ffffff;
      border-radius: 16px;
      box-shadow: 0 20px 50px rgba(15, 23, 42, 0.18), 0 0 0 1px rgba(15, 23, 42, 0.08);
      z-index: 99999;
      overflow: hidden;
      animation: slideDown 0.22s cubic-bezier(0.16, 1, 0.3, 1);
    }
    .notif-panel.align-left {
      right: auto;
      left: 0;
    }

    @keyframes slideDown {
      from { opacity: 0; transform: translateY(-8px) scale(0.98); }
      to   { opacity: 1; transform: translateY(0) scale(1); }
    }

    .notif-panel-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 16px 20px;
      background: #ffffff;
      border-bottom: 1px solid #f1f5f9;
    }
    .header-title-box {
      display: flex;
      flex-direction: column;
    }
    .notif-panel-title {
      font-weight: 800;
      font-size: 14px;
      color: #0f172a;
      letter-spacing: -0.01em;
    }
    .notif-panel-count {
      font-size: 11px;
      color: #64748b;
      margin-top: 1px;
    }
    .notif-mark-all {
      background: #f1f5f9;
      border: none;
      cursor: pointer;
      font-size: 11px;
      color: #4338ca;
      font-weight: 700;
      padding: 6px 10px;
      border-radius: 8px;
      transition: background 0.15s;
    }
    .notif-mark-all:hover {
      background: #e0e7ff;
    }

    .notif-list {
      max-height: 380px;
      overflow-y: auto;
    }

    .notif-empty {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 44px 20px;
      color: #94a3b8;
      gap: 12px;
      text-align: center;
    }
    .notif-empty svg { width: 36px; height: 36px; opacity: 0.4; }
    .notif-empty p { font-size: 13px; margin: 0; }

    .notif-item {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      padding: 14px 20px;
      border-bottom: 1px solid #f8fafc;
      cursor: pointer;
      transition: background 0.15s;
      position: relative;
    }
    .notif-item:hover { background: #f8fafc; }
    .notif-item.unread {
      background: #f5f7ff;
      border-left: 3px solid #6366f1;
    }

    .notif-icon {
      width: 36px;
      height: 36px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 17px;
      flex-shrink: 0;
      box-shadow: 0 1px 2px rgba(0,0,0,0.05);
    }
    .icon-new    { background: #ede9fe; }
    .icon-claim  { background: #dbeafe; }
    .icon-done   { background: #dcfce7; }
    .icon-reject { background: #fee2e2; }
    .icon-cancel { background: #fef3c7; }
    .icon-assign { background: #e0f2fe; }
    .icon-info   { background: #f1f5f9; }

    .notif-content {
      flex: 1;
      min-width: 0;
    }
    .notif-title {
      font-weight: 700;
      font-size: 13px;
      color: #0f172a;
      line-height: 1.35;
      margin-bottom: 3px;
      word-break: break-word;
    }
    .notif-message {
      font-size: 12px;
      color: #475569;
      line-height: 1.45;
      word-break: break-word;
    }
    .notif-time {
      font-size: 11px;
      color: #94a3b8;
      margin-top: 5px;
    }

    .notif-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #6366f1;
      flex-shrink: 0;
      margin-top: 4px;
    }

    .notif-footer {
      display: flex;
      align-items: center;
      gap: 7px;
      padding: 10px 20px;
      background: #fafafa;
      border-top: 1px solid #f1f5f9;
      font-size: 11px;
      color: #64748b;
    }
    .conn-dot {
      width: 7px;
      height: 7px;
      border-radius: 50%;
      background: #94a3b8;
      display: inline-block;
    }
    .conn-dot.connected {
      background: #22c55e;
      box-shadow: 0 0 6px rgba(34, 197, 94, 0.6);
    }
    .conn-text { font-weight: 500; }
  `]
})
export class NotificationBellComponent {
  @Input() variant: 'light' | 'dark' = 'dark';
  @Input() align: 'right' | 'left' = 'right';

  open = signal(false);

  constructor(public ws: NotificationWebSocketService) {}

  toggle(): void {
    this.open.update(v => !v);
    if (this.open()) {
      this.ws.loadHistory();
    }
  }

  markAll(): void { this.ws.markAllRead(); }

  read(n: WsNotification): void { this.ws.markOneRead(n); }

  iconEmoji(type: string): string {
    const map: Record<string, string> = {
      NOUVELLE_DEMANDE: '🔔',
      DEMANDE_CREEE_CLIENT: '✅',
      TACHE_PRISE_EN_CHARGE: '👷',
      TACHE_PRISE_EN_CHARGE_STAFF: '👷',
      TACHE_ASSIGNEE_EMPLOYE: '📋',
      TACHE_ASSIGNEE_CLIENT: '📋',
      TACHE_ASSIGNEE_RECEPTION: '📋',
      TACHE_TERMINEE: '✅',
      TACHE_TERMINEE_STAFF: '✅',
      TACHE_REJETEE: '❌',
      TACHE_ANNULEE: '⚠️',
    };
    return map[type] ?? '📣';
  }

  iconClass(type: string): string {
    if (type?.includes('NOUVELLE') || type?.includes('CREEE')) return 'notif-icon icon-new';
    if (type?.includes('PRISE_EN_CHARGE')) return 'notif-icon icon-claim';
    if (type?.includes('TERMINEE')) return 'notif-icon icon-done';
    if (type?.includes('REJETEE')) return 'notif-icon icon-reject';
    if (type?.includes('ANNULEE')) return 'notif-icon icon-cancel';
    if (type?.includes('ASSIGNEE')) return 'notif-icon icon-assign';
    return 'notif-icon icon-info';
  }

  formatTime(ts: string): string {
    try {
      const d = new Date(ts);
      const now = new Date();
      const diff = Math.floor((now.getTime() - d.getTime()) / 1000);
      if (diff < 60) return 'À l\'instant';
      if (diff < 3600) return `Il y a ${Math.floor(diff / 60)} min`;
      if (diff < 86400) return `Il y a ${Math.floor(diff / 3600)} h`;
      return d.toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' });
    } catch { return ''; }
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    const target = event.target as HTMLElement;
    if (!target.closest('app-notification-bell')) {
      this.open.set(false);
    }
  }
}

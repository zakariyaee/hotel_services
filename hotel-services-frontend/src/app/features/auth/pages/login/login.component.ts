import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { Html5Qrcode } from 'html5-qrcode';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent implements OnInit, OnDestroy {
  email = '';
  motDePasse = '';
  errorMessage = signal('');
  isLoading = signal(false);
  isAutoLogin = signal(false);

  // QR Code Scanner State
  showScanner = signal(false);
  scannerLoading = signal(false);
  scannerError = signal<string | null>(null);
  scannerSuccess = signal<string | null>(null);
  private html5QrCode: Html5Qrcode | null = null;

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // QR Code auto-login: /login?email=...&code=...&auto=true
    this.route.queryParams.subscribe(params => {
      const emailParam = params['email'];
      const codeParam = params['code'];
      const autoParam = params['auto'];

      if (autoParam === 'true' && emailParam && codeParam) {
        this.isAutoLogin.set(true);
        this.email = emailParam;
        this.motDePasse = codeParam;
        this.performLogin(emailParam, codeParam);
      }
    });
  }

  ngOnDestroy(): void {
    this.cleanupScanner();
  }

  /**
   * Ouvre la modale et démarre le scanner caméra
   */
  openQrScanner(): void {
    this.showScanner.set(true);
    this.scannerError.set(null);
    this.scannerSuccess.set(null);
    this.scannerLoading.set(true);

    // Laisse à Angular le temps de restituer le conteneur #qr-reader
    setTimeout(() => {
      this.startCamera();
    }, 150);
  }

  /**
   * Ferme la modale du scanner
   */
  closeQrScanner(): void {
    this.cleanupScanner();
    this.showScanner.set(false);
    this.scannerError.set(null);
    this.scannerSuccess.set(null);
    this.scannerLoading.set(false);
  }

  private async startCamera(): Promise<void> {
    try {
      this.html5QrCode = new Html5Qrcode('qr-reader');
      await this.html5QrCode.start(
        { facingMode: 'environment' },
        {
          fps: 10,
          qrbox: (viewfinderWidth: number, viewfinderHeight: number) => {
            const minEdge = Math.min(viewfinderWidth, viewfinderHeight);
            const box = Math.max(180, Math.floor(minEdge * 0.72));
            return { width: box, height: box };
          },
        },
        (decodedText: string) => {
          this.handleQrDecoded(decodedText);
        },
        () => {
          // Erreur d'analyse continue de flux vidéo ignorée
        }
      );
      this.scannerLoading.set(false);
    } catch (err: any) {
      console.warn('Erreur activation caméra:', err);
      this.scannerLoading.set(false);
      this.scannerError.set(
        "Impossible d'accéder à la caméra. Vérifiez les autorisations de votre navigateur ou importez une image de votre QR Code."
      );
    }
  }

  /**
   * Scan alternatif via sélection d'une image/fichier QR Code
   */
  async onQrFileSelected(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];

    this.scannerLoading.set(true);
    this.scannerError.set(null);

    try {
      if (!this.html5QrCode) {
        this.html5QrCode = new Html5Qrcode('qr-reader');
      }
      const decodedText = await this.html5QrCode.scanFile(file, true);
      this.handleQrDecoded(decodedText);
    } catch (err) {
      this.scannerLoading.set(false);
      this.scannerError.set("Aucun QR Code valide n'a été détecté dans cette image.");
    } finally {
      input.value = '';
    }
  }

  /**
   * Traitement du contenu décodé du QR Code
   */
  private async handleQrDecoded(decodedText: string): Promise<void> {
    if (!decodedText || this.scannerSuccess()) return;

    this.scannerSuccess.set('Pass QR Code détecté ! Connexion en cours...');
    await this.cleanupScanner();

    // 1. URL d'auto-login complète (ex: http://.../login?email=...&code=...)
    try {
      const url = new URL(decodedText);
      const email = url.searchParams.get('email');
      const code = url.searchParams.get('code');
      if (email && code) {
        this.showScanner.set(false);
        this.email = email;
        this.motDePasse = code;
        this.performLogin(email, code);
        return;
      }
    } catch {
      // Ce n'est pas une URL standard, on continue l'analyse
    }

    // 2. Format JSON (ex: { "email": "...", "code": "..." })
    if (decodedText.trim().startsWith('{')) {
      try {
        const data = JSON.parse(decodedText);
        if (data.email && (data.code || data.motDePasse)) {
          this.showScanner.set(false);
          const email = data.email;
          const code = data.code || data.motDePasse;
          this.email = email;
          this.motDePasse = code;
          this.performLogin(email, code);
          return;
        }
      } catch {
        // format non JSON
      }
    }

    // 3. Code d'accès direct de séjour (ex: RES-948210)
    const trimmed = decodedText.trim();
    if (trimmed.startsWith('RES-') || trimmed.length === 10) {
      this.showScanner.set(false);
      this.isLoading.set(true);
      this.authService.guestLogin(trimmed).subscribe({
        next: () => {
          this.isLoading.set(false);
          this.router.navigate(['/client']);
        },
        error: () => {
          this.isLoading.set(false);
          this.errorMessage.set('Code de séjour invalide ou expiré.');
        },
      });
      return;
    }

    // 4. Format délimité simple email:code ou email|code
    const parts = trimmed.split(/[:|;]/);
    if (parts.length >= 2 && parts[0].includes('@')) {
      this.showScanner.set(false);
      this.email = parts[0].trim();
      this.motDePasse = parts[1].trim();
      this.performLogin(this.email, this.motDePasse);
      return;
    }

    // Contenu inconnu
    this.scannerSuccess.set(null);
    this.scannerError.set("Le QR Code scanné n'est pas un pass de séjour valide.");
  }

  private async cleanupScanner(): Promise<void> {
    if (this.html5QrCode) {
      try {
        if (this.html5QrCode.isScanning) {
          await this.html5QrCode.stop();
        }
        this.html5QrCode.clear();
      } catch (err) {
        console.warn('Erreur arrêt scanner:', err);
      }
      this.html5QrCode = null;
    }
  }

  private performLogin(email: string, password: string): void {
    this.isLoading.set(true);
    this.authService.login({ email: email.trim(), motDePasse: password.trim() }).subscribe({
      next: user => {
        this.isLoading.set(false);
        this.isAutoLogin.set(false);
        this.router.navigate([this.routeForRole(user.role)]);
      },
      error: err => {
        this.isLoading.set(false);
        this.isAutoLogin.set(false);
        const msg = err.error?.message ?? 'Identifiant ou mot de passe incorrect.';
        this.errorMessage.set(msg);
      },
    });
  }

  login(): void {
    this.errorMessage.set('');
    if (!this.email.trim() || !this.motDePasse.trim()) {
      this.errorMessage.set('Veuillez renseigner votre email et mot de passe.');
      return;
    }
    this.performLogin(this.email, this.motDePasse);
  }

  fillDemo(email: string, pass: string): void {
    this.email = email;
    this.motDePasse = pass;
    this.errorMessage.set('');
  }

  private routeForRole(role: string): string {
    switch (role.toUpperCase()) {
      case 'ADMIN': return '/admin';
      case 'PERSONNEL': return '/staff/personnel';
      case 'RECEPTIONNISTE': return '/staff/reception';
      case 'EMPLOYE':
      case 'STAFF': return '/staff/employe';
      default: return '/client';
    }
  }
}


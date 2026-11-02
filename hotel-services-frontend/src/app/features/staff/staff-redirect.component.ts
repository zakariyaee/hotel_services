import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-staff-redirect',
  standalone: true,
  template: '',
})
export class StaffRedirectComponent implements OnInit {
  constructor(private auth: AuthService, private router: Router) {}

  ngOnInit(): void {
    const role = (this.auth.getRole() || '').toUpperCase();
    switch (role) {
      case 'PERSONNEL':
        this.router.navigate(['/staff/personnel']);
        break;
      case 'RECEPTIONNISTE':
        this.router.navigate(['/staff/reception']);
        break;
      case 'EMPLOYE':
      case 'STAFF':
      default:
        this.router.navigate(['/staff/employe']);
        break;
    }
  }
}


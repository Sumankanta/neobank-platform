import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Sidebar } from '../sidebar/sidebar';
import { Alert } from '../alert/alert';
import { AuthService } from '../../../core/services/auth';
import { TopbarComponent } from '../topbar/topbar';

@Component({
  selector: 'app-layout',
  imports: [RouterOutlet, Sidebar, Alert, TopbarComponent],
  templateUrl: './layout.html',
  styleUrl: './layout.css',
})
export class Layout {
  authService = inject(AuthService);
}

import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-auth-layout',
  imports: [RouterOutlet],
  template: `
    <div class="auth-page">
      <router-outlet></router-outlet>
    </div>
  `,
  styles: [`
    .auth-page {
      min-height: 100vh;
      width: 100vw;
      background: #eef6ff;
    }
  `]
})
export class AuthLayout {}

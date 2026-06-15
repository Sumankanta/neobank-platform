import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './topbar.html',
  styleUrl: './topbar.css'
})
export class TopbarComponent {
  authService = inject(AuthService);
  router = inject(Router);
  
  searchQuery = '';
  currentDate = new Date().toLocaleDateString('en-IN', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });

  logout() {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }

  onSearch() {
    if (this.searchQuery.trim()) {
      // For now, just a console log or navigate to a search page if it existed
      console.log('Searching for:', this.searchQuery);
    }
  }
}

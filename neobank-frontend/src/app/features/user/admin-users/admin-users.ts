import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService } from '../../../core/services/user';
import { User } from '../../../core/models/user';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { ToastService } from '../../../core/services/toast';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [
    CommonModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatIconModule,
    MatButtonModule,
  ],
  templateUrl: './admin-users.html',
  styleUrl: './admin-users.css',
})
export class AdminUsers implements OnInit {
  private userService = inject(UserService);
  private toastService = inject(ToastService);

  users = signal<User[]>([]);
  isLoading = signal(true);

  displayedColumns: string[] = ['name', 'email', 'role', 'status', 'joined', 'actions'];

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.isLoading.set(true);
    this.userService.getAdminUsers().subscribe({
      next: (users) => {
        this.users.set(users);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  toggleUserStatus(user: User): void {
    this.userService.toggleUserStatus(user.id).subscribe({
      next: (updatedUser) => {
        this.users.update(users => users.map(u => u.id === updatedUser.id ? updatedUser : u));
        this.toastService.success(`User ${updatedUser.isActive ? 'activated' : 'deactivated'} successfully`);
      },
      error: () => this.toastService.error('Failed to update user status')
    });
  }
}

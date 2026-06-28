import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../core/services/admin';
import { ToastService } from '../../../core/services/toast';

@Component({
  selector: 'app-admin-accounts',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-accounts.html',
  styleUrl: './admin-accounts.css',
})
export class AdminAccounts implements OnInit {
  private adminService = inject(AdminService);
  private toastService = inject(ToastService);

  allUsers = signal<any[]>([]);
  pendingUsers = signal<any[]>([]);
  activeTab = signal<'all' | 'pending'>('pending');
  isLoading = signal(true);
  searchQuery = signal('');

  filteredUsers = computed(() => {
    const q = this.searchQuery().toLowerCase();
    const list = this.activeTab() === 'pending' ? this.pendingUsers() : this.allUsers();
    if (!q) return list;
    return list.filter(u =>
      u.fullName?.toLowerCase().includes(q) ||
      u.email?.toLowerCase().includes(q) ||
      u.role?.toLowerCase().includes(q)
    );
  });

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading.set(true);
    this.adminService.getAllUsers().subscribe({
      next: (users) => {
        this.allUsers.set(users);
        this.pendingUsers.set(users.filter((u: any) => !u.active));
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  toggleStatus(user: any): void {
    this.adminService.toggleUserStatus(user.id).subscribe({
      next: (updated) => {
        this.allUsers.update(list => list.map(u => u.id === updated.id ? updated : u));
        this.pendingUsers.set(this.allUsers().filter((u: any) => !u.active));
        this.toastService.success(`User ${updated.active ? 'activated' : 'deactivated'} successfully`);
      },
      error: () => this.toastService.error('Failed to update user status'),
    });
  }

  setTab(tab: 'all' | 'pending'): void {
    this.activeTab.set(tab);
  }
}

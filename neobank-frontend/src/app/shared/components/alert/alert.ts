import { Component, inject } from '@angular/core';
import { ToastService, ToastMessage } from '../../../core/services/toast';

@Component({
  selector: 'app-alert',
  imports: [],
  templateUrl: './alert.html',
  styleUrl: './alert.css',
})
export class Alert {
  toastService = inject(ToastService);

  getIcon(type: ToastMessage['type']): string {
    switch (type) {
      case 'success': return 'fa-circle-check';
      case 'error': return 'fa-circle-xmark';
      case 'warning': return 'fa-circle-exclamation';
      default: return 'fa-circle-info';
    }
  }
}

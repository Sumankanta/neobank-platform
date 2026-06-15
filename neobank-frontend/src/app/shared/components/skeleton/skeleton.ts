import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-skeleton',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div 
      class="skeleton-loader" 
      [ngClass]="type" 
      [style.width]="width" 
      [style.height]="height"
      [style.border-radius]="borderRadius"
    ></div>
  `,
  styles: [`
    .skeleton-loader {
      background: linear-gradient(
        90deg,
        rgba(255, 255, 255, 0.03) 25%,
        rgba(255, 255, 255, 0.08) 37%,
        rgba(255, 255, 255, 0.03) 63%
      );
      background-size: 400% 100%;
      animation: skeleton-loading 1.4s ease infinite;
    }

    @keyframes skeleton-loading {
      0% { background-position: 100% 50%; }
      100% { background-position: 0% 50%; }
    }

    .text { height: 1rem; margin-bottom: 0.5rem; border-radius: 4px; }
    .title { height: 1.5rem; margin-bottom: 1rem; border-radius: 6px; width: 60%; }
    .avatar { width: 40px; height: 40px; border-radius: 50%; }
    .card { height: 200px; border-radius: 16px; width: 100%; }
    .rect { border-radius: 8px; }
  `]
})
export class SkeletonComponent {
  @Input() type: 'text' | 'title' | 'avatar' | 'card' | 'rect' = 'text';
  @Input() width: string = '100%';
  @Input() height: string = '';
  @Input() borderRadius: string = '';
}

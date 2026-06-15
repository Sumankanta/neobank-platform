import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { RewardService } from '../../../core/services/reward';
import { AuthService } from '../../../core/services/auth';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format-pipe';

@Component({
  selector: 'app-rewards-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  rewardService = inject(RewardService);
  authService = inject(AuthService);
  router = inject(Router);

  isLoading = signal(false);

  ngOnInit(): void {
    this.refreshPoints();
  }

  refreshPoints(): void {
    this.isLoading.set(true);
    this.rewardService.refreshBalance();
    setTimeout(() => this.isLoading.set(false), 800);
  }

  getTier(): string {
    const points = this.rewardService.pointsBalance();
    if (points >= 35000) return 'Platinum Elite';
    if (points >= 15000) return 'Gold Elite';
    return 'Silver Elite';
  }

  getPointsForNextTier(): number {
    const points = this.rewardService.pointsBalance();
    if (points >= 35000) return 0;
    if (points >= 15000) return 35000;
    return 15000;
  }

  getTierProgress(): number {
    const points = this.rewardService.pointsBalance();
    if (points >= 35000) return 100;
    
    let currentTierBase = 0;
    let nextTierPoints = 15000;

    if (points >= 15000) {
      currentTierBase = 15000;
      nextTierPoints = 35000;
    }

    const range = nextTierPoints - currentTierBase;
    const progress = points - currentTierBase;
    
    return (progress / range) * 100;
  }

  getTierIcon(): string {
    const tier = this.getTier();
    if (tier === 'Platinum Elite') return 'fa-solid fa-gem';
    if (tier === 'Gold Elite') return 'fa-solid fa-crown';
    return 'fa-solid fa-medal';
  }

  getTierColorClass(): string {
    const tier = this.getTier();
    if (tier === 'Platinum Elite') return 'text-primary';
    if (tier === 'Gold Elite') return 'text-warning';
    return 'text-secondary';
  }

  getTierBgClass(): string {
    const tier = this.getTier();
    if (tier === 'Platinum Elite') return 'tier-platinum-gradient';
    if (tier === 'Gold Elite') return 'tier-gold-gradient';
    return 'bg-surface-container';
  }

  navToGallery(): void {
    this.router.navigate(['/rewards/gallery']);
  }
}

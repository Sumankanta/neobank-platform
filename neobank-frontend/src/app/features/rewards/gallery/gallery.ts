import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { RewardService } from '../../../core/services/reward';

@Component({
  selector: 'app-rewards-gallery',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './gallery.html',
  styleUrl: './gallery.css',
})
export class Gallery {
  rewardService = inject(RewardService);
  router = inject(Router);

  getTier(): string {
    const points = this.rewardService.pointsBalance();
    if (points >= 35000) return 'Platinum Elite';
    if (points >= 15000) return 'Gold Elite';
    return 'Silver Elite';
  }

  getTierProgressToPlatinum(): number {
    const points = this.rewardService.pointsBalance();
    if (points >= 35000) return 100;
    return (points / 35000) * 100;
  }

  claimReward(cost: number, rewardName: string): void {
    if (this.rewardService.pointsBalance() >= cost) {
      // In a real app we'd call the backend to deduct points
      // We will route to success screen
      this.router.navigate(['/rewards/success'], { queryParams: { reward: rewardName, cost: cost } });
    } else {
      alert('Insufficient points to claim this reward.');
    }
  }

  navBack(): void {
    this.router.navigate(['/rewards/dashboard']);
  }
}

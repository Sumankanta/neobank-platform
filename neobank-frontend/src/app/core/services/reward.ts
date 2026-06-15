import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { ApiEndpoints } from '../utils/api-endpoints';
import { Reward } from '../models/reward';
import { StorageUtil } from '../utils/storage.util';

@Injectable({
  providedIn: 'root'
})
export class RewardService {
  private http = inject(HttpClient);
  pointsBalance = signal<number>(0);

  getBalance(userId: number | string): Observable<Reward> {
    return this.http.get<Reward>(ApiEndpoints.rewards.balance(userId)).pipe(
      tap((res) => {
        this.pointsBalance.set(res.pointsBalance);
      })
    );
  }

  refreshBalance(): void {
    const user = StorageUtil.getUser();
    if (user) {
      this.getBalance(user.userId).subscribe({
        error: () => {}
      });
    }
  }
}

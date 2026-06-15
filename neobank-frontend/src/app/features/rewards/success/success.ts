import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { RewardService } from '../../../core/services/reward';

@Component({
  selector: 'app-rewards-success',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './success.html',
  styleUrl: './success.css',
})
export class Success implements OnInit {
  route = inject(ActivatedRoute);
  router = inject(Router);
  rewardService = inject(RewardService);

  rewardName = '';
  rewardCost = 0;
  claimCode = '';

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.rewardName = params['reward'] || 'Mystery Reward';
      this.rewardCost = params['cost'] ? Number(params['cost']) : 0;
      
      // Generate a random claim code
      const randomPart = Math.random().toString(36).substring(2, 10).toUpperCase();
      this.claimCode = `NEO-${randomPart}-2024`;
      
      // Note: in a real app, deduction happens on backend before routing here.
      // We are just displaying the mock state.
      
      this.initConfetti();
    });
  }

  getTierProgressToPlatinum(): number {
    const points = this.rewardService.pointsBalance();
    if (points >= 35000) return 100;
    return (points / 35000) * 100;
  }

  navToDashboard(): void {
    this.router.navigate(['/rewards/dashboard']);
  }

  copyCode(): void {
    navigator.clipboard.writeText(this.claimCode).then(() => {
      alert('Code copied to clipboard!');
    });
  }

  initConfetti(): void {
    // Simple confetti script
    const canvas = document.getElementById('confetti') as HTMLCanvasElement;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;

    let particles: any[] = [];
    const colors = ['#000000', '#7c580f', '#ffcc7a', '#bec6e0', '#86efac'];

    class Particle {
        x = Math.random() * canvas.width;
        y = Math.random() * canvas.height - canvas.height;
        size = Math.random() * 8 + 4;
        speedX = Math.random() * 3 - 1.5;
        speedY = Math.random() * 3 + 2;
        color = colors[Math.floor(Math.random() * colors.length)];
        rotation = Math.random() * 360;
        rotationSpeed = Math.random() * 5 - 2.5;

        update() {
            this.y += this.speedY;
            this.x += this.speedX;
            this.rotation += this.rotationSpeed;
            if (this.y > canvas.height) {
                this.y = -20;
                this.x = Math.random() * canvas.width;
            }
        }

        draw() {
            if (!ctx) return;
            ctx.save();
            ctx.translate(this.x, this.y);
            ctx.rotate(this.rotation * Math.PI / 180);
            ctx.fillStyle = this.color;
            ctx.fillRect(-this.size/2, -this.size/2, this.size, this.size);
            ctx.restore();
        }
    }

    for (let i = 0; i < 60; i++) {
        particles.push(new Particle());
    }

    const animate = () => {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        particles.forEach(p => {
            p.update();
            p.draw();
        });
        requestAnimationFrame(animate);
    };

    animate();

    setTimeout(() => {
        particles = []; // Stop after 5s
    }, 5000);
  }
}

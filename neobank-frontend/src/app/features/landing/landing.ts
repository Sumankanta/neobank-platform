import { AfterViewInit, Component, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
// @ts-ignore - browser-only animation helper loaded for the landing page chunk.
import { initLandingAnimations } from './landing-animations.js';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [RouterLink, FormsModule],
  templateUrl: './landing.html',
  styleUrl: './landing.css',
})
export class Landing implements AfterViewInit, OnDestroy {
  menuOpen = false;
  newsletterEmail = '';

  private observer?: IntersectionObserver;
  private animationCleanup?: () => void;

  ngAfterViewInit(): void {
    this.observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            entry.target.classList.add('is-visible');
            this.observer?.unobserve(entry.target);
          }
        }
      },
      { threshold: 0.16, rootMargin: '0px 0px -8% 0px' },
    );

    document.querySelectorAll<HTMLElement>('[data-reveal]').forEach((el) => {
      this.observer?.observe(el);
    });

    this.animationCleanup = initLandingAnimations();
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
    this.animationCleanup?.();
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  closeMenu(): void {
    this.menuOpen = false;
  }

  scrollToSection(event: Event, targetId: string): void {
    event.preventDefault();
    this.closeMenu();
    document.getElementById(targetId)?.scrollIntoView({
      behavior: 'smooth',
      block: 'start',
    });
  }

  onNewsletterSubmit(): void {
    const email = this.newsletterEmail.trim();

    if (!email) {
      return;
    }

    console.log('Newsletter subscribed:', email);
    this.newsletterEmail = '';
  }
}





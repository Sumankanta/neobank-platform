import { Component, AfterViewInit, OnDestroy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [RouterLink, FormsModule],
  templateUrl: './landing.html',
  styleUrl: './landing.css',
})
export class Landing implements AfterViewInit, OnDestroy {
  private scrollHandler = () => this.revealElements();
  menuOpen = false;

  ngAfterViewInit(): void {
    window.addEventListener('scroll', this.scrollHandler, { passive: true });
    this.revealElements();

    // Smooth anchor scroll
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
      anchor.addEventListener('click', (e) => {
        const href = (anchor as HTMLAnchorElement).getAttribute('href');
        if (href && href !== '#') {
          e.preventDefault();
          document.querySelector(href)?.scrollIntoView({ behavior: 'smooth' });
        }
      });
    });
  }

  ngOnDestroy(): void {
    window.removeEventListener('scroll', this.scrollHandler);
  }

  private revealElements(): void {
    const vh = window.innerHeight;
    document.querySelectorAll<HTMLElement>('.reveal:not(.v)').forEach(el => {
      if (el.getBoundingClientRect().top < vh - 80) {
        el.classList.add('v');
      }
    });
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
    const links = document.querySelector<HTMLElement>('.nav-links');
    if (links) {
      links.style.display = this.menuOpen ? 'flex' : '';
      links.style.flexDirection = 'column';
      links.style.position = 'absolute';
      links.style.top = '68px';
      links.style.left = '0';
      links.style.right = '0';
      links.style.background = 'rgba(2,8,19,0.97)';
      links.style.padding = '20px 24px';
      links.style.borderBottom = '1px solid rgba(255,255,255,0.07)';
      links.style.gap = '20px';
    }
  }

  onNewsletterSubmit(): void {
    // TODO: wire to your newsletter API
    console.log('Newsletter subscribed');
  }
}

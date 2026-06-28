import { Injectable, signal, effect } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  isDarkMode = signal<boolean>(this._loadTheme());

  constructor() {
    effect(() => {
      this._applyTheme(this.isDarkMode());
    });
  }

  toggle() {
    this.isDarkMode.update(v => !v);
    localStorage.setItem('neobank-theme', this.isDarkMode() ? 'dark' : 'light');
  }

  private _loadTheme(): boolean {
    const saved = localStorage.getItem('neobank-theme');
    return saved ? saved === 'dark' : true; // default dark
  }

  private _applyTheme(dark: boolean) {
    if (dark) {
      document.body.classList.remove('light-theme');
    } else {
      document.body.classList.add('light-theme');
    }
  }
}

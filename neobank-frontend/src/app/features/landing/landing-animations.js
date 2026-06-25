export function initLandingAnimations() {
  const root = document.querySelector('.landing-shell');
  if (!root) {
    return () => {};
  }

  const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  const cleanups = [];

  const scrollBar = document.createElement('div');
  scrollBar.className = 'scroll-progress';
  document.body.prepend(scrollBar);

  function updateProgress() {
    const total = document.documentElement.scrollHeight - window.innerHeight;
    const value = total > 0 ? (window.scrollY / total) * 100 : 0;
    scrollBar.style.width = `${Math.max(0, Math.min(100, value))}%`;
  }

  window.addEventListener('scroll', updateProgress, { passive: true });
  updateProgress();
  cleanups.push(() => window.removeEventListener('scroll', updateProgress));
  cleanups.push(() => scrollBar.remove());

  const heroVisual = root.querySelector('.hero-visual');
  const transferCard = root.querySelector('.transfer-card');
  const insightCard = root.querySelector('.insight-card');

  if (heroVisual && transferCard && insightCard && !prefersReducedMotion) {
    const onMove = (event) => {
      const rect = heroVisual.getBoundingClientRect();
      const offsetX = (event.clientX - rect.left) / rect.width - 0.5;
      const offsetY = (event.clientY - rect.top) / rect.height - 0.5;

      transferCard.style.transform = `translate(${offsetX * -12}px, ${offsetY * -10}px) rotate(-0.8deg)`;
      insightCard.style.transform = `translate(${offsetX * 12}px, ${offsetY * 10}px) rotate(0.8deg)`;
    };

    const onLeave = () => {
      transferCard.style.transform = '';
      insightCard.style.transform = '';
    };

    heroVisual.addEventListener('mousemove', onMove);
    heroVisual.addEventListener('mouseleave', onLeave);
    cleanups.push(() => heroVisual.removeEventListener('mousemove', onMove));
    cleanups.push(() => heroVisual.removeEventListener('mouseleave', onLeave));
  }

  const bars = Array.from(root.querySelectorAll('.mini-bars span'));
  bars.forEach((bar, index) => {
    if (!bar.hasAttribute('data-val')) {
      bar.setAttribute('data-val', ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'][index] || `W${index + 1}`);
    }
  });

  const navLinks = Array.from(root.querySelectorAll('.nav-links a[href^="#"]'));
  const sections = Array.from(root.querySelectorAll('section[id]'));

  function setActiveLink(id) {
    navLinks.forEach((link) => {
      const href = link.getAttribute('href')?.slice(1);
      link.classList.toggle('is-active', href === id);
    });
  }

  const sectionObserver = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (!entry.isIntersecting) return;
        const target = entry.target;
        target.classList.add('is-visible');

        if (target.matches('section[id]')) {
          setActiveLink(target.id);
        }
      });
    },
    { threshold: 0.22, rootMargin: '0px 0px -20% 0px' },
  );

  sections.forEach((section) => sectionObserver.observe(section));
  root.querySelectorAll('.reveal, .eyebrow-pill, .section-eyebrow, .feature-card, .step-card, .security-panel, .cta-card, .newsletter-card, .trust-grid, .footer-grid').forEach((el) => {
    sectionObserver.observe(el);
  });
  cleanups.push(() => sectionObserver.disconnect());

  const trustNumbers = Array.from(root.querySelectorAll('.trust-grid strong'));

  function parseDisplayValue(text) {
    const trimmed = text.trim();
    if (trimmed.includes('/')) {
      return null;
    }

    const match = trimmed.match(/^([^\d]*)([\d.,]+)(.*)$/);
    if (!match) return null;

    return {
      prefix: match[1] || '',
      number: match[2] || '',
      suffix: match[3] || '',
    };
  }

  function animateCounter(el, display) {
    const target = Number(display.number.replace(/,/g, ''));
    if (!Number.isFinite(target)) return;

    const decimals = display.number.includes('.') ? display.number.split('.')[1].length : 0;
    const duration = 1800;
    const start = performance.now();
    el.classList.add('is-counting');

    function tick(now) {
      const progress = Math.min((now - start) / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3);
      const current = target * eased;
      const formatted = decimals > 0 ? current.toFixed(decimals) : Math.round(current).toLocaleString('en-IN');
      el.textContent = `${display.prefix}${formatted}${display.suffix}`.trim();

      if (progress < 1) {
        requestAnimationFrame(tick);
      } else {
        el.textContent = `${display.prefix}${display.number}${display.suffix}`.trim();
        el.classList.remove('is-counting');
      }
    }

    requestAnimationFrame(tick);
  }

  const countObserver = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (!entry.isIntersecting) return;
        const el = entry.target;
        const raw = el.getAttribute('data-count');
        const display = parseDisplayValue(raw || '');
        if (display) {
          animateCounter(el, display);
        }
        countObserver.unobserve(el);
      });
    },
    { threshold: 0.6 },
  );

  trustNumbers.forEach((el) => {
    const text = el.textContent?.trim() || '';
    const parsed = parseDisplayValue(text);
    if (!parsed) {
      return;
    }

    el.setAttribute('data-count', text);
    el.textContent = text;
    countObserver.observe(el);
  });
  cleanups.push(() => countObserver.disconnect());

  if (prefersReducedMotion) {
    scrollBar.style.display = 'none';
  }

  return () => {
    cleanups.forEach((fn) => fn());
  };
}

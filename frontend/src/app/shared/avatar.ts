import { Component, computed, input } from '@angular/core';

/** Avatar à initiales, couleur stable dérivée du nom. */
@Component({
  selector: 'app-avatar',
  template: `<span class="avatar" [style.width.px]="size()" [style.height.px]="size()"
    [style.font-size.px]="size() * 0.4" [style.background]="color()">{{ initials() }}</span>`,
  styles: `.avatar { display:inline-flex; align-items:center; justify-content:center; border-radius:50%;
    color:#fff; font-weight:600; flex-shrink:0; letter-spacing:.02em; }`
})
export class Avatar {
  name = input.required<string>();
  size = input(40);
  initials = computed(() => this.name().split(/\s+/).filter(Boolean).slice(0, 2).map(w => w[0]).join('').toUpperCase());
  color = computed(() => {
    let h = 0;
    for (const c of this.name()) h = (h * 31 + c.charCodeAt(0)) % 360;
    return `hsl(${h} 55% 45%)`;
  });
}

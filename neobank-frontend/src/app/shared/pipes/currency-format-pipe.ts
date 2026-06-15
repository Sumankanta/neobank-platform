import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'currencyFormat',
  standalone: true
})
export class CurrencyFormatPipe implements PipeTransform {
  transform(value: number | string | null | undefined, currencyCode: string = 'INR'): string {
    if (value === null || value === undefined) return '';
    
    const amount = typeof value === 'string' ? parseFloat(value) : value;
    
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: currencyCode,
    }).format(amount);
  }
}

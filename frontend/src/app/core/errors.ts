import { HttpErrorResponse } from '@angular/common/http';

export function errorMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.error?.message) return err.error.message;
    if (err.status === 0) return 'Serveur injoignable. Vérifiez que le backend est démarré.';
  }
  return 'Une erreur est survenue.';
}

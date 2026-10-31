
import { inject, Injectable, signal } from '@angular/core';
import { HttpService } from '../../backend/http.service';
import { LoginDto } from '../../../dtos/login/LoginDto';
import { HttpErrorResponse } from '@angular/common/http';
import { catchError, EMPTY, finalize, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LoginStateService {

  // ============================================================
  // INJECTIONS
  // ============================================================

  private http = inject(HttpService);

  // ============================================================
  // LOGIN SIGNALS
  // ============================================================

  private _loginLoading = signal(false);
  private _loginMessage = signal('');
  private _loginStatus = signal<'success' | 'error' | 'default'>('default');

  // LOGIN SIGNALS READONLY
  public readonly loginLoading = this._loginLoading.asReadonly();
  public readonly loginMessage = this._loginMessage.asReadonly();
  public readonly loginStatus = this._loginStatus.asReadonly();

  // ============================================================
  // LOGOUT SIGNALS
  // ============================================================

  private _logoutLoading = signal(false);
  private _logoutMessage = signal('');
  private _logoutStatus = signal<'success' | 'error' | 'default'>('default');

  // LOGOUT SIGNALS READONLY
  public readonly logoutLoading = this._logoutLoading.asReadonly();
  public readonly logoutMessage = this._logoutMessage.asReadonly();
  public readonly logoutStatus = this._logoutStatus.asReadonly();

  // ============================================================
  // LOGIN
  // ============================================================

  login(request: LoginDto) {

    this._loginLoading.set(true);

    this.http.login(request)
      .pipe(
        tap((response) => {

          // Salva somente o accessToken
          localStorage.setItem('accessToken', response.accessToken);

          this._loginMessage.set('Login feito com sucesso');
          this._loginStatus.set('success');
        }),

        catchError((error: HttpErrorResponse) => {
          this._loginMessage.set(
            error.error?.message || 'Erro ao tentar logar'
          );

          this._loginStatus.set('error');

          return EMPTY;
        }),

        finalize(() => {
          this._loginLoading.set(false);
          console.log('Requisição de login finalizada.');
        })
      )
      .subscribe();
  }

  // ============================================================
  // LOGOUT
  // ============================================================

  logout() {

    this._logoutLoading.set(true);

    this.http.logout()
      .pipe(
        tap(() => {

          this._logoutMessage.set('Logout realizado com sucesso');
          this._logoutStatus.set('success');

          localStorage.removeItem('accessToken');
        }),

        catchError((error: HttpErrorResponse) => {
          this._logoutMessage.set(
            error.error?.message || 'Erro ao realizar logout'
          );

          this._logoutStatus.set('error');

          return EMPTY;
        }),

        finalize(() => {
          this._logoutLoading.set(false);
          console.log('Requisição de logout finalizada.');
        })
      )
      .subscribe();
  }

  // ============================================================
  // RESET LOGIN STATUS
  // ============================================================

  resetLoginStatus() {
    this._loginStatus.set('default');
    this._loginMessage.set('');
  }

  // ============================================================
  // RESET LOGOUT STATUS
  // ============================================================

  resetLogoutStatus() {
    this._logoutStatus.set('default');
    this._logoutMessage.set('');
  }

  // ============================================================
  // RESET ALL STATUS
  // ============================================================

  resetStatus() {
    this.resetLoginStatus();
    this.resetLogoutStatus();
  }

}

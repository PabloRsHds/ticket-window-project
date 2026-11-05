import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpService } from '../../backend/http.service';
import { ResponseAllUsersDto } from '../../../dtos/users/ResponseAllUsersDto';
import { RequestUserDto } from '../../../dtos/users/RequestUserDto';
import { ResponseUserInfoDto } from '../../../dtos/users/ResponseUserInfoDto';
import { UpdateUserDto } from '../../../dtos/users/UpdateUserDto';
import { ResponseCountTotalUsersStatisticsDto } from '../../../dtos/users/statistics/ResponseCountTotalUsersStatisticsDto';
import { ResponseUserPercentagesStatisticsDto } from '../../../dtos/users/statistics/ResponseUserPercentagesStatisticsDto';
import { ResponseUsersCreatedByMonthStatisticsDto } from '../../../dtos/users/statistics/ResponseUsersCreatedByMonthStatisticsDto';
import { ResponseServicesByUserStatisticsDto } from '../../../dtos/users/statistics/ResponseServicesByUserStatisticsDto';
import { ResponseUsersByRoleStatisticsDto } from '../../../dtos/users/statistics/ResponseUsersByRoleStatisticsDto';
import { HttpErrorResponse } from '@angular/common/http';
import { catchError, EMPTY, finalize, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserStateService {

  // Injections
  private http = inject(HttpService);

  // User Signals
  private _users = signal<ResponseAllUsersDto[]>([]);
  private _userLogged = signal<ResponseUserInfoDto | null>(null);
  private _userInfo = signal<ResponseUserInfoDto | null>(null);
  private _loadUserByToken = signal<boolean>(false);

  // User Signals readOnly
  public readonly users = this._users.asReadonly();
  public readonly userLogged = this._userLogged.asReadonly();
  public readonly userInfo = this._userInfo.asReadonly();
  public readonly loadUserByToken = this._loadUserByToken.asReadonly();

  // Messages Signals
  private _registerMessage = signal('');
  private _registerStatus = signal<'success' | 'error' | 'default'>('default');

  private _updateMessage = signal('');
  private _updateStatus = signal<'success' | 'error' | 'default'>('default');

  private _deleteMessage = signal('');
  private _deleteStatus = signal<'success' | 'error' | 'default'>('default');

  // Messages Signals readOnly
  public readonly registerMessage = this._registerMessage.asReadonly();
  public readonly registerStatus = this._registerStatus.asReadonly();

  public readonly updateMessage = this._updateMessage.asReadonly();
  public readonly updateStatus = this._updateStatus.asReadonly();

  public readonly deleteMessage = this._deleteMessage.asReadonly();
  public readonly deleteStatus = this._deleteStatus.asReadonly();

  // Photo Signals
  private _photoMessage = signal('');
  private _photoStatus = signal<'success' | 'error' | 'default'>('default');
  private _userPhoto = signal<string | null>(null);

  // Photo Signals readOnly
  public readonly photoMessage = this._photoMessage.asReadonly();
  public readonly photoStatus = this._photoStatus.asReadonly();
  public readonly userPhoto = this._userPhoto.asReadonly();

  // Metricas Signals
  private _countTotalUsersStatistics = signal<ResponseCountTotalUsersStatisticsDto | null>(null);
  private _userPercentagesStatistics = signal<ResponseUserPercentagesStatisticsDto | null>(null);
  private _usersCreatedByMonthStatistics = signal<ResponseUsersCreatedByMonthStatisticsDto[] | null>([]);
  private _countServicesByUsers = signal<ResponseServicesByUserStatisticsDto[] | null>([]);
  private _countRoleByUsers = signal<ResponseUsersByRoleStatisticsDto[] | null>([]);

  // Metricas Signals readOnly
  public readonly countTotalUsersStatistics = this._countTotalUsersStatistics.asReadonly();
  public readonly userPercentagesStatistics = this._userPercentagesStatistics.asReadonly();
  public readonly usersCreatedByMonthStatistics = this._usersCreatedByMonthStatistics.asReadonly();
  public readonly countServicesByUsers = this._countServicesByUsers.asReadonly();
  public readonly countRoleByUsers = this._countRoleByUsers.asReadonly();

  public countTotalAdmin = computed(() => this.countRoleByUsers()?.find(u => u.role === 'ROLE_ADMIN')?.totalUsers || 0);

  // Pagination Signals
  private _page = signal<number>(0);
  private _totalElements = signal(0);

  // Pagination Signals readOnly
  public readonly page = this._page.asReadonly();
  public readonly size = 4;
  public readonly totalElements = this._totalElements.asReadonly();

  public totalPages = computed(() =>
    Math.ceil(this.totalElements() / this.size)
  );

  // Search Signals
  private _search = signal('');

  // Search Signals readOnly
  public readonly search = this._search.asReadonly();

  // modal Signals
  private _modalRegister = signal<Boolean>(false);

  // modal Signals readOnly
  public readonly modalRegister = this._modalRegister.asReadonly();

  // Create user
  registerUser(request: RequestUserDto) {

    this.http.createUser(request)
      .pipe(
        tap(() => {
          this._registerMessage.set('Usuario criado com sucesso!');
          this._registerStatus.set('success');
          this.loadingAllUsers();
        }),
        catchError((error: HttpErrorResponse) => {
          this._registerMessage.set(error.error?.message || 'Erro ao criar usuario');
          this._registerStatus.set('error');

          return EMPTY;
        }),
        finalize(() => {
          this._modalRegister.set(false);
        })
      )
      .subscribe();
  }

  updateUser(request: UpdateUserDto) {

    this.http.updateUser(request)
      .pipe(
        tap(() => {
          this._updateMessage.set('Usuario atualizado com sucesso!');
          this._updateStatus.set('success');

          this.loadingAllUsers();
          this.getUserByToken();
        }),
        catchError((error: HttpErrorResponse) => {
          this._updateMessage.set(error.error?.message || 'Erro ao atualizar usuario');
          this._updateStatus.set('error');

          return EMPTY;
        }),
        finalize(() => {
          console.log('Requisição de atualização finalizada.');
        })
      )
      .subscribe();
  }

  deleteUser() {

    const userId = this.userInfo()?.userId;
    if (!userId) return;

    this.http.deleteUser(userId)
      .pipe(
        tap(() => {
          this._deleteMessage.set('Usuario deletado com sucesso!');
          this._deleteStatus.set('success');

          this._users.update(users => {
            this._totalElements.update(total => total - 1);
            return users.filter(u => u.userId !== userId);
          });
        }),
        catchError((error: HttpErrorResponse) => {
          this._deleteMessage.set(error.error?.message || 'Erro ao deletar usuario');
          this._deleteStatus.set('error');

          return EMPTY;
        }),
        finalize(() => {
          console.log('Requisição de exclusão finalizada.');
        })
      )
      .subscribe();
  }

  // All users
  loadingAllUsers() {

    this.http.getAllUsers(
        this.page(),
        this.size,
        this.search()
      )
      .pipe(
        tap((response) => {
          this._users.set(response.content);
          this._totalElements.set(response.totalElements);
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Erro ao carregar usuarios', error);
          return EMPTY;
        }),
        finalize(() => {
          console.log('Requisição de carregamento finalizada.');
        })
      )
      .subscribe();
  }

  // ===== LOAD STATISTICS =====
  loadStatistics() {

    this.http.getUserStatistics()
      .pipe(
        tap((response) => {
          this._countTotalUsersStatistics.set(response.countTotalUsersStatistics);
          this._userPercentagesStatistics.set(response.userPercentagesStatistics);
          this._usersCreatedByMonthStatistics.set(response.usersCreatedByMonthStatistics);
          this._countServicesByUsers.set(response.countServicesByUsers);
          this._countRoleByUsers.set(response.countRoleByUsers);
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Erro ao carregar estatísticas de usuarios', error);
          return EMPTY;
        }),
        finalize(() => {
          console.log('Requisição de carregamento finalizada.');
        })
      )
      .subscribe();
  }

  // Get user by id
  getUserById(userId: string) {

    this.http.getUserById(userId)
      .pipe(
        tap((response) => {
          this._userInfo.set(response);
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Erro ao carregar usuario', error);
          return EMPTY;
        }),
        finalize(() => {
          console.log('Requisição de carregamento finalizada.');
        })
      )
      .subscribe();
  }

  // Get user by token
  getUserByToken() {

    this._loadUserByToken.set(true);

    this.http.getUserByToken()
      .pipe(
        tap((response) => {
          this._userLogged.set(response);

          if (response.hasPhoto) {
            this.loadUserPhoto();
          } else {
            this._userPhoto.set(null);
            this._loadUserByToken.set(false);
          }
        }),
        catchError((error: HttpErrorResponse) => {
          this._loadUserByToken.set(false);
          console.error('Erro ao carregar usuario', error);
          return EMPTY;
        }),
        finalize(() => {
          console.log('Requisição de carregamento finalizada.');
        }
      ))
      .subscribe();
  }

  // ===================== PAGINATION =====================

  nextPage() {

    if (this.page() + 1 >= this.totalPages()) return;

    this._page.update(p => p + 1);

    this.loadingAllUsers();
  }

  previousPage() {

    if (this.page() === 0) return;
    this._page.update(p => p - 1);

    this.loadingAllUsers();
  }

  goToPage(page: number) {

    if (page < 0 || page >= this.totalPages()) return;
    this._page.set(page);

    this.loadingAllUsers();
  }

  // ===================== SEARCH =====================

  setSearch(value: string) {

    this._search.set(value);
    // volta pra primeira página
    this._page.set(0);

    this.loadingAllUsers();
  }

  // ===================== RESET =====================

  resetLoadDepartments() {
    this.loadingAllUsers();
  }

  resetStatus() {
    this._registerStatus.set('default');
    this._updateStatus.set('default');
    this._deleteStatus.set('default');
  }

  resetInfoUser() {
    this._userInfo.set(null);
  }

  updatePhoto(photo: File) {

    this.http.userUpdatePhoto(photo)
      .pipe(

        tap(() => {
          this._photoMessage.set('Foto atualizada com sucesso!');
          this._photoStatus.set('success');
          this.loadUserPhoto();
        }),
        catchError((error: HttpErrorResponse) => {
          this._photoMessage.set(
            error.error?.message || 'Erro ao atualizar foto'
          );
          this._photoStatus.set('error');
          return EMPTY;
        }),
        finalize(() => {
          console.log('Requisição de atualização finalizada.');
        })
      )
      .subscribe();
  }

  loadUserPhoto() {

    this.http.getUserPhoto()
      .pipe(

        tap((blob) => {
          const oldUrl = this.userPhoto();
          if (oldUrl) {
            URL.revokeObjectURL(oldUrl);
          }

          const imageUrl = URL.createObjectURL(blob);
          this._userPhoto.set(imageUrl);
          this._loadUserByToken.set(false);
        }),
        catchError((error: HttpErrorResponse) => {
          this._loadUserByToken.set(false);
          console.error('Erro ao carregar foto', error);
          return EMPTY;
        }),
        finalize(() => {
          console.log('Requisição de carregamento finalizada.');
        })
      )
      .subscribe();
  }

  setModalRegister() {
    this._modalRegister.update(value => !value);
  }

  resetUserInfo() {
    this._userInfo.set(null);
  }
}

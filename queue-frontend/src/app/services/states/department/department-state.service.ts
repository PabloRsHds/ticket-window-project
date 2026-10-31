
import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpService } from '../../backend/http.service';

import { ResponseDepartmentDto } from '../../../dtos/department/ResponseDepartmentDto';
import { CreateDepartmentDto } from '../../../dtos/department/CreateDepartmentDto';
import { UpdateDepartmentDto } from '../../../dtos/department/UpdateDepartmentDto';
import { DepartmentNameAndId } from '../../../dtos/department/DepartmentNameAndId';

import { ResponseCountTotalDepartmentsStatisticsDto } from '../../../dtos/department/statistics/ResponseCountTotalDepartmentsStatisticsDto';
import { ResponseCountServicesByDepartmentsStatisticsDto } from '../../../dtos/department/statistics/ResponseCountServicesByDepartmentsStatisticsDto';
import { ResponseDepartmentPercentagesStatisticsDto } from '../../../dtos/department/statistics/ResponseDepartmentPercentagesStatisticsDto';
import { ResponseDepartmentsCreatedByMonthStatisticsDto } from '../../../dtos/department/statistics/ResponseDepartmentsCreatedByMonthStatisticsDto';

import { HttpErrorResponse } from '@angular/common/http';
import { catchError, EMPTY, finalize, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DepartmentStateService {

  // ============================================================
  // INJECTIONS
  // ============================================================

  private http = inject(HttpService);

  // ============================================================
  // DEPARTMENT SIGNALS
  // ============================================================

  private _departments = signal<ResponseDepartmentDto[]>([]);
  private _departmentInfo = signal<ResponseDepartmentDto | null>(null);
  private _departmentNameAndId = signal<DepartmentNameAndId[]>([]);

  // DEPARTMENT SIGNALS READONLY
  public readonly departments = this._departments.asReadonly();
  public readonly departmentInfo = this._departmentInfo.asReadonly();
  public readonly departmentNameAndId = this._departmentNameAndId.asReadonly();

  // ============================================================
  // MESSAGES SIGNALS
  // ============================================================

  private _registerMessage = signal('');
  private _registerStatus = signal<'success' | 'error' | 'default'>('default');

  private _updateMessage = signal('');
  private _updateStatus = signal<'success' | 'error' | 'default'>('default');

  private _deleteMessage = signal('');
  private _deleteStatus = signal<'success' | 'error' | 'default'>('default');

  private _loadMessage = signal('');
  private _loadStatus = signal<'loading' | 'success' | 'error' | 'default'>('default');

  // MESSAGES SIGNALS READONLY
  public readonly registerMessage = this._registerMessage.asReadonly();
  public readonly registerStatus = this._registerStatus.asReadonly();

  public readonly updateMessage = this._updateMessage.asReadonly();
  public readonly updateStatus = this._updateStatus.asReadonly();

  public readonly deleteMessage = this._deleteMessage.asReadonly();
  public readonly deleteStatus = this._deleteStatus.asReadonly();

  public readonly loadMessage = this._loadMessage.asReadonly();
  public readonly loadStatus = this._loadStatus.asReadonly();

  // ============================================================
  // LOADING SIGNALS
  // ============================================================

  private _allDepartmentsLoading = signal(false);
  private _registerLoading = signal(false);
  private _updateLoading = signal(false);
  private _deleteLoading = signal(false);
  private _statisticsLoading = signal(false);
  private _departmentInfoLoading = signal(false);
  private _departmentNamesLoading = signal(false);

  // LOADING SIGNALS READONLY
  public readonly allDepartmentsLoading = this._allDepartmentsLoading.asReadonly();
  public readonly registerLoading = this._registerLoading.asReadonly();
  public readonly updateLoading = this._updateLoading.asReadonly();
  public readonly deleteLoading = this._deleteLoading.asReadonly();
  public readonly statisticsLoading = this._statisticsLoading.asReadonly();
  public readonly departmentInfoLoading = this._departmentInfoLoading.asReadonly();
  public readonly departmentNamesLoading = this._departmentNamesLoading.asReadonly();

  // ============================================================
  // PAGINATION SIGNALS
  // ============================================================

  private _page = signal<number>(0);
  private _size = signal<number>(4);
  private _totalElements = signal<number>(0);

  // PAGINATION SIGNALS READONLY
  public readonly page = this._page.asReadonly();
  public readonly size = this._size.asReadonly();
  public readonly totalElements = this._totalElements.asReadonly();

  public totalPages = computed(() =>
    Math.ceil(this.totalElements() / this.size())
  );

  // ============================================================
  // SEARCH SIGNALS
  // ============================================================

  private _search = signal<string>('');

  // SEARCH SIGNALS READONLY
  public readonly search = this._search.asReadonly();

  // ============================================================
  // MODAL SIGNALS
  // ============================================================

  private _modalRegister = signal<boolean>(false);

  // MODAL SIGNALS READONLY
  public readonly modalRegister = this._modalRegister.asReadonly();

  // ============================================================
  // STATISTICS SIGNALS
  // ============================================================

  private _countServicesByDepartment =
    signal<ResponseCountServicesByDepartmentsStatisticsDto[] | null>([]);

  private _countTotalDepartment =
    signal<ResponseCountTotalDepartmentsStatisticsDto | null>(null);

  private _getPercentagesByDepartment =
    signal<ResponseDepartmentPercentagesStatisticsDto | null>(null);

  private _departmentsCreatedByMonth =
    signal<ResponseDepartmentsCreatedByMonthStatisticsDto[] | null>([]);

  // STATISTICS SIGNALS READONLY
  public readonly countServicesByDepartment =
    this._countServicesByDepartment.asReadonly();

  public readonly countTotalDepartment =
    this._countTotalDepartment.asReadonly();

  public readonly getPercentagesByDepartment =
    this._getPercentagesByDepartment.asReadonly();

  public readonly departmentsCreatedByMonth =
    this._departmentsCreatedByMonth.asReadonly();

  // ============================================================
  // LOAD DEPARTMENTS
  // ============================================================

  loadDepartments() {

    this._allDepartmentsLoading.set(true);
    this._loadStatus.set('loading');
    this._loadMessage.set('Carregando departamentos...');

    this.http.getAllDepartments(
      this.page(),
      this.size(),
      this.search()
    )
      .pipe(
        tap((response) => {
          this._departments.set(response.content);
          this._totalElements.set(response.totalElements);

          this._loadStatus.set('success');
          this._loadMessage.set('Departamentos carregados com sucesso!');
        }),

        catchError((error: HttpErrorResponse) => {
          this._loadStatus.set('error');
          this._loadMessage.set(
            error.error?.message || 'Erro ao carregar departamentos'
          );

          console.error('Erro ao carregar departamentos:', error);

          return EMPTY;
        }),

        finalize(() => {
          this._allDepartmentsLoading.set(false);
          console.log('Requisição de carregamento finalizada.');
        })
      )
    .subscribe();
  }

  // ============================================================
  // LOAD DEPARTMENTS FOR CREATE USER
  // ============================================================

  loadDepartmentsForCreateUser() {

    this._departmentNamesLoading.set(true);

    this.http.getAllDepartments(0, 9999)
      .pipe(
        tap((response) => {

          const departments = response.content
            .filter(department => department.active)
            .map(department => ({
              departmentId: department.departmentId,
              name: department.name
            }));

          this._departmentNameAndId.set(departments);
        }),

        catchError((error: HttpErrorResponse) => {
          console.error(
            'Erro ao carregar departamentos:',
            error.error?.message || error.message || 'Erro desconhecido'
          );

          return EMPTY;
        }),

        finalize(() => {
          this._departmentNamesLoading.set(false);
          console.log('Requisição de departamentos para cadastro finalizada.');
        })
      )
    .subscribe();
  }

  // ============================================================
  // LOAD STATISTICS
  // ============================================================

  loadStatistics() {

    this._statisticsLoading.set(true);

    this.http.getDepartmentStatistics()
      .pipe(
        tap((response) => {
          this._countServicesByDepartment.set(
            response.countServicesByDepartments
          );

          this._countTotalDepartment.set(
            response.countTotalDepartmentsStatistics
          );

          this._getPercentagesByDepartment.set(
            response.departmentPercentagesStatistics
          );

          this._departmentsCreatedByMonth.set(
            response.departmentsCreatedByMonthStatistics
          );
        }),

        catchError((error: HttpErrorResponse) => {
          console.error(
            'Erro ao carregar estatísticas:',
            error.error?.message || error.message || 'Erro desconhecido'
          );

          return EMPTY;
        }),

        finalize(() => {
          this._statisticsLoading.set(false);
          console.log('Requisição de estatísticas finalizada.');
        })
      )
    .subscribe();
  }

  // ============================================================
  // CREATE DEPARTMENT
  // ============================================================

  createDepartment(request: CreateDepartmentDto) {

    this._registerLoading.set(true);

    this.http.createDepartment(request)
      .pipe(
        tap((response) => {

          this._registerMessage.set('Departamento criado com sucesso!');
          this._registerStatus.set('success');

          this._page.set(0);

          this._departments.update(departments => {
            this._totalElements.update(total => total + 1);
            return [response, ...departments];
          });

          this.loadStatistics();
        }),

        catchError((error: HttpErrorResponse) => {
          this._registerMessage.set(
            error.error?.message || 'Erro ao criar departamento'
          );

          this._registerStatus.set('error');

          return EMPTY;
        }),

        finalize(() => {
          this._registerLoading.set(false);
          console.log('Requisição de criação finalizada.');
        })
      )
    .subscribe();
  }

  // ============================================================
  // UPDATE DEPARTMENT
  // ============================================================

  updateDepartment(request: UpdateDepartmentDto) {

    this._updateLoading.set(true);

    this.http.updateDepartment(request)
      .pipe(
        tap((response) => {

          this._updateMessage.set('Departamento atualizado com sucesso!');
          this._updateStatus.set('success');

          this._departments.update(departments =>
            departments.map(department =>
              department.departmentId === response.departmentId
                ? response
                : department
            )
          );

          this.loadStatistics();
          this.getInfoDepartment(response.departmentId);
        }),

        catchError((error: HttpErrorResponse) => {
          this._updateMessage.set(
            error.error?.message || 'Erro ao atualizar departamento'
          );

          this._updateStatus.set('error');

          return EMPTY;
        }),

        finalize(() => {
          this._updateLoading.set(false);
          console.log('Requisição de atualização finalizada.');
        })
      )
    .subscribe();
  }

  // ============================================================
  // DELETE DEPARTMENT
  // ============================================================

  deleteDepartment(departmentId: string) {

    this._deleteLoading.set(true);

    this.http.deleteDepartment(departmentId)
      .pipe(
        tap(() => {

          this._deleteMessage.set('Departamento excluído com sucesso!');
          this._deleteStatus.set('success');

          // Volta uma página caso tenha excluído o último item
          if (this.departments().length === 1 && this.page() > 0) {
            this._page.update(page => page - 1);
          }

          this._departments.update(departments => {
            this._totalElements.update(total => total - 1);

            return departments.filter(
              department => department.departmentId !== departmentId
            );
          });

          this.loadStatistics();

          this._departmentInfo.set(null);
        }),

        catchError((error: HttpErrorResponse) => {
          this._deleteMessage.set(
            error.error?.message || 'Erro ao excluir departamento'
          );

          this._deleteStatus.set('error');

          return EMPTY;
        }),

        finalize(() => {
          this._deleteLoading.set(false);
          console.log('Requisição de exclusão finalizada.');
        })
      )
    .subscribe();
  }

  // ============================================================
  // GET DEPARTMENT BY ID
  // ============================================================

  getInfoDepartment(departmentId: string) {

    const department = this.departments().find(
      department => department.departmentId === departmentId
    );

    if (department) {
      this._departmentInfo.set(department);
      return;
    }

    this._departmentInfoLoading.set(true);
  }

  // ============================================================
  // GET DEPARTMENT NAMES AND IDS
  // ============================================================

  getDepartmentNameAndId() {

    const names = this.departments()
      .filter(department => department.active)
      .map(department => ({
        departmentId: department.departmentId,
        name: department.name
      }));

    this._departmentNameAndId.set(names);
  }

  // ============================================================
  // PAGINATION
  // ============================================================

  nextPage() {

    if (this.page() + 1 >= this.totalPages()) return;

    this._page.update(page => page + 1);

    this.loadDepartments();
  }

  previousPage() {

    if (this.page() === 0) return;

    this._page.update(page => page - 1);

    this.loadDepartments();
  }

  goToPage(page: number) {

    if (page < 0 || page >= this.totalPages()) return;

    this._page.set(page);

    this.loadDepartments();
  }

  // ============================================================
  // SEARCH
  // ============================================================

  setSearch(value: string) {

    this._search.set(value);
    this._page.set(0);

    this.loadDepartments();
  }

  // ============================================================
  // SET SIZE
  // ============================================================

  setSize(size: number) {

    this._size.set(size);
    this._page.set(0);

    this.loadDepartments();
  }

  // ============================================================
  // MODAL REGISTER
  // ============================================================

  setModalRegister() {
    this._modalRegister.update(value => !value);
  }

  // ============================================================
  // REFRESH
  // ============================================================

  refresh() {
    this.loadDepartments();
  }

  // ============================================================
  // RESET STATUS
  // ============================================================

  resetRegisterStatus() {
    this._registerStatus.set('default');
    this._registerMessage.set('');
  }

  resetUpdateStatus() {
    this._updateStatus.set('default');
    this._updateMessage.set('');
  }

  resetDeleteStatus() {
    this._deleteStatus.set('default');
    this._deleteMessage.set('');
  }

  resetLoadStatus() {
    this._loadStatus.set('default');
    this._loadMessage.set('');
  }

  resetStatus() {
    this.resetRegisterStatus();
    this.resetUpdateStatus();
    this.resetDeleteStatus();
    this.resetLoadStatus();
  }

  // ============================================================
  // RESET DEPARTMENT INFO
  // ============================================================

  resetDepartmentInfo() {
    this._departmentInfo.set(null);
  }

  // ============================================================
  // RESET DEPARTMENT NAMES
  // ============================================================

  resetDepartmentNames() {
    this._departmentNameAndId.set([]);
  }

}

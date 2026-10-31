
import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpService } from '../../backend/http.service';

import { CreateServiceManagementDto } from '../../../dtos/services/CreateServiceManagementDto';
import { ResponseServiceManagementDto } from '../../../dtos/services/ResponseServiceManagementDto';
import { UpdateServiceManagementDto } from '../../../dtos/services/UpdateServiceManagementDto';
import { ResponseGetServiceByIdDto } from '../../../dtos/services/ResponseGetServiceByIdDto';
import { ResponseServiceNamesAndDepartments } from '../../../dtos/services/ResponseServiceNamesAndDepartments';

import { ResponseCountTotalServicesStatisticsDto } from '../../../dtos/services/statistics/ResponseCountTotalServicesStatisticsDto';
import { ResponseSchedulesByServiceStatisticsDto } from '../../../dtos/services/statistics/ResponseSchedulesByServiceStatisticsDto';
import { ResponseServicePercentagesStatisticsDto } from '../../../dtos/services/statistics/ResponseServicePercentagesStatisticsDto';
import { ResponseServicesByDepartmentStatisticsDto } from '../../../dtos/services/statistics/ResponseServicesByDepartmentStatisticsDto';
import { ResponseServicesCreatedByMonthStatisticsDto } from '../../../dtos/services/statistics/ResponseServicesCreatedByMonthStatisticsDto';
import { ResponseTicketsByServiceStatisticsDto } from '../../../dtos/services/statistics/ResponseTicketsByServiceStatisticsDto';
import { ResponseUsersByServiceStatisticsDto } from '../../../dtos/services/statistics/ResponseUsersByServiceStatisticsDto';

import { HttpErrorResponse } from '@angular/common/http';
import { catchError, EMPTY, finalize, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ServiceManagementService {

  // ============================================================
  // INJECTIONS
  // ============================================================

  private http = inject(HttpService);

  // ============================================================
  // SERVICE SIGNALS
  // ============================================================

  private _services = signal<ResponseServiceManagementDto[]>([]);
  private _serviceNamesAndDepartments = signal<ResponseServiceNamesAndDepartments[]>([]);
  private _selectedService = signal<ResponseServiceManagementDto | null>(null);
  private _serviceInfo = signal<ResponseGetServiceByIdDto | null>(null);

  // SERVICE SIGNALS READONLY
  public readonly services = this._services.asReadonly();
  public readonly serviceNamesAndDepartments = this._serviceNamesAndDepartments.asReadonly();
  public readonly selectedService = this._selectedService.asReadonly();
  public readonly serviceInfo = this._serviceInfo.asReadonly();

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

  private _allServicesLoading = signal(false);
  private _registerLoading = signal(false);
  private _updateLoading = signal(false);
  private _deleteLoading = signal(false);
  private _statisticsLoading = signal(false);
  private _serviceInfoLoading = signal(false);
  private _serviceNamesLoading = signal(false);

  // LOADING SIGNALS READONLY
  public readonly allServicesLoading = this._allServicesLoading.asReadonly();
  public readonly registerLoading = this._registerLoading.asReadonly();
  public readonly updateLoading = this._updateLoading.asReadonly();
  public readonly deleteLoading = this._deleteLoading.asReadonly();
  public readonly statisticsLoading = this._statisticsLoading.asReadonly();
  public readonly serviceInfoLoading = this._serviceInfoLoading.asReadonly();
  public readonly serviceNamesLoading = this._serviceNamesLoading.asReadonly();

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

  private _countTotalServicesStatistics =
    signal<ResponseCountTotalServicesStatisticsDto | null>(null);

  private _servicePercentagesStatistics =
    signal<ResponseServicePercentagesStatisticsDto | null>(null);

  private _servicesCreatedByMonth =
    signal<ResponseServicesCreatedByMonthStatisticsDto[] | null>([]);

  private _servicesByDepartment =
    signal<ResponseServicesByDepartmentStatisticsDto[] | null>([]);

  private _usersByService =
    signal<ResponseUsersByServiceStatisticsDto[] | null>([]);

  private _schedulesByService =
    signal<ResponseSchedulesByServiceStatisticsDto[] | null>([]);

  private _ticketsByService =
    signal<ResponseTicketsByServiceStatisticsDto[] | null>([]);

  // STATISTICS SIGNALS READONLY
  public readonly countTotalServicesStatistics =
    this._countTotalServicesStatistics.asReadonly();

  public readonly servicePercentagesStatistics =
    this._servicePercentagesStatistics.asReadonly();

  public readonly servicesCreatedByMonth =
    this._servicesCreatedByMonth.asReadonly();

  public readonly servicesByDepartment =
    this._servicesByDepartment.asReadonly();

  public readonly usersByService =
    this._usersByService.asReadonly();

  public readonly schedulesByService =
    this._schedulesByService.asReadonly();

  public readonly ticketsByService =
    this._ticketsByService.asReadonly();

  // ============================================================
  // REGISTER SERVICE
  // ============================================================

  registerServiceManagement(request: CreateServiceManagementDto) {

    this._registerLoading.set(true);

    this.http.createServiceManagement(request)
      .pipe(
        tap(() => {
          this._registerMessage.set('Serviço cadastrado com sucesso!');
          this._registerStatus.set('success');

          this._page.set(0);

          this.loadServices();
          this.loadStatistics();
        }),

        catchError((error: HttpErrorResponse) => {
          this._registerMessage.set(
            error.error?.message || 'Erro ao cadastrar serviço.'
          );

          this._registerStatus.set('error');

          return EMPTY;
        }),

        finalize(() => {
          this._registerLoading.set(false);
          console.log('Requisição de cadastro finalizada.');
        })
      )
      .subscribe();
  }

  // ============================================================
  // UPDATE SERVICE
  // ============================================================

  updateServiceManagement(request: UpdateServiceManagementDto) {

    this._updateLoading.set(true);

    this.http.updateServiceManagement(request)
      .pipe(
        tap(() => {
          this._updateMessage.set('Serviço atualizado com sucesso!');
          this._updateStatus.set('success');

          this._page.set(0);

          this.loadServices();
          this.loadStatistics();
        }),

        catchError((error: HttpErrorResponse) => {
          this._updateMessage.set(
            error.error?.message || 'Erro ao atualizar serviço.'
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
  // DELETE SERVICE
  // ============================================================

  deleteServiceManagement(serviceManagementId: string) {

    this._deleteLoading.set(true);

    this.http.deleteServiceManagement(serviceManagementId)
      .pipe(
        tap(() => {
          this._deleteMessage.set('Serviço excluído com sucesso!');
          this._deleteStatus.set('success');

          this._page.set(0);

          this.loadServices();
          this.loadStatistics();
        }),

        catchError((error: HttpErrorResponse) => {
          this._deleteMessage.set(
            error.error?.message || 'Erro ao excluir serviço.'
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
  // LOAD ALL SERVICES
  // ============================================================

  loadServices() {

    this._allServicesLoading.set(true);
    this._loadStatus.set('loading');
    this._loadMessage.set('Carregando serviços...');

    this.http.getAllServicesManagement(
      this.page(),
      this.size(),
      this.search()
    )
      .pipe(
        tap((response) => {
          this._services.set(response.content);
          this._totalElements.set(response.totalElements);

          this._loadStatus.set('success');
          this._loadMessage.set('Serviços carregados com sucesso!');
        }),

        catchError((error: HttpErrorResponse) => {
          this._loadStatus.set('error');
          this._loadMessage.set(
            error.error?.message || 'Erro ao carregar serviços.'
          );

          console.error('Erro ao carregar serviços:', error);

          return EMPTY;
        }),

        finalize(() => {
          this._allServicesLoading.set(false);
          console.log('Requisição de carregamento finalizada.');
        })
      )
    .subscribe();
  }

  // ============================================================
  // LOAD SERVICE NAMES AND DEPARTMENTS
  // ============================================================

  loadServiceNamesAndDepartments() {

    this._serviceNamesLoading.set(true);

    this.http.getServiceNamesAndDepartments()
      .pipe(
        tap((response) => {
          this._serviceNamesAndDepartments.set(response);
        }),

        catchError((error: HttpErrorResponse) => {
          console.error('Erro ao carregar nomes e departamentos:', error);

          return EMPTY;
        }),

        finalize(() => {
          this._serviceNamesLoading.set(false);
          console.log('Requisição de nomes e departamentos finalizada.');
        })
      )
    .subscribe();
  }

  // ============================================================
  // LOAD STATISTICS
  // ============================================================

  loadStatistics() {

    this._statisticsLoading.set(true);

    this.http.getServiceStatistics()
      .pipe(
        tap((response) => {
          this._countTotalServicesStatistics.set(
            response.countTotalServicesStatistics
          );

          this._servicePercentagesStatistics.set(
            response.servicePercentagesStatistics
          );

          this._servicesCreatedByMonth.set(
            response.servicesCreatedByMonth
          );

          this._servicesByDepartment.set(
            response.servicesByDepartment
          );

          this._usersByService.set(
            response.usersByService
          );

          this._schedulesByService.set(
            response.schedulesByService
          );

          this._ticketsByService.set(
            response.ticketsByService
          );
        }),

        catchError((error: HttpErrorResponse) => {
          console.error('Erro ao carregar estatísticas:', error);

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
  // GET SERVICE BY ID
  // ============================================================

  getInfoService(serviceManagementId: string) {

    this._serviceInfoLoading.set(true);

    this.http.getServiceManagementById(serviceManagementId)
      .pipe(
        tap((response) => {
          this._serviceInfo.set(response);
        }),

        catchError((error: HttpErrorResponse) => {
          this._serviceInfo.set(null);

          console.error('Erro ao buscar serviço:', error);

          return EMPTY;
        }),

        finalize(() => {
          this._serviceInfoLoading.set(false);
          console.log('Requisição de serviço por ID finalizada.');
        })
      )
    .subscribe();
  }

  // ============================================================
  // PAGINATION
  // ============================================================

  nextPage() {

    if (this.page() + 1 >= this.totalPages()) return;

    this._page.update(page => page + 1);

    this.loadServices();
  }

  previousPage() {

    if (this.page() === 0) return;

    this._page.update(page => page - 1);

    this.loadServices();
  }

  goToPage(page: number) {

    if (page < 0 || page >= this.totalPages()) return;

    this._page.set(page);

    this.loadServices();
  }

  // ============================================================
  // SEARCH
  // ============================================================

  setSearch(value: string) {

    this._search.set(value);
    this._page.set(0);

    this.loadServices();
  }

  // ============================================================
  // SET SIZE
  // ============================================================

  setSize(size: number) {

    this._size.set(size);
    this._page.set(0);

    this.loadServices();
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
    this.loadServices();
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
  // RESET SERVICE INFO
  // ============================================================

  resetInfoService() {
    this._serviceInfo.set(null);
  }

  // ============================================================
  // RESET SELECTED SERVICE
  // ============================================================

  resetSelectedService() {
    this._selectedService.set(null);
  }

  // ============================================================
  // RESET SELECTED SERVICE
  // ============================================================

  resetServiceNamesAndDepartments() {
    this._serviceNamesAndDepartments.set([]);
  }

}

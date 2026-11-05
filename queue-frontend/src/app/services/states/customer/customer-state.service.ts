
import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpService } from '../../backend/http.service';

import { ResponseAllCustomersDto } from '../../../dtos/customer/ResponseAllCustomersDto';
import { ResponseCustomerInfoDto } from '../../../dtos/customer/ResponseCustomerInfoDto';
import { ResponseCustomerIdsAndNames } from '../../../dtos/customer/ResponseCustomerIdsAndNames';
import { CreateCustomerDto } from '../../../dtos/customer/CreateCustomerDto';
import { UpdateCustomerDto } from '../../../dtos/customer/UpdateCustomerDto';
import { ResponseCountTotalCustomersStatisticsDto } from '../../../dtos/customer/statistics/ResponseCountTotalCustomersStatisticsDto';
import { ResponseCustomersCreatedByMonthStatisticsDto } from '../../../dtos/customer/statistics/ResponseCustomersCreatedByMonthStatisticsDto';
import { CustomerPhotoRequestDto } from '../../../dtos/customer/CustomerPhotoRequestDto';
import { ResponseCustomerDto } from '../../../dtos/customer/ResponseCustomerDto';

import { HttpErrorResponse } from '@angular/common/http';
import { catchError, EMPTY, finalize, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CustomerStateService {

  // ============================================================
  // INJECTIONS
  // ============================================================

  private http = inject(HttpService);

  // ============================================================
  // CUSTOMER SIGNALS
  // ============================================================

  private _customers = signal<ResponseAllCustomersDto[]>([]);
  private _customer = signal<ResponseCustomerDto | null>(null);
  private _customerInfo = signal<ResponseCustomerInfoDto | null>(null);
  private _customerId = signal<string | null>(null);
  private _customerIdsAndNames = signal<ResponseCustomerIdsAndNames[]>([]);
  private _customerSuggestions = signal<ResponseAllCustomersDto[]>([]);

  private _modalCustomerRegister = signal(false);
  public modalCustomerRegister = this._modalCustomerRegister.asReadonly();

  // CUSTOMER SIGNALS READONLY

  public readonly customers = this._customers.asReadonly();
  public readonly customer = this._customer.asReadonly();
  public readonly customerInfo = this._customerInfo.asReadonly();
  public readonly customerId = this._customerId.asReadonly();
  public readonly customerIdsAndNames = this._customerIdsAndNames.asReadonly();
  public readonly customerSuggestions = this._customerSuggestions.asReadonly();

  // ============================================================
  // MESSAGES SIGNALS
  // ============================================================

  private _registerCustomerMessage = signal('');
  private _registerCustomerStatus = signal<'success' | 'error' | 'default'>('default');

  private _updateCustomerMessage = signal('');
  private _updateCustomerStatus = signal<'success' | 'error' | 'default'>('default');

  private _deleteCustomerMessage = signal('');
  private _deleteCustomerStatus = signal<'success' | 'error' | 'default'>('default');

  private _loadMessage = signal('');
  private _loadStatus = signal<'loading' | 'success' | 'error' | 'default'>('default');

  private _photoMessage = signal('');
  private _photoStatus = signal<'success' | 'error' | 'default'>('default');

  // MESSAGES SIGNALS READONLY

  public readonly registerCustomerMessage = this._registerCustomerMessage.asReadonly();
  public readonly registerCustomerStatus = this._registerCustomerStatus.asReadonly();

  public readonly updateCustomerMessage = this._updateCustomerMessage.asReadonly();
  public readonly updateCustomerStatus = this._updateCustomerStatus.asReadonly();

  public readonly deleteCustomerMessage = this._deleteCustomerMessage.asReadonly();
  public readonly deleteCustomerStatus = this._deleteCustomerStatus.asReadonly();

  public readonly loadMessage = this._loadMessage.asReadonly();
  public readonly loadStatus = this._loadStatus.asReadonly();

  public readonly photoMessage = this._photoMessage.asReadonly();
  public readonly photoStatus = this._photoStatus.asReadonly();

  // ============================================================
  // LOADING SIGNALS
  // ============================================================

  private _allCustomersLoading = signal(false);
  private _registerCustomerLoading = signal(false);
  private _updateCustomerLoading = signal(false);
  private _deleteCustomerLoading = signal(false);
  private _statisticsLoading = signal(false);
  private _customerInfoLoading = signal(false);
  private _customerIdsLoading = signal(false);
  private _customerSuggestionsLoading = signal(false);
  private _photoLoading = signal(false);

  // LOADING SIGNALS READONLY

  public readonly allCustomersLoading = this._allCustomersLoading.asReadonly();
  public readonly registerCustomerLoading = this._registerCustomerLoading.asReadonly();
  public readonly updateCustomerLoading = this._updateCustomerLoading.asReadonly();
  public readonly deleteCustomerLoading = this._deleteCustomerLoading.asReadonly();
  public readonly statisticsLoading = this._statisticsLoading.asReadonly();
  public readonly customerInfoLoading = this._customerInfoLoading.asReadonly();
  public readonly customerIdsLoading = this._customerIdsLoading.asReadonly();
  public readonly customerSuggestionsLoading = this._customerSuggestionsLoading.asReadonly();
  public readonly photoLoading = this._photoLoading.asReadonly();

  // ============================================================
  // PAGINATION SIGNALS
  // ============================================================

  private _customerPage = signal(0);

  public readonly customerPage = this._customerPage.asReadonly();

  public customerSize = 4;

  private _customerTotalElements = signal(0);

  public readonly customerTotalElements = this._customerTotalElements.asReadonly();

  public customerTotalPages = computed(() =>
    Math.ceil(this.customerTotalElements() / this.customerSize)
  );

  // ============================================================
  // SEARCH SIGNALS
  // ============================================================

  private _customerSearch = signal('');

  public readonly customerSearch = this._customerSearch.asReadonly();

  // ============================================================
  // STATISTICS SIGNALS
  // ============================================================

  private _totalCustomers =
    signal<ResponseCountTotalCustomersStatisticsDto | null>(null);

  private _totalCustomersByMonth =
    signal<ResponseCustomersCreatedByMonthStatisticsDto[] | []>([]);

  // STATISTICS SIGNALS READONLY

  public readonly totalCustomers = this._totalCustomers.asReadonly();

  public readonly totalCustomersByMonth =
    this._totalCustomersByMonth.asReadonly();

  // ============================================================
  // PHOTO SIGNALS
  // ============================================================

  private _customerPhoto = signal<string | null>(null);

  public readonly customerPhoto = this._customerPhoto.asReadonly();

  // ============================================================
  // LOAD CUSTOMERS
  // ============================================================

  loadCustomers() {

    this._allCustomersLoading.set(true);
    this._loadStatus.set('loading');
    this._loadMessage.set('Carregando clientes...');

    this.http.getAllCustomers(
      this.customerPage(),
      this.customerSize,
      this.customerSearch()
    )
      .pipe(
        tap((response) => {

          this._customers.set(response.content);
          this._customerTotalElements.set(response.totalElements);

          this._loadStatus.set('success');
          this._loadMessage.set('Clientes carregados com sucesso!');
        }),

        catchError((error: HttpErrorResponse) => {

          this._loadStatus.set('error');
          this._loadMessage.set(
            error.error?.message || 'Erro ao carregar clientes.'
          );

          console.error('Erro ao carregar clientes:', error);

          return EMPTY;
        }),

        finalize(() => {
          this._allCustomersLoading.set(false);
          console.log('Requisição de carregamento de clientes finalizada.');
        })
      )
      .subscribe();
  }

  // ============================================================
  // LOAD CUSTOMER IDS AND NAMES
  // ============================================================

  loadCustomerIdsAndNames() {

    this._customerIdsLoading.set(true);

    this.http.getCustomerIdsAndNames()
      .pipe(
        tap((response) => {
          this._customerIdsAndNames.set(response);
        }),

        catchError((error: HttpErrorResponse) => {

          this._customerIdsAndNames.set([]);

          console.error('Erro ao carregar IDs e nomes dos clientes:', error);

          return EMPTY;
        }),

        finalize(() => {
          this._customerIdsLoading.set(false);
          console.log('Requisição de IDs e nomes finalizada.');
        })
      )
      .subscribe();
  }

  // ============================================================
  // SEARCH CUSTOMERS
  // ============================================================

  searchCustomers(search: string) {

    this._customerSuggestionsLoading.set(true);

    this.http.getAllCustomers(0, 5, search)
      .pipe(
        tap((response) => {
          this._customerSuggestions.set(response.content);
        }),

        catchError((error: HttpErrorResponse) => {

          this._customerSuggestions.set([]);

          console.error('Erro ao buscar sugestões de clientes:', error);

          return EMPTY;
        }),

        finalize(() => {
          this._customerSuggestionsLoading.set(false);
          console.log('Requisição de sugestões finalizada.');
        })
      )
      .subscribe();
  }

  // ============================================================
  // GET CUSTOMER INFO
  // ============================================================

  getInfoCustomer(customerId: string) {

    this._customerInfoLoading.set(true);

    this.http.getCustomerById(customerId)
      .pipe(
        tap((response) => {
          this._customerInfo.set(response);
        }),

        catchError((error: HttpErrorResponse) => {

          this._customerInfo.set(null);

          console.error('Erro ao buscar informações do cliente:', error);

          return EMPTY;
        }),

        finalize(() => {
          this._customerInfoLoading.set(false);
          console.log('Requisição de informações do cliente finalizada.');
        })
      )
      .subscribe();
  }

  // ============================================================
  // LOAD STATISTICS
  // ============================================================

  loadStatistics() {

    this._statisticsLoading.set(true);

    this.http.getCustomerStatistics()
      .pipe(
        tap((response) => {

          this._totalCustomers.set(
            response.countTotalCustomersStatistics
          );

          this._totalCustomersByMonth.set(
            response.customersCreatedByMonthStatistics
          );
        }),

        catchError((error: HttpErrorResponse) => {

          console.error('Erro ao carregar estatísticas de clientes:', error);

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
  // CREATE CUSTOMER
  // ============================================================

  createCustomer(request: CreateCustomerDto) {

    this._registerCustomerLoading.set(true);

    this.http.createCustomer(request)
      .pipe(
        tap((response) => {

          this._customerId.set(response.customerId);
          this._customer.set(response);

          this._registerCustomerMessage.set('Cliente registrado com sucesso!');
          this._registerCustomerStatus.set('success');

          this._customerPage.set(0);

          this.loadCustomers();
          this.loadStatistics();
        }),

        catchError((error: HttpErrorResponse) => {

          this._registerCustomerMessage.set(
            error.error?.message || 'Erro ao registrar cliente.'
          );

          this._registerCustomerStatus.set('error');

          return EMPTY;
        }),

        finalize(() => {
          this._registerCustomerLoading.set(false);
          console.log('Requisição de cadastro de cliente finalizada.');
        })
      )
      .subscribe();
  }

  // ============================================================
  // UPDATE CUSTOMER
  // ============================================================

  updateCustomer(request: UpdateCustomerDto) {

    this._updateCustomerLoading.set(true);

    this.http.updateCustomer(request)
      .pipe(
        tap(() => {

          this._updateCustomerMessage.set('Cliente atualizado com sucesso!');
          this._updateCustomerStatus.set('success');

          this._customerPage.set(0);

          this.loadCustomers();
          this.loadStatistics();

          // Atualiza o detalhe se estiver aberto
          if (this.customerInfo()?.customerId === request.customerId) {
            this.getInfoCustomer(request.customerId);
          }
        }),

        catchError((error: HttpErrorResponse) => {

          this._updateCustomerMessage.set(
            error.error?.message || 'Erro ao atualizar cliente.'
          );

          this._updateCustomerStatus.set('error');

          return EMPTY;
        }),

        finalize(() => {
          this._updateCustomerLoading.set(false);
          console.log('Requisição de atualização de cliente finalizada.');
        })
      )
      .subscribe();
  }

  // ============================================================
  // DELETE CUSTOMER
  // ============================================================

  deleteCustomer(customerId: string) {

    this._deleteCustomerLoading.set(true);

    this.http.deleteCustomer(customerId)
      .pipe(
        tap(() => {

          this._deleteCustomerMessage.set('Cliente deletado com sucesso!');
          this._deleteCustomerStatus.set('success');

          // Se deletou o último item da página, volta uma página
          if (this.customers().length === 1 && this.customerPage() > 0) {
            this._customerPage.update(page => page - 1);
          }

          this.loadCustomers();
          this.loadStatistics();

          this._customerInfo.set(null);
          this._customer.set(null);
          this._customerId.set(null);
        }),

        catchError((error: HttpErrorResponse) => {

          this._deleteCustomerMessage.set(
            error.error?.message || 'Erro ao deletar cliente.'
          );

          this._deleteCustomerStatus.set('error');

          return EMPTY;
        }),

        finalize(() => {
          this._deleteCustomerLoading.set(false);
          console.log('Requisição de exclusão de cliente finalizada.');
        })
      )
      .subscribe();
  }

  // ============================================================
  // UPDATE CUSTOMER PHOTO
  // ============================================================

  updatePhoto(dto: CustomerPhotoRequestDto, photo: File) {

    this._photoLoading.set(true);

    this.http.customerUpdatePhoto(dto, photo)
      .pipe(
        tap(() => {

          this._photoMessage.set('Foto salva com sucesso!');
          this._photoStatus.set('success');

          this.loadCustomerPhoto(dto.customerId);
        }),

        catchError((error: HttpErrorResponse) => {

          this._photoMessage.set(
            error.error?.message || 'Erro ao atualizar foto.'
          );

          this._photoStatus.set('error');

          return EMPTY;
        }),

        finalize(() => {
          this._photoLoading.set(false);
          console.log('Requisição de atualização de foto finalizada.');
        })
      )
      .subscribe();
  }

  // ============================================================
  // LOAD CUSTOMER PHOTO
  // ============================================================

  loadCustomerPhoto(customerId: string) {

    this._photoLoading.set(true);

    this.http.getCustomerPhoto(customerId)
      .pipe(
        tap((blob) => {

          // Libera a URL antiga
          const oldUrl = this._customerPhoto();

          if (oldUrl) {
            URL.revokeObjectURL(oldUrl);
          }

          const imageUrl = URL.createObjectURL(blob);

          this._customerPhoto.set(imageUrl);
        }),

        catchError((error: HttpErrorResponse) => {

          console.error('Erro ao carregar foto do cliente:', error);

          return EMPTY;
        }),

        finalize(() => {
          this._photoLoading.set(false);
          console.log('Requisição de carregamento da foto finalizada.');
        })
      )
      .subscribe();
  }

  // ============================================================
  // PAGINATION
  // ============================================================

  nextPage() {

    if (this.customerPage() + 1 >= this.customerTotalPages()) return;

    this._customerPage.update(page => page + 1);

    this.loadCustomers();
  }

  previousPage() {

    if (this.customerPage() === 0) return;

    this._customerPage.update(page => page - 1);

    this.loadCustomers();
  }

  goToPage(page: number) {

    if (page < 0 || page >= this.customerTotalPages()) return;

    this._customerPage.set(page);

    this.loadCustomers();
  }

  // ============================================================
  // SEARCH
  // ============================================================

  setSearch(value: string) {

    this._customerSearch.set(value);
    this._customerPage.set(0);

    this.loadCustomers();
  }

  // ============================================================
  // SET SIZE
  // ============================================================

  setSize(size: number) {

    if (size <= 0) return;

    this.customerSize = size;
    this._customerPage.set(0);

    this.loadCustomers();
  }

  // ============================================================
  // REFRESH
  // ============================================================

  refresh() {
    this.loadCustomers();
  }

  // ============================================================
  // RESET REGISTER STATUS
  // ============================================================

  resetRegisterStatus() {

    this._registerCustomerStatus.set('default');
    this._registerCustomerMessage.set('');
  }

  // ============================================================
  // RESET UPDATE STATUS
  // ============================================================

  resetUpdateStatus() {

    this._updateCustomerStatus.set('default');
    this._updateCustomerMessage.set('');
  }

  // ============================================================
  // RESET DELETE STATUS
  // ============================================================

  resetDeleteStatus() {

    this._deleteCustomerStatus.set('default');
    this._deleteCustomerMessage.set('');
  }

  // ============================================================
  // RESET LOAD STATUS
  // ============================================================

  resetLoadStatus() {

    this._loadStatus.set('default');
    this._loadMessage.set('');
  }

  // ============================================================
  // RESET PHOTO STATUS
  // ============================================================

  resetPhotoStatus() {

    this._photoStatus.set('default');
    this._photoMessage.set('');
  }

  // ============================================================
  // RESET ALL STATUS
  // ============================================================

  resetStatus() {

    this.resetRegisterStatus();
    this.resetUpdateStatus();
    this.resetDeleteStatus();
    this.resetLoadStatus();
    this.resetPhotoStatus();
  }

  // ============================================================
  // RESET CUSTOMER INFO
  // ============================================================

  resetCustomerInfo() {
    this._customerInfo.set(null);
  }

  // ============================================================
  // RESET CUSTOMER
  // ============================================================

  resetCustomer() {
    this._customer.set(null);
    this._customerId.set(null);
  }

  // ============================================================
  // RESET CUSTOMER SUGGESTIONS
  // ============================================================

  resetCustomerSuggestions() {
    this._customerSuggestions.set([]);
  }

  // ============================================================
  // RESET CUSTOMER IDS AND NAMES
  // ============================================================

  resetCustomerIdsAndNames() {
    this._customerIdsAndNames.set([]);
  }

  // ============================================================
  // RESET CUSTOMER PHOTO
  // ============================================================

  resetCustomerPhoto() {

    const oldUrl = this._customerPhoto();

    if (oldUrl) {
      URL.revokeObjectURL(oldUrl);
    }

    this._customerPhoto.set(null);
  }

  setCustomerIdsAndNames() {
    this._customerIdsAndNames.set([]);
  }

  setModalCustomerRegister() {
    this._modalCustomerRegister.update(value => !value);
  }
}

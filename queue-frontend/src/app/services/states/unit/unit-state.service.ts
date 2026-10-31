
import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpService } from '../../backend/http.service';
import { ResponseUnitDto } from '../../../dtos/unit/ResponseUnitDto';
import { CreateUnitDto } from '../../../dtos/unit/CreateUnitDto';
import { UpdateUnitDto } from '../../../dtos/unit/UpdateUnitDto';
import { PageResponse } from '../../../dtos/page/PageResponse';
import { HttpErrorResponse } from '@angular/common/http';
import { catchError, EMPTY, finalize, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UnitStateService {

  // INJECTIONS
  private http = inject(HttpService);

  // ============================================================
  // UNIT SIGNALS
  // ============================================================

  private _units = signal<ResponseUnitDto[]>([]);
  private _unitById = signal<ResponseUnitDto | null>(null);

  // UNIT SIGNALS READONLY
  public readonly units = this._units.asReadonly();
  public readonly unitById = this._unitById.asReadonly();

  // ============================================================
  // MESSAGES SIGNALS
  // ============================================================

  private _createMessage = signal('');
  private _createStatus = signal<'success' | 'error' | 'default'>('default');

  private _updateMessage = signal('');
  private _updateStatus = signal<'success' | 'error' | 'default'>('default');

  private _deleteMessage = signal('');
  private _deleteStatus = signal<'success' | 'error' | 'default'>('default');

  private _loadMessage = signal('');
  private _loadStatus = signal<'loading' | 'success' | 'error' | 'default'>('default');

  // MESSAGES SIGNALS READONLY
  public readonly createMessage = this._createMessage.asReadonly();
  public readonly createStatus = this._createStatus.asReadonly();

  public readonly updateMessage = this._updateMessage.asReadonly();
  public readonly updateStatus = this._updateStatus.asReadonly();

  public readonly deleteMessage = this._deleteMessage.asReadonly();
  public readonly deleteStatus = this._deleteStatus.asReadonly();

  public readonly loadMessage = this._loadMessage.asReadonly();
  public readonly loadStatus = this._loadStatus.asReadonly();

  // ============================================================
  // LOADING SIGNALS
  // ============================================================

  private _allUnitsLoading = signal(false);
  private _createLoading = signal(false);
  private _updateLoading = signal(false);
  private _deleteLoading = signal(false);

  // LOADING SIGNALS READONLY
  public readonly allUnitsLoading = this._allUnitsLoading.asReadonly();
  public readonly createLoading = this._createLoading.asReadonly();
  public readonly updateLoading = this._updateLoading.asReadonly();
  public readonly deleteLoading = this._deleteLoading.asReadonly();

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
  // COMPUTED
  // ============================================================

  public totalUnits = computed(() => this.units().length);

  public activeUnits = computed(() =>
    this.units().filter(unit => unit.active).length
  );

  public inactiveUnits = computed(() =>
    this.units().filter(unit => !unit.active).length
  );

  // ============================================================
  // CREATE UNIT
  // ============================================================

  createUnit(request: CreateUnitDto) {

    this._createLoading.set(true);

    this.http.createUnit(request)
      .pipe(
        tap((response) => {
          this._createMessage.set('Unidade criada com sucesso!');
          this._createStatus.set('success');

          this._units.update(units => {
            this._totalElements.update(total => total + 1);
            return [response, ...units];
          });
        }),

        catchError((error: HttpErrorResponse) => {
          this._createMessage.set(
            error.error?.message || 'Erro ao criar unidade'
          );
          this._createStatus.set('error');

          return EMPTY;
        }),

        finalize(() => {
          this._createLoading.set(false);
          console.log('Requisição de criação finalizada.');
        })
      )
      .subscribe();
  }

  // ============================================================
  // UPDATE UNIT
  // ============================================================

  updateUnit(request: UpdateUnitDto) {

    this._updateLoading.set(true);

    this.http.updateUnit(request)
      .pipe(
        tap((response) => {
          this._updateMessage.set('Unidade atualizada com sucesso!');
          this._updateStatus.set('success');

          this._units.update(
            units => units.map(unit =>
              unit.unitId === response.unitId ? response : unit
            )
          );
        }),

        catchError((error: HttpErrorResponse) => {
          this._updateMessage.set(
            error.error?.message || 'Erro ao atualizar unidade'
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
  // DELETE UNIT
  // ============================================================

  deleteUnit(unitId: string) {

    this._deleteLoading.set(true);

    this.http.deleteUnit(unitId)
      .pipe(
        tap(() => {
          this._deleteMessage.set('Unidade excluída com sucesso!');
          this._deleteStatus.set('success');

          this._units.update(units => {
            this._totalElements.update(total => total - 1);

            return units.filter(unit => unit.unitId !== unitId);
          });

          this._unitById.set(null);
        }),

        catchError((error: HttpErrorResponse) => {
          this._deleteMessage.set(
            error.error?.message || 'Erro ao excluir unidade'
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
  // GET ALL UNITS
  // ============================================================

  loadingAllUnits() {

    this._allUnitsLoading.set(true);
    this._loadStatus.set('loading');
    this._loadMessage.set('Carregando unidades...');

    this.http.getAllUnits(
      this.page(),
      this.size(),
      this.search()
    )
      .pipe(
        tap((response: PageResponse<ResponseUnitDto>) => {
          this._units.set(response.content);
          this._totalElements.set(response.totalElements);

          this._loadStatus.set('success');
          this._loadMessage.set('Unidades carregadas com sucesso!');
        }),

        catchError((error: HttpErrorResponse) => {
          this._loadStatus.set('error');
          this._loadMessage.set(
            error.error?.message || 'Erro ao carregar unidades'
          );

          console.error('Erro ao carregar unidades:', error);

          return EMPTY;
        }),

        finalize(() => {
          this._allUnitsLoading.set(false);
          console.log('Requisição de carregamento finalizada.');
        })
      )
      .subscribe();
  }

  // ============================================================
  // GET ALL UNITS FOR LOGIN
  // ============================================================

  loadingAllUnitsForLogin() {

    this._loadStatus.set('loading');
    this._loadMessage.set('Carregando unidades...');

    this.http.getAllUnits(0, 999)
      .pipe(
        tap((response: PageResponse<ResponseUnitDto>) => {
          this._units.set(response.content);
          this._totalElements.set(response.totalElements);

          this._loadStatus.set('success');
          this._loadMessage.set('Unidades carregadas com sucesso!');
        }),

        catchError((error: HttpErrorResponse) => {
          this._loadStatus.set('error');
          this._loadMessage.set(
            error.error?.message || 'Erro ao carregar unidades'
          );

          console.error('Erro ao carregar unidades:', error);

          return EMPTY;
        }),

        finalize(() => {
          console.log('Requisição de carregamento para login finalizada.');
        })
      )
      .subscribe();
  }

  // ============================================================
  // GET UNIT BY ID
  // ============================================================

  getUnitById(unitId: string) {

    const unit = this.units().find(unit => unit.unitId === unitId);

    if (unit) {
      this._unitById.set(unit);
      return;
    }

    this.http.getUnitById(unitId)
      .pipe(
        tap((response) => {
          this._unitById.set(response);
        }),

        catchError((error: HttpErrorResponse) => {
          console.error('Erro ao carregar unidade:', error);
          return EMPTY;
        }),

        finalize(() => {
          console.log('Requisição de unidade por ID finalizada.');
        })
      )
      .subscribe();
  }

  // ============================================================
  // RESET UNIT BY ID
  // ============================================================

  resetUnitById() {
    this._unitById.set(null);
  }

  // ============================================================
  // PAGINATION
  // ============================================================

  nextPage() {

    if (this.page() + 1 >= this.totalPages()) return;

    this._page.update(page => page + 1);

    this.loadingAllUnits();
  }

  previousPage() {

    if (this.page() === 0) return;

    this._page.update(page => page - 1);

    this.loadingAllUnits();
  }

  goToPage(page: number) {

    if (page < 0 || page >= this.totalPages()) return;

    this._page.set(page);

    this.loadingAllUnits();
  }

  // ============================================================
  // SEARCH
  // ============================================================

  setSearch(value: string) {

    this._search.set(value);
    this._page.set(0);

    this.loadingAllUnits();
  }

  // ============================================================
  // SET SIZE
  // ============================================================

  setSize(size: number) {

    this._size.set(size);
    this._page.set(0);

    this.loadingAllUnits();
  }

  // ============================================================
  // RESET STATUS
  // ============================================================

  resetCreateStatus() {
    this._createStatus.set('default');
    this._createMessage.set('');
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
    this.resetCreateStatus();
    this.resetUpdateStatus();
    this.resetDeleteStatus();
    this.resetLoadStatus();
  }

  // ============================================================
  // REFRESH UNITS
  // ============================================================

  refreshUnits() {
    this.loadingAllUnits();
  }
}

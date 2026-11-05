
import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { EMPTY } from 'rxjs';
import { catchError, finalize, tap } from 'rxjs/operators';

import { HttpService } from '../../backend/http.service';

import { CreateScheduleDto } from '../../../dtos/schedule/CreateScheduleDto';
import { UpdateScheduleDto } from '../../../dtos/schedule/UpdateScheduleDto';
import { ResponseAllSchedulesDto } from '../../../dtos/schedule/ResponseAllSchedulesDto';
import { ResponseScheduleDto } from '../../../dtos/schedule/ResponseScheduleDto';

import { ResponseCountTotalSchedulesStatisticsDto } from '../../../dtos/schedule/statistics/ResponseCountTotalSchedulesStatisticsDto';
import { ResponseSchedulePercentagesStatisticsDto } from '../../../dtos/schedule/statistics/ResponseSchedulePercentagesStatisticsDto';
import { ResponseSchedulesByDepartmentStatisticsDto } from '../../../dtos/schedule/statistics/ResponseSchedulesByDepartmentStatisticsDto';
import { ResponseSchedulesByHourStatisticsDto } from '../../../dtos/schedule/statistics/ResponseSchedulesByHourStatisticsDto';
import { ResponseSchedulesByPriorityStatisticsDto } from '../../../dtos/schedule/statistics/ResponseSchedulesByPriorityStatisticsDto';
import { ResponseSchedulesCreatedByDayStatisticsDto } from '../../../dtos/schedule/statistics/ResponseSchedulesCreatedByDayStatisticsDto';
import { ResponseSchedulesCreatedByMonthStatisticsDto } from '../../../dtos/schedule/statistics/ResponseSchedulesCreatedByMonthStatisticsDto';
import { ResponseSchedulesCreatedByWeekStatisticsDto } from '../../../dtos/schedule/statistics/ResponseSchedulesCreatedByWeekStatisticsDto';
import { ResponseSchedulesByServiceStatisticsDto } from '../../../dtos/services/statistics/ResponseSchedulesByServiceStatisticsDto';

@Injectable({
  providedIn: 'root'
})
export class ScheduleStateService {

  private http = inject(HttpService);

  // ===== STATES =====

  private _schedules = signal<ResponseAllSchedulesDto[]>([]);
  public schedules = this._schedules.asReadonly();

  private _scheduleInfo = signal<ResponseScheduleDto | null>(null);
  public scheduleInfo = this._scheduleInfo.asReadonly();

  // ===== PAGINATION =====

  private _schedulePage = signal(0);
  public schedulePage = this._schedulePage.asReadonly();

  public scheduleSize = 4;

  private _scheduleTotalElements = signal(0);
  public scheduleTotalElements = this._scheduleTotalElements.asReadonly();

  public scheduleTotalPages = computed(() =>
    Math.ceil(this.scheduleTotalElements() / this.scheduleSize)
  );

  // ===== SEARCH =====

  private _scheduleSearch = signal('');
  public scheduleSearch = this._scheduleSearch.asReadonly();

  private _scheduleSearchDate = signal<string | null>(null);
  public scheduleSearchDate = this._scheduleSearchDate.asReadonly();

  // ===== MESSAGES =====

  private _registerMessage = signal('');
  public registerMessage = this._registerMessage.asReadonly();

  private _registerStatus = signal<'success' | 'error' | 'default'>('default');
  public registerStatus = this._registerStatus.asReadonly();

  private _updateMessage = signal('');
  public updateMessage = this._updateMessage.asReadonly();

  private _updateStatus = signal<'success' | 'error' | 'default'>('default');
  public updateStatus = this._updateStatus.asReadonly();

  private _deleteMessage = signal('');
  public deleteMessage = this._deleteMessage.asReadonly();

  private _deleteStatus = signal<'success' | 'error' | 'default'>('default');
  public deleteStatus = this._deleteStatus.asReadonly();

  // ===== LOADING =====

  private _allSchedulesLoading = signal(false);
  public allSchedulesLoading = this._allSchedulesLoading.asReadonly();

  private _scheduleInfoLoading = signal(false);
  public scheduleInfoLoading = this._scheduleInfoLoading.asReadonly();

  private _statisticsLoading = signal(false);
  public statisticsLoading = this._statisticsLoading.asReadonly();

  private _registerLoading = signal(false);
  public registerLoading = this._registerLoading.asReadonly();

  private _updateLoading = signal(false);
  public updateLoading = this._updateLoading.asReadonly();

  private _deleteLoading = signal(false);
  public deleteLoading = this._deleteLoading.asReadonly();

  // ===== STATISTICS =====

  private _countTotalScheduleStatistics =
    signal<ResponseCountTotalSchedulesStatisticsDto | null>(null);
  public countTotalScheduleStatistics = this._countTotalScheduleStatistics.asReadonly();

  private _schedulePercentagesStatistics =
    signal<ResponseSchedulePercentagesStatisticsDto | null>(null);
  public schedulePercentagesStatistics = this._schedulePercentagesStatistics.asReadonly();

  private _schedulesCreatedByMonth =
    signal<ResponseSchedulesCreatedByMonthStatisticsDto[] | null>([]);
  public schedulesCreatedByMonth = this._schedulesCreatedByMonth.asReadonly();

  private _schedulesCreatedByWeek =
    signal<ResponseSchedulesCreatedByWeekStatisticsDto[] | null>([]);
  public schedulesCreatedByWeek = this._schedulesCreatedByWeek.asReadonly();

  private _scheduleCreatedByDay =
    signal<ResponseSchedulesCreatedByDayStatisticsDto | null>(null);
  public scheduleCreatedByDay = this._scheduleCreatedByDay.asReadonly();

  private _schedulesByDepartment =
    signal<ResponseSchedulesByDepartmentStatisticsDto[] | null>([]);
  public schedulesByDepartment = this._schedulesByDepartment.asReadonly();

  private _schedulesByService =
    signal<ResponseSchedulesByServiceStatisticsDto[] | null>([]);
  public schedulesByService = this._schedulesByService.asReadonly();

  private _schedulesByPriority =
    signal<ResponseSchedulesByPriorityStatisticsDto[] | null>([]);
  public schedulesByPriority = this._schedulesByPriority.asReadonly();

  private _schedulesByHour =
    signal<ResponseSchedulesByHourStatisticsDto[] | null>([]);
  public schedulesByHour = this._schedulesByHour.asReadonly();

  // ===== MODALS =====

  private _modalSchedulingRegister = signal(false);
  public modalSchedulingRegister = this._modalSchedulingRegister.asReadonly();

  private _table = signal('Scheduling');
  public table = this._table.asReadonly();

  // ===== METHODS =====

  // Register Schedule
  createSchedule(request: CreateScheduleDto): void {
    this._registerLoading.set(true);
    this._registerStatus.set('default');
    this._registerMessage.set('');

    this.http.createSchedule(request).pipe(
      tap(() => {
        this._registerMessage.set('Agendamento realizado com sucesso!');
        this._registerStatus.set('success');

        this._schedulePage.set(0);
        this.loadSchedules();
        this.loadStatistics();
      }),

      catchError((error: HttpErrorResponse) => {
        this._registerMessage.set(
          error.error?.message || 'Erro ao realizar agendamento'
        );
        this._registerStatus.set('error');

        return EMPTY;
      }),

      finalize(() => this._registerLoading.set(false))
    ).subscribe();
  }

  // Update Schedule
  updateSchedule(request: UpdateScheduleDto): void {
    this._updateLoading.set(true);
    this._updateStatus.set('default');
    this._updateMessage.set('');

    this.http.updateSchedule(request).pipe(
      tap(() => {
        this._updateMessage.set('Agendamento atualizado com sucesso!');
        this._updateStatus.set('success');

        this.loadSchedules();
        this.loadStatistics();
      }),

      catchError((error: HttpErrorResponse) => {
        this._updateMessage.set(
          error.error?.message || 'Erro ao atualizar agendamento'
        );
        this._updateStatus.set('error');

        return EMPTY;
      }),

      finalize(() => this._updateLoading.set(false))
    ).subscribe();
  }

  // aqui onde nos deletamos os agendamentos, e enviam

  // Delete Schedule
  deleteSchedule(scheduleId: string): void {
    this._deleteLoading.set(true);
    this._deleteStatus.set('default');
    this._deleteMessage.set('');

    this.http.deleteSchedule(scheduleId).pipe(
      tap(() => {
        this._deleteMessage.set('Agendamento deletado com sucesso!');
        this._deleteStatus.set('success');

        this.loadSchedules();
        this.loadStatistics();
      }),

      catchError((error: HttpErrorResponse) => {
        this._deleteMessage.set(
          error.error?.message || 'Erro ao deletar agendamento'
        );
        this._deleteStatus.set('error');

        return EMPTY;
      }),

      finalize(() => this._deleteLoading.set(false))
    ).subscribe();
  }

  // ===== STATISTICS =====

  loadStatistics(): void {
    this._statisticsLoading.set(true);

    this.http.getScheduleStatistics().pipe(
      tap(response => {
        this._countTotalScheduleStatistics.set(
          response.countTotalScheduleStatistics
        );

        this._schedulePercentagesStatistics.set(
          response.schedulePercentagesStatistics
        );

        this._schedulesCreatedByMonth.set(
          response.schedulesCreatedByMonth
        );

        this._schedulesCreatedByWeek.set(
          response.schedulesCreatedByWeek
        );

        this._scheduleCreatedByDay.set(
          response.scheduleCreatedByDay
        );

        this._schedulesByDepartment.set(
          response.schedulesByDepartment
        );

        this._schedulesByService.set(
          response.schedulesByService
        );

        this._schedulesByPriority.set(
          response.schedulesByPriority
        );

        this._schedulesByHour.set(
          response.schedulesByHour
        );
      }),

      catchError((error: HttpErrorResponse) => {
        console.error('Erro ao carregar estatísticas:', error);
        return EMPTY;
      }),

      finalize(() => this._statisticsLoading.set(false))
    ).subscribe();
  }

  // ===== LOAD SCHEDULES =====

  loadSchedules(): void {
    this._allSchedulesLoading.set(true);

    this.http.getAllScheduling(
      this.schedulePage(),
      this.scheduleSize,
      this.scheduleSearch(),
      this.scheduleSearchDate()
    ).pipe(
      tap(response => {
        this._schedules.set(response.content);
        this._scheduleTotalElements.set(response.totalElements);
      }),

      catchError((error: HttpErrorResponse) => {
        console.error('Erro ao carregar agendamentos:', error);
        return EMPTY;
      }),

      finalize(() => this._allSchedulesLoading.set(false))
    ).subscribe();
  }

  // ===== GET SCHEDULE BY ID =====

  getScheduleById(scheduleId: string): void {
    this._scheduleInfoLoading.set(true);

    this.http.getScheduleById(scheduleId).pipe(
      tap(response => {
        this._scheduleInfo.set(response);
      }),

      catchError((error: HttpErrorResponse) => {
        this._scheduleInfo.set(null);
        console.error('Erro ao buscar agendamento:', error);

        return EMPTY;
      }),

      finalize(() => this._scheduleInfoLoading.set(false))
    ).subscribe();
  }

  // ===== PAGINATION =====

  nextPage(): void {
    if (this.schedulePage() + 1 >= this.scheduleTotalPages()) return;

    this._schedulePage.update(page => page + 1);
    this.loadSchedules();
  }

  previousPage(): void {
    if (this.schedulePage() === 0) return;

    this._schedulePage.update(page => page - 1);
    this.loadSchedules();
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.scheduleTotalPages()) return;

    this._schedulePage.set(page);
    this.loadSchedules();
  }

  // ===== SEARCH =====

  setSearch(value: string): void {
    this._scheduleSearch.set(value);
    this._schedulePage.set(0);
    this.loadSchedules();
  }

  setSearchDate(value: string | null): void {
    this._scheduleSearchDate.set(value);
    this._schedulePage.set(0);
    this.loadSchedules();
  }

  // ===== MODALS =====

  setModalSchedulingRegister() {
    this._modalSchedulingRegister.set(!this.modalSchedulingRegister);
  }

  setTable(value: string): void {
    this._table.set(value);
  }

  // ===== RESETS =====

  resetStatus(): void {
    this._registerMessage.set('');
    this._registerStatus.set('default');

    this._updateMessage.set('');
    this._updateStatus.set('default');

    this._deleteMessage.set('');
    this._deleteStatus.set('default');
  }

  resetScheduleInfo(): void {
    this._scheduleInfo.set(null);
  }
}

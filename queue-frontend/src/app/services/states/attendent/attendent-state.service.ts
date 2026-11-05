
import { inject, Injectable, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { EMPTY } from 'rxjs';
import { catchError, finalize, tap } from 'rxjs/operators';

import { HttpService } from '../../backend/http.service';

import { ResponseCountTotalAttendancesStatisticsDto } from '../../../dtos/attendance/statistics/ResponseCountTotalAttendancesStatisticsDto';
import { ResponseAverageServiceTimeStatisticsDto } from '../../../dtos/attendance/statistics/ResponseAverageServiceTimeStatisticsDto';
import { ResponseAttendancesByCustomerStatisticsDto } from '../../../dtos/attendance/statistics/ResponseAttendancesByCustomerStatisticsDto';
import { ResponseAttendancesByDepartmentStatisticsDto } from '../../../dtos/attendance/statistics/ResponseAttendancesByDepartmentStatisticsDto';
import { ResponseAttendancesByHourStatisticsDto } from '../../../dtos/attendance/statistics/ResponseAttendancesByHourStatisticsDto';
import { ResponseAttendancesByServiceStatisticsDto } from '../../../dtos/attendance/statistics/ResponseAttendancesByServiceStatisticsDto';
import { ResponseAttendancesByWeekStatisticsDto } from '../../../dtos/attendance/statistics/ResponseAttendancesByWeekStatisticsDto';
import { ResponseAttendancesCreatedByMonthStatisticsDto } from '../../../dtos/attendance/statistics/ResponseAttendancesCreatedByMonthStatisticsDto';
import { ResponseAverageAttendanceByUserStatisticsDto } from '../../../dtos/attendance/statistics/ResponseAverageAttendanceByUserStatisticsDto';
import { ResponseAverageWaitingTimeStatisticsDto } from '../../../dtos/attendance/statistics/ResponseAverageWaitingTimeStatisticsDto';

import { TicketStateService } from '../ticket/ticket-state.service';

@Injectable({
  providedIn: 'root'
})
export class AttendentStateService {

  // ===== INJECTIONS =====

  private http = inject(HttpService);
  private ticketState = inject(TicketStateService);

  // ===== ATTENDANCE STATES =====

  private _countTotalAttendances =
    signal<ResponseCountTotalAttendancesStatisticsDto | null>(null);
  public countTotalAttendances = this._countTotalAttendances.asReadonly();

  private _averageWaitingTime =
    signal<ResponseAverageWaitingTimeStatisticsDto | null>(null);
  public averageWaitingTime = this._averageWaitingTime.asReadonly();

  private _averageServiceTime =
    signal<ResponseAverageServiceTimeStatisticsDto | null>(null);
  public averageServiceTime = this._averageServiceTime.asReadonly();

  private _averageAttendanceByUser =
    signal<ResponseAverageAttendanceByUserStatisticsDto[] | null>(null);
  public averageAttendanceByUser = this._averageAttendanceByUser.asReadonly();

  private _attendancesCreatedByMonth =
    signal<ResponseAttendancesCreatedByMonthStatisticsDto[] | null>(null);
  public attendancesCreatedByMonth = this._attendancesCreatedByMonth.asReadonly();

  private _attendancesByWeek =
    signal<ResponseAttendancesByWeekStatisticsDto[] | null>(null);
  public attendancesByWeek = this._attendancesByWeek.asReadonly();

  private _attendancesByService =
    signal<ResponseAttendancesByServiceStatisticsDto[] | null>(null);
  public attendancesByService = this._attendancesByService.asReadonly();

  private _attendancesByHour =
    signal<ResponseAttendancesByHourStatisticsDto[] | null>(null);
  public attendancesByHour = this._attendancesByHour.asReadonly();

  private _attendancesByDepartment =
    signal<ResponseAttendancesByDepartmentStatisticsDto[] | null>(null);
  public attendancesByDepartment = this._attendancesByDepartment.asReadonly();

  private _attendancesByCustomer =
    signal<ResponseAttendancesByCustomerStatisticsDto[] | null>(null);
  public attendancesByCustomer = this._attendancesByCustomer.asReadonly();

  // ===== TIME =====

  private _currentTimer = signal<string>('00:00:00');
  public currentTimer = this._currentTimer.asReadonly();

  private intervalId: ReturnType<typeof setInterval> | undefined;

  // ===== LOADING =====

  private _statisticsLoading = signal(false);
  public statisticsLoading = this._statisticsLoading.asReadonly();

  private _startAttendanceLoading = signal(false);
  public startAttendanceLoading = this._startAttendanceLoading.asReadonly();

  private _finishAttendanceLoading = signal(false);
  public finishAttendanceLoading = this._finishAttendanceLoading.asReadonly();

  private _cancelAttendanceLoading = signal(false);
  public cancelAttendanceLoading = this._cancelAttendanceLoading.asReadonly();

  // ===== MESSAGES =====

  private _startAttendanceMessage = signal('');
  public startAttendanceMessage = this._startAttendanceMessage.asReadonly();

  private _startAttendanceStatus =
    signal<'success' | 'error' | 'default'>('default');
  public startAttendanceStatus = this._startAttendanceStatus.asReadonly();

  private _finishAttendanceMessage = signal('');
  public finishAttendanceMessage = this._finishAttendanceMessage.asReadonly();

  private _finishAttendanceStatus =
    signal<'success' | 'error' | 'default'>('default');
  public finishAttendanceStatus = this._finishAttendanceStatus.asReadonly();

  private _cancelAttendanceMessage = signal('');
  public cancelAttendanceMessage = this._cancelAttendanceMessage.asReadonly();

  private _cancelAttendanceStatus =
    signal<'success' | 'error' | 'default'>('default');
  public cancelAttendanceStatus = this._cancelAttendanceStatus.asReadonly();


  // ===== LOAD STATISTICS =====

  loadStatistics(): void {

    this._statisticsLoading.set(true);

    this.http.getAttendanceStatistics().pipe(

      tap(response => {
        this._countTotalAttendances.set(response.countTotalAttendances);
        this._averageWaitingTime.set(response.averageWaitingTime);
        this._averageServiceTime.set(response.averageServiceTime);
        this._averageAttendanceByUser.set(response.averageAttendanceByUser);
        this._attendancesCreatedByMonth.set(response.attendancesCreatedByMonth);
        this._attendancesByWeek.set(response.attendancesByWeek);
        this._attendancesByService.set(response.attendancesByService);
        this._attendancesByHour.set(response.attendancesByHour);
        this._attendancesByDepartment.set(response.attendancesByDepartment);
        this._attendancesByCustomer.set(response.attendancesByCustomer);
      }),

      catchError(() => EMPTY),

      finalize(() => this._statisticsLoading.set(false))

    ).subscribe();
  }


  // ===== START ATTENDANCE =====

  startAttendance(ticketId: string): void {

    this._startAttendanceLoading.set(true);
    this._startAttendanceStatus.set('default');
    this._startAttendanceMessage.set('');

    this.http.startAttendance({ ticketId }).pipe(

      tap(response => {
        this.startTimer(response.startedAt);

        this.loadStatistics();
        this.ticketState.getTicketsForAttendence();

        this._startAttendanceMessage.set(
          'Atendimento iniciado com sucesso!'
        );
        this._startAttendanceStatus.set('success');
      }),

      catchError((error: HttpErrorResponse) => {
        this._startAttendanceMessage.set(
          error.error?.message || 'Erro ao iniciar atendimento!'
        );
        this._startAttendanceStatus.set('error');

        return EMPTY;
      }),

      finalize(() => this._startAttendanceLoading.set(false))

    ).subscribe();
  }


  // ===== FINISH ATTENDANCE =====

  finishAttendance(ticketId: string, resolution: string): void {

    this._finishAttendanceLoading.set(true);
    this._finishAttendanceStatus.set('default');
    this._finishAttendanceMessage.set('');

    this.http.finishAttendance({ ticketId, resolution }).pipe(

      tap(() => {
        this.stopTimer();

        this.loadStatistics();
        this.ticketState.getTicketsForAttendence();
        this.ticketState.getHistoryTickets();
        this.ticketState.resetTicketForPanel();

        this._finishAttendanceMessage.set(
          'Atendimento finalizado com sucesso!'
        );
        this._finishAttendanceStatus.set('success');
      }),

      catchError((error: HttpErrorResponse) => {
        this._finishAttendanceMessage.set(
          error.error?.message || 'Erro ao finalizar atendimento!'
        );
        this._finishAttendanceStatus.set('error');

        return EMPTY;
      }),

      finalize(() => this._finishAttendanceLoading.set(false))

    ).subscribe();
  }


  // ===== CANCEL ATTENDANCE =====

  cancelAttendance(ticketId: string): void {

    this._cancelAttendanceLoading.set(true);
    this._cancelAttendanceStatus.set('default');
    this._cancelAttendanceMessage.set('');

    this.http.cancelTicket(ticketId).pipe(

      tap(() => {
        this.ticketState.getTicketsForAttendence();

        this._cancelAttendanceMessage.set(
          'Atendimento cancelado com sucesso!'
        );
        this._cancelAttendanceStatus.set('success');
      }),

      catchError((error: HttpErrorResponse) => {
        this._cancelAttendanceMessage.set(
          error.error?.message || 'Erro ao cancelar atendimento!'
        );
        this._cancelAttendanceStatus.set('error');

        return EMPTY;
      }),

      finalize(() => this._cancelAttendanceLoading.set(false))

    ).subscribe();
  }


  // ===== TIMER =====

  startTimer(startedAt: string): void {

    const startDate = this.convertBackendDateToLocal(startedAt);

    if (this.intervalId) {
      clearInterval(this.intervalId);
    }

    const updateTimer = () => {

      const diff = Math.floor(
        (Date.now() - startDate.getTime()) / 1000
      );

      const hours = Math.floor(diff / 3600);
      const minutes = Math.floor((diff % 3600) / 60);
      const seconds = diff % 60;

      this._currentTimer.set(
        `${hours.toString().padStart(2, '0')}:` +
        `${minutes.toString().padStart(2, '0')}:` +
        `${seconds.toString().padStart(2, '0')}`
      );
    };

    updateTimer();

    this.intervalId = setInterval(updateTimer, 1000);
  }


  private convertBackendDateToLocal(startedAt: string): Date {

    const date = new Date(startedAt);
    date.setHours(date.getHours() - 3);

    return date;
  }


  stopTimer(): void {

    if (this.intervalId) {
      clearInterval(this.intervalId);
      this.intervalId = undefined;
    }

    this._currentTimer.set('00:00:00');
  }


  // ===== RESETS =====

  resetStatus(): void {

    this._startAttendanceMessage.set('');
    this._startAttendanceStatus.set('default');

    this._finishAttendanceMessage.set('');
    this._finishAttendanceStatus.set('default');

    this._cancelAttendanceMessage.set('');
    this._cancelAttendanceStatus.set('default');
  }


  // ===== INCREMENT =====

  incrementWaitingAttendances(): void {

    this._countTotalAttendances.update(state => {
      if (!state) return state;

      return {
        ...state,
        countAttendancesWaiting: state.countAttendancesWaiting + 1
      };
    });
  }

}

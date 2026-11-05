
import { inject, Injectable, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { EMPTY } from 'rxjs';
import { catchError, finalize, tap } from 'rxjs/operators';

import { HttpService } from '../../backend/http.service';

import { CreateTicketDto } from '../../../dtos/ticket/CreateTicketDto';
import { ResponseTicketDto } from '../../../dtos/ticket/ResponseTicketDto';
import { ResponseTicketsForAttendanceDto } from '../../../dtos/ticket/ResponseTicketsForAttendanceDto';

@Injectable({
  providedIn: 'root'
})
export class TicketStateService {

  // ===== INJECTIONS =====

  private http = inject(HttpService);

  // ===== STATES =====

  private _ticketInfo = signal<ResponseTicketDto | null>(null);
  public ticketInfo = this._ticketInfo.asReadonly();

  private _ticketForPanel = signal<ResponseTicketDto | null>(null);
  public ticketForPanel = this._ticketForPanel.asReadonly();

  // ===== MESSAGES =====

  private _createStatus = signal<'success' | 'error' | 'default'>('default');
  public createStatus = this._createStatus.asReadonly();

  private _createMessage = signal('');
  public createMessage = this._createMessage.asReadonly();

  private _deleteStatus = signal<'success' | 'error' | 'default'>('default');
  public deleteStatus = this._deleteStatus.asReadonly();

  private _deleteMessage = signal('');
  public deleteMessage = this._deleteMessage.asReadonly();

  private _callTicketStatus = signal<'success' | 'error' | 'default'>('default');
  public callTicketStatus = this._callTicketStatus.asReadonly();

  private _callTicketMessage = signal('');
  public callTicketMessage = this._callTicketMessage.asReadonly();

  private _callCustomerStatus = signal<'success' | 'error' | 'default'>('default');
  public callCustomerStatus = this._callCustomerStatus.asReadonly();

  private _callCustomerMessage = signal('');
  public callCustomerMessage = this._callCustomerMessage.asReadonly();

  private _resetCodeStatus = signal<'success' | 'error' | 'default'>('default');
  public resetCodeStatus = this._resetCodeStatus.asReadonly();

  private _resetCodeMessage = signal('');
  public resetCodeMessage = this._resetCodeMessage.asReadonly();

  private _resetAllCodesStatus = signal<'success' | 'error' | 'default'>('default');
  public resetAllCodesStatus = this._resetAllCodesStatus.asReadonly();

  private _resetAllCodesMessage = signal('');
  public resetAllCodesMessage = this._resetAllCodesMessage.asReadonly();

  // ===== LOADING =====

  private _createLoading = signal(false);
  public createLoading = this._createLoading.asReadonly();

  private _deleteLoading = signal(false);
  public deleteLoading = this._deleteLoading.asReadonly();

  private _callTicketLoading = signal(false);
  public callTicketLoading = this._callTicketLoading.asReadonly();

  private _callCustomerLoading = signal(false);
  public callCustomerLoading = this._callCustomerLoading.asReadonly();

  private _ticketsLoading = signal(false);
  public ticketsLoading = this._ticketsLoading.asReadonly();

  private _historyLoading = signal(false);
  public historyLoading = this._historyLoading.asReadonly();

  private _ticketInfoLoading = signal(false);
  public ticketInfoLoading = this._ticketInfoLoading.asReadonly();

  private _resetCodeLoading = signal(false);
  public resetCodeLoading = this._resetCodeLoading.asReadonly();

  private _resetAllCodesLoading = signal(false);
  public resetAllCodesLoading = this._resetAllCodesLoading.asReadonly();

  // ===== TICKETS =====

  private _ticketsForAttendance = signal<ResponseTicketsForAttendanceDto[]>([]);
  public ticketsForAttendance = this._ticketsForAttendance.asReadonly();

  private _historyTicketsForAttendant = signal<ResponseTicketsForAttendanceDto[]>([]);
  public historyTicketsForAttendant = this._historyTicketsForAttendant.asReadonly();

  private _historyTickets = signal<ResponseTicketsForAttendanceDto[]>([]);
  public historyTickets = this._historyTickets.asReadonly();

  private _totalTickets = signal(0);
  public totalTickets = this._totalTickets.asReadonly();

  // ===== PAGINATION TICKETS =====

  private _pageTickets = signal(0);
  public pageTickets = this._pageTickets.asReadonly();

  public readonly size = 6;

  private _totalElementsTickets = signal(0);
  public totalElementsTickets = this._totalElementsTickets.asReadonly();

  private _totalPagesTickets = signal(0);
  public totalPagesTickets = this._totalPagesTickets.asReadonly();

  // ===== PAGINATION HISTORY =====

  private _pageHistory = signal(0);
  public pageHistory = this._pageHistory.asReadonly();

  private _totalElementsHistory = signal(0);
  public totalElementsHistory = this._totalElementsHistory.asReadonly();

  private _totalPagesHistory = signal(0);
  public totalPagesHistory = this._totalPagesHistory.asReadonly();


  // ===== CREATE TICKET =====

  createTicket(request: CreateTicketDto): void {

    this._createLoading.set(true);
    this._createStatus.set('default');
    this._createMessage.set('');

    this.http.createTicket(request).pipe(

      tap(response => {
        this._createStatus.set('success');
        this._createMessage.set('Ticket criado com sucesso!');

        this._ticketInfo.set(response);

        this._pageTickets.set(0);
        this.getTicketsForAttendence();
      }),

      catchError((error: HttpErrorResponse) => {
        this._createStatus.set('error');
        this._createMessage.set(
          error.error?.message || 'Erro ao criar ticket'
        );

        return EMPTY;
      }),

      finalize(() => this._createLoading.set(false))

    ).subscribe();
  }


  // ===== DELETE TICKET =====

  deleteTicket(request: string): void {

    this._deleteLoading.set(true);
    this._deleteStatus.set('default');
    this._deleteMessage.set('');

    this.http.deleteTicket(request).pipe(

      tap(() => {
        this._deleteStatus.set('success');
        this._deleteMessage.set('Ticket deletado com sucesso!');

        this.getTicketsForAttendence();
      }),

      catchError((error: HttpErrorResponse) => {
        this._deleteStatus.set('error');
        this._deleteMessage.set(
          error.error?.message || 'Erro ao deletar ticket'
        );

        return EMPTY;
      }),

      finalize(() => this._deleteLoading.set(false))

    ).subscribe();
  }


  // ===== CALL TICKET =====

  callTicket(ticketId: string): void {

    this._callTicketLoading.set(true);
    this._callTicketStatus.set('default');
    this._callTicketMessage.set('');

    this.http.callTicket(ticketId).pipe(

      tap(() => {
        this._callTicketStatus.set('success');
        this._callTicketMessage.set('Ticket chamado com sucesso!');
      }),

      catchError((error: HttpErrorResponse) => {
        this._callTicketStatus.set('error');
        this._callTicketMessage.set(
          error.error?.message || 'Erro ao chamar ticket'
        );

        return EMPTY;
      }),

      finalize(() => this._callTicketLoading.set(false))

    ).subscribe();
  }


  // ===== CALL CUSTOMER =====

  callCustomer(ticketId: string): void {

    this._callCustomerLoading.set(true);
    this._callCustomerStatus.set('default');
    this._callCustomerMessage.set('');

    this.http.callCustomer(ticketId).pipe(

      tap(() => {
        this._callCustomerStatus.set('success');
        this._callCustomerMessage.set('Cliente chamado com sucesso!');
      }),

      catchError((error: HttpErrorResponse) => {
        this._callCustomerStatus.set('error');
        this._callCustomerMessage.set(
          error.error?.message || 'Erro ao chamar cliente'
        );

        return EMPTY;
      }),

      finalize(() => this._callCustomerLoading.set(false))

    ).subscribe();
  }


  // ===== GET TICKETS FOR ATTENDANCE =====

  getTicketsForAttendence(): void {

    this._ticketsLoading.set(true);

    this.http.getTicketsForAttendance(
      this.pageTickets(),
      this.size
    ).pipe(

      tap(response => {
        this._ticketsForAttendance.set(response.content);
        this._totalTickets.set(response.totalElements);
        this._totalElementsTickets.set(response.totalElements);
        this._totalPagesTickets.set(response.totalPages);
      }),

      catchError((error: HttpErrorResponse) => {
        console.error('Erro ao carregar tickets:', error);
        return EMPTY;
      }),

      finalize(() => this._ticketsLoading.set(false))

    ).subscribe();
  }


  // ===== GET HISTORY TICKETS BY ATTENDANT =====

  getHistoryTicketsByAttendant(): void {

    this._historyLoading.set(true);

    this.http.getHistoryTicketsByAttendant(
      this.pageHistory(),
      this.size
    ).pipe(

      tap(response => {
        this._historyTicketsForAttendant.set(response.content);
        this._totalTickets.set(response.totalElements);
        this._totalElementsHistory.set(response.totalElements);
        this._totalPagesHistory.set(response.totalPages);
      }),

      catchError((error: HttpErrorResponse) => {
        console.error('Erro ao carregar histórico do atendente:', error);
        return EMPTY;
      }),

      finalize(() => this._historyLoading.set(false))

    ).subscribe();
  }


  // ===== GET HISTORY TICKETS =====

  getHistoryTickets(): void {

    this._historyLoading.set(true);

    this.http.getHistoryTickets(
      this.pageHistory(),
      this.size
    ).pipe(

      tap(response => {
        this._historyTickets.set(response.content);
        this._totalElementsHistory.set(response.totalElements);
        this._totalPagesHistory.set(response.totalPages);
      }),

      catchError((error: HttpErrorResponse) => {
        console.error('Erro ao carregar histórico de tickets:', error);
        return EMPTY;
      }),

      finalize(() => this._historyLoading.set(false))

    ).subscribe();
  }


  // ===== RESET CODE BY ID =====

  resetCodeById(serviceManagementId: string): void {

    this._resetCodeLoading.set(true);
    this._resetCodeStatus.set('default');
    this._resetCodeMessage.set('');

    this.http.resetCodeById(serviceManagementId).pipe(

      tap(() => {
        this._resetCodeStatus.set('success');
        this._resetCodeMessage.set('Código resetado com sucesso!');
      }),

      catchError((error: HttpErrorResponse) => {
        this._resetCodeStatus.set('error');
        this._resetCodeMessage.set(
          error.error?.message || 'Erro ao resetar código'
        );

        return EMPTY;
      }),

      finalize(() => this._resetCodeLoading.set(false))

    ).subscribe();
  }


  // ===== RESET ALL CODES =====

  resetAllCodes(): void {

    this._resetAllCodesLoading.set(true);
    this._resetAllCodesStatus.set('default');
    this._resetAllCodesMessage.set('');

    this.http.resetAllCodes().pipe(

      tap(() => {
        this._resetAllCodesStatus.set('success');
        this._resetAllCodesMessage.set(
          'Todos os códigos resetados com sucesso!'
        );
      }),

      catchError((error: HttpErrorResponse) => {
        this._resetAllCodesStatus.set('error');
        this._resetAllCodesMessage.set(
          error.error?.message || 'Erro ao resetar todos os códigos'
        );

        return EMPTY;
      }),

      finalize(() => this._resetAllCodesLoading.set(false))

    ).subscribe();
  }


  // ===== PAGINATION TICKETS =====

  nextPageTickets(): void {

    if (this.pageTickets() + 1 >= this.totalPagesTickets()) return;

    this._pageTickets.update(page => page + 1);
    this.getTicketsForAttendence();
  }

  previousPageTickets(): void {

    if (this.pageTickets() === 0) return;

    this._pageTickets.update(page => page - 1);
    this.getTicketsForAttendence();
  }

  goToPageTickets(page: number): void {

    if (page < 0 || page >= this.totalPagesTickets()) return;

    this._pageTickets.set(page);
    this.getTicketsForAttendence();
  }


  // ===== PAGINATION HISTORY =====

  nextPageHistory(): void {

    if (this.pageHistory() + 1 >= this.totalPagesHistory()) return;

    this._pageHistory.update(page => page + 1);
    this.getHistoryTicketsByAttendant();
  }

  previousPageHistory(): void {

    if (this.pageHistory() === 0) return;

    this._pageHistory.update(page => page - 1);
    this.getHistoryTicketsByAttendant();
  }

  goToPageHistory(page: number): void {

    if (page < 0 || page >= this.totalPagesHistory()) return;

    this._pageHistory.set(page);
    this.getHistoryTicketsByAttendant();
  }


  // ===== RESETS =====

  resetStatus(): void {

    this._createStatus.set('default');
    this._createMessage.set('');

    this._deleteStatus.set('default');
    this._deleteMessage.set('');

    this._callTicketStatus.set('default');
    this._callTicketMessage.set('');

    this._callCustomerStatus.set('default');
    this._callCustomerMessage.set('');

    this._resetCodeStatus.set('default');
    this._resetCodeMessage.set('');

    this._resetAllCodesStatus.set('default');
    this._resetAllCodesMessage.set('');
  }

  resetTicketInfo(): void {
    this._ticketInfo.set(null);
  }

  resetTicketForPanel(): void {
    this._ticketForPanel.set(null);
  }

  resetTicketsForAttendance(): void {
    this._ticketsForAttendance.set([]);
  }

  resetHistoryTickets(): void {
    this._historyTickets.set([]);
    this._historyTicketsForAttendant.set([]);
  }


  // ===== WEBSOCKET =====

  addTicketFromWebSocket(ticket: ResponseTicketsForAttendanceDto): void {

    this._ticketsForAttendance.update(tickets => [
      ticket,
      ...tickets
    ]);

    this._totalTickets.update(total => total + 1);
    this._totalElementsTickets.update(total => total + 1);
  }

  setTicketForPainel(ticket: ResponseTicketDto) {
    this._ticketForPanel.set(ticket);
  }

  setHistoryTicketsForAttendant(history: ResponseTicketsForAttendanceDto[]) {
    this._historyTicketsForAttendant.set(history);
  }

  setTicketForPanel(ticket: ResponseTicketDto) {
    this._ticketForPanel.set(ticket);
  }

  setHistoryTickets(tickets: ResponseTicketsForAttendanceDto[]) {
    this._historyTickets.set(tickets);
  }

}


import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client/dist/sockjs';
import { inject, Injectable, signal } from '@angular/core';

import { ResponseTicketsForAttendanceDto } from '../../dtos/ticket/ResponseTicketsForAttendanceDto';
import { ResponseTicketDto } from '../../dtos/ticket/ResponseTicketDto';

import { TicketStateService } from '../states/ticket/ticket-state.service';
import { AttendentStateService } from '../states/attendent/attendent-state.service';

import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {

  // ===== CONFIGURATION =====
  private readonly API_URL = environment.API_URL;

  // ===== INJECTIONS =====
  private ticketState = inject(TicketStateService);
  private attendentState = inject(AttendentStateService);

  // ===== STATES =====
  private _lastTicket = signal<ResponseTicketsForAttendanceDto | null>(null);
  public lastTicket = this._lastTicket.asReadonly();

  // ===== CLIENT =====
  private client?: Client;


  // ===== CONNECT =====
  connect(): void {

    if (this.client?.active) return;

    this.client = new Client({

      webSocketFactory: () => new SockJS(`${this.API_URL}/ws`),

      reconnectDelay: 5000,

      onConnect: () => {

        // ===== NEW TICKETS =====
        this.client?.subscribe('/topic/tickets', message => {

          const ticket: ResponseTicketsForAttendanceDto =
            JSON.parse(message.body);
          this.ticketState.addTicketFromWebSocket(ticket);
          this.attendentState.incrementWaitingAttendances();
        });


        // ===== QUEUE DISPLAY =====
        this.client?.subscribe('/topic/queue-display', message => {

          const ticket: ResponseTicketDto =
            JSON.parse(message.body);
          this.ticketState.setTicketForPanel(ticket);
        });


        // ===== ATTENDANT TICKET HISTORY =====
        this.client?.subscribe('/topic/tickets/history', message => {

          const history: ResponseTicketsForAttendanceDto[] =
            JSON.parse(message.body);
          this.ticketState.setHistoryTicketsForAttendant(history);
        });


        // ===== CALLED TICKET =====
        this.client?.subscribe('/topic/queue-display/call', message => {

          const ticket: ResponseTicketDto =
            JSON.parse(message.body);

          this.ticketState.setTicketForPanel(ticket);
        });


        // ===== TICKET HISTORY =====
        this.client?.subscribe('/topic/ticket-history', message => {

          const response = JSON.parse(message.body);
          this.ticketState.setHistoryTickets(response.content);
        });
      }
    });

    this.client.activate();
  }


  // ===== DISCONNECT =====
  disconnect(): void {
    if (!this.client) return;

    this.client.deactivate();
    this.client = undefined;
  }

}

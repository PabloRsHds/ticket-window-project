import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client/dist/sockjs';
import { inject, Injectable, signal } from '@angular/core';
import { ResponseTicketsForAttendanceDto } from '../../dtos/ticket/ResponseTicketsForAttendanceDto';
import { TicketStateService } from '../states/ticket/ticket-state.service';
import { AttendentStateService } from '../states/attendent/attendent-state.service';
import { ResponseTicketDto } from '../../dtos/ticket/ResponseTicketDto';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {

  private readonly API_URL = environment.API_URL;

  // Inject
  private ticketState = inject(TicketStateService);
  private attendentState = inject(AttendentStateService);

  private client!: Client;

  readonly lastTicket = signal<ResponseTicketsForAttendanceDto | null>(null);

  connect() {

    this.client = new Client({
    webSocketFactory: () => new SockJS(`${this.API_URL}/ws`),

    reconnectDelay: 5000,

    onConnect: () => {
      console.log('WebSocket conectado');

      this.client.subscribe('/topic/tickets', message => {

        const ticket: ResponseTicketsForAttendanceDto =
          JSON.parse(message.body);

        this.ticketState.addTicketFromWebSocket(ticket);
        this.attendentState.incrementWaitingAttendances();
      });

      this.client.subscribe('/topic/queue-display', message => {

        const ticket: ResponseTicketDto = JSON.parse(message.body);
        this.ticketState.ticketForPanel.set(ticket);

      });

      this.client.subscribe('/topic/tickets/history', message => {

        const history: ResponseTicketsForAttendanceDto[] = JSON.parse(message.body);
        this.ticketState.historyTicketsForAttendant.set(history);
      })

      this.client.subscribe('/topic/queue-display/call', message => {

          const ticket: ResponseTicketDto = JSON.parse(message.body);
          this.ticketState.ticketForPanel.set(ticket);

      });

      this.client.subscribe('/topic/ticket-history', message => {

        const response = JSON.parse(message.body);
        this.ticketState.historyTickets.set(response.content);

      });

    },

    onStompError: error => {
      console.error('Erro STOMP:', error);
    },

    onWebSocketError: error => {
      console.error('Erro WebSocket:', error);
    }
  });

  this.client.activate();
  }

  disconnect() {
    this.client.deactivate();
  }

}

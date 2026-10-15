package br.com.queue.dtos.ticket;

public record ResponseTicketDto(

        String ticketId,
        Integer counterNumber,
        String code,
        String customerId,
        String customerName,
        String serviceManagementId,
        String serviceManagementName,
        String departmentName,
        String priority,
        String status,
        String createdAt,
        String calledAt
) {
}

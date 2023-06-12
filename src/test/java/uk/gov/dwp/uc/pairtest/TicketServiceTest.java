package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketPurchaseRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;

public class TicketServiceTest {

    TicketPaymentService paymentService = Mockito.mock(TicketPaymentService.class);
    SeatReservationService reservationService  = Mockito.mock(SeatReservationService.class);
    TicketService ticketService = new TicketServiceImpl(paymentService, reservationService);

    @DisplayName("Test Payment is called")
    @Test
    public void testPaymentSuccessfullyCalled() {
        TicketRequest ticketRequestA = new TicketRequest(TicketRequest.Type.ADULT, 10);
        TicketRequest ticketRequestB = new TicketRequest(TicketRequest.Type.CHILD, 1);
        TicketRequest ticketRequestC = new TicketRequest(TicketRequest.Type.INFANT, 2);
        TicketRequest tickets[] = {ticketRequestA, ticketRequestB, ticketRequestC};
        TicketPurchaseRequest ticketPurchaseRequest = new TicketPurchaseRequest(1234, tickets);

        doNothing().when(paymentService).makePayment(Mockito.anyLong(), Mockito.anyInt());
        ticketService.purchaseTickets(ticketPurchaseRequest);
        Mockito.verify(paymentService,Mockito.times(1)).makePayment(Mockito.anyLong(), Mockito.anyInt());
    }

    @DisplayName("Test Reservation is called")
    @Test
    public void testReservationSuccessfullyCalled() {
        TicketRequest ticketRequestA = new TicketRequest(TicketRequest.Type.ADULT, 10);
        TicketRequest ticketRequestB = new TicketRequest(TicketRequest.Type.CHILD, 1);
        TicketRequest ticketRequestC = new TicketRequest(TicketRequest.Type.INFANT, 2);
        TicketRequest tickets[] = {ticketRequestA, ticketRequestB, ticketRequestC};
        TicketPurchaseRequest ticketPurchaseRequest = new TicketPurchaseRequest(1234, tickets);

        doNothing().when(reservationService).reserveSeat(Mockito.anyLong(), Mockito.anyInt());
        ticketService.purchaseTickets(ticketPurchaseRequest);
        Mockito.verify(reservationService,Mockito.times(1)).reserveSeat(Mockito.anyLong(), Mockito.anyInt());

    }

    @DisplayName("Test throw exception when no adult ticket")
    @Test
    public void testThrowWhenTNoAdultTicket() {
        TicketPurchaseRequest ticketPurchaseRequest = ticketsNoAdult();
        Throwable exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(ticketPurchaseRequest);
        });
        assertEquals("Child and Infant tickets cannot be purchased without purchasing an Adult ticket", exception.getMessage());
    }

    @DisplayName("Test throw exception when no more than 20 tickets")
    @Test
    public void testWhenMoreThan20Tickets() {
        TicketPurchaseRequest ticketPurchaseRequest = moreThan20Tickets();
        Throwable exception = assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(ticketPurchaseRequest);
        });
        assertEquals("Only a maximum of 20 tickets that can be purchased at a time", exception.getMessage());
    }

    private TicketPurchaseRequest ticketsNoAdult() {
        TicketRequest ticketRequestB = new TicketRequest(TicketRequest.Type.CHILD, 12);
        TicketRequest ticketRequestC = new TicketRequest(TicketRequest.Type.INFANT, 2);
        TicketRequest tickets[] = {ticketRequestB, ticketRequestC};
        return new TicketPurchaseRequest(1234, tickets);
    }

    private TicketPurchaseRequest moreThan20Tickets() {
        TicketRequest ticketRequestB = new TicketRequest(TicketRequest.Type.CHILD, 19);
        TicketRequest ticketRequestC = new TicketRequest(TicketRequest.Type.INFANT, 2);
        TicketRequest tickets[] = {ticketRequestB, ticketRequestC};
        return new TicketPurchaseRequest(1234, tickets);
    }


}

package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketPurchaseRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.util.Constants;

import java.util.HashMap;
import java.util.Map;


public class TicketServiceImpl implements TicketService {

    //DI makes it easier to test and maintain the code
    //instantiating within the class method is a bad practice
    private TicketPaymentService paymentService;
    private SeatReservationService reservationService;

    public TicketServiceImpl(TicketPaymentService paymentService, SeatReservationService reservationService) {
        this.paymentService = paymentService;
        this.reservationService = reservationService;
    }

    /**
     * Should only have private methods other than the one below.
     */



    @Override
    public void purchaseTickets(TicketPurchaseRequest ticketPurchaseRequest) throws InvalidPurchaseException {
        //There are 3 types of tickets i.e. Infant, Child, and Adult.Integer>();
        Map<String,Integer> ticketDescription = createTicketPurchaseRequestMap(ticketPurchaseRequest);

        // Validate ticket request
        //Only a maximum of 20 tickets that can be purchased at a time.
        //Child and Infant tickets cannot be purchased without purchasing an Adult ticket.
        validateTicketRequest(ticketDescription);

        //Calculate Amount
        //The ticket prices are based on the type of ticket (see table below).
        //The ticket purchaser declares how many and what type of tickets they want to buy.
        //Multiple tickets can be purchased at any given time.
        int amount = calculateAmount(ticketDescription);

        //Payment
        //There is an existing TicketPaymentService responsible for taking payments.
        paymentService.makePayment(ticketPurchaseRequest.getAccountId(), amount);

        //Calculate Number of Seats
        //Infants do not pay for a ticket and are not allocated a seat. They will be sitting on an Adult's lap.
        int totalSeatsToAllocate = calculateNumberOfSeats(ticketDescription);

        //Allocate Seat
        //There is an existing SeatReservationService responsible for reserving seats.
        reservationService.reserveSeat(ticketPurchaseRequest.getAccountId(), totalSeatsToAllocate);

    }

    private static void validateTicketRequest(final Map<String, Integer> ticketDescription) {

        Integer totalNoOfTickets = ticketDescription.getOrDefault(TicketRequest.Type.ADULT.name(), 0)
                + ticketDescription.getOrDefault(TicketRequest.Type.CHILD.name(), 0)
                + ticketDescription.getOrDefault(TicketRequest.Type.INFANT.name(), 0);
        if (totalNoOfTickets > 20) {
            throw new InvalidPurchaseException("Only a maximum of 20 tickets that can be purchased at a time");
        } else if (ticketDescription.getOrDefault(TicketRequest.Type.ADULT.name(), 0) == 0) {
            throw new InvalidPurchaseException("Child and Infant tickets cannot be purchased without purchasing an Adult ticket");
        }
    }

    private static int calculateAmount(final Map<String, Integer> ticketDescription) {
        return ticketDescription.getOrDefault(TicketRequest.Type.ADULT.name(), 0) * Constants.ADULT_TICKET_PRICE
                + ticketDescription.getOrDefault(TicketRequest.Type.CHILD.name(), 0) * Constants.CHILD_TICKET_PRICE
                + ticketDescription.getOrDefault(TicketRequest.Type.INFANT.name(), 0) * Constants.INFANT_TICKET_PRICE;
    }

    private static int calculateNumberOfSeats(final Map<String, Integer> ticketDescription) {
        return ticketDescription.getOrDefault(TicketRequest.Type.ADULT.name(), 0)
                + ticketDescription.getOrDefault(TicketRequest.Type.CHILD.name(), 0);
    }

    private static Map<String, Integer>  createTicketPurchaseRequestMap(final TicketPurchaseRequest ticketPurchaseRequest) {
        final Map<String, Integer> ticketDescription = new HashMap<String, Integer>();
        for (TicketRequest ticketRequest : ticketPurchaseRequest.getTicketTypeRequests()) {
            if (ticketRequest.getTicketType() == TicketRequest.Type.ADULT) {
                ticketDescription.put(TicketRequest.Type.ADULT.toString(),ticketRequest.getNoOfTickets());
            } else if (ticketRequest.getTicketType() == TicketRequest.Type.CHILD) {
                ticketDescription.put(TicketRequest.Type.CHILD.toString(),ticketRequest.getNoOfTickets());
            } else if (ticketRequest.getTicketType() == TicketRequest.Type.INFANT) {
                ticketDescription.put(TicketRequest.Type.INFANT.toString(),ticketRequest.getNoOfTickets());
            }
        }
        return ticketDescription;
    }

}

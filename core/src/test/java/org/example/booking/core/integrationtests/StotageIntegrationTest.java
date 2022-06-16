package org.example.booking.core.integrationtests;

import org.example.booking.core.facade.BookingFacadeImpl;
import org.example.booking.core.model.EventImpl;
import org.example.booking.core.model.UserImpl;
import org.example.booking.intro.facade.BookingFacade;
import org.example.booking.intro.model.Event;
import org.example.booking.intro.model.Ticket;
import org.example.booking.intro.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Date;
import java.util.List;

public class StotageIntegrationTest {
    private static final ClassPathXmlApplicationContext ctx;

    static {
        ctx = new ClassPathXmlApplicationContext("context.xml");
        ctx.getEnvironment().setActiveProfiles("test");
        ctx.refresh();
    }

    @Test
    void contextLoadTest() {
        Assertions.assertNotNull(ctx);
    }

    @Test
    void facadeWiredTest() {
        BookingFacade facade = (BookingFacadeImpl) ctx.getBean("bookingFacadeImpl");
        Assertions.assertNotNull(facade);
    }

    @Test
    void realLifeScenarioTest() {
        BookingFacade facade = (BookingFacadeImpl) ctx.getBean("bookingFacadeImpl");
        Assertions.assertNotNull(facade);

        User user1 = facade.createUser(new UserImpl(0, "U1", "em1"));
        User user2 = facade.createUser(new UserImpl(0, "U2", "em2"));
        User user3 = facade.createUser(new UserImpl(0, "U3", "em3"));
        Event event = facade.createEvent(new EventImpl(0, "Event_1", new Date(2022, 5, 15)));
        Ticket ticket1 = facade.bookTicket(user1.getId(), event.getId(), 1, Ticket.Category.BAR);
        Ticket ticket2 = facade.bookTicket(user2.getId(), event.getId(), 2, Ticket.Category.BAR);
        Ticket ticket3 = facade.bookTicket(user3.getId(), event.getId(), 3, Ticket.Category.PREMIUM);
        Assertions.assertNotNull(ticket3);

        List<Ticket> tickets = facade.getBookedTickets(event, 3, 1);
        Assertions.assertTrue(tickets.size() == 3);

        tickets = facade.getBookedTickets(event, 2, 1);
        Assertions.assertTrue(tickets.size() == 2);

        tickets = facade.getBookedTickets(event, 2, 2);
        Assertions.assertTrue(tickets.size() == 1);

        facade.cancelTicket(ticket1.getId());
        facade.cancelTicket(ticket2.getId());
        facade.cancelTicket(ticket3.getId());
        facade.deleteEvent(event.getId());
        facade.deleteUser(user1.getId());
        facade.deleteUser(user2.getId());
        facade.deleteUser(user3.getId());

    }
}

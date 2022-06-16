package org.example.booking.core.util;

import org.example.booking.core.model.EventImpl;
import org.example.booking.core.model.TicketImpl;
import org.example.booking.core.model.UserImpl;
import org.example.booking.intro.model.Event;
import org.example.booking.intro.model.Ticket;
import org.example.booking.intro.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageTest {
    static Storage storage;

    static {
        storage = new Storage();
        try {
            storage.storageFileName = "storage-test-read.json";
            storage.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldCreateStorageKeysForModels() {
        Event event = new EventImpl(12345, "Cinema", Date.from(Instant.now()));
        String key = BookingUtil.storageKeyCreate(event);
        Assertions.assertEquals("event:12345", key);

        User user = new UserImpl(12345, "Vasya", "vasya@gmail.com");
        key = BookingUtil.storageKeyCreate(user);
        Assertions.assertEquals("user:12345", key);

        Ticket ticket = new TicketImpl();
        ticket.setId(12345);
        key = BookingUtil.storageKeyCreate(ticket);
        Assertions.assertEquals("ticket:12345", key);
    }

    @Test
    public void shouldCreateKeysForIDsAndCassParam() {
        String key = BookingUtil.storageKeyCreate(Event.class, 12345);
        Assertions.assertEquals("event:12345", key);

        key = BookingUtil.storageKeyCreate(EventImpl.class, 12345);
        Assertions.assertEquals("event:12345", key);

    }

    @Test
    public void shouldThrowExceptions() {
        Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            BookingUtil.storageKeyCreate(new Object());
        });
        exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            BookingUtil.storageKeyCreate(String.class, 12345);
        });


    }

    @Test
    public void shouldMapEntitiesToStrings() {
//        Event event = new EventImpl(12345, "", Date.from(Instant.parse("2007-12-03T00:10:00.01Z")));
        Event event = new EventImpl(12345, "", new Date(2007, 12, 3));
        String value = storage.createMapValue(event);
        Assertions.assertEquals("{\"id\":12345,\"title\":\"\",\"date\":61157455200000}", value);

        User user = new UserImpl(12345, "Vasya", "email@emaol.com");
        value = storage.createMapValue(user);
        Assertions.assertEquals("{\"id\":12345,\"name\":\"Vasya\",\"email\":\"email@emaol.com\"}", value);

        Ticket ticket = new TicketImpl(12345, 23456, 34567, Ticket.Category.BAR, 12);
        value = storage.createMapValue(ticket);
        Assertions.assertEquals("{\"id\":12345,\"eventId\":23456,\"userId\":34567,\"category\":\"BAR\",\"place\":12}", value);
    }

    @Test
    public void shouldConvertInMemryMap() {
        Map<String, Object> cache = getStringObjectMap();

        Map<String, String> result = storage.convertStringObjectMap(cache);
        String expected = "{event:12345={\"id\":12345,\"title\":\"\",\"date\":61157455200000}, user:12345={\"id\":12345,\"name\":\"Vasya\",\"email\":\"email@emaol.com\"}, user:12346={\"id\":12346,\"name\":\"Vasya\",\"email\":\"email@emaol.com\"}, ticket:12345={\"id\":12345,\"eventId\":23456,\"userId\":34567,\"category\":\"BAR\",\"place\":12}}";
        Assertions.assertEquals(expected, result.toString());
    }

    private Map<String, Object> getStringObjectMap() {
        Map<String, Object> cache = new HashMap<>();
//        Event event = new EventImpl(12345, "", Date.from(Instant.parse("2007-12-03T00:10:00.01Z")));
        Event event = new EventImpl(12345, "", new Date(2007, 12, 3));
        User user = new UserImpl(12345, "Vasya", "email@emaol.com");
        User user1 = new UserImpl(12346, "Vasya", "email@emaol.com");
        Ticket ticket = new TicketImpl(12345, 23456, 34567, Ticket.Category.BAR, 12);

        cache.put(BookingUtil.storageKeyCreate(event), event);
        cache.put(BookingUtil.storageKeyCreate(user), user);
        cache.put(BookingUtil.storageKeyCreate(user1), user1);
        cache.put(BookingUtil.storageKeyCreate(ticket), ticket);
        return cache;
    }

    @Test
    public void saveMapTest() throws IOException {
        Resource resource = new ClassPathResource("storage-test.json");
        Map<String, Object> stringObjectMap = getStringObjectMap();
        storage.saveMap(stringObjectMap, resource);
    }

    @Test
    public void regexTest() {
        String id = "event:12345".replaceFirst("[^:]*:", "");
        Assertions.assertEquals("12345", id);

        String classParam = "event:12345".replaceFirst(":.*", "");
        Assertions.assertEquals("event", classParam);

    }

    @Test
    public void shouldReadMapFromFile() throws IOException {
        String fileName = "storage-test-read.json";
        Map<String, Object> stringObjectMap = storage.loadMap(fileName);

        Assertions.assertEquals(stringObjectMap.size(), 7);
    }

    @Disabled
    @Test
    void propertyTest() {
        Assertions.assertEquals(" . ", storage.storageFileName);
    }

    @Test
    void shouldReturnEvent12345() {
        Event actual = storage.getEventById(12345);
        String expected = "EventImpl{id=12345, title='Cinema', date=Mon Dec 03 02:05:00 EET 2007}";
        Assertions.assertEquals(expected, actual.toString());
    }

    @Test
    void shouldReturnPagesByTitle() {
        List<Event> eventList = storage.getEventsByTitle("Action", 3, 1);
        Assertions.assertTrue(eventList.size() == 1);

        eventList = storage.getEventsByTitle("Cinema", 3, 1);
        Assertions.assertTrue(eventList.size() == 3);

        eventList = storage.getEventsByTitle("Cinema", 2, 2);
        Assertions.assertTrue(eventList.size() == 1);
    }

    @Test
    void shouldReturnPagesByDate() {
        Date date = new Date(2008, 0, 3);
        List<Event> eventList = storage.getEventsForDay(date, 3, 1);
        Assertions.assertTrue(eventList.size() == 3);

        eventList = storage.getEventsForDay(date, 2, 1);
        Assertions.assertTrue(eventList.size() == 2);

        eventList = storage.getEventsForDay(date, 2, 2);
        Assertions.assertTrue(eventList.size() == 1);
    }

    @Test
    void shouldCreateEventWithNewIdAndDeleteIt() {
        Event event = new EventImpl(10, "Skating", new Date(2022, 5, 9));
        Event updated = storage.createEvent(event);
        Assertions.assertTrue(updated.getId() != 10);

        Event loaded = storage.getEventById(updated.getId());
        Assertions.assertTrue(loaded.getId() == updated.getId());
        Assertions.assertEquals(event.getTitle(), loaded.getTitle());
        Assertions.assertEquals(event.getDate(), loaded.getDate());

        Assertions.assertTrue(storage.deleteEvent(loaded.getId()));
    }

    @Test
    void shouldUpdateEventAndDeleteIt() {
        Event event = new EventImpl(20, "Skating", new Date(2022, 5, 9));
        Event updated = storage.updateEvent(event);
        Event loaded = storage.getEventById(event.getId());
        Assertions.assertEquals(event.getTitle(), loaded.getTitle());
        Assertions.assertEquals(event.getDate(), loaded.getDate());

        Assertions.assertTrue(storage.deleteEvent(loaded.getId()));
    }

    @Test
    void shouldGetUserById() {
        Assertions.assertNotNull(storage.getUserById(12345));
        Assertions.assertNull(storage.getUserById(999999));
    }

    @Test
    void shouldGetUserByEmail() {
        Assertions.assertNotNull(storage.getUserByEmail("email@emaol.com"));
        Assertions.assertNull(storage.getUserByEmail("xxx@email.com"));
    }

    @Test
    void shouldGetUserListByName() {
        List<User> userList;
        userList = storage.getUsersByName("Vasya", 2, 1);
        System.out.println(userList);
        Assertions.assertTrue(userList.size() == 2);

        userList = storage.getUsersByName("Vasya", 1, 1);
        Assertions.assertTrue(userList.size() == 1);

        userList = storage.getUsersByName("Vasya", 1, 2);
        Assertions.assertTrue(userList.size() == 1);

        userList = storage.getUsersByName("Xxxx", 5, 1);
        Assertions.assertTrue(userList.size() == 0);
    }

    @Test
    void shouldCreateUserWithNewIdAndDeleteIt() {
        User user = new UserImpl(10, "Sergio", "absent@email.com");
        User updated = storage.createUser(user);
        Assertions.assertTrue(updated.getId() != 10);

        User loaded = storage.getUserById(updated.getId());
        Assertions.assertTrue(loaded.getId() == updated.getId());
        Assertions.assertEquals(user.getName(), loaded.getName());
        Assertions.assertEquals(user.getEmail(), loaded.getEmail());

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            User user2 = new UserImpl(11, "Sergio2", "email@emaol.com");
            storage.createUser(user2);
        });

        Assertions.assertTrue(storage.deleteUser(loaded.getId()));
    }

    @Test
    void shouldUpdateUser() {
        User user = new UserImpl(13, "Sergio3", "sergio3@email.com");
        User created = storage.createUser(user);
        created.setName("Sergio31");
        created.setEmail("sergio31@email.com");
        User updated = storage.updateUser(created);

        Assertions.assertEquals("Sergio31", updated.getName());
        Assertions.assertEquals("sergio31@email.com", updated.getEmail());

        Assertions.assertTrue(storage.deleteUser(updated.getId()));

    }

    @Test
    void updateUser2() {
        User user2 = new UserImpl(14, "Sergio4", "sergio4@email.com");
        User created2 = storage.createUser(user2);
        created2.setName("Sergio41");
        created2.setEmail("email@emaol.com");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            User updated2 = storage.updateUser(created2);
        });

        Assertions.assertTrue(storage.deleteUser(created2.getId()));
    }

    @Test
    void shouldBookTicket() {
        Ticket created1 = storage.bookTicket(1, 1, 1, Ticket.Category.BAR);
        System.out.println(created1);
        Assertions.assertNotNull(created1);

        Ticket created2 = storage.bookTicket(1, 1, 1, Ticket.Category.BAR);
        System.out.println(created2);
        Assertions.assertNull(created2);
    }

    @Test
    void shouldReturnBookedByUserOrderedTicketLIst() {
        User user = storage.createUser(new UserImpl(0, "Ticket User", "ticketUserEmail"));
        Event event1 = storage.createEvent(new EventImpl(0, "Event 1", new Date(2022, 0, 1)));
        Event event2 = storage.createEvent(new EventImpl(0, "Event 2", new Date(2022, 1, 1)));
        Event event3 = storage.createEvent(new EventImpl(0, "Event 3", new Date(2022, 2, 1)));
        Ticket ticket1 = storage.bookTicket(user.getId(), event1.getId(), 1, Ticket.Category.BAR);
        Ticket ticket2 = storage.bookTicket(user.getId(), event2.getId(), 2, Ticket.Category.STANDARD);
        Ticket ticket3 = storage.bookTicket(user.getId(), event3.getId(), 3, Ticket.Category.PREMIUM);
        List<Ticket> ticketList = storage.getBookedTickets(user, 2, 1);
        Assertions.assertTrue(ticketList.size() == 2);
        ticketList = storage.getBookedTickets(user, 2, 2);
        Assertions.assertTrue(ticketList.size() == 1);

        storage.deleteEvent(event1.getId());
        storage.deleteEvent(event2.getId());
        storage.deleteEvent(event3.getId());
        storage.deleteUser(user.getId());
        long ticket1Id = ticket1.getId();
        storage.cancelTicket(ticket1.getId());
        storage.cancelTicket(ticket2.getId());
        storage.cancelTicket(ticket3.getId());

        Assertions.assertNotNull(user);
        Assertions.assertEquals(storage.getBookedTickets(user, 1, 1).size(), 0);

    }

    @Test
    void shouldReturnBookedForEventOrderedByUserEmailTicketLists() {
        User user1 = storage.createUser(new UserImpl(0, "U1", "em1"));
        User user2 = storage.createUser(new UserImpl(0, "U2", "em2"));
        User user3 = storage.createUser(new UserImpl(0, "U3", "em3"));
        Event event = storage.createEvent(new EventImpl(0, "Event_1", new Date(2022, 5, 15)));
        Ticket ticket1 = storage.bookTicket(user1.getId(), event.getId(), 1, Ticket.Category.BAR);
        Ticket ticket2 = storage.bookTicket(user2.getId(), event.getId(), 2, Ticket.Category.BAR);
        Ticket ticket3 = storage.bookTicket(user3.getId(), event.getId(), 3, Ticket.Category.PREMIUM);
        Assertions.assertNotNull(ticket3);

        List<Ticket> tickets = storage.getBookedTickets(event, 3, 1);
        Assertions.assertTrue(tickets.size() == 3);

        tickets = storage.getBookedTickets(event, 2, 1);
        Assertions.assertTrue(tickets.size() == 2);

        tickets = storage.getBookedTickets(event, 2, 2);
        Assertions.assertTrue(tickets.size() == 1);

        storage.cancelTicket(ticket1.getId());
        storage.cancelTicket(ticket2.getId());
        storage.cancelTicket(ticket3.getId());
        storage.deleteEvent(event.getId());
        storage.deleteUser(user1.getId());
        storage.deleteUser(user2.getId());
        storage.deleteUser(user3.getId());
    }
}


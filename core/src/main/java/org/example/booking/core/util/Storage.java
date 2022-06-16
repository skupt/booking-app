package org.example.booking.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.booking.core.model.EventImpl;
import org.example.booking.core.model.TicketImpl;
import org.example.booking.core.model.UserImpl;
import org.example.booking.intro.model.Event;
import org.example.booking.intro.model.Ticket;
import org.example.booking.intro.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class Storage {
    private static final Logger LOGGER = LoggerFactory.getLogger(Storage.class);
    public static final Set<String> allowedTypeParams = Arrays.stream(new String[]{"ticket", "user", "event"}).collect(Collectors.toSet());
    private static Random random = new Random(System.nanoTime());
    private Map<String, Object> cache = new HashMap<>();
    private Set<String> emailSet = new HashSet<>();
    private ObjectMapper objectMapper = new ObjectMapper();
    public String storageFileName;

    public void init() throws IOException {
        loadMap(storageFileName);
        emailSet = getEmailSet();
    }

    public void setStorageFileName(String storageFileName) {
        this.storageFileName = storageFileName;
    }

    public void destroy() throws IOException {
        saveMap(cache, new ClassPathResource(storageFileName));
    }

    public String createMapValue(Object entity) {
        String classParam = entity.getClass().getSimpleName().toLowerCase().replace("impl", "");
        String value = null;
        switch (classParam) {
            case "event":
                try {
                    value = objectMapper.writeValueAsString((Event) entity);
                } catch (JsonProcessingException e) {
                    LOGGER.warn(e.getMessage(), e);
                    throw new IllegalArgumentException("Exception during mapping Event to String");
                }
                ;
                break;
            case "user":
                try {
                    value = objectMapper.writeValueAsString((User) entity);
                } catch (JsonProcessingException e) {
                    LOGGER.warn(e.getMessage(), e);
                    throw new IllegalArgumentException("Exception during mapping User to String");
                }
                ;
                break;
            case "ticket":
                try {
                    value = objectMapper.writeValueAsString((Ticket) entity);
                } catch (JsonProcessingException e) {
                    LOGGER.warn(e.getMessage(), e);
                    throw new IllegalArgumentException("Exception during mapping User to String");
                }
                break;
            default:
                throw new IllegalArgumentException("Passed object not a Event, User or Ticket");
        }
        return value;
    }

    Map<String, String> convertStringObjectMap(Map<String, Object> inMemoryMap) {
        Map<String, String> convertedMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : inMemoryMap.entrySet()) {
            String key = entry.getKey();
            String value = createMapValue(entry.getValue());
            convertedMap.put(key, value);
        }
        return convertedMap;
    }

    public void saveMap(Map<String, Object> stringObjectMap, Resource resource) throws IOException {
        FileWriter fileWriter = new FileWriter(resource.getFilename(), StandardCharsets.UTF_8);
        Map<String, String> mapAsString = convertStringObjectMap(stringObjectMap);
        objectMapper.writeValue(fileWriter, mapAsString);
    }

    public Map<String, Object> loadMap(String fileName) throws IOException {
        Map<String, String> stringStringMap = objectMapper.readValue(new File(fileName), new TypeReference<Map<String, String>>() {
        });
        Map<String, Object> stringObjectMap = new HashMap<>();
        Object value = null;
        for (Map.Entry<String, String> entry : stringStringMap.entrySet()) {
            String classParam = entry.getKey().replaceFirst(":.*", "");
            switch (classParam) {
                case "event":
                    value = objectMapper.readValue(entry.getValue(), EventImpl.class);
                    break;
                case "user":
                    value = objectMapper.readValue(entry.getValue(), UserImpl.class);
                    break;
                case "ticket":
                    value = objectMapper.readValue(entry.getValue(), TicketImpl.class);
                    break;
                default:
                    throw new IllegalArgumentException("Passed object not a Event, User or Ticket");
            }
            stringObjectMap.put(entry.getKey(), value);
        }
        cache = stringObjectMap;
        return stringObjectMap;
    }

    public Event getEventById(long eventId) {
        String key = BookingUtil.storageKeyCreate(Event.class, eventId);
        return (Event) cache.get(key);
    }

    public List<Event> getEventsByTitle(String title, int pageSize, int pageNum) {
        List<Event> wholeListEvent = cache.entrySet()
                .stream().
                filter(e -> e.getKey().startsWith("event"))
                .map(e -> ((Event) e.getValue()))
                .filter(v -> v.getTitle().contains(title))
                .collect(Collectors.toList());

        return getPage(pageSize, pageNum, wholeListEvent);
    }

    public List<Event> getEventsForDay(Date day, int pageSize, int pageNum) {
        Instant instantFilter = normalizeDay(day).toInstant();
        List<Event> wholeListEvent = cache.entrySet()
                .stream().
                filter(e -> e.getKey().startsWith("event"))
                .map(e -> ((Event) e.getValue()))
                .filter(v -> normalizeDay(v.getDate()).toInstant().equals(instantFilter))
                .collect(Collectors.toList());

        return getPage(pageSize, pageNum, wholeListEvent);
    }

    public Event createEvent(Event event) {
        long id;
        String key;
        do {
            id = random.nextLong();
            key = BookingUtil.storageKeyCreate(Event.class, id);
        } while (cache.get(key) != null);

        event.setId(id);
        cache.put(key, event);
        return event;
    }

    public Event updateEvent(Event event) {
        String key = BookingUtil.storageKeyCreate(event);
        cache.put(key, event);
        return event;
    }

    public boolean deleteEvent(long eventId) {
        String key = BookingUtil.storageKeyCreate(Event.class, eventId);
        Event deletedEvent = (Event) cache.remove(key);
        if (deletedEvent != null) return true;
        return false;
    }

    public User getUserById(long userId) {
        String key = BookingUtil.storageKeyCreate(User.class, userId);
        return (User) cache.get(key);
    }

    public User getUserByEmail(String email) {
        User user = cache.entrySet()
                .stream()
                .filter(e -> e.getKey().startsWith("user"))
                .map(e -> (User) e.getValue())
                .filter(u -> u.getEmail().equals(email))
                .findAny().orElse(null);

        return user;
    }

    public List<User> getUsersByName(String name, int pageSize, int pageNum) {
        List<User> wholeListUser = cache.entrySet()
                .stream()
                .filter(e -> e.getKey().startsWith("user"))
                .map(e -> ((User) e.getValue()))
                .filter(v -> v.getName().contains(name))
                .collect(Collectors.toList());

        return getPage(pageSize, pageNum, wholeListUser);
    }

    public User createUser(User user) {
        if (emailSet.contains(user.getEmail()))
            throw new IllegalArgumentException("User to create has email that is already exists: " + user.getEmail());
        long id;
        String key;
        do {
            id = random.nextLong();
            key = BookingUtil.storageKeyCreate(User.class, id);
        } while (cache.get(key) != null);

        user.setId(id);
        cache.put(key, user);
        emailSet.add(user.getEmail());

        User clonedUser = null;
        try {
            clonedUser = (User) ((UserImpl) user).clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException("CloneNotSupportedException thrown during creating user.");
        }

        return clonedUser;
    }

    public User updateUser(User user) {
        User updated = null;
        String key = BookingUtil.storageKeyCreate(user);
        User oldUser = (User) cache.get(key);
        if (oldUser != null) {
            String newEmail = user.getEmail();
            if (newEmail != null && newEmail.equals(oldUser.getEmail())) {
                cache.put(key, user);
                updated = user;
            } else {
                if (emailSet.contains(newEmail)) {
                    throw new IllegalArgumentException("User to update has updated email that is already exists: " + newEmail);
                }
                cache.put(key, user);
                emailSet.add(newEmail);
                emailSet.remove(oldUser.getEmail());
                updated = user;
            }
        } else {
            updated = createUser(user);
        }
        return updated;
    }

    public boolean deleteUser(long userId) {
        String key = BookingUtil.storageKeyCreate(User.class, userId);
        if (cache.remove(key) != null) return true;
        return false;
    }

    @Nullable
    public Ticket bookTicket(long userId, long eventId, int place, Ticket.Category category) {
        boolean free = isPlaceFree(eventId, place, category);
        if (free) {
            long id;
            String key;
            do {
                id = random.nextLong();
                key = BookingUtil.storageKeyCreate(Ticket.class, id);
            } while (cache.get(key) != null);
            Ticket ticket = new TicketImpl();
            ticket.setId(id);
            ticket.setUserId(userId);
            ticket.setEventId(eventId);
            ticket.setPlace(place);
            ticket.setCategory(category);
            cache.put(key, ticket);

            Ticket result = null;
            try {
                result = (Ticket) ((TicketImpl) ticket).clone();
            } catch (CloneNotSupportedException e) {
                throw new IllegalArgumentException("CloneNotSupportedException during ticket clone");
            }
            return result;

        }
        return null;
    }

    public List<Ticket> getBookedTickets(User user, int pageSize, int pageNum) {
        Comparator<Ticket> ticketComparator = new Comparator<>() {
            @Override
            public int compare(Ticket o1, Ticket o2) {
                Event e1 = getEventById(o1.getEventId());
                Event e2 = getEventById(o2.getEventId());
                return e2.getDate().compareTo(e1.getDate());
            }
        };

        List<Ticket> ticketList = cache.entrySet().stream().filter(e -> e.getKey().startsWith("ticket"))
                .map(e -> (Ticket) e.getValue())
                .filter(t -> t.getUserId() == user.getId())
                .sorted(ticketComparator)
                .collect(Collectors.toList());

        return getPage(pageSize, pageNum, ticketList);
    }

    public List<Ticket> getBookedTickets(Event event, int pageSize, int pageNum) {
        Comparator<Ticket> ticketComparator = new Comparator<>() {
            @Override
            public int compare(Ticket o1, Ticket o2) {
                User e1 = getUserById(o1.getUserId());
                User e2 = getUserById(o2.getUserId());
                return e1.getEmail().compareTo(e2.getEmail());
            }
        };
        List<Ticket> ticketList = cache.entrySet().stream().filter(e -> e.getKey().startsWith("ticket"))
                .map(e -> (Ticket) e.getValue())
                .filter(t -> t.getEventId() == event.getId())
                .sorted(ticketComparator)
                .collect(Collectors.toList());

        return getPage(pageSize, pageNum, ticketList);
    }

    public boolean cancelTicket(long ticketId) {
        if (cache.remove(BookingUtil.storageKeyCreate(Ticket.class, ticketId)) != null) return true;
        return false;
    }

    private <T> List<T> getPage(int pageSize, int pageNum, List<T> list) {
        int totalPages = ((list.size() % pageSize) > 0) ? list.size() / pageSize + 1 : list.size() / pageSize;
        int localPageNum = 1;
        if (pageNum > 0) localPageNum = pageNum;
        if (pageNum > totalPages) localPageNum = totalPages;
        if (localPageNum == 0) localPageNum = 1;
        int startPosition = (localPageNum - 1) * pageSize;

        return list.stream().skip(startPosition).limit(pageSize).collect(Collectors.toList());
    }

    private Date normalizeDay(Date day) {
        int year = day.getYear();
        int month = day.getMonth();
        int date = day.getDate();
        return new Date(year, month, date);
    }

    private Set<String> getEmailSet() {
        return cache.entrySet()
                .stream()
                .filter(e -> e.getKey().startsWith("user"))
                .map(e -> ((User) e.getValue()).getEmail())
                .collect(Collectors.toSet());
    }

    private boolean isPlaceFree(long eventId, int place, Ticket.Category category) {
        long timesPlaceBooked = cache.entrySet().stream()
                .filter(e -> e.getKey().startsWith("ticket")).map(e -> (Ticket) e.getValue())
                .filter(v -> v.getEventId() == eventId)
                .filter(v -> v.getCategory().equals(category))
                .filter(v -> v.getPlace() == place)
                .count();
        if (timesPlaceBooked == 0) return true;
        return false;
    }


}
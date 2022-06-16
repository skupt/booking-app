package org.example.booking.core.dao;

import org.example.booking.core.util.Storage;
import org.example.booking.intro.model.Event;

import java.util.Date;
import java.util.List;

public class EventDao {

    private Storage storage;

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public Event getEventById(long eventId) {
        return storage.getEventById(eventId);
    }

    public List<Event> getEventsByTitle(String title, int pageSize, int pageNum) {
        return storage.getEventsByTitle(title, pageSize, pageNum);
    }

    public List<Event> getEventsForDay(Date day, int pageSize, int pageNum) {
        return storage.getEventsForDay(day, pageSize, pageNum);
    }

    public Event createEvent(Event event) {
        return storage.createEvent(event);
    }

    public Event updateEvent(Event event) {
        return storage.updateEvent(event);
    }

    public boolean deleteEvent(long eventId) {
        return storage.deleteEvent(eventId);
    }
}

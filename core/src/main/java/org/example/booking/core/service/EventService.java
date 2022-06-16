package org.example.booking.core.service;

import org.example.booking.core.dao.EventDao;
import org.example.booking.intro.model.Event;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class EventService {

    @Autowired
    private EventDao eventDao;

    public Event getEventById(long eventId) {
        return eventDao.getEventById(eventId);
    }

    public List<Event> getEventsByTitle(String title, int pageSize, int pageNum) {
        return eventDao.getEventsByTitle(title, pageSize, pageNum);
    }

    public List<Event> getEventsForDay(Date day, int pageSize, int pageNum) {
        return eventDao.getEventsForDay(day, pageSize, pageNum);
    }

    public Event createEvent(Event event) {
        return eventDao.createEvent(event);
    }

    public Event updateEvent(Event event) {
        return eventDao.updateEvent(event);
    }

    public boolean deleteEvent(long eventId) {
        return eventDao.deleteEvent(eventId);
    }
}

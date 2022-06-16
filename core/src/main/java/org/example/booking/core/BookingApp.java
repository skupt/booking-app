package org.example.booking.core;

import org.example.booking.core.facade.BookingFacadeImpl;
import org.example.booking.core.model.EventImpl;
import org.example.booking.intro.facade.BookingFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Date;

public class BookingApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(BookingApp.class);

    public static void main(String[] args) {
        LOGGER.info("Application starts");
        LOGGER.warn("WARN");
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("context.xml");
        applicationContext.getEnvironment().setActiveProfiles("dev");
        applicationContext.refresh();
        BookingFacade bookingFacade = applicationContext.getBean(BookingFacadeImpl.class);
        LOGGER.info(bookingFacade.getBookedTickets(new EventImpl(23456, "T", new Date(2022, 1, 1)), 3, 1).toString());
        LOGGER.info("Application stops");
    }
}

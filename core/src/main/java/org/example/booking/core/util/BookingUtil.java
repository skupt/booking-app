package org.example.booking.core.util;

import org.example.booking.intro.model.Event;
import org.example.booking.intro.model.Ticket;
import org.example.booking.intro.model.User;

public class BookingUtil {
    public static String storageKeyCreate(Object entity) {
        String classParam = entity.getClass().getSimpleName().toLowerCase().replace("impl", "");
        String key = null;
        switch (classParam) {
            case "event":
                key = classParam + ":" + ((Event) entity).getId();
                break;
            case "user":
                key = classParam + ":" + ((User) entity).getId();
                break;
            case "ticket":
                key = classParam + ":" + ((Ticket) entity).getId();
                break;
            default:
                throw new IllegalArgumentException("Passed object not a Event, User or Ticket");
        }
        return key;
    }

    public static String storageKeyCreate(Class clazz, long id) {
        String classParam = clazz.getSimpleName().toLowerCase().replace("impl", "");
        if (!Storage.allowedTypeParams.contains(classParam))
            throw new IllegalArgumentException("Class param is not allowed: " + clazz.getSimpleName());
        return classParam + ":" + id;
    }


}

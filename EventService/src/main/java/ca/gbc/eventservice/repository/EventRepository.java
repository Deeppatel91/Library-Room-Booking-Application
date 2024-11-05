package ca.gbc.eventservice.repository;

import ca.gbc.eventservice.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {
    // You can add custom query methods if needed
}

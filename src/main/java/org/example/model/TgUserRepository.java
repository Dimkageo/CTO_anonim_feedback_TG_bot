package org.example.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TgUserRepository extends CrudRepository<TgUser, Long > {
    Optional<TgUser> findByChatId(Long chatId);
}

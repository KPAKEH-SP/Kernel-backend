package ru.lcp.kernel.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.lcp.kernel.entities.PersonalChat;
import ru.lcp.kernel.entities.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonalChatRepository extends JpaRepository<PersonalChat, UUID> {
    List<PersonalChat> findByFirstUser(User firstUser);
    List<PersonalChat> findBySecondUser(User secondUser);

    Optional<PersonalChat> findPersonalChatByFirstUserAndSecondUser(User firstUser, User secondUser);
}

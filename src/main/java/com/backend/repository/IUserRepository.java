package com.backend.repository;

import com.backend.dto.UserDTO;
import org.springframework.data.repository.CrudRepository;

public interface IUserRepository extends CrudRepository<UserDTO, Long> {
}

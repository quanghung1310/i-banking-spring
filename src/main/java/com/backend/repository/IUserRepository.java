package com.backend.repository;

import com.backend.dto.UserDTO;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface IUserRepository extends CrudRepository<UserDTO, Long> {
    UserDTO findFirstByUserName(String userName);

    UserDTO findFirstByUserNameAndPassword(String userName, String password);

    List<UserDTO> findAllByRole(String role);
}

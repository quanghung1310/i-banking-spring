package com.backend.service.impl;

import com.backend.dto.UserDTO;
import com.backend.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service("USER_DETAIL")
public class UserDetailService implements UserDetailsService {

    IUserRepository userRepository;

    @Autowired
    public UserDetailService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        UserDTO user = userRepository.findFirstByUserName(username);
        return new User(user.getUserName(), user.getPassword(), new ArrayList<>());
    }
}

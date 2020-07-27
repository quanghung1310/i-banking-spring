package com.backend.service.impl;

import com.backend.constants.StringConstant;
import com.backend.dto.UserDTO;
import com.backend.model.request.employee.RegisterRequest;
import com.backend.model.response.RegisterResponse;
import com.backend.process.UserProcess;
import com.backend.repository.IUserRepository;
import com.backend.service.IAdminService;
import com.backend.util.DataUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService implements IAdminService {
    private static final Logger logger = LogManager.getLogger(AdminService.class);

    @Value("${role.employer}")
    private String EMPLOYER;

    private IUserRepository userRepository;

    @Autowired
    public AdminService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public RegisterResponse registerEmployee(String logId, RegisterRequest request, String role) {
        String userName = request.getName().toLowerCase().replaceAll("\\s+","");
        List<UserDTO> users = userRepository.findAllByRole(role);
        for (UserDTO user : users) {
            if (user.getUserName().equals(userName)) {
                logger.warn("{}| Username - {} was existed!", logId, userName);
                return null;
            }
        }
        logger.warn("{}| Username - {} is not exist!", logId, userName);
        UserDTO userDTO = userRepository.save(UserProcess.createUser(request, userName, role));
        Long userId = userDTO.getId();

        if (userId == null) {
            logger.warn("{}| Save user false!", logId);
            return null;
        }
        logger.info("{}| Save user - {}: success!", logId, userId);

        return RegisterResponse.builder()
                .userName(userDTO.getUserName())
                .password(userDTO.getPassword())
                .createDate(DataUtil.convertTimeWithFormat(userDTO.getCreatedAt().getTime(), StringConstant.FORMAT_ddMMyyyyTHHmmss))
                .build();
    }


}

package com.backend.service.impl;

import com.backend.constants.StringConstant;
import com.backend.dto.UserDTO;
import com.backend.mapper.UserMapper;
import com.backend.model.request.employee.RegisterRequest;
import com.backend.model.response.EmployeeResponse;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @Override
    public List<EmployeeResponse> getEmployee(String logId, Long employerId, String role) {
        List<EmployeeResponse> employeeResponses = new ArrayList<>();
        if (employerId == null) {
            List<UserDTO> userDTOS = userRepository.findAllByRole(role);
            userDTOS.forEach(userDTO -> employeeResponses.add(UserMapper.toModelEmployee(userDTO)));

        } else {
            List<UserDTO> userDTOS = userRepository.findAllByRoleAndId(role, employerId);
            if (userDTOS.size() <= 0) {
                logger.warn("{}| Employee - {} not found!", logId, employerId);
                return null;
            }
            userDTOS.forEach(userDTO -> employeeResponses.add(UserMapper.toModelEmployee(userDTO)));
        }
        return employeeResponses;
    }

    @Override
    public UserDTO findById(long id) {
        Optional<UserDTO> userDTO = userRepository.findById(id);
        return userDTO.orElse(null);
    }

    @Override
    public EmployeeResponse saveEmployee(UserDTO userDTO) {
        UserDTO userDTO1 = userRepository.save(userDTO);
        return UserMapper.toModelEmployee(userDTO1);
    }
}

package com.backend.service.impl;

import com.backend.dto.AccountPaymentDTO;
import com.backend.repository.IAccountPaymentRepository;
import com.backend.service.IAccountPaymentService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountPaymentService implements IAccountPaymentService {
    private static final Logger logger = LogManager.getLogger(AccountPaymentService.class);

    @Autowired
    IAccountPaymentRepository accountPaymentRepository;

    @Override
    public AccountPaymentDTO updateBalance(String logId, long id, long newBalance) {
        AccountPaymentDTO accountPaymentDTO = accountPaymentRepository.findById(id).get();

        if (accountPaymentDTO.getId() == null) {
            logger.warn("{}| Account payment - {} not found!", logId, id);
            return null;
        } else {
            logger.info("{}| Update account payment - {} with balance - {}!", logId, id, newBalance);
            accountPaymentDTO.setBalance(newBalance);
            return accountPaymentRepository.save(accountPaymentDTO);
        }
    }
}

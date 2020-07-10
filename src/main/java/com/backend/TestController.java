package com.backend;

import com.backend.model.Partner;
import com.backend.service.IPartnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {
    @Autowired
    private IPartnerService partnerService;

    @GetMapping(path="/all")
    public @ResponseBody
    Iterable<Partner> getAllUsers() {
        // This returns a JSON or XML with the users
        return partnerService.getAll();
    }
}

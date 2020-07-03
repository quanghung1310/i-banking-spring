package com.backend.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Timestamp;

@Getter
@Setter
@Builder
public class Account {
    public long id;
    public long cardNumber;
    public String cardName;
    public String closeDate;
    public String createdAt;
    public String updatedAt;
    public String description;
    public Integer type;

}

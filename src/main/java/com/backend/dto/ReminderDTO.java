package com.backend.dto;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name="reminder")
public class ReminderDTO {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;

    private long userId;

    private long cardNumber;

    @Column(length = 100)
    private String nameReminisce;

    private int isActive;

    private int type;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    private long merchantId;
}

package com.backend.dto;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name="notify")
public class NotifyDTO {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;

    private long userId;

    @Column(length = 1000)
    private String title;

    @Column(length = 3000)
    private String content;

    private Timestamp createAt;

    private Timestamp updateAt;

    private boolean isSeen;

    private int isActive;
}

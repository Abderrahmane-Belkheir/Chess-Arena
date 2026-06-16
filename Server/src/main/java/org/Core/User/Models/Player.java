package org.Core.User.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Player {
    @Id
    @GeneratedValue
    private String id;
}

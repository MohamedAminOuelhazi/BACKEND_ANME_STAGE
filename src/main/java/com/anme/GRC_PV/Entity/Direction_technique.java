package com.anme.GRC_PV.Entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@DiscriminatorValue("Direction_technique")
@Getter
@Setter
@ToString(callSuper = true)
public class Direction_technique extends user {

    private String departement;

}

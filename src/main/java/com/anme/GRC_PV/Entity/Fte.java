package com.anme.GRC_PV.Entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@DiscriminatorValue("Fte")
@Getter
@Setter
@ToString(callSuper = true)
public class Fte extends user {

}

package com.anme.GRC_PV.Entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@DiscriminatorValue("Commission_techniques")
@Getter
@Setter
@ToString(callSuper = true)
public class Commission_techniques extends user {

}

package com.hotelservicesbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "agent")
@PrimaryKeyJoinColumn(name = "idUtilisateur")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Agent extends Employe {

    private boolean disponible;

    @OneToMany(mappedBy = "agent", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Affectation> affectations = new ArrayList<>();
}

package com.remitly.exercise.swiftCode.core;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "swift_codes")
public class SwiftCodeEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    private String swiftCode;

    private String bankName;

    private String address;

    private String countryISO2;

    private String countryName;

    private Boolean isHeadquarter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "headquarter_swift_code")
    @JsonBackReference
    @JsonIgnore
    private SwiftCodeEntity headquarter;

    @OneToMany(mappedBy = "headquarter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<SwiftCodeEntity> branches = new ArrayList<>();

}

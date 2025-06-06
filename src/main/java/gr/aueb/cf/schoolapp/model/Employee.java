package gr.aueb.cf.schoolapp.model;

import gr.aueb.cf.schoolapp.model.static_data.EducationalUnit;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "employees")
@Builder
public class Employee extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String uuid;

    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToMany
    @JoinTable(
            name = "employees_edu_units"
    )
    private Set<EducationalUnit> eduUnits = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    public void addEducationalUnit(EducationalUnit educationalUnit) {
        if (eduUnits == null) {
            eduUnits = new HashSet<>();
        }
        eduUnits.add(educationalUnit);
        educationalUnit.getEmployees().add(this);
    }

    @PrePersist
    public void initializeUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", isActive=" + isActive +
                ", eduUnits=" + eduUnits +
                '}';
    }
}

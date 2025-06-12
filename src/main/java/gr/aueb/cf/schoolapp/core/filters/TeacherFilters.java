package gr.aueb.cf.schoolapp.core.filters;

import lombok.*;
import org.springframework.lang.Nullable;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class TeacherFilters extends GenericFilters {

    @Nullable
    private String uuid;

    @Nullable
    private String userAfm;

    @Nullable
    private String userAmka;

    @Nullable
    private Boolean isActive;

    @Override
    public String toString() {
        return "TeacherFilters{" +
                "uuid='" + uuid + '\'' +
                ", userAfm='" + userAfm + '\'' +
                ", userAmka='" + userAmka + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}

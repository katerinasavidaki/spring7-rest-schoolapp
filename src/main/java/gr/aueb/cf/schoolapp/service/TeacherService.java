package gr.aueb.cf.schoolapp.service;

import gr.aueb.cf.schoolapp.core.exceptions.AppObjectAlreadyExistsException;
import gr.aueb.cf.schoolapp.core.exceptions.AppObjectInvalidArgumentException;
import gr.aueb.cf.schoolapp.dto.TeacherInsertDTO;
import gr.aueb.cf.schoolapp.dto.TeacherReadOnlyDTO;
import gr.aueb.cf.schoolapp.mapper.Mapper;
import gr.aueb.cf.schoolapp.model.Teacher;
import gr.aueb.cf.schoolapp.repository.PersonalInfoRepository;
import gr.aueb.cf.schoolapp.repository.TeacherRepository;
import gr.aueb.cf.schoolapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final Mapper mapper;
    private final UserRepository userRepository;
    private final PersonalInfoRepository personalInfoRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(TeacherService.class);

    public TeacherReadOnlyDTO saveTeacher(TeacherInsertDTO teacherInsertDTO, MultipartFile amkaFile)
    throws AppObjectAlreadyExistsException, AppObjectInvalidArgumentException, IOException {

        if (userRepository.findByVat(teacherInsertDTO.getUser().getVat()).isPresent()) {
            throw new AppObjectAlreadyExistsException("User", "User with VAT " + teacherInsertDTO.getUser().getVat() + " already exists");
        }

        if (userRepository.findByUsername(teacherInsertDTO.getUser().getUsername()).isPresent()) {
            throw new AppObjectAlreadyExistsException("User", "User with username " + teacherInsertDTO.getUser().getUsername() + " already exists");
        }

        if (personalInfoRepository.findByAmka(teacherInsertDTO.getPersonalInfo().getAmka()).isPresent()) {
            throw new AppObjectAlreadyExistsException("PersonalInfo", "PersonalInfo with AMKA " + teacherInsertDTO.getPersonalInfo().getAmka() + " already exists");
        }

        // Saving the teacher cascades the user and personal info
        Teacher teacher = mapper.mapToTeacherEntity(teacherInsertDTO);
        Teacher savedTeacher = teacherRepository.save(teacher);

        return mapper.mapToTeacherReadOnlyDTO(savedTeacher);
    }
}

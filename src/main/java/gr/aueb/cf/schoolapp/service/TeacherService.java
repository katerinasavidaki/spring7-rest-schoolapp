package gr.aueb.cf.schoolapp.service;

import gr.aueb.cf.schoolapp.core.exceptions.AppObjectAlreadyExistsException;
import gr.aueb.cf.schoolapp.core.exceptions.AppObjectInvalidArgumentException;
import gr.aueb.cf.schoolapp.core.filters.Paginated;
import gr.aueb.cf.schoolapp.core.filters.TeacherFilters;
import gr.aueb.cf.schoolapp.core.specifications.TeacherSpecification;
import gr.aueb.cf.schoolapp.dto.TeacherInsertDTO;
import gr.aueb.cf.schoolapp.dto.TeacherReadOnlyDTO;
import gr.aueb.cf.schoolapp.mapper.Mapper;
import gr.aueb.cf.schoolapp.model.Attachment;
import gr.aueb.cf.schoolapp.model.PersonalInfo;
import gr.aueb.cf.schoolapp.model.Teacher;
import gr.aueb.cf.schoolapp.repository.PersonalInfoRepository;
import gr.aueb.cf.schoolapp.repository.TeacherRepository;
import gr.aueb.cf.schoolapp.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final Mapper mapper;
    private final UserRepository userRepository;
    private final PersonalInfoRepository personalInfoRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(TeacherService.class);

    @Transactional(rollbackOn = {AppObjectAlreadyExistsException.class, IOException.class})
    public TeacherReadOnlyDTO saveTeacher(TeacherInsertDTO teacherInsertDTO, MultipartFile amkaFile)
    throws AppObjectAlreadyExistsException, AppObjectInvalidArgumentException, IOException {

        if (userRepository.findByAfm(teacherInsertDTO.getUser().getAfm()).isPresent()) {
            throw new AppObjectAlreadyExistsException("User", "User with VAT " + teacherInsertDTO.getUser().getAfm() + " already exists");
        }

        if (userRepository.findByUsername(teacherInsertDTO.getUser().getUsername()).isPresent()) {
            throw new AppObjectAlreadyExistsException("User", "User with username " + teacherInsertDTO.getUser().getUsername() + " already exists");
        }

        if (personalInfoRepository.findByAmka(teacherInsertDTO.getPersonalInfo().getAmka()).isPresent()) {
            throw new AppObjectAlreadyExistsException("PersonalInfo", "PersonalInfo with AMKA " + teacherInsertDTO.getPersonalInfo().getAmka() + " already exists");
        }

        // Saving the teacher cascades the user and personal info
        Teacher teacher = mapper.mapToTeacherEntity(teacherInsertDTO);

        saveAmkaFile(teacher.getPersonalInfo(), amkaFile);
        Teacher savedTeacher = teacherRepository.save(teacher);

        return mapper.mapToTeacherReadOnlyDTO(savedTeacher);
    }

    public void saveAmkaFile(PersonalInfo personalInfo, MultipartFile amkaFile) throws IOException {

        if (amkaFile == null || amkaFile.isEmpty()) return;

        String originalFilename = amkaFile.getOriginalFilename();
        String savedName = UUID.randomUUID().toString() + getFileExtension(originalFilename);

        String uploadDirectory = "uploads/";
        Path filePath = Paths.get(uploadDirectory + savedName);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, amkaFile.getBytes());

        Attachment attachment = new Attachment();
        attachment.setFilename(originalFilename);
        attachment.setSavedName(savedName);
        attachment.setFilePath(filePath.toString());
        attachment.setContentType(amkaFile.getContentType());
        attachment.setExtension(getFileExtension(originalFilename));

        personalInfo.setAmkaFile(attachment);

    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    @Transactional
    public Page<TeacherReadOnlyDTO> getPaginatedTeachers(int page, int size) {

        String defaultSort = "id";
        Pageable pageable = PageRequest.of(page, size, Sort.by(defaultSort).ascending());
        return teacherRepository.findAll(pageable).map(mapper::mapToTeacherReadOnlyDTO);
    }

    @Transactional
    public Page<TeacherReadOnlyDTO> getPaginatedSorted(int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return teacherRepository.findAll(pageable).map(mapper::mapToTeacherReadOnlyDTO);
    }

    public List<TeacherReadOnlyDTO> getTeachersFiltered(TeacherFilters filters) {
        return teacherRepository.findAll(getSpecsFromFilters(filters))
                .stream()
                .map(mapper::mapToTeacherReadOnlyDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public Paginated<TeacherReadOnlyDTO> getTeachersFilteredPaginated(TeacherFilters filters) {

        var filtered = teacherRepository.findAll(getSpecsFromFilters(filters), filters.getPageable());
        return new Paginated<>(filtered.map(mapper::mapToTeacherReadOnlyDTO));
    }

    private Specification<Teacher> getSpecsFromFilters(TeacherFilters filters) {

        return Specification
                .where(TeacherSpecification.teacherStringFieldLike("uuid", filters.getUuid())
                .and(TeacherSpecification.teacherUserAfmIs(filters.getUserAfm()))
                .and(TeacherSpecification.teacherPersonalInfoAmkaIs(filters.getUserAmka()))
                .and(TeacherSpecification.teacherIsActive(filters.getIsActive())));
    }




}

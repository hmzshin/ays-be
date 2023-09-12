package com.ays.assignment.service.impl;

import com.ays.AbstractUnitTest;
import com.ays.assignment.model.Assignment;
import com.ays.assignment.model.dto.request.AssignmentListRequest;
import com.ays.assignment.model.dto.request.AssignmentListRequestBuilder;
import com.ays.assignment.model.entity.AssignmentEntity;
import com.ays.assignment.model.entity.AssignmentEntityBuilder;
import com.ays.assignment.model.mapper.AssignmentEntityToAssignmentMapper;
import com.ays.assignment.repository.AssignmentRepository;
import com.ays.assignment.util.exception.AysAssignmentNotExistByIdException;
import com.ays.auth.model.AysIdentity;
import com.ays.common.model.AysPage;
import com.ays.common.model.AysPageBuilder;
import com.ays.common.util.AysRandomUtil;
import com.ays.location.model.UserLocation;
import com.ays.location.model.entity.UserLocationEntity;
import com.ays.location.model.entity.UserLocationEntityBuilder;
import com.ays.location.model.mapper.UserLocationEntityToUserLocationMapper;
import com.ays.location.repository.UserLocationRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;


class AssignmentServiceImplTest extends AbstractUnitTest {

    @InjectMocks
    private AssignmentServiceImpl assignmentService;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private UserLocationRepository userLocationRepository;

    @Mock
    private AysIdentity identity;

    private static final AssignmentEntityToAssignmentMapper ASSIGNMENT_ENTITY_TO_ASSIGNMENT_MAPPER = AssignmentEntityToAssignmentMapper.initialize();
    private static final UserLocationEntityToUserLocationMapper USER_LOCATION_ENTITY_TO_USER_LOCATION_MAPPER = UserLocationEntityToUserLocationMapper.initialize();

    @Test
    void givenAssignmentId_whenGetAssignment_thenReturnAssignment() {

        // Given
        String mockInstitutionId = AysRandomUtil.generateUUID();

        AssignmentEntity mockAssignmentEntity = new AssignmentEntityBuilder()
                .withValidFields()
                .withInstitutionId(mockInstitutionId).build();
        String mockAssignmentId = mockAssignmentEntity.getId();
        UserLocationEntity mockUserLocationEntity = new UserLocationEntityBuilder().withValidFields().build();

        UserLocation mockUserLocation = USER_LOCATION_ENTITY_TO_USER_LOCATION_MAPPER.map(mockUserLocationEntity);
        Assignment mockAssignment = ASSIGNMENT_ENTITY_TO_ASSIGNMENT_MAPPER.map(mockAssignmentEntity);
        mockAssignment.getUser().setLocation(mockUserLocation.getPoint());

        // When
        Mockito.when(identity.getInstitutionId())
                .thenReturn(mockInstitutionId);
        Mockito.when(assignmentRepository.findByIdAndInstitutionId(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Optional.of(mockAssignmentEntity));
        Mockito.when(userLocationRepository.findByUserId(Mockito.anyString())).thenReturn(Optional.of(mockUserLocationEntity));

        // Then
        Assignment assignment = assignmentService.getAssignmentById(mockAssignmentId);

        Assertions.assertEquals(mockAssignment.getFirstName(), assignment.getFirstName());
        Assertions.assertEquals(mockAssignment.getDescription(), assignment.getDescription());
        Assertions.assertEquals(mockAssignment.getLastName(), assignment.getLastName());
        Assertions.assertEquals(mockAssignment.getPhoneNumber().getCountryCode(), assignment.getPhoneNumber().getCountryCode());
        Assertions.assertEquals(mockAssignment.getPhoneNumber().getLineNumber(), assignment.getPhoneNumber().getLineNumber());
        Assertions.assertEquals(mockAssignment.getStatus(), assignment.getStatus());
        Assertions.assertEquals(mockAssignment.getPoint(), assignment.getPoint());
        Assertions.assertEquals(mockAssignment.getUser().getLocation().getLongitude(),
                assignment.getUser().getLocation().getLongitude());
        Assertions.assertEquals(mockAssignment.getUser().getLocation().getLatitude(),
                assignment.getUser().getLocation().getLatitude());

        Mockito.verify(assignmentRepository, Mockito.times(1))
                .findByIdAndInstitutionId(mockAssignmentId, mockInstitutionId);
        Mockito.verify(identity, Mockito.times(1))
                .getInstitutionId();

    }

    @Test
    void givenAssignmentId_whenAssignmentNotFound_thenThrowAysAssignmentNotExistByIdException() {

        // Given
        String mockAssignmentId = AysRandomUtil.generateUUID();
        String mockInstitutionId = AysRandomUtil.generateUUID();

        // When
        Mockito.when(identity.getInstitutionId())
                .thenReturn(mockInstitutionId);
        Mockito.when(assignmentRepository.findByIdAndInstitutionId(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Optional.empty());

        // Then
        Assertions.assertThrows(
                AysAssignmentNotExistByIdException.class,
                () -> assignmentService.getAssignmentById(mockAssignmentId)
        );

        Mockito.when(assignmentRepository.findByIdAndInstitutionId(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Optional.empty());
        Mockito.verify(identity, Mockito.times(1))
                .getInstitutionId();
    }

    @Test
    void givenAssignmentListRequest_whenAssignmentStatusIsAvailable_thenReturnAysPageAssignmentResponse() {

        // Given
        String mockInstitutionId = AysRandomUtil.generateUUID();

        AssignmentListRequest assignmentListRequest = new AssignmentListRequestBuilder()
                .withValidValues()
                .withPhoneNumber(null)
                .build();

        List<AssignmentEntity> mockAssignmentEntities = List.of(new AssignmentEntityBuilder()
                .withValidFields().build());
        Page<AssignmentEntity> mockPageAssignmentEntities = new PageImpl<>(mockAssignmentEntities);

        List<Assignment> mockAssignments = ASSIGNMENT_ENTITY_TO_ASSIGNMENT_MAPPER.map(mockAssignmentEntities);
        AysPage<Assignment> mockAysPageAssignments = AysPage.of(assignmentListRequest.getFilter(), mockPageAssignmentEntities, mockAssignments);

        // When
        Mockito.when(identity.getInstitutionId()).thenReturn(mockInstitutionId);
        Mockito.when(assignmentRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
                .thenReturn(mockPageAssignmentEntities);

        // Then
        AysPage<Assignment> aysPageAssignment = assignmentService.getAssignments(assignmentListRequest);

        AysPageBuilder.assertEquals(mockAysPageAssignments, aysPageAssignment);

        Mockito.verify(assignmentRepository, Mockito.times(1))
                .findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class));
        Mockito.verify(identity, Mockito.times(1)).getInstitutionId();
    }

    @Test
    void givenAssignmentListRequest_whenPhoneNumberIsAvailable_thenReturnAysPageAssignmentResponse() {

        // Given
        String mockInstitutionId = AysRandomUtil.generateUUID();

        AssignmentListRequest assignmentListRequest = new AssignmentListRequestBuilder()
                .withValidValues()
                .withStatuses(null)
                .build();

        List<AssignmentEntity> mockAssignmentEntities = List.of(new AssignmentEntityBuilder()
                .withValidFields().build());
        Page<AssignmentEntity> mockPageAssignmentEntities = new PageImpl<>(mockAssignmentEntities);

        List<Assignment> mockAssignments = ASSIGNMENT_ENTITY_TO_ASSIGNMENT_MAPPER.map(mockAssignmentEntities);
        AysPage<Assignment> mockAysPageAssignments = AysPage.of(assignmentListRequest.getFilter(), mockPageAssignmentEntities, mockAssignments);

        // When
        Mockito.when(identity.getInstitutionId()).thenReturn(mockInstitutionId);
        Mockito.when(assignmentRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
                .thenReturn(mockPageAssignmentEntities);

        // Then
        AysPage<Assignment> aysPageAssignment = assignmentService.getAssignments(assignmentListRequest);

        AysPageBuilder.assertEquals(mockAysPageAssignments, aysPageAssignment);

        Mockito.verify(assignmentRepository, Mockito.times(1))
                .findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class));
        Mockito.verify(identity, Mockito.times(1)).getInstitutionId();
    }

    @Test
    void givenAssignmentListRequest_whenAssignmentStatusAndPhoneNumberAvailable_thenReturnAysPageAssignmentResponse() {

        // Given
        String mockInstitutionId = AysRandomUtil.generateUUID();

        AssignmentListRequest assignmentListRequest = new AssignmentListRequestBuilder()
                .withValidValues()
                .build();

        List<AssignmentEntity> mockAssignmentEntities = List.of(new AssignmentEntityBuilder()
                .withValidFields().build());
        Page<AssignmentEntity> mockPageAssignmentEntities = new PageImpl<>(mockAssignmentEntities);

        List<Assignment> mockAssignments = ASSIGNMENT_ENTITY_TO_ASSIGNMENT_MAPPER.map(mockAssignmentEntities);
        AysPage<Assignment> mockAysPageAssignments = AysPage.of(assignmentListRequest.getFilter(), mockPageAssignmentEntities, mockAssignments);

        // When
        Mockito.when(identity.getInstitutionId()).thenReturn(mockInstitutionId);
        Mockito.when(assignmentRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
                .thenReturn(mockPageAssignmentEntities);

        // Then
        AysPage<Assignment> aysPageAssignment = assignmentService.getAssignments(assignmentListRequest);

        AysPageBuilder.assertEquals(mockAysPageAssignments, aysPageAssignment);

        Mockito.verify(assignmentRepository, Mockito.times(1))
                .findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class));
        Mockito.verify(identity, Mockito.times(1)).getInstitutionId();
    }

}
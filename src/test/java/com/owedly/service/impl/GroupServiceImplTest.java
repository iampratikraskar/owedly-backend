package com.owedly.service.impl;

import com.owedly.dto.request.AddGroupMemberRequest;
import com.owedly.dto.request.CreateGroupRequest;
import com.owedly.dto.response.GroupResponse;
import com.owedly.entity.Group;
import com.owedly.entity.GroupMember;
import com.owedly.entity.Role;
import com.owedly.entity.User;
import com.owedly.exception.GroupAccessDeniedException;
import com.owedly.repository.GroupMemberRepository;
import com.owedly.repository.GroupRepository;
import com.owedly.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class GroupServiceImplTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GroupServiceImpl groupService;

    private User pratik;
    private User rahul;
    private User akash;

    private Group group;

    @BeforeEach
    void setUp() {

        pratik = new User();
        pratik.setId(1L);
        pratik.setName("Pratik");
        pratik.setEmail("pratik@example.com");
        pratik.setRole(Role.USER);

        rahul = new User();
        rahul.setId(2L);
        rahul.setName("Rahul");
        rahul.setEmail("rahul@example.com");
        rahul.setRole(Role.USER);

        akash = new User();
        akash.setId(3L);
        akash.setName("Akash");
        akash.setEmail("akash@example.com");
        akash.setRole(Role.USER);

        group = new Group();
        group.setId(1L);
        group.setName("Goa Trip");
        group.setDescription("Goa trip expenses");
        group.setCreatedBy(pratik);
    }

    @Test
    void createGroup_shouldCreateGroupAndAddCreatorAsMember() {

        CreateGroupRequest request =
                new CreateGroupRequest();

        request.setName("Goa Trip");
        request.setDescription("Goa trip expenses");

        when(userRepository.findByEmail("pratik@example.com"))
                .thenReturn(Optional.of(pratik));

        when(groupRepository.save(any(Group.class)))
                .thenReturn(group);

        when(groupMemberRepository.findByGroupId(1L))
                .thenReturn(List.of());

        GroupResponse response =
                groupService.createGroup(
                        request,
                        "pratik@example.com"
                );

        assertNotNull(response);

        assertEquals(
                1L,
                response.getId()
        );

        assertEquals(
                "Goa Trip",
                response.getName()
        );

        assertEquals(
                "Goa trip expenses",
                response.getDescription()
        );

        assertEquals(
                1L,
                response.getCreatedBy()
        );

        verify(groupRepository)
                .save(any(Group.class));

        verify(groupMemberRepository)
                .save(any(GroupMember.class));
    }

    @Test
    void addMember_shouldAddNewMember() {

        AddGroupMemberRequest request =
                new AddGroupMemberRequest();

        request.setUserId(2L);

        when(userRepository.findByEmail("pratik@example.com"))
                .thenReturn(Optional.of(pratik));

        when(groupRepository.findById(1L))
                .thenReturn(Optional.of(group));

        // Pratik is already a member
        when(groupMemberRepository.existsByGroupIdAndUserId(
                1L,
                1L
        )).thenReturn(true);

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(rahul));

        // Rahul is not already a member
        when(groupMemberRepository.existsByGroupIdAndUserId(
                1L,
                2L
        )).thenReturn(false);

        when(groupMemberRepository.findByGroupId(1L))
                .thenReturn(List.of());

        GroupResponse response =
                groupService.addMember(
                        1L,
                        request,
                        "pratik@example.com"
                );

        assertNotNull(response);

        verify(groupMemberRepository)
                .save(any(GroupMember.class));
    }

    @Test
    void addMember_shouldRejectNonMember() {

        AddGroupMemberRequest request =
                new AddGroupMemberRequest();

        request.setUserId(2L);

        when(userRepository.findByEmail("akash@example.com"))
                .thenReturn(Optional.of(akash));

        when(groupRepository.findById(1L))
                .thenReturn(Optional.of(group));

        // Akash is NOT a member
        when(groupMemberRepository.existsByGroupIdAndUserId(
                1L,
                3L
        )).thenReturn(false);

        assertThrows(
                GroupAccessDeniedException.class,
                () -> groupService.addMember(
                        1L,
                        request,
                        "akash@example.com"
                )
        );

        verify(groupMemberRepository, never())
                .save(any(GroupMember.class));
    }

    @Test
    void addMember_shouldRejectDuplicateMember() {

        AddGroupMemberRequest request =
                new AddGroupMemberRequest();

        request.setUserId(2L);

        when(userRepository.findByEmail("pratik@example.com"))
                .thenReturn(Optional.of(pratik));

        when(groupRepository.findById(1L))
                .thenReturn(Optional.of(group));

        // Pratik is a member
        when(groupMemberRepository.existsByGroupIdAndUserId(
                1L,
                1L
        )).thenReturn(true);

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(rahul));

        // Rahul is ALREADY a member
        when(groupMemberRepository.existsByGroupIdAndUserId(
                1L,
                2L
        )).thenReturn(true);

        assertThrows(
                GroupAccessDeniedException.class,
                () -> groupService.addMember(
                        1L,
                        request,
                        "pratik@example.com"
                )
        );

        verify(groupMemberRepository, never())
                .save(any(GroupMember.class));
    }
}
package com.owedly.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.owedly.dto.request.CreateExpenseRequest;
import com.owedly.dto.request.ExpenseSplitRequest;
import com.owedly.dto.response.ExpenseResponse;
import com.owedly.dto.response.ExpenseSplitResponse;
import com.owedly.entity.ActivityType;
import com.owedly.entity.Category;
import com.owedly.entity.Expense;
import com.owedly.entity.ExpenseSplit;
import com.owedly.entity.Group;
import com.owedly.entity.User;
import com.owedly.exception.GroupAccessDeniedException;
import com.owedly.exception.InvalidExpenseException;
import com.owedly.exception.ResourceNotFoundException;
import com.owedly.repository.CategoryRepository;
import com.owedly.repository.ExpenseRepository;
import com.owedly.repository.ExpenseSplitRepository;
import com.owedly.repository.GroupMemberRepository;
import com.owedly.repository.GroupRepository;
import com.owedly.repository.UserRepository;
import com.owedly.service.ActivityLogService;
import com.owedly.service.ExpenseService;

@Service
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

	private static final int MONEY_SCALE = 2;

	private final ExpenseRepository expenseRepository;
	private final ExpenseSplitRepository expenseSplitRepository;
	private final GroupRepository groupRepository;
	private final GroupMemberRepository groupMemberRepository;
	private final UserRepository userRepository;
	private final CategoryRepository categoryRepository;
	private final ActivityLogService activityLogService;

	public ExpenseServiceImpl(ExpenseRepository expenseRepository, ExpenseSplitRepository expenseSplitRepository,
			GroupRepository groupRepository, GroupMemberRepository groupMemberRepository, UserRepository userRepository,
			CategoryRepository categoryRepository, ActivityLogService activityLogService) {

		this.expenseRepository = expenseRepository;
		this.expenseSplitRepository = expenseSplitRepository;
		this.groupRepository = groupRepository;
		this.groupMemberRepository = groupMemberRepository;
		this.userRepository = userRepository;
		this.categoryRepository = categoryRepository;
		this.activityLogService = activityLogService;
	}

	// =========================================================
	// CREATE EXPENSE
	// =========================================================

	@Override
	public ExpenseResponse createExpense(Long groupId, CreateExpenseRequest request, String userEmail) {

		User payer = getUserByEmail(userEmail);

		Group group = getGroup(groupId);

		validateMembership(groupId, payer.getId());

		Category category = null;

		if (request.getCategoryId() != null) {
			category = categoryRepository.findById(request.getCategoryId()).orElseThrow(
					() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
		}

		validateSplitParticipants(groupId, request.getSplits());

		Expense expense = new Expense();

		expense.setGroup(group);
		expense.setPaidBy(payer);
		expense.setCategory(category);

		expense.setDescription(request.getDescription().trim());

		expense.setAmount(request.getAmount().setScale(MONEY_SCALE, RoundingMode.HALF_UP));

		expense.setSplitMethod(request.getSplitMethod());

		expense.setExpenseDate(request.getExpenseDate() != null ? request.getExpenseDate() : LocalDateTime.now());

		Expense savedExpense = expenseRepository.save(expense);

		List<ExpenseSplit> splits = buildSplits(savedExpense, request);

		expenseSplitRepository.saveAll(splits);
		
		activityLogService.log(
		        payer,
		        group,
		        ActivityType.EXPENSE_CREATED,
		        "Added expense \"" + savedExpense.getDescription()
		                + "\" of ₹"
		                + savedExpense.getAmount(),
		        savedExpense.getId()
		);

		return buildExpenseResponse(savedExpense, splits);
	}

	// =========================================================
	// GET GROUP EXPENSES
	// =========================================================

	@Override
	@Transactional(readOnly = true)
	public List<ExpenseResponse> getGroupExpenses(Long groupId, String userEmail) {

		User user = getUserByEmail(userEmail);

		getGroup(groupId);

		validateMembership(groupId, user.getId());

		return expenseRepository.findByGroupIdOrderByExpenseDateDesc(groupId).stream().map(this::buildExpenseResponse)
				.toList();
	}

	// =========================================================
	// GET SINGLE EXPENSE
	// =========================================================

	@Override
	@Transactional(readOnly = true)
	public ExpenseResponse getExpense(Long expenseId, String userEmail) {

		User user = getUserByEmail(userEmail);

		Expense expense = expenseRepository.findById(expenseId)
				.orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + expenseId));

		validateMembership(expense.getGroup().getId(), user.getId());

		return buildExpenseResponse(expense);
	}

	// =========================================================
	// BUILD SPLITS
	// =========================================================

	private List<ExpenseSplit> buildSplits(Expense expense, CreateExpenseRequest request) {

		if (request.getSplits() == null || request.getSplits().isEmpty()) {

			throw new InvalidExpenseException("At least one expense split is required");
		}

		if (request.getSplitMethod() == null) {
			throw new InvalidExpenseException("Split method is required");
		}

		switch (request.getSplitMethod()) {

		case EQUAL:

			validateEqualSplits(request.getSplits());

			return buildEqualSplits(expense, request.getSplits());

		case EXACT:

			validateExactSplits(request.getSplits());

			return buildExactSplits(expense, request.getSplits());

		case PERCENTAGE:

			validatePercentageSplits(request.getSplits());

			return buildPercentageSplits(expense, request.getSplits());

		default:

			throw new InvalidExpenseException("Unsupported split method");
		}
	}

	// =========================================================
	// EQUAL SPLITS
	// =========================================================

	private List<ExpenseSplit> buildEqualSplits(
	        Expense expense,
	        List<ExpenseSplitRequest> splitRequests) {

	    int participantCount = splitRequests.size();

	    if (participantCount == 0) {
	        throw new InvalidExpenseException(
	                "At least one participant is required"
	        );
	    }

	    long totalCents = expense.getAmount()
	            .movePointRight(MONEY_SCALE)
	            .longValueExact();

	    long baseCents =
	            totalCents / participantCount;

	    long remainderCents =
	            totalCents % participantCount;

	    List<ExpenseSplit> splits =
	            new ArrayList<>();

	    for (int i = 0; i < participantCount; i++) {

	        long participantCents = baseCents;

	        if (i < remainderCents) {
	            participantCents++;
	        }

	        BigDecimal shareAmount =
	                BigDecimal.valueOf(participantCents)
	                        .movePointLeft(MONEY_SCALE)
	                        .setScale(
	                                MONEY_SCALE,
	                                RoundingMode.HALF_UP
	                        );

	        ExpenseSplit split =
	                new ExpenseSplit();

	        split.setExpense(expense);

	        // FIX:
	        // Do not use i inside a lambda.
	        // Use the existing getUserById() method.
	        split.setUser(
	                getUserById(
	                        splitRequests
	                                .get(i)
	                                .getUserId()
	                )
	        );

	        split.setShareAmount(
	                shareAmount
	        );

	        split.setPercentage(null);

	        splits.add(split);
	    }

	    return splits;
	}

	// =========================================================
	// EXACT SPLITS
	// =========================================================

	private List<ExpenseSplit> buildExactSplits(Expense expense, List<ExpenseSplitRequest> requests) {

		BigDecimal total = requests.stream().map(ExpenseSplitRequest::getShareAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add).setScale(MONEY_SCALE, RoundingMode.HALF_UP);

		if (total.compareTo(expense.getAmount()) != 0) {

			throw new InvalidExpenseException("Exact split amounts must total " + expense.getAmount());
		}

		return requests.stream().map(request -> {

			ExpenseSplit split = new ExpenseSplit();

			split.setExpense(expense);

			split.setUser(getUserById(request.getUserId()));

			split.setShareAmount(request.getShareAmount().setScale(MONEY_SCALE, RoundingMode.HALF_UP));

			split.setPercentage(null);

			return split;
		}).toList();
	}

	// =========================================================
	// PERCENTAGE SPLITS
	// =========================================================

	private List<ExpenseSplit> buildPercentageSplits(Expense expense, List<ExpenseSplitRequest> requests) {

		BigDecimal totalPercentage = requests.stream().map(ExpenseSplitRequest::getPercentage)
				.reduce(BigDecimal.ZERO, BigDecimal::add).setScale(MONEY_SCALE, RoundingMode.HALF_UP);

		if (totalPercentage.compareTo(new BigDecimal("100.00")) != 0) {

			throw new InvalidExpenseException("Percentage splits must total 100%");
		}

		List<ExpenseSplit> splits = new ArrayList<>();

		BigDecimal allocated = BigDecimal.ZERO;

		for (int i = 0; i < requests.size(); i++) {

			ExpenseSplitRequest request = requests.get(i);

			BigDecimal share;

			if (i == requests.size() - 1) {

				/*
				 * Give the final participant the remaining amount so that the total always
				 * equals the expense exactly.
				 */

				share = expense.getAmount().subtract(allocated);

			} else {

				share = expense.getAmount().multiply(request.getPercentage()).divide(new BigDecimal("100"), MONEY_SCALE,
						RoundingMode.HALF_UP);

				allocated = allocated.add(share);
			}

			ExpenseSplit split = new ExpenseSplit();

			split.setExpense(expense);

			split.setUser(getUserById(request.getUserId()));

			split.setPercentage(request.getPercentage());

			split.setShareAmount(share.setScale(MONEY_SCALE, RoundingMode.HALF_UP));

			splits.add(split);
		}

		return splits;
	}

	// =========================================================
	// VALIDATE SPLIT PARTICIPANTS
	// =========================================================

	private void validateSplitParticipants(Long groupId, List<ExpenseSplitRequest> requests) {

		if (requests == null || requests.isEmpty()) {

			throw new InvalidExpenseException("At least one expense participant is required");
		}

		Set<Long> userIds = new HashSet<>();

		for (ExpenseSplitRequest request : requests) {

			if (request == null || request.getUserId() == null) {

				throw new InvalidExpenseException("Split participant is required");
			}

			Long userId = request.getUserId();

			if (!userIds.add(userId)) {

				throw new InvalidExpenseException("A user cannot appear more than once " + "in an expense split");
			}

			boolean member = groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);

			if (!member) {

				throw new GroupAccessDeniedException("User with id " + userId + " is not a member " + "of this group");
			}
		}
	}

	// =========================================================
	// VALIDATE MEMBERSHIP
	// =========================================================

	private void validateMembership(Long groupId, Long userId) {

		boolean member = groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);

		if (!member) {

			throw new GroupAccessDeniedException("You are not a member of this group");
		}
	}

	// =========================================================
	// GET GROUP
	// =========================================================

	private Group getGroup(Long groupId) {

		return groupRepository.findById(groupId)
				.orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
	}

	// =========================================================
	// GET USER BY EMAIL
	// =========================================================

	private User getUserByEmail(String email) {

		return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	// =========================================================
	// GET USER BY ID
	// =========================================================

	private User getUserById(Long userId) {

		return userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
	}

	// =========================================================
	// BUILD EXPENSE RESPONSE
	// =========================================================

	private ExpenseResponse buildExpenseResponse(Expense expense) {

		List<ExpenseSplit> splits = expenseSplitRepository.findByExpenseId(expense.getId());

		return buildExpenseResponse(expense, splits);
	}

	private ExpenseResponse buildExpenseResponse(Expense expense, List<ExpenseSplit> splits) {

		ExpenseResponse response = new ExpenseResponse();

		response.setId(expense.getId());

		response.setGroupId(expense.getGroup().getId());

		response.setDescription(expense.getDescription());

		response.setAmount(expense.getAmount());

		response.setPaidBy(expense.getPaidBy().getId());

		response.setPaidByName(expense.getPaidBy().getName());

		if (expense.getCategory() != null) {

			response.setCategoryId(expense.getCategory().getId());

			response.setCategoryName(expense.getCategory().getName());
		}

		response.setSplitMethod(expense.getSplitMethod());

		response.setExpenseDate(expense.getExpenseDate());

		response.setCreatedAt(expense.getCreatedAt());

		response.setUpdatedAt(expense.getUpdatedAt());

		List<ExpenseSplitResponse> splitResponses = splits.stream()
				.map(split -> new ExpenseSplitResponse(split.getUser().getId(), split.getUser().getName(),
						split.getShareAmount(), split.getPercentage()))
				.toList();

		response.setSplits(splitResponses);

		return response;
	}

	// =========================================================
	// VALIDATE EQUAL SPLITS
	// =========================================================

	private void validateEqualSplits(List<ExpenseSplitRequest> splits) {

		for (ExpenseSplitRequest split : splits) {

			if (split.getShareAmount() != null) {

				throw new InvalidExpenseException("Share amount must not be provided " + "for EQUAL split");
			}

			if (split.getPercentage() != null) {

				throw new InvalidExpenseException("Percentage must not be provided " + "for EQUAL split");
			}
		}
	}

	// =========================================================
	// VALIDATE EXACT SPLITS
	// =========================================================

	private void validateExactSplits(List<ExpenseSplitRequest> splits) {

		for (ExpenseSplitRequest split : splits) {

			if (split.getShareAmount() == null) {

				throw new InvalidExpenseException("Share amount is required " + "for EXACT split");
			}

			if (split.getShareAmount().compareTo(BigDecimal.ZERO) < 0) {

				throw new InvalidExpenseException("Share amount cannot be negative");
			}

			if (split.getPercentage() != null) {

				throw new InvalidExpenseException("Percentage must not be provided " + "for EXACT split");
			}
		}
	}

	// =========================================================
	// VALIDATE PERCENTAGE SPLITS
	// =========================================================

	private void validatePercentageSplits(List<ExpenseSplitRequest> splits) {

		for (ExpenseSplitRequest split : splits) {

			if (split.getPercentage() == null) {

				throw new InvalidExpenseException("Percentage is required " + "for PERCENTAGE split");
			}

			if (split.getPercentage().compareTo(BigDecimal.ZERO) < 0) {

				throw new InvalidExpenseException("Percentage cannot be negative");
			}

			if (split.getPercentage().compareTo(new BigDecimal("100")) > 0) {

				throw new InvalidExpenseException("Percentage cannot exceed 100");
			}

			if (split.getShareAmount() != null) {

				throw new InvalidExpenseException("Share amount must not be provided " + "for PERCENTAGE split");
			}
		}
	}

	// =========================================================
	// UPDATE EXPENSE
	// =========================================================

	@Override
	@Transactional
	public ExpenseResponse updateExpense(Long expenseId, CreateExpenseRequest request, String userEmail) {

		// -----------------------------------------------------
		// Find existing expense
		// -----------------------------------------------------

		Expense expense = expenseRepository.findById(expenseId)
				.orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + expenseId));

		// -----------------------------------------------------
		// Find current logged-in user
		// -----------------------------------------------------

		User currentUser = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		// -----------------------------------------------------
		// Only original payer can update
		// -----------------------------------------------------

		if (!expense.getPaidBy().getId().equals(currentUser.getId())) {

			throw new GroupAccessDeniedException("Only the expense payer can update " + "this expense");
		}

		// -----------------------------------------------------
		// Make sure payer is still group member
		// -----------------------------------------------------

		Long groupId = expense.getGroup().getId();

		validateMembership(groupId, currentUser.getId());

		// -----------------------------------------------------
		// Validate request
		// -----------------------------------------------------

		if (request == null) {

			throw new InvalidExpenseException("Expense request cannot be null");
		}

		if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {

			throw new InvalidExpenseException("Expense description is required");
		}

		if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {

			throw new InvalidExpenseException("Expense amount must be greater than zero");
		}

		if (request.getSplitMethod() == null) {

			throw new InvalidExpenseException("Split method is required");
		}

		// -----------------------------------------------------
		// Validate participants
		// -----------------------------------------------------

		validateSplitParticipants(groupId, request.getSplits());

		// -----------------------------------------------------
		// Update category
		// -----------------------------------------------------

		Category category = null;

		if (request.getCategoryId() != null) {

			category = categoryRepository.findById(request.getCategoryId()).orElseThrow(
					() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
		}

		expense.setCategory(category);

		// -----------------------------------------------------
		// Update basic expense fields
		// -----------------------------------------------------

		expense.setDescription(request.getDescription().trim());

		expense.setAmount(request.getAmount().setScale(MONEY_SCALE, RoundingMode.HALF_UP));

		expense.setSplitMethod(request.getSplitMethod());

		if (request.getExpenseDate() != null) {

			expense.setExpenseDate(request.getExpenseDate());
		}

		// -----------------------------------------------------
		// Delete old splits
		// -----------------------------------------------------

		List<ExpenseSplit> oldSplits = expenseSplitRepository.findByExpenseId(expenseId);

		if (!oldSplits.isEmpty()) {

			expenseSplitRepository.deleteAll(oldSplits);

			/*
			 * Flush immediately so the old split records are removed before new split
			 * records are inserted.
			 */
			expenseSplitRepository.flush();
		}

		// -----------------------------------------------------
		// Build new splits
		// -----------------------------------------------------

		List<ExpenseSplit> newSplits = buildSplits(expense, request);

		// -----------------------------------------------------
		// Save updated expense
		// -----------------------------------------------------

		Expense savedExpense = expenseRepository.save(expense);

		// -----------------------------------------------------
		// Save new splits
		// -----------------------------------------------------

		List<ExpenseSplit> savedSplits = expenseSplitRepository.saveAll(newSplits);
		
		activityLogService.log(
		        currentUser,
		        expense.getGroup(),
		        ActivityType.EXPENSE_UPDATED,
		        "Updated expense \"" + expense.getDescription()
		                + "\"",
		        expense.getId()
		);

		// -----------------------------------------------------
		// Return updated response
		// -----------------------------------------------------

		return buildExpenseResponse(savedExpense, savedSplits);
	}

	// =========================================================
	// DELETE EXPENSE
	// =========================================================

	@Override
	@Transactional
	public void deleteExpense(Long expenseId, String userEmail) {

		// -----------------------------------------------------
		// Find expense
		// -----------------------------------------------------

		Expense expense = expenseRepository.findById(expenseId)
				.orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + expenseId));

		// -----------------------------------------------------
		// Find current user
		// -----------------------------------------------------

		User currentUser = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		// -----------------------------------------------------
		// Only payer can delete
		// -----------------------------------------------------

		if (!expense.getPaidBy().getId().equals(currentUser.getId())) {

			throw new GroupAccessDeniedException("Only the expense payer can delete " + "this expense");
		}

		// -----------------------------------------------------
		// Validate group membership
		// -----------------------------------------------------

		Long groupId = expense.getGroup().getId();

		validateMembership(groupId, currentUser.getId());

		// -----------------------------------------------------
		// Delete dependent splits first
		// -----------------------------------------------------

		List<ExpenseSplit> splits = expenseSplitRepository.findByExpenseId(expenseId);

		if (!splits.isEmpty()) {

			expenseSplitRepository.deleteAll(splits);

			expenseSplitRepository.flush();
		}

		// -----------------------------------------------------
		// Delete expense
		// -----------------------------------------------------

		expenseRepository.delete(expense);
		
//		expenseRepository.delete(expense);

//		activityLogService.log(
//		        currentUser,
//		        group,
//		        ActivityType.EXPENSE_DELETED,
//		        "Deleted expense \"" + "\"",
//		        expenseId
//		);
	}
}
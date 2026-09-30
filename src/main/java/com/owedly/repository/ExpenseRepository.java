package com.owedly.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.owedly.entity.Expense;

public interface ExpenseRepository
        extends JpaRepository<Expense, Long> {

    List<Expense> findByGroupIdOrderByExpenseDateDesc(Long groupId);
    
    long countByPaidById(Long userId);
    
    @Query("""
    	    SELECT COALESCE(SUM(e.amount), 0)
    	    FROM Expense e
    	    WHERE e.paidBy.id = :userId
    	""")
    	BigDecimal sumAmountByPaidById(@Param("userId") Long userId);
    
    
    @Query("""
    	    SELECT COUNT(e)
    	    FROM Expense e
    	    WHERE e.group.id IN (
    	        SELECT gm.group.id
    	        FROM GroupMember gm
    	        WHERE gm.user.id = :userId
    	    )
    	""")
    	long countExpensesForUserGroups(@Param("userId") Long userId);
    
    @Query("""
    	    SELECT COALESCE(SUM(e.amount), 0)
    	    FROM Expense e
    	    WHERE e.group.id IN (
    	        SELECT gm.group.id
    	        FROM GroupMember gm
    	        WHERE gm.user.id = :userId
    	    )
    	""")
    	BigDecimal sumExpensesForUserGroups(@Param("userId") Long userId);
    
    
    @Query("""
    	    SELECT e
    	    FROM Expense e
    	    WHERE e.group.id IN (
    	        SELECT gm.group.id
    	        FROM GroupMember gm
    	        WHERE gm.user.id = :userId
    	    )
    	    ORDER BY e.expenseDate DESC, e.id DESC
    	""")
    	List<Expense> findRecentExpensesForUserGroups(
    	        @Param("userId") Long userId,
    	        Pageable pageable
    	);
    
    @Query("""
    	    SELECT COALESCE(SUM(e.amount), 0)
    	    FROM Expense e
    	    WHERE e.group.id IN (
    	        SELECT gm.group.id
    	        FROM GroupMember gm
    	        WHERE gm.user.id = :userId
    	    )
    	""")
    	BigDecimal sumAmountForUserGroups(@Param("userId") Long userId);
    
    
    @Query("""
    	    SELECT e.group.id,
    	           e.group.name,
    	           COALESCE(SUM(e.amount), 0)
    	    FROM Expense e
    	    WHERE e.group.id IN (
    	        SELECT gm.group.id
    	        FROM GroupMember gm
    	        WHERE gm.user.id = :userId
    	    )
    	    GROUP BY e.group.id, e.group.name
    	    ORDER BY SUM(e.amount) DESC
    	""")
    	List<Object[]> findSpendingByGroup(
    	        @Param("userId") Long userId);
    
    	@Query("""
    		    SELECT e.splitMethod,
    		           COUNT(e)
    		    FROM Expense e
    		    WHERE e.group.id IN (
    		        SELECT gm.group.id
    		        FROM GroupMember gm
    		        WHERE gm.user.id = :userId
    		    )
    		    GROUP BY e.splitMethod
    		    ORDER BY COUNT(e) DESC
    		""")
    		List<Object[]> findExpensesBySplitMethod(
    		        @Param("userId") Long userId);
    		
    		
    		@Query("""
    			    SELECT YEAR(e.expenseDate),
    			           MONTH(e.expenseDate),
    			           COALESCE(SUM(e.amount), 0)
    			    FROM Expense e
    			    WHERE e.group.id IN (
    			        SELECT gm.group.id
    			        FROM GroupMember gm
    			        WHERE gm.user.id = :userId
    			    )
    			    GROUP BY YEAR(e.expenseDate), MONTH(e.expenseDate)
    			    ORDER BY YEAR(e.expenseDate), MONTH(e.expenseDate)
    			""")
    			List<Object[]> findMonthlySpending(
    			        @Param("userId") Long userId);
    
}
package com.owedly.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlements")
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 300)
    private String note;

    @Column(name = "settled_at", nullable = false, updatable = false)
    private LocalDateTime settledAt;

    @PrePersist
    protected void onCreate() {
        settledAt = LocalDateTime.now();
    }

    public Settlement() {
    }

    public Long getId() {
        return id;
    }

    public Group getGroup() {
        return group;
    }

    public User getFromUser() {
        return fromUser;
    }

    public User getToUser() {
        return toUser;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getNote() {
        return note;
    }

    public LocalDateTime getSettledAt() {
        return settledAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public void setFromUser(User fromUser) {
        this.fromUser = fromUser;
    }

    public void setToUser(User toUser) {
        this.toUser = toUser;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setSettledAt(LocalDateTime settledAt) {
        this.settledAt = settledAt;
    }
}
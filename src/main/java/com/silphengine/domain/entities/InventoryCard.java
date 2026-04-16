package com.silphengine.domain.entities;

import com.silphengine.domain.enums.CardFinish;
import com.silphengine.domain.enums.CardCondition;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/*
 * Unique constraint to prevent duplicate logical entries in the inventory.
 * A user should have only one row per unique combination of: card, condition, and finish.
 * Business logic (Service layer) must handle quantity increments when a collision occurs,
 * but this constraint acts as the final safety net for data integrity.
 *
 * Example: A user cannot have two separate rows for "Pikachu | Near Mint | Holo".
 */

@Entity
@Table(name = "inventory_cards", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_inventory_user_card_condition_finish",
                columnNames = {"owner_id", "card_id", "card_condition", "card_finish"}
        )
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@ToString
public class InventoryCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @ToString.Exclude
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    @ToString.Exclude
    private Card card;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_condition", nullable = false)
    private CardCondition cardCondition;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_finish", nullable = false)
    private CardFinish cardFinish;

    public void changeQuantity(Integer newQuantity) {
        if (newQuantity > 0) {
            this.quantity = newQuantity;
        }
    }

    protected void setOwner(User owner) {
        this.owner = owner;
    }
}

package com.silphengine.domain.entities;

import com.silphengine.domain.enums.CardFinish;
import com.silphengine.domain.enums.CardCondition;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "inventory_cards")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Setter
@ToString
public class InventoryCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @ToString.Exclude
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Column(name = "quantity" , nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_condition")
    private CardCondition cardCondition;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_finish")
    private CardFinish cardFinish;
}

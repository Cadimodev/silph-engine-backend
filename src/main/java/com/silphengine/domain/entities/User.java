package com.silphengine.domain.entities;

import com.silphengine.domain.enums.Role;
import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@ToString
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<InventoryCard> collection = new ArrayList<>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Deck> decks = new ArrayList<>();

    public void updateProfile(String nickname, String email) {
        this.nickname = nickname;
        this.email = email;
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void addInventoryCard(InventoryCard card) {
        collection.add(card);
        card.setOwner(this);
    }

    public void removeInventoryCard(InventoryCard card) {
        collection.remove(card);
        card.setOwner(null);
    }

    public void assignDefaultRole() {
        role = Role.USER;
    }

    @Override
    @Nonnull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    @Nonnull
    public String getUsername() {
        return nickname;
    }

    @Override
    @Nonnull
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @Nonnull
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @Nonnull
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @Nonnull
    public boolean isEnabled() {
        return true;
    }

}

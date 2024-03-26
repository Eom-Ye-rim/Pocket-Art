package com.pocekt.art.user.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.pocekt.art.board.domain.Contest;
import com.pocekt.art.entity.BaseTime;
import com.pocekt.art.entity.Comment;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
public class Users extends BaseTime implements UserDetails {

    @Id
    @GeneratedValue
    @Column(name = "users_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column
    private String email;

    @Column
    private String name;

    @Column
    private String password;

    @Column
    private String ProfileImg;
    @Enumerated(value = EnumType.STRING)
    private AuthProvider provider;

    @Column
    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<String> roles = new ArrayList<>();

    @OneToMany(mappedBy = "users", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonBackReference
    @Builder.Default
    private List<Contest> contestList =new ArrayList<>();

    @OneToMany(mappedBy = "users", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonBackReference
    @Builder.Default
    @JsonIgnore
    private List<Comment> commentList =new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @PrePersist
    public void prePersist(){
        this.ProfileImg = this.ProfileImg == null ? "https://sunny-pj.s3.ap-southeast-2.amazonaws.com/Group+7194.png" : this.ProfileImg;
    }
}

package org.cubewhy.chat.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@Data
@Entity
public class Account implements BaseData, Principal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    @ManyToMany(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinTable(
            name = "account_roles",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    private String nickname;
    private String email;
    private String bio;

    @OneToMany(mappedBy = "user")
    private List<ChannelUser> channelUsers;

    @Override
    public String getName() {
        return username;
    }
}

package hu.springconfig.data.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor
public class Invite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String inviteKey;
    private String email;
    private boolean used;
    private Date date;
    @Version
    private long version;

    public Invite(Invite invite) {
        this.inviteKey = invite.inviteKey;
        this.email = invite.email;
        this.used = invite.used;
        this.date = invite.date;
        this.version = invite.version;
    }
}
